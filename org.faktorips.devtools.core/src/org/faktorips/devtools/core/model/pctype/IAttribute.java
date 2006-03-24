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

package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IValueDatatypeProvider;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.product.ConfigElementType;


/**
 * A policy component type's attribute.
 */
public interface IAttribute extends IMember, IValueDatatypeProvider {

    // property names
    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$
    public final static String PROPERTY_ATTRIBUTE_TYPE = "attributeType"; //$NON-NLS-1$
    public final static String PROPERTY_MODIFIER = "modifier"; //$NON-NLS-1$
    public final static String PROPERTY_PRODUCT_RELEVANT = "productRelevant"; //$NON-NLS-1$
    public final static String PROPERTY_DEFAULT_VALUE = "defaultValue"; //$NON-NLS-1$
    public final static String PROPERTY_FORMULA_PARAMETERS = "parameters"; //$NON-NLS-1$
    
    public final static String PROPERTY_FORMULAPARAM_NAME = "param.name"; //$NON-NLS-1$
    public final static String PROPERTY_FORMULAPARAM_DATATYPE = "param.datatype"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "ATTRIBUTE-"; //$NON-NLS-1$

    /**
	 * Validation message code to indicate that an attribute can't be product
	 * relevant if the type is configurable by product.
	 */
	public final static String MSGCODE_ATTRIBUTE_CANT_BE_PRODUCT_RELEVANT_IF_TYPE_IS_NOT = MSGCODE_PREFIX
			+ "AttributeCantBeProductRelevantIfTypeIsNot"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the name of the attribute is not a valid
	 * java field identifier.
	 */
	public final static String MSGCODE_INVALID_ATTRIBUTE_NAME = MSGCODE_PREFIX
			+ "InvalidAttributeName"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the datatype of this attribute is not set.
	 */
	public final static String MSGCODE_DEFAULT_NOT_PARSABLE_UNKNOWN_DATATYPE = MSGCODE_PREFIX
			+ "DefaultNotParsableUnknownDatatype"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the datatype of this attibute is not a valid datatype.
	 */
	public final static String MSGCODE_DEFAULT_NOT_PARSABLE_INVALID_DATATYPE = MSGCODE_PREFIX
			+ "ValueNotParsableInvalidDatatype"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the default-value of this attribute can not be
	 * parsed by the datatype of this attribute.
	 */
	public final static String MSGCODE_VALUE_NOT_PARSABLE = MSGCODE_PREFIX
			+ "ValueTypeMissmatch"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the default-value of this attribute
	 * is not contained in the valueset of this attribute.
	 */
	public final static String MSGCODE_DEFAULT_NOT_IN_VALUESET = MSGCODE_PREFIX
			+ "DefaultNotInValueSet"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that there is no input parameter
	 * for this (computed or derived) attribute.
	 */
	public final static String MSGCODE_NO_INPUT_PARAMETERS = MSGCODE_PREFIX
			+ "NoInputParameters"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that parameters are provided, but they
	 * are not neccessary because this attribute is neither computed nor derived.
	 */
	public final static String MSGCODE_NO_PARAMETERS_NECCESSARY = MSGCODE_PREFIX
			+ "NoParametersNeccessary"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the name of a parameter is empty.
	 */
	public final static String MSGCODE_EMPTY_PARAMETER_NAME = MSGCODE_PREFIX
			+ "EmptyParameterName"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the name of a parameter is not
	 * a valid java identifier.
	 */
	public final static String MSGCODE_INVALID_PARAMETER_NAME = MSGCODE_PREFIX
			+ "InvalidParameterName"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that a datatype is missing for a parameter.
	 */
	public final static String MSGCODE_NO_DATATYPE_FOR_PARAMETER = MSGCODE_PREFIX
			+ "NoDatatypeForParameter"; //$NON-NLS-1$
    
    /**
	 * Validation message code to indicate that the datatype provided for a parameter 
	 * is not valid.
	 */
	public final static String MSGCODE_DATATYPE_NOT_FOUND = MSGCODE_PREFIX
			+ "DatatypeNotFound"; //$NON-NLS-1$
    
    /**
	 * Returns the attribute's datatype.
	 */
    public String getDatatype();
    
    /**
     * Sets the attribute's datatype.
     */
    public void setDatatype(String newDatatype);
    
    /**
     * Returns the attribute's datatype or <code>null</code> if the datatype name
     * can't be resolved.
     * 
     * @throws CoreException if an error occurs while searching for the datatype.
     */
    public Datatype findDatatype() throws CoreException;
    
    /**
     * Returns the attribute's type.
     */
    public AttributeType getAttributeType();
    
    /**
     * Sets the attribute's type.
     */
    public void setAttributeType(AttributeType newType);
    
    /**
     * Returns true if the attribute type is changeable.
     */
    public boolean isChangeable();
    
    /**
     * Returns true if this attribute is derivede or computed.
     */
    public boolean isDerivedOrComputed();
    
    /**
     * Returns the type of the product configuration element this attribute
     * defines. Returns <code>null</code> if this attribute is not product
     * relevant.
     */
    public ConfigElementType getConfigElementType();
    
    /**
     * Returns the attribute's modifier.
     */
    public Modifier getModifier();
    
    /**
     * Sets the attribute's modifier.
     */
    public void setModifier(Modifier newModifier);
    
    /**
     * Returns the attribute's default value.
     */
    public String getDefaultValue();
    
    /**
     * Sets the attribute's default value.
     */
    public void setDefaultValue(String newValue);
    
    /**
     * Returns true if this attribute is product relevant, that means the
     * product developer can configure some aspect of the attribute.
     */
    public boolean isProductRelevant();

    /**
     * Sets if this attribute is product relevant or not.
     */
    public void setProductRelevant(boolean newValue);
    
    /**
     * Returns the attribute's formnula parameters. Returns an empty array if the method
     * doesn't have any parameter. 
     */
    public Parameter[] getFormulaParameters();
    
    /**
     * Returns the number of formula parameters.
     */
    public int getNumOfFormulaParameters();
    
    /**
     * Sets the attributes's formula parameters.
     * 
     * @throws NullPointerException if params if <code>null</code>.
     */
    public void setFormulaParameters(Parameter[] params);
    
    /**
     * Returns the set of allowed values.
     */
    public IValueSet getValueSet();

    /**
     * Sets the type of the value set defining the values valid for this attribute.
     * If the type of the currently existing value set is the same as the one to set, all
     * old informations (e.g. bounds and step for a range value set) are removed.
     */
    public void setValueSetType(ValueSetType type);
    
}
