package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.product.ConfigElementType;


/**
 * A policy component type's attribute.
 */
public interface IAttribute extends IMember {

    // property names
    public final static String PROPERTY_DATATYPE = "datatype";
    public final static String PROPERTY_ATTRIBUTE_TYPE = "attributeType";
    public final static String PROPERTY_MODIFIER = "modifier";
    public final static String PROPERTY_PRODUCT_RELEVANT = "productRelevant";
    public final static String PROPERTY_DEFAULT_VALUE = "defaultValue";
    public final static String PROPERTY_FORMULA_PARAMETERS = "parameters";
    
    public final static String PROPERTY_FORMULAPARAM_NAME = "param.name";
    public final static String PROPERTY_FORMULAPARAM_DATATYPE = "param.datatype";
    
    ////Konstanten die mit JAVA_ anfangen sind deprecated
    // type constants for corresponding Java methods
    public final static int JAVA_NUMOF_METHOD = 15;
    
    public final static int JAVA_GETTER_METHOD_IMPLEMENATION = 0;
    public final static int JAVA_SETTER_METHOD_IMPLEMENATION = 1;
    public final static int JAVA_GETTER_METHOD_INTERFACE = 2;
    public final static int JAVA_SETTER_METHOD_INTERFACE = 3;
    
    /** 
     * Constant for the computation method for a derived or computed attribute in the 
     * product component interface.     
     */
    public final static int JAVA_COMPUTE_ATTRIBUTE_METHOD_PRODUCTCMPT_INTERFACE = 4;

    public final static int JAVA_GETTER_METHOD_DEFAULTVALUE_PRODUCT_INTERFACE =5;
    public final static int JAVA_GETTER_METHOD_VALUE_PRODUCT_INTERFACE = 6;
    public final static int JAVA_GETTER_METHOD_DEFAULTVALUE_PRODUCT_IMPL =7;
    public final static int JAVA_GETTER_METHOD_VALUE_PRODUCT_IMPL = 8;
    
    public final static int JAVA_GETTER_METHOD_MAX_VALUESET_POLICY_INTERFACE = 9;
    public final static int JAVA_GETTER_METHOD_MAX_VALUESET_POLICY_IMPL = 10;
    public final static int JAVA_GETTER_METHOD_MAX_VALUESET_PRODUCT_INTERFACE = 11;
    public final static int JAVA_GETTER_METHOD_MAX_VALUESET_PRODUCT_IMPL = 12;
    public final static int JAVA_GETTER_METHOD_VALUESET_POLICY_INTERFACE = 13;
    public final static int JAVA_GETTER_METHOD_VALUESET_POLICY_IMPL = 14;
    
    
    // type constants for corresponding Java fields
    public final static int JAVA_FIELD_VALUE_POLICY = 0;
    public final static int JAVA_FIELD_VALUESET_POLICY = 1;
    public final static int JAVA_FIELD_VALUE_PRODUCT = 0;   
    public final static int JAVA_FIELD_VALUESET_PRODUCT = 1;    
    
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
     * defines. Returns <code>null</code> if this attrihute is not product
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
     * Returns the Java method that corresponds to the attribute and is of
     * the indicated type.
     * 
     * @param type A type constant identifying the type of method.
     * @return The corresponding Java method. Note that the method might not
     * exists!
     * @deprecated
     * @throws IllegalArgumentException if the type constant is illegal.   
     */
    public IMethod getJavaMethod(int type) throws CoreException;
    
    /**
     * Returns the set of allowed values.
     */
    public ValueSet getValueSet();

    /**
     * Sets the set of allowed values.
     */
    public void setValueSet(ValueSet set);
    
}
