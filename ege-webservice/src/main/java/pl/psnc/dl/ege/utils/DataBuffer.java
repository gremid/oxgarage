package pl.psnc.dl.ege.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileCleaningTracker;

import pl.psnc.dl.ege.EGEImpl;
import pl.psnc.dl.ege.EGEConstants;

/**
 * Keeps collection of buffered byte data, which is used mainly by conversion
 * with validation process.<br/><br/> 
 * 
 * Each singular data item can be kept in memory - if it does not reaches specified
 * threshold (in number of bytes). If it does DataBuffer is permitted to save data
 * as temporary file.<br/><br/>
 * 
 * <b>Important:</b> while buffer is referenced it is not cleared from data;
 * assigning new items indefinitely may result in stack overflow.<br/>
 * Memory can be relieved by class methods.<br/>
 * 
 * Each allocation in buffer is of type : single-write/multiple-read.<br/>
 * 
 *    
 * @author mariuszs
 *
 */
/*
 * TODO : Przerobki - nie zwracac 'id' alokacji, ale Item. Pozwolic na wielokrotny zapis (?).
 */
public class DataBuffer
{

	private static final Logger LOGGER = Logger.getLogger(DataBuffer.class.getName());

	/**
	 * Default value : max size of item (in number of bytes), that allows to keep it in memory.
	 */
	public static final int DEFAULT_ITEM_MAX_SIZE = 102400;

	/*
	 * List of buffered items
	 */
	private Map<String, Item> items = new HashMap<String, Item>();

	/*
	 * Maximum size of data item (in number of bytes)
	 */
	private int itemMaxSize = DEFAULT_ITEM_MAX_SIZE;

	/*
	 * Tracker of temporary files - which are deleted, when reference to data item is dropped.
	 */
	private final FileCleaningTracker tracker = new FileCleaningTracker();


	/**
	 * Creates instance of data buffer with specified temporary files directory
	 * and threshold for every contained data item. 
	 * 
	 * @param itemMaxSize maximum size of data item
	 */
	public DataBuffer(int itemMaxSize) {
		this.itemMaxSize = itemMaxSize;
	}


	/**
	 * <p>Allocates clean data item in buffer.</p>
	 * Method returns id of allocated data item.  
	 * 
	 * @return 'id' of allocated data item.
	 */
	public String allocate()
	{
		String id = UUID.randomUUID().toString();
		Item item = new Item(id);
		items.put(id, item);
		tracker.track(item.getFile(), item);
		return id;
	}
	
	/**
	 * <p>Returns buffer allocation output stream.</p> 
	 * If output stream of selected item was closed, using
	 * this method again on the same item will result in IllegalStateException.
	 * 
	 * @param id of buffer item
	 * @return buffer item output stream
	 * @throws IllegalStateException
	 */
	public OutputStream getElementOutputStream(String id) throws IllegalStateException {
		Item item = items.get(id);
		if(item.isCommited()){
			throw new IllegalStateException("Buffer element already filled.");
		}
		return item.getOutputStream();
	}
	
	/**
	 * Reads data from specified input stream and creates
	 * single data item.<br/> 
	 * If item maximum size is reached, data is written to temporary file.   
	 * Method returns unique id of created item.
	 * 
	 * @param inputStream streamed input data
	 * @return 'id' of allocated data item
	 */
	public String allocate(InputStream inputStream)
	{
		String id = UUID.randomUUID().toString();
		Item item = new Item(id);
		item.write(inputStream);
		items.put(id, item);
		tracker.track(item.getFile(), item);
		return id;
	}
	
	/**
	 * Reads data from specified input stream and creates
	 * single data item.<br> 
	 * <p>If item maximum size is reached, data is written to temporary file
	 * with name of 'itemName'.</p>   
	 * Method returns unique id of created item.
	 *  
	 * @param inputStream
	 * @param itemName
	 * @return
	 */
	public String allocate(InputStream inputStream, String itemName){
		String id = UUID.randomUUID().toString();
		Item item = new Item(id, itemName);
		item.write(inputStream);
		items.put(id, item);
		tracker.track(item.getFile(), item);
		return id;
	}
	
	/**
	 * <p>Returns specified by id - data item as input stream.</p>
	 * If item does not exists in buffer method returns 'null'.
	 *  
	 * @param 'id' of a data item.
	 * @return streamed data item
	 */
	public InputStream getDataAsStream(String id)
	{
		try {
			Item item = items.get(id);
			if (item != null) {
				return item.getStream();
			}
			return null;
		}
		catch (FileNotFoundException ex) {
			throw new RuntimeException("Temporary file generator exception!");
		}

	}
	
	/**
	 * Returns temp dir of allocated item - can be null if item
	 * is stored in memory.
	 * 
	 * @param id
	 * @return
	 */
	public File getDataDir(String id){
		Item item = items.get(id);
		if(item!=null){
			return item.getDir();
		}
		return null;
	}

	/**
	 * <p>Relieves selected data item.</p>
	 * If 'forceDelete' parameter is set to 'true' temporary file (if it was created)
	 * will be deleted immediately, otherwise it will be deleted after release 
	 * of memory by garbage collector.<br/>   
	 * Method returns 'false' if selected item does not exists in buffer, 
	 * otherwise it returns 'true'.
	 * 
	 * @param 'id' of data item
	 */
	public boolean removeData(String id, boolean forceDelete)
	{
		Item item = items.get(id);
		if (item == null)
			return false;
		if (forceDelete) {
			item.deleteDir();
		}
		items.remove(id);
		return true;
	}


	/**
	 * <p>Relieves all stored in buffer data items</p>
	 * If 'forceDelete' parameter is set to 'true' all temporary files 
	 * will be deleted immediately, otherwise they will be deleted
	 * after release of memory by garbage collector.
	 * 
	 */
	public void clear(boolean forceDelete)
	{
		if (forceDelete) {
			for (Item item : items.values()) {
				item.deleteDir();
			}
		}
		items = new HashMap<String,Item>();
	}
	
	/*
	 * Inner class : represents single item of data. 
	 */
	class Item
	{

		private BufferOutputStream os;

		private File tmpFile;

		private File tmpDir;
		
		private boolean commited = false;


		public Item(String id) {
			this(id, "backup.ebu");
		}
		
		public Item(String id, String itemName){
            this.tmpDir = new File(EGEConstants.tempDir(), id);
            this.tmpFile = new File(tmpDir, itemName);
            if (!tmpDir.isDirectory() && !tmpDir.mkdirs()) {
                throw new IllegalStateException(tmpDir.toString());
            }
            os = new BufferOutputStream(itemMaxSize, tmpFile, this);
		}
		
		public void write(InputStream is)
		{
			int b;
			byte[] buf = new byte[EGEImpl.BUFFER_SIZE];
			try {
				while ((b = is.read(buf)) != -1) {
					os.write(buf, 0, b);
				}
			}
			catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex, ex::getMessage);
			}
			finally {
				try {
					os.close();
				}
				catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex, ex::getMessage);
				}
			}
		}


		public InputStream getStream()
			throws FileNotFoundException
		{
			byte[] data = os.getData();
			if (data == null) {
				return new FileInputStream(os.getFile());
			}
			else {
				return new ByteArrayInputStream(data);
			}
		}


		/*
		 * Deletes temporary file
		 */
		public void deleteFile()
		{
			if (!os.isInMemory()) {
				if (os.getFile().exists()) {
					LOGGER.fine("Removing tmp file : " + os.getFile());
					os.getFile().delete();
				}
			}
		}
		
		public void deleteDir(){
			if (!os.isInMemory()) {
				if(tmpDir.exists()){
					EGEIOUtils.deleteDirectory(tmpDir);
				}
			}
		}
		
		public OutputStream getOutputStream(){
			return os;
		}

		public File getFile()
		{
			return tmpFile;
		}
		
		private File getDir(){
			return tmpDir;
		}

		public boolean isCommited()
		{
			return Boolean.valueOf(commited);
		}
		
		public void commit(){
			this.commited = true;
		}

	}

}
