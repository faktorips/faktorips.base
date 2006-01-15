package org.faktorips.devtools.core.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.fl.ExprCompiler;


/**
 * A product attribute is a product component's attribute that
 * is based upon a constant, product relevant attribute of the policy component type
 * this product component is based on. 
 * <p>
 * For example a policy component could have a constant attribute interestRate.
 * All product components based on that policy component have a matching
 * product attribute that stores the concrete interest rate value.  
 */
public interface IConfigElement extends IIpsObjectPart {
    
    /**
     * Constant that indentfies the compute method that corresponds to this config element
     * if it is a formula.
     */
    public final static int JAVA_METHOD_COMPUTE = 0;

    public final static String PROPERTY_TYPE = "type";
    public final static String PROPERTY_PCTYPE_ATTRIBUTE = "pcTypeAttribute";
    public final static String PROPERTY_VALUE = "value";
    
    /**
     * Validation message code to indicate that the attribute's datatype can't be found and so the 
     * formula's datatype can't be checked against it.
     */
    public final static String MSGCODE_CANT_FIND_ATTRIBUTE_DATATYPE = "PRODCMPT-Can't find attribute datatype";
    
    /**
     * Validation message code to indicate that the formula's datatype is not compatible with the 
     * one defined by the attribute.
     */
    public final static String MSGCODE_FORMULA_HAS_WRONG_DATATYPE = "PRODCMPT-Formula has wrong datatype";
    
    /**
     * Returns the product component generation this config element belongs to.
     */
    public IProductCmpt getProductCmpt();
    
    /**
     * Returns the product component generation this config element belongs to.
     */
    public IProductCmptGeneration getProductCmptGeneration();
    
    /**
     * Returns this element's type.
     */
    public ConfigElementType getType();
    
    /**
     * Sets this element's type.
     */
    public void setType(ConfigElementType newType);
    
    /**
     * Returns the name of the policy component type's attribute
     * this attribute is based on.
     */
    public String getPcTypeAttribute();
    
    /**
     * Sets the name of the policy component type's attribute
     * this attribute is based on.
     * 
     * @throws IllegalArgumentException if name is <code>null</code>.
     */
    public void setPcTypeAttribute(String name);
    
    /**
     * Returns the attribute's value. 
     */
    public String getValue();
    
    /**
     * Sets the attribute's value. 
     */
    public void setValue(String newValue);
    
    /**
     * Returns the set of allowed values.
     */
    public ValueSet getValueSet();

    /**
     * Sets the set of allowed values.
     */
    public void setValueSet(ValueSet set);

    /**
     * Returns an expression compiler that can be used to compile the formula.
     * or <code>null</code> if the element does not contain a formula.
     */
    public ExprCompiler getExprCompiler() throws CoreException;
    
    /**
     * Finds the corresponding attribute in the policy component type this
     * product component is based on.
     * 
     * @return the corresponding attribute or <code>null</code> if no such
     * attribute exists.
     * 
     * @throws CoreException if an exception occurs while searching the policy component type. 
     */
    public IAttribute findPcTypeAttribute() throws CoreException;
    
}
