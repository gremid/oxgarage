package pl.psnc.dl.ege.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pl.psnc.dl.ege.Converter;
import pl.psnc.dl.ege.exception.ConverterException;

/**
 * Converter action class is a base for nodes of convert graph - each action
 * describes that it is possible to use referenced converter to convert from
 * chosen input format to chosen output format. <br/><br/>
 * Input formats are contained within {@link Conversion}.<br/><br/>
 * Each instance should contain reference to converter and one action point with
 * specified input and output formats.
 * 
 * @author mariuszs
 */
public class ConversionAction {
	
	private final Conversion conversion;
	private final Converter converter;

	/**
	 * Constructor with basic parameters: convertionActionArguments and
	 * reference to converter.
	 * 
	 * @param conversion arguments for conversion action
	 * @param converter implementation of {@link Converter} interface 
	 */
	public ConversionAction(
			Conversion conversion,
			Converter converter) {
		if(conversion == null || converter == null){
			throw new IllegalArgumentException();
		}
		this.conversion = conversion;
		this.converter = converter;
	}
	
	/**
	 * Returns reference to converter.
	 * 
	 * @return converter reference
	 */
	public Converter getConverter() {
		return converter;
	}

	/**
	 * Performing convert operation using contained reference
	 * to converter.
	 * 	
	 * @param inputStream source of data to convert  
	 * @param outputStream reference to stream where data will be put after conversion.
	 * @throws ConverterException
	 */
	public void convert(InputStream inputStream, OutputStream outputStream)
			throws ConverterException, IOException {
		converter.convert(inputStream, outputStream, conversion);
	}

	/**
	 * Returns conversion action arguments.
	 * 
	 * @return conversion action arguments.
	 */
	public Conversion getConversion() {
		return conversion;
	}

	/**
	 * Returns input {@link DataType} for this action.
	 * 
	 * @return input {@link DataType}
	 */
	public DataType getConversionInputType() {
		return (conversion == null ? null
				: conversion.getInputType());
	}

	/**
	 * Returns output {@link DataType} for this action.
	 * 
	 * @return output {@link DataType}
	 */
	public DataType getConversionOutputType() {
		return (conversion == null ? null
				: conversion.getOutputType());
	}

	/**
	 * Returns the cost of this conversion

	 * 
	 * @return cost
	 */
	public int getCost() {
		return conversion.getCost();
	}

	/**
	 * Method returns <code>true</code> if action references same converter and
	 * both compared action data types are equal.
	 * 
	 * @return boolean value
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof ConversionAction)) {
			return false;
		}
		ConversionAction ca = (ConversionAction) o;
		if (ca.getConverter() != null
				&& ca.getConverter().equals(this.getConverter())) {
			if (ca.getConversion() != null
					&& ca.getConversion().equals(
							this.getConversion())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = 7;
		hashCode = hashCode
				+ (this.conversion == null ? 0
						: this.conversion.hashCode());
		hashCode = hashCode
				+ (this.converter == null ? 0 : this.converter.hashCode());
		return hashCode;
	}

	@Override
	public String toString() {
        return conversion.toString() + "(" + converter.toString() + ")";
	}
}
