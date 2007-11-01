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

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.devtools.core.model.type.IMethod;

/**
 * Method signatures for product component types extend the "normal" method. The provide an implementation type
 * that defines how the method is implemented. 
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeMethod extends IMethod, IProdDefProperty {

    public final static String PROPERTY_FORMULA_SIGNATURE_DEFINITION = "formulaSignatureDefinition";
    public final static String PROPERTY_FORMULA_NAME = "formulaName";
    
    public final static String PROPERTY_IMPLEMENTATION_TYPE= "implementationType";

    /**
     * Validation message code to indicate that the datatype of a formula signature definition is
     * void or a none valuedatatype.
     */
    public final static String MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES = MSGCODE_PREFIX + "DatatypeMustBeAValueDatatypeForFormulaSignature"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that a method is marked as formula signature but 
     * the formula name is missing.
     */
    public final static String MSGCODE_FORMULA_NAME_IS_EMPTY = MSGCODE_PREFIX + "FormulaNameIsEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a method is marked as formula signature but 
     * the formula name is missing.
     */
    public final static String MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT = MSGCODE_PREFIX + "FormulaMustntBeAbstract"; //$NON-NLS-1$

    /**
     * Returns the product component type this method belongs to.
     */
    public IProductCmptType getProductCmptType();
    
    /**
     * Returns <code>true</code> if this is formula signature definition, <code>false</code> if
     * it is not. 
     */
    public boolean isFormulaSignatureDefinition();

    public void setFormulaSignatureDefinition(boolean newValue);
    
    public String getFormulaName();
    
    public void setFormulaName(String newName);
    
    /**
     * Returns a default name for the method based on the formula name. E.g. if the formula name is
     * 'Premium' the method  meight return 'computePremium'.
     */
    public String getDefaultMethodName();
    
}
