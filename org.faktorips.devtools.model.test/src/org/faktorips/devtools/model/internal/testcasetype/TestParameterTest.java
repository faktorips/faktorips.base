/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcasetype;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestParameter;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.runtime.MessageList;
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
        assertThat(ml, lacksMessageCode(ITestParameter.MSGCODE_DUPLICATE_NAME));

        testCaseType.newExpectedResultPolicyCmptTypeParameter().setName(testParam.getName());
        ml = testParam.validate(project);
        assertThat(ml, hasMessageCode(ITestParameter.MSGCODE_DUPLICATE_NAME));

        testParam.setName("param1");
        ml = testParam.validate(project);
        assertThat(ml, lacksMessageCode(ITestParameter.MSGCODE_DUPLICATE_NAME));

        ITestPolicyCmptTypeParameter paramChild1 = testParam.newTestPolicyCmptTypeParamChild();
        ITestPolicyCmptTypeParameter paramChild2 = testParam.newTestPolicyCmptTypeParamChild();
        paramChild1.setName("child1");
        paramChild2.setName("child1");
        ml = paramChild1.validate(project);
        assertThat(ml, hasMessageCode(ITestParameter.MSGCODE_DUPLICATE_NAME));
        ml = paramChild2.validate(project);
        assertThat(ml, hasMessageCode(ITestParameter.MSGCODE_DUPLICATE_NAME));

        paramChild2.setName("child2");
        ml = paramChild1.validate(project);
        assertThat(ml, lacksMessageCode(ITestParameter.MSGCODE_DUPLICATE_NAME));
        ml = paramChild2.validate(project);
        assertThat(ml, lacksMessageCode(ITestParameter.MSGCODE_DUPLICATE_NAME));
    }

    @Test
    public void testValidateInvalidName() throws Exception {
        MessageList ml = testParam.validate(project);
        assertThat(ml, lacksMessageCode(ITestParameter.MSGCODE_INVALID_NAME));

        testParam.setName("1");
        ml = testParam.validate(project);
        assertThat(ml, hasMessageCode(ITestParameter.MSGCODE_INVALID_NAME));

        testParam.setName("param 1");
        ml = testParam.validate(project);
        assertThat(ml, hasMessageCode(ITestParameter.MSGCODE_INVALID_NAME));

        testParam.setName("param1");
        ml = testParam.validate(project);
        assertThat(ml, lacksMessageCode(ITestParameter.MSGCODE_INVALID_NAME));
    }
}
