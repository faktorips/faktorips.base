/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype;

import java.lang.reflect.Method;

/**
 * Generic enum datatype. See the superclass for more Details.
 * 
 * @author Jan Ortmann
 */
public abstract class GenericEnumDatatype extends GenericValueDatatype implements EnumDatatype {

	private String getAllValuesMethodName = "getAllValues";

	private String getNameMethodName = "getName";

	private boolean isSupportingNames = false;

	protected Method getAllValuesMethod;

	protected Method getNameMethod;

	public GenericEnumDatatype() {
		super();
	}

	public String getGetAllValuesMethodName() {
		return getAllValuesMethodName;
	}

	public void setGetAllValuesMethodName(String getAllValuesMethodName) {
		this.getAllValuesMethodName = getAllValuesMethodName;
		getAllValuesMethod = null;
	}

	/**
	 * Returns the name of the getName(String) method.
	 */
	public String getGetNameMethodName() {
		return getNameMethodName;
	}

	/**
	 * Sets the name of the getName(String) method.
	 */
	public void setGetNameMethodName(String getNameMethodName) {
		this.getNameMethod = null;
		this.getNameMethodName = getNameMethodName;
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isSupportingNames() {
		return isSupportingNames;
	}

	public void setIsSupportingNames(boolean isSupportingNames) {
		this.isSupportingNames = isSupportingNames;
	}

	public String[] getAllValueIds() {
		try {
			Object[] values = (Object[]) getGetAllValuesMethod().invoke(null, new Object[0]);
			String[] ids = new String[values.length];
			for (int i = 0; i < ids.length; i++) {
				ids[i] = this.valueToString(values[i]);
			}
			return ids;
		} catch (Exception e) {
			throw new RuntimeException("Error invoking method " + valueOfMethod);
		}
	}

	Method getGetAllValuesMethod() {
		if (getAllValuesMethod == null && getAllValuesMethodName != null) {
			try {
				getAllValuesMethod = getAdaptedClass().getMethod(getAllValuesMethodName,
						new Class[0]);
				if (getAllValuesMethod == null) {
					throw new NullPointerException();
				}
			} catch (Exception e) {
				throw new RuntimeException("Can't get method getAllValues(), Class: "
						+ getAdaptedClass() + ", Methodname: " + getAllValuesMethodName);
			}
		}
		return getAllValuesMethod;
	}

	Method getGetNameMethod() {
		if (getNameMethod == null && getNameMethodName != null) {
			try {
				getNameMethod = getAdaptedClass().getMethod(getNameMethodName, new Class[0]);
			} catch (Exception e) {
				throw new RuntimeException("Unable to access the method " + getNameMethodName
						+ " on the adapted class " + getAdaptedClass(), e);
			}
		}
		return getNameMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValueName(String id) {

		if (isSupportingNames) {
			try {
                Object value = getValue(id);
                if(value == null){
                    return null;
                }
				return (String)getGetNameMethod().invoke(value, new Object[0]);
			} catch (Exception e) {
				throw new RuntimeException("Unable to invoke the method " + getNameMethodName
						+ " on the class: " + getAdaptedClass());
			}
		}
		throw new UnsupportedOperationException(
				"This enumeration type does not support a getName(String) method, enumeration type class: "
						+ getAdaptedClass());
	}
}
