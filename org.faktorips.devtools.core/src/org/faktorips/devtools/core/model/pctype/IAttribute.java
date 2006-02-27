package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.product.ConfigElementType;


/**
 * A policy component type's attribute.
 */
public interface IAttribute extends IMember {

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
    public ValueSet getValueSet();

    /**
     * Sets the set of allowed values.
     */
    public void setValueSet(ValueSet set);
    
}
