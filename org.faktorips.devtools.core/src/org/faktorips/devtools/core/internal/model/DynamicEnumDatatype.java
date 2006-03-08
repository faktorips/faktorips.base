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

package org.faktorips.devtools.core.internal.model;

import org.faktorips.datatype.DefaultGenericEnumDatatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * A dynamic enum datatype. See the super class for more detais.
 * 
 * @author Jan Ortmann
 */
public class DynamicEnumDatatype extends DynamicValueDatatype implements
		EnumDatatype {

	private String getAllValuesMethodName = ""; //$NON-NLS-1$

	private String getNameMethodName = ""; //$NON-NLS-1$

	private boolean isSupportingNames = false;

	public DynamicEnumDatatype(IIpsProject ipsProject) {
		super(ipsProject);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getAllValueIds() {
		if (getAdaptedClass() == null) {
			throw new RuntimeException("Datatype " + getQualifiedName() //$NON-NLS-1$
					+ ", Class " + getAdaptedClassName() + " not found."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		DefaultGenericEnumDatatype datatype = new DefaultGenericEnumDatatype(
				getAdaptedClass());
		datatype.setGetAllValuesMethodName(getAllValuesMethodName);
		return datatype.getAllValueIds();
	}

	/**
	 * Sets the name of the method that provides all values of the datatype.
	 */
	public void setAllValuesMethodName(String getAllValuesMethodName) {
		this.getAllValuesMethodName = getAllValuesMethodName;
	}

	/**
	 * Sets the name of the method that returns the isSupportingNames flag of the enumeration class wrapped by this dynamic enum datatype.
	 */
	public void setIsSupportingNames(boolean supporting){
		this.isSupportingNames = supporting;
	}
	
	/**
	 * Sets the name of the method that returns the name of a value of the enumeration class wrapped by this dynamic enum datatype.
	 */
	public void setGetNameMethodName(String getNameMethodName){
		this.getNameMethodName = getNameMethodName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isSupportingNames() {
		return isSupportingNames;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValueName(String id) {

		if (isSupportingNames) {
			DefaultGenericEnumDatatype datatype = new DefaultGenericEnumDatatype(
					getAdaptedClass());
			datatype.setIsSupportingNames(isSupportingNames);
			datatype.setGetNameMethodName(getNameMethodName);
			return datatype.getValueName(id);
		}
		throw new UnsupportedOperationException(
				"The getName(String) method is not supported by this enumeration class: " //$NON-NLS-1$
						+ getAdaptedClass());
	}

}
