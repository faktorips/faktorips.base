/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IParameter;

/**
 * Specification of a input test value to test a product formula.
 * 
 * @author Joerg Ortmann
 */
public interface IFormulaTestInputValue extends IIpsObjectPart {

    public final static String PROPERTY_IDENTIFIER = "identifier"; //$NON-NLS-1$
    public final static String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "FORMULATESTINPUTVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the formula parameter exists.
     */
    public final static String MSGCODE_FORMULA_PARAMETER_NOT_FOUND = MSGCODE_PREFIX + "FormulaParameterNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the formula parameter has an unsupported data type.
     */
    public final static String MSGCODE_FORMULA_PARAMETER_HAS_UNSUPPORTED_DATATYPE = MSGCODE_PREFIX
            + "FormulaParameterHasUnsupportedDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of the related attribute wasn't found.
     */
    public final static String MSGCODE_DATATYPE_NOT_FOUND = MSGCODE_PREFIX + "DatatypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of the related attribute wasn't found.
     */
    public final static String MSGCODE_DATATYPE_OF_RELATED_ATTRIBUTE_NOT_FOUND = MSGCODE_PREFIX
            + "DatatypeOfRelatedAttributeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the related attribute wasn't found.
     */
    public final static String MSGCODE_RELATED_ATTRIBUTE_NOT_FOUND = MSGCODE_PREFIX + "RelatedAttributeNotFound"; //$NON-NLS-1$

    /**
     * Sets the identifier of the formula test input.
     */
    public void setIdentifier(String identifier);

    /**
     * Returns the identifier of the formula test input.
     */
    public String getIdentifier();

    /**
     * Returns the formula parameter the formula test input belongs to, or <code>null</code> if the
     * parameter wasn't found.
     * 
     * @throws CoreException If an error occurred
     */
    public IParameter findFormulaParameter(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets the value of the formula test input.
     */
    public void setValue(String value);

    /**
     * Returns the identifier of the formula test input value.
     */
    public String getValue();

    /**
     * Search and returns the data type of the corresponding formula parameter. If the formula
     * parameter specifies a ValueDataType then this data type will be returned and if the formula
     * parameter specifies a type parameter then the data type of the attribute which is identified
     * by the identifier will be returned (e.g. if the identifier is "policy.premium" then the data
     * type of the attribute premium will be returned).
     * 
     * @throws CoreException If an error occurs during searching the corresponding data type.
     */
    public ValueDatatype findDatatypeOfFormulaParameter(IIpsProject ipsProject) throws CoreException;

}
