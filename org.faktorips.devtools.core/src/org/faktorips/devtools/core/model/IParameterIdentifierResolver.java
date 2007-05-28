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

package org.faktorips.devtools.core.model;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.fl.IdentifierResolver;

/**
 * A specialized interface of an <code>IdentifierResolver</code> interface where
 * Parameters can be set to and evaluated in the compile method. An
 * <code>IIpsArtefactBuilderSet</code> provides an implementation of this
 * interface if it supports the formula language compiler.
 * <p>
 * Note: This identifier resolver not only resolves parameter identifiers but also enum values.
 * E.g. given an enum type gender and a parameter insuredPerson the following is a valid expression:
 * WENN(insuredPerson.gender=Gender.MALE; 1; 2). Here we have to identifiers. One for the parameter 
 * and one for the enum value.
 * <p>
 * 
 * @author Peter Erzberger
 */
public interface IParameterIdentifierResolver extends IdentifierResolver {

	/**
	 * Parameters are provided to identifier resolver implementations by means of this method.
	 */
	public void setParameters(Parameter[] parameters);

    /**
     * Sets enum datatypes that can be resolved by this identifier.
     */
    public void setEnumDatatypes(EnumDatatype[] enumtypes);
    
    /**
     * Returns enum datatypes that can be resolved by this identifier.
     */
    public EnumDatatype[] getEnumDatatypes();
    
	/**
	 * Implementations might need a reference to the current IpsProject which is provided by this method.
	 * @param ipsProject
	 */
	public void setIpsProject(IIpsProject ipsProject);
    
    /**
     * Removes from the provided identifiers the ones which this IdentifierResolver recognizes as enum values.
     * It leaves the provided string array untouched and returns a new string array. 
     */
    public String[] removeIdentifieresOfEnumDatatypes(String[] allIdentifiersUsedInFormula);    
}
