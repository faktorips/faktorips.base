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
				Object[] allValues = (Object[])getGetAllValuesMethod().invoke(null, new Object[0]);
				for (int i = 0; i < allValues.length; i++) {
					if(valueToString(allValues[i]).equals(id)){
						return (String)getGetNameMethod().invoke(allValues[i], new Object[0]);
					}
				}
				throw new IllegalArgumentException("The provided id " + id + " is not a valid for this enumeration datatype " + getAdaptedClass());
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
