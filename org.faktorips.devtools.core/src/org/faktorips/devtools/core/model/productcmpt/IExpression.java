/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpt;

import java.util.List;

import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.fl.ExprCompiler;

/**
 * Base interface for expressions using the formula language.
 * 
 * @author Jan Ortmann
 */
public interface IExpression extends IIpsObjectPart, IDescribedElement {

    public final static String PROPERTY_FORMULA_SIGNATURE_NAME = "formulaSignature"; //$NON-NLS-1$
    public final static String PROPERTY_EXPRESSION = "expression"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "CONFIGELEMENT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the formula signature's can't be found.
     */
    public final static String MSGCODE_SIGNATURE_CANT_BE_FOUND = MSGCODE_PREFIX + "SignatureCantBeFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the formula signature's data type can't be found and
     * so the formula's data type can't be checked against it.
     */
    public final static String MSGCODE_UNKNOWN_DATATYPE_FORMULA = MSGCODE_PREFIX + "UnknownDatatypeFormula"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the formula's data type is not compatible with the
     * one defined by the signature.
     */
    public final static String MSGCODE_WRONG_FORMULA_DATATYPE = MSGCODE_PREFIX + "WrongFormulaDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the expression is empty (and it should not.
     */
    public final static String MSGCODE_EXPRESSION_IS_EMPTY = MSGCODE_PREFIX + "ExpressionIsEmpty"; //$NON-NLS-1$

    /**
     * For formulas this IIpsElement method returns the formula signature name.
     * 
     * @see #getFormulaSignature()
     */
    @Override
    String getName();

    /**
     * Returns the name of the product component type method signature this formula is an
     * implementation of.
     */
    String getFormulaSignature();

    /**
     * Sets the name of the product component type method signature this formula is an
     * implementation of.
     */
    void setFormulaSignature(String newName);

    /**
     * Returns the method signature this formula implements. Returns <code>null</code> if the method
     * signature is not found.
     */
    IProductCmptTypeMethod findFormulaSignature(IIpsProject ipsProject);

    /**
     * Returns the product component type of the product component this formula belongs to.
     */
    IProductCmptType findProductCmptType(IIpsProject ipsProject);

    /**
     * Returns the value data types the results of this formula expression are instances of. This
     * data type is equal to the formula signatures data type. If the formula signature's return
     * type is not a value data type (which is an error in the model and is reported via
     * validation), this method returns <code>null</code>.
     */
    ValueDatatype findValueDatatype(IIpsProject ipsProject);

    /**
     * Returns the formula expression.
     */
    String getExpression();

    /**
     * Sets the new formula expression.
     */
    void setExpression(String newExpression);

    /**
     * Returns an expression compiler that can be used to compile the formula. or <code>null</code>
     * if the element does not contain a formula.
     */
    ExprCompiler newExprCompiler(IIpsProject ipsProject);

    /**
     * Returns an expression compiler that can be used to compile the formula. or <code>null</code>
     * if the element does not contain a formula.
     * 
     * @param formulaTest if <code>true</code> the formula will be compiled for usage inside a
     *            formula test, in formula tests all type parameters will be replaced by their
     *            value, which is defined inside the formula test.
     */
    ExtendedExprCompiler newExprCompiler(IIpsProject ipsProject, boolean formulaTest);

    /**
     * Returns the enumeration data types that can be use in this formula. Allowed enumeration types
     * are those that are used as data type in one of the parameters or in a table used by the
     * product component generation this configuration element belongs to. If the data type of the
     * formula is itself an enumeration data type, this one is also returned.
     * <p>
     * Returns an empty array if this configuration element does not represent a formula.
     */
    EnumDatatype[] getEnumDatatypesAllowedInFormula();

    /**
     * Returns all parameter identifiers which are used in the formula. Identifiers used in a
     * formula an be either identify a parameter or an enum value. This methods returns all
     * identifiers identifying parameters. Returns an empty string array if no identifier was found
     * in formula or the config element is no formula type or the policy component type attribute -
     * which specifies the formula interface - wasn't found.
     * 
     * @param ipsProject The project which ips object path is used to search.
     * 
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    String[] getParameterIdentifiersUsedInFormula(IIpsProject ipsProject);

    List<IAttribute> findMatchingProductCmptTypeAttributes();

}
