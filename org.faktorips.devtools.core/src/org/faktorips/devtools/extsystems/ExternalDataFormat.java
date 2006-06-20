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

package org.faktorips.devtools.extsystems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.extsystems.excel.ExcelDataFormat;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

/**
 * 
 * @author Thorsten Waertel
 */
public abstract class ExternalDataFormat extends DefaultEnumValue {
	
    protected final static DefaultEnumType enumType;
    
    public final static ExternalDataFormat XLS;
    
    static {
        enumType = new DefaultEnumType("ExternalDataFormat", ExternalDataFormat.class); //$NON-NLS-1$
        XLS = new ExcelDataFormat();
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    public final static ExternalDataFormat getAttributeType(String id) {
        return (ExternalDataFormat) enumType.getEnumValue(id);
    }
    
	private Map converters = new HashMap();
	
	public abstract String getDefaultFileExtension();
	public abstract void openSystemEditor(IFile file);
	public abstract String getIpsValue(Class externalDataClass, Object externalDataValue, Datatype datatype);
	public abstract Object getExternalDataValue(String ipsValue, Datatype datatype);
	public abstract Object getExternalDataValue(Class externalDataClass, String ipsValue, Datatype datatype);
	
	protected ExternalDataFormat(DefaultEnumType enumType, String id) {
		super(enumType, id);
	}
	
	public ValueConverter getValueConverter(Class externalDataClass, Datatype datatype) {
		List entries = (List) converters.get(datatype);
		if (entries == null) {
			return null;
		}
		for (Iterator it = entries.iterator(); it.hasNext();) {
			ValueConverterEntry entry = (ValueConverterEntry) it.next();
			if (entry.getClazz().equals(externalDataClass)) {
				return entry.getConverter();
			}
		}
		return null;
	}
	
	/**
	 * Returns the first ValueConverter found for the given Datatype or <code>null</code> if no ValueConverters
	 * are registered for this Datatype.
	 */
	public ValueConverter getValueConverter(Datatype datatype) {
		List entries = (List) converters.get(datatype);
		if (entries != null && entries.size() > 0) {
			return ((ValueConverterEntry) entries.get(0)).getConverter();
		}
		return null;
	}
	
	public void setValueConverter(Class externalDataClass, Datatype datatype, ValueConverter converter) {
		List entries = (List) converters.get(datatype);
		if (entries == null) {
			entries = new ArrayList();
			converters.put(datatype, entries);
		}
		ValueConverterEntry entry = new ValueConverterEntry(externalDataClass, converter);
		if (!entries.contains(entry)) {
			entries.add(entry);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return getId();
	}
	
	private static class ValueConverterEntry {
		private Class clazz;
		private ValueConverter converter;

		public ValueConverterEntry(Class clazz, ValueConverter converter) {
			super();
			this.clazz = clazz;
			this.converter = converter;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			ValueConverterEntry other = (ValueConverterEntry) obj;
			return clazz.equals(other.clazz) && converter.equals(other.converter);
		}

		/**
		 * {@inheritDoc}
		 */
		public int hashCode() {
			return clazz.hashCode() + converter.hashCode();
		}

		/**
		 * @return Returns the converter.
		 */
		public ValueConverter getConverter() {
			return converter;
		}

		/**
		 * @return Returns the clazz.
		 */
		public Class getClazz() {
			return clazz;
		}

	}
}
