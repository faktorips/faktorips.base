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

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * Specification of a formula test to test a product formula.
 * 
 * @author Joerg Ortmann
 */
public interface IFormulaTestCase extends IIpsObjectPart {

    /** Property names */
    public final static String PROPERTY_EXPECTED_RESULT = "expectedResult"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "FORMULATESTCASE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the test parameter name is duplicate.
     */
    public final static String MSGCODE_DUPLICATE_NAME = MSGCODE_PREFIX + "DuplicateName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the identifier in the formula all given as
     * identifier for the test input values and no formula input value is unnecessary.
     */
    public final static String MSGCODE_IDENTIFIER_MISMATCH = MSGCODE_PREFIX + "IdentifierMismatch"; //$NON-NLS-1$

    /**
     * Sets the name of the formula test.
     */
    public void setName(String name);

    /**
     * Returns the formula this test case tests.
     */
    public IFormula getFormula();

    /**
     * Adds a new formula test input value.
     */
    public IFormulaTestInputValue newFormulaTestInputValue();

    /**
     * Returns all formula test input values. Returns <code>null</code> if no values exist.
     */
    public IFormulaTestInputValue[] getFormulaTestInputValues();

    /**
     * Returns the given formula test input value or <code>null</code> if the value not exists.
     */
    public IFormulaTestInputValue getFormulaTestInputValue(String identifier);

    /**
     * Returns the expected result.
     */
    public String getExpectedResult();

    /**
     * Sets the result which is expected if executing this formula test.
     */
    public void setExpectedResult(String expectedResult);

    /**
     * Execute the formula test with the stored values and returns the result of the executed
     * formula.
     * 
     * @throws Exception If an error occurs while evaluating the result
     */
    public Object execute(IIpsProject ipsProject) throws Exception;

    /**
     * Adds new or delete unused formula test input values. Returns <code>true</code> if one or more
     * formula test input values are deleted or added. If there was no change returns
     * <code>false</code>.
     */
    public boolean addOrDeleteFormulaTestInputValues(String[] newIdentifiers, IIpsProject ipsProject);

    /**
     * Returns <code>true</code> if the formula test case is empty. The formula is empty if it has
     * no formula test input values or all input values are empty.
     */
    public boolean isFormulaTestCaseEmpty();

    /**
     * Evaluates and returns an unique name of a formula test case based on the given name proposal.<br>
     * If there is already a name with the given name then the name concatenated with " (n)" will be
     * returned, n will be incremented until the name is unique for all formula test cases in the
     * parent configuration element.
     */
    // TODO should be moved to IFormula
    public String generateUniqueNameForFormulaTestCase(String nameProposal);

}
