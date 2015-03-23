package pl.psnc.dl.ege;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.psnc.dl.ege.configuration.EGEConfigurationManager;
import pl.psnc.dl.ege.exception.ConverterException;
import pl.psnc.dl.ege.exception.EGEException;
import pl.psnc.dl.ege.exception.ValidatorException;
import pl.psnc.dl.ege.types.ConversionAction;
import pl.psnc.dl.ege.types.ConversionsPath;
import pl.psnc.dl.ege.types.DataType;
import pl.psnc.dl.ege.types.ValidationResult;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * <p>
 * Standard Enrich Garage Engine(EGE) implementation.
 * </p>
 * Implementation uses JUNG library for generating graph of conversions.
 * 
 * @author mariuszs
 */
public class EGEImpl implements EGE, ExceptionListener {

	public final static int BUFFER_SIZE = 131072;

	private static final Logger LOGGER = Logger.getLogger(EGEImpl.class.getName());

	/*
	 * Contains last thrown exception from ConversionPerformer thread.
	 */
	private List<Exception> exceptions = new LinkedList<>();

    /*
     * List of available validator plugins : loaded through extension manager.
     */
	private List<Validator> validators;

    /*
     * Directed graph of connections between available converter plugins.
     */
	private final Graph<ConversionAction, Integer> graph = new DirectedSparseMultigraph<ConversionAction, Integer>();

	/**
	 * Default Constructor : initializes basic structures.
	 */
	public EGEImpl() {
        EGEConfigurationManager em = EGEConfigurationManager.getInstance();
        List<Converter> converters = em.getAvailableConverters();
        this.validators = em.getAvailableValidators();

        final Set<ConversionAction> nodes = new HashSet<ConversionAction>();
        converters.stream().forEach(conv -> conv.getPossibleConversions().stream().map(ac -> new ConversionAction(ac, conv)).forEach(nodes::add));

        int index = 0;
        for (ConversionAction ca : nodes) {
            graph.addVertex(ca);
            for (ConversionAction sca : nodes) {
                if (ca.getConversionOutputType().equals(sca.getConversionInputType())) {
                    graph.addEdge(index++, ca, sca);
                }
            }
        }
    }


	/**
	 * Method returns every possible conversion path for specified input
	 * <code>DataType</code>. One of the received paths can be then used to
	 * perform chained conversion.
	 * 
	 * @param sourceDataType
	 *          	input data type
	 * @return list of possible convert paths.
	 */
	public List<ConversionsPath> findConversionPaths(
			final DataType sourceDataType)
	{
		List<ConversionAction> startNodes = getStartNodes(sourceDataType);
		List<ConversionsPath> paths = new ArrayList<ConversionsPath>();
		for (ConversionAction ca : startNodes) {
			expandPathsSet(new ConversionsPath(new ArrayList<>()), ca, paths, null);
		}
		Collections.sort(paths);	
		return paths;
	}


	/**
	 * Method return every possible/unique convert path for specified input type
	 * data with pointed output type data.
	 * 
	 * @param sourceDataType
	 *            input data type
	 * @param resultDataType
	 *            expected output data type
	 * @return list of possible convert paths.
	 */
	public List<ConversionsPath> findConversionPaths(
			final DataType sourceDataType, final DataType resultDataType)
	{
		List<ConversionAction> startNodes = getStartNodes(sourceDataType);
		List<ConversionsPath> paths = new ArrayList<ConversionsPath>();
		for (ConversionAction ca : startNodes) {
			expandPathsSet(new ConversionsPath(
					new ArrayList<ConversionAction>()), ca, paths,
				resultDataType);
		}
		Collections.sort(paths);		
		return paths;
	}


	/**
	 * <p>Method performs validation using all loaded through extension mechanism
	 * {@link Validator} implementations.</p>
	 * Method returns instance of {@link ValidationResult} which contains validation
	 * status and error/warning messages.<br/>
	 * If there is no validator that supports specified data type, then 
	 * ValidatorException will be throw.<br/>
	 * If some unexpected errors occurs during validation, method will throw   
	 * EGEException.
	 * 
	 * @param inputData
	 *            input stream that contains necessary data
	 * @param inputDataType
	 *            validation argument
	 * @return instance of {@link ValidationResult}
	 * @throws IOException
	 * @throws {@link ValidatorException}
	 * @throws {@link EGEException}
	 */
	public ValidationResult performValidation(final InputStream inputData,
			final DataType inputDataType)
		throws IOException, ValidatorException, EGEException
	{
		for (Validator v : validators) {
			for (DataType dt : v.getSupportedValidationTypes()) {
				if (dt.equals(inputDataType)) {
					return v.validate(inputData, inputDataType);
				}
			}
		}
		throw new ValidatorException(inputDataType);
	}
	
	

	/**
	 * Performs sequence of conversions based on specified convert path.<br/>
	 * Data is taken from selected input stream and after all sequenced
	 * conversions sent to pointed output stream.
	 * 
	 * @param inputStream
	 *            source of data to convert
	 * @param outputStream
	 *            output stream for converted data
	 * @param path
	 *            defines sequence of conversion.
	 * @throws EGEException
	 *             if unexpected error occurred within method.
	 * @throws ConverterException
	 *             if during conversion method an exception occurred.
	 */
	public void performConversion(final InputStream inputStream,
			OutputStream outputStream, ConversionsPath path)
		throws ConverterException, EGEException, IOException
	{
		try {
			clearExceptionsStack();
			final PipedOutputStream os = new PipedOutputStream();
			PipedInputStream is = new PipedInputStream(os);
			int size = 0;
			if (path != null && path.getPath() != null) {
				size = path.getPath().size();
			}
			// uses inner class ReWriter
			ReWriter cr = new ReWriter(inputStream, os);
			cr.start();
			Thread last = null;
			for (int i = 0; i < size; i++) {
				ConversionAction ca = path.getPath().get(i);
				PipedOutputStream os2 = new PipedOutputStream();
				PipedInputStream is2 = new PipedInputStream(os2);
				Thread convt = new Thread(new ConversionPerformer(ca, is, os2,
						this));
				convt.start();
				last = convt;
				is = is2;
			}
			byte[] buf = new byte[BUFFER_SIZE];
			int b = 0;
			while ((b = is.read(buf)) != -1) {
				outputStream.write(buf, 0, b);
			}
			last.join();
			// catches exception reported in ConversionPerfomer thread
			Exception ex = throwException();
			if (ex != null) {
				throw ex;
			}
		}
		catch (ConverterException ex) {
            LOGGER.log(Level.SEVERE, ex, ex::getMessage);
			throw ex;
		}
		catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex, ex::getMessage);
			throw ex;
		}
		catch (Exception ex) {
			LOGGER.log(Level.SEVERE, ex, ex::getMessage);
			throw new EGEException(ex.getMessage());
		}
	}

	/**
	 * <p>Returns set of data types that are supported for validation.</p> 
	 * 
	 * @return set of data types
	 */
	public Set<DataType> returnSupportedValidationFormats(){
		Set<DataType> supported = new TreeSet<DataType>();
		for(Validator v : validators){
			supported.addAll(v.getSupportedValidationTypes());
		}
		return supported;
	}
	
	/**
	 * <p>Returns all supported by EGE input formats
	 * - entry points for conversion.</p>
	 * 
	 * @return set of a supported input formats
	 */
	public Set<DataType> returnSupportedInputFormats()
	{
		// sort alphabetically (to keep the documents from same family together)
		Set<DataType> inputTypes = new TreeSet<DataType>();
		for (ConversionAction ca : graph.getVertices()) {
			if(ca.getConversion().getVisible()) inputTypes.add(ca.getConversionInputType());
		}
		return inputTypes;
	}


	/*
	 * Gets all nodes considered as starting points for provided input type. 
	 */
	private List<ConversionAction> getStartNodes(DataType inputType)
	{
		List<ConversionAction> nodes = new ArrayList<ConversionAction>(graph
				.getVertices());
		List<ConversionAction> startNodes = new ArrayList<ConversionAction>();
		for (ConversionAction ca : nodes) {
			if (ca.getConversionInputType() != null
					&& ca.getConversionInputType().equals(inputType)) {
				startNodes.add(ca);
			}
		}
		return startNodes;
	}


	/*
	 * Recursive algorithm for adding paths to paths sequence.
	 */
	private void expandPathsSet(ConversionsPath currentPath,
			ConversionAction node, List<ConversionsPath> paths,
			DataType outputType)
	{
		int size = currentPath.getPath().size();
		boolean loop = false;
		// check for loops and cycles : deny cycles.
		for (int i = 0; i < size; i++) {
			ConversionAction ca = currentPath.getPath().get(i);
			if (ca.getConversionInputType().equals(node.getConversionInputType()) || ca.getConversionOutputType().equals(node.getConversionOutputType())) {
				if (i == (size - 1)) {
					if (i > 0) {
						ConversionAction ca2 = currentPath.getPath().get(i - 1);
						if (!ca2.equals(node)) {
							currentPath.getPath().add(node);
							addPath(currentPath, paths, outputType, node
									.getConversionOutputType());
							loop = true;
						}
						else {
							return;
						}
					}
					else {
						currentPath.getPath().add(node);
						addPath(currentPath, paths, outputType, node
								.getConversionOutputType());
						loop = true;
					}
				}
				else {
					return;
				}
			}
		}
		if (!loop) {
			currentPath.getPath().add(node);
			if (currentPath.getPath().size() > 0) {
				if (!(currentPath.getPath().get(0).getConversionInputType()
						.equals(node.getConversionOutputType()))) {
					addPath(currentPath, paths, outputType, node
							.getConversionOutputType());
				}
			}
			else {
				addPath(currentPath, paths, outputType, node
						.getConversionOutputType());
			}
		}
		// only search other paths, if the path we currently have is not longer than equal path already stored in the list of paths
		// if we search all the paths, it takes too long
		int indexOfPath = paths.indexOf(currentPath);
		if(indexOfPath==-1 || paths.get(indexOfPath).getCost()>=currentPath.getCost()) {
			List<ConversionAction> succs = new ArrayList<ConversionAction>(graph
					.getSuccessors(node));
			for (ConversionAction ca : succs) {
				expandPathsSet(new ConversionsPath(new ArrayList<ConversionAction>(
						currentPath.getPath())), ca, paths, outputType);
			}
		}
	}


	/*
	 * Add path if current node output type equals expected output type. If no
	 * output type was specified add path by default - finding all connections.
	 */
	private void addPath(ConversionsPath path, List<ConversionsPath> paths,
			DataType destinOutputType, DataType currentOutputType)
	{
		if(!path.getInputDataType().equals(currentOutputType)){
			if (destinOutputType != null) {
				if (currentOutputType.equals(destinOutputType)) {
					int indexOfPath = paths.indexOf(path);
					if(indexOfPath==-1 || paths.get(indexOfPath).getCost()>path.getCost()) {
						if(indexOfPath!=-1) { 
							paths.remove(indexOfPath);					
							paths.add(indexOfPath, path);
						}
						else paths.add(path);
					}
				}
			}
			else {
				int indexOfPath = paths.indexOf(path);
				if(indexOfPath==-1 || paths.get(indexOfPath).getCost()>path.getCost()) {
					if(indexOfPath!=-1) { 
						paths.remove(indexOfPath);					
						paths.add(indexOfPath, path);
					}
					else paths.add(path);
				}
			}
		}
	}


	/**
	 * Returns conversion graph.
	 * 
	 * @return JUNG graph structure of conversion actions
	 */
	public Graph<ConversionAction, Integer> getConvertersGraph()
	{
		return (Graph<ConversionAction, Integer>) graph;
	}


	/*
	 * Method used by conversion threads to report exceptions caught in run() method. 
	 * (non-Javadoc)
	 * @see pl.psnc.dl.ege.ExceptionListener#catchException(java.lang.Exception)
	 */
	public synchronized void catchException(Exception ex)
	{
		exceptions.add(ex);
	}


	private synchronized void clearExceptionsStack()
	{
		exceptions.clear();
	}


	private synchronized Exception throwException()
	{
		try {
			return exceptions.remove(0);
		}
		catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}

	/*
	 * ReWriting streams utility - triggering piped input/output streaming.
	 */
	private static class ReWriter
		extends Thread
	{

		private final InputStream is;

		private final OutputStream os;


		public ReWriter(InputStream is, OutputStream os)
		{
			super();
			this.is = is;
			this.os = os;
		}


		@Override
		public void run()
		{
			int b;
			byte[] buf = new byte[BUFFER_SIZE];
			try {
				while ((b = is.read(buf)) != -1) {
					os.write(buf, 0, b);
					os.flush();

				}
			}
			catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex, ex::getMessage);
				ex.printStackTrace();
			}
			finally {
				if (os != null) {
					try {
						os.close();
					}
					catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, ex, ex::getMessage);
                    }
				}
			}

		}

	}

}
