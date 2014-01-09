/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * Specification of a test policy component link.
 * 
 * @author Joerg Ortmann
 */
public interface ITestPolicyCmptLink extends IIpsObjectPart {

    /** Property names */
    public final static String PROPERTY_POLICYCMPTTYPE = "testPolicyCmptType"; //$NON-NLS-1$
    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TESTPOLICYCMPTLINK-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the target of an association is not in the test
     * case.
     */
    public final static String MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE = MSGCODE_PREFIX
            + "AssoziationTargetNotInTestCase"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test case type parameter not
     * exists.
     */
    public final static String MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND = MSGCODE_PREFIX + "TestCaseTypeParamNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the model link which is related by the corresponding
     * test case type parameter not exists.
     */
    public final static String MSGCODE_MODEL_LINK_NOT_FOUND = MSGCODE_PREFIX + "ModelLinkNotFound"; //$NON-NLS-1$

    /**
     * Returns the name of test policy component type parameter.
     */
    public String getTestPolicyCmptTypeParameter();

    /**
     * Sets the name of test policy component type parameter.
     */
    public void setTestPolicyCmptTypeParameter(String pcType);

    /**
     * Returns the test policy component type parameter or <code>null</code> if the test policy
     * component type parameter doesn't exists.
     * 
     * @param ipsProject The ips project which object path is used to search.
     * 
     * @throws CoreException if an error occurs while searching for the test policy component type
     *             parameter.
     */
    public ITestPolicyCmptTypeParameter findTestPolicyCmptTypeParameter(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the target.
     */
    public String getTarget();

    /**
     * Sets the target.
     */
    public void setTarget(String target);

    /**
     * Returns the target or <code>null</code> if the target does not exists.
     * 
     * @throws CoreException if an error occurs while searching for the target.
     */
    public ITestPolicyCmpt findTarget() throws CoreException;

    /**
     * Creates a new test policy component as child and returns it.
     */
    public ITestPolicyCmpt newTargetTestPolicyCmptChild();

    /**
     * Returns <code>true</code> if the link is an association.
     */
    public boolean isAccoziation();

    /**
     * Returns <code>true</code> if the link is a composition.
     */
    public boolean isComposition();

    /**
     * Returns the test case this link belongs to.
     */
    public ITestCase getTestCase();

}
