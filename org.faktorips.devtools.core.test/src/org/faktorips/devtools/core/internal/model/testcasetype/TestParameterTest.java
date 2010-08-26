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

package org.faktorips.devtools.core.internal.model.testcasetype;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestParameter;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestParameterTest extends AbstractIpsPluginTest {

    private ITestCaseType testCaseType;
    private ITestPolicyCmptTypeParameter testParam;
    private IIpsProject project;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        testParam = testCaseType.newInputTestPolicyCmptTypeParameter();
        testParam.setName("testPolicyCmptTypeParam");
    }

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
