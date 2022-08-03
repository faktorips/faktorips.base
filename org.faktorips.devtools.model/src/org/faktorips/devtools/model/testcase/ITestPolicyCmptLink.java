/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcase;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * Specification of a test policy component link.
 * 
 * @author Joerg Ortmann
 */
public interface ITestPolicyCmptLink extends IIpsObjectPart {

    /** Property names */
    String PROPERTY_POLICYCMPTTYPE = "testPolicyCmptType"; //$NON-NLS-1$
    String PROPERTY_TARGET = "target"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "TESTPOLICYCMPTLINK-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target of an association is not in the test
     * case.
     */
    String MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE = MSGCODE_PREFIX
            + "AssoziationTargetNotInTestCase"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test case type parameter not
     * exists.
     */
    String MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND = MSGCODE_PREFIX + "TestCaseTypeParamNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the model link which is related by the corresponding
     * test case type parameter not exists.
     */
    String MSGCODE_MODEL_LINK_NOT_FOUND = MSGCODE_PREFIX + "ModelLinkNotFound"; //$NON-NLS-1$

    /**
     * Returns the name of test policy component type parameter.
     */
    String getTestPolicyCmptTypeParameter();

    /**
     * Sets the name of test policy component type parameter.
     */
    void setTestPolicyCmptTypeParameter(String pcType);

    /**
     * Returns the test policy component type parameter or <code>null</code> if the test policy
     * component type parameter doesn't exists.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching for the test policy component type
     *             parameter.
     */
    ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the target.
     */
    String getTarget();

    /**
     * Sets the target.
     */
    void setTarget(String target);

    /**
     * Returns the target or <code>null</code> if the target does not exist.
     */
    ITestPolicyCmpt findTarget();

    /**
     * Creates a new test policy component as child and returns it.
     */
    ITestPolicyCmpt newTargetTestPolicyCmptChild();

    /**
     * Returns <code>true</code> if the link is an association.
     */
    boolean isAssociation();

    /**
     * Returns <code>true</code> if the link is a composition.
     */
    boolean isComposition();

    /**
     * Returns the test case this link belongs to.
     */
    ITestCase getTestCase();

}
