/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.extsystems.excel;

import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.extsystems.ExternalDataFormat;
import org.faktorips.devtools.extsystems.ValueConverter;

/**
 * 
 * @author Thorsten Waertel
 */
public class ExcelDataFormat extends ExternalDataFormat {
	
	public final static String DEFAULT_EXTENSION = ".xls";
	public final static String ID = "Excel";
	
	public ExcelDataFormat() {
		super(enumType, ID);
		setValueConverter(Date.class, Datatype.GREGORIAN_CALENDAR_DATE, new DateValueConverter());
		ValueConverter doubleConverter = new DoubleValueConverter();
		setValueConverter(Double.class, Datatype.DOUBLE, doubleConverter);
		setValueConverter(Double.class, Datatype.DECIMAL, doubleConverter);
		setValueConverter(Double.class, Datatype.MONEY, doubleConverter);
		ValueConverter intConverter = new IntegerValueConverter();
		setValueConverter(Double.class, Datatype.INTEGER, intConverter);
		setValueConverter(Double.class, Datatype.PRIMITIVE_INT, intConverter);
		ValueConverter longConverter = new LongValueConverter();
		setValueConverter(Double.class, Datatype.LONG, longConverter);
		ValueConverter booleanConverter = new BooleanValueConverter();
		setValueConverter(Boolean.class, Datatype.BOOLEAN, booleanConverter);
		setValueConverter(Boolean.class, Datatype.PRIMITIVE_BOOLEAN, booleanConverter);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultFileExtension() {
		return DEFAULT_EXTENSION;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return ID;
	}

	/**
	 * {@inheritDoc}
	 */
	public void openSystemEditor(IFile file) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	public String getIpsValue(Class externalDataClass, Object externalDataValue, Datatype datatype) {
		if (datatype == null) {
			return externalDataValue.toString();
		}
		ValueConverter converter = getValueConverter(externalDataClass, datatype);
		if (converter != null) {
			return converter.getIpsValue(externalDataValue);
		}
		return externalDataValue.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getExternalDataValue(String ipsValue, Datatype datatype) {
		if (datatype == null) {
			return ipsValue;
		}
		ValueConverter converter = getValueConverter(datatype);
		if (converter != null) {
			return converter.getExternalDataValue(ipsValue);
		}
		return ipsValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getExternalDataValue(Class externalDataClass, String ipsValue, Datatype datatype) {
		if (datatype == null) {
			return ipsValue;
		}
		ValueConverter converter = getValueConverter(externalDataClass, datatype);
		if (converter != null) {
			return converter.getExternalDataValue(ipsValue);
		}
		return ipsValue;
	}
}
