/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.testcasetype;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestParameterTest extends AbstractIpsPluginTest {

    private ITestCaseType testCaseType;
    private ITestPolicyCmptTypeParameter testParam;
    private IIpsProject project;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        testParam = testCaseType.newInputTestPolicyCmptTypeParameter();
        testParam.setName("testPolicyCmptTypeParam");
    }

    @Test
    public void testValidateDuplicateName() throws Exception {
        MessageList ml = testParam.validate(project);
        assertNull(ml.getMessageByCode(ITestParameter.MSGCODE_DUPLICATE_NAME));

        testCaseType.newExpectedResultPolicyCmptTypeParameter().setName(testParam.getName());
        ml = testParam.validate(project);
        assertNotNull(ml.getMessageByCode(ITestParameter.MSGCODE_DUPLICATE_NAME));

        testParam.setName("param1");
        ml = testParam.validate(project);
        assertNull(ml.getMessageByCode(ITestParameter.MSGCODE_DUPLICATE_NAME));

        ITestPolicyCmptTypeParameter paramChild1 = testParam.newTestPolicyCmptTypeParamChild();
        ITestPolicyCmptTypeParameter paramChild2 = testParam.newTestPolicyCmptTypeParamChild();
        paramChild1.setName("child1");
        paramChild2.setName("child1");
        ml = paramChild1.validate(project);
        assertNotNull(ml.getMessageByCode(ITestParameter.MSGCODE_DUPLICATE_NAME));
        ml = paramChild2.validate(project);
        assertNotNull(ml.getMessageByCode(ITestParameter.MSGCODE_DUPLICATE_NAME));

        paramChild2.setName("child2");
        ml = paramChild1.validate(project);
        assertNull(ml.getMessageByCode(ITestParameter.MSGCODE_DUPLICATE_NAME));
        ml = paramChild2.validate(project);
        assertNull(ml.getMessageByCode(ITestParameter.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testValidateInvalidName() throws Exception {
        MessageList ml = testParam.validate(project);
        assertNull(ml.getMessageByCode(ITestParameter.MSGCODE_INVALID_NAME));

        testParam.setName("1");
        ml = testParam.validate(project);
        assertNotNull(ml.getMessageByCode(ITestParameter.MSGCODE_INVALID_NAME));

        testParam.setName("param 1");
        ml = testParam.validate(project);
        assertNotNull(ml.getMessageByCode(ITestParameter.MSGCODE_INVALID_NAME));

        testParam.setName("param1");
        ml = testParam.validate(project);
        assertNull(ml.getMessageByCode(ITestParameter.MSGCODE_INVALID_NAME));
    }
}
