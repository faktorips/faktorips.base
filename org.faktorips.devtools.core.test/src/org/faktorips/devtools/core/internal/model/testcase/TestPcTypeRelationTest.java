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

package org.faktorips.devtools.core.internal.model.testcase;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestPcTypeRelationTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITestCase testCase;
    private ITestPolicyCmptRelation testPcTypeRelation;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "PremiumCalculation");
        ITestPolicyCmptTypeParameter param = testCaseType.newExpectedResultPolicyCmptTypeParameter();
        param.setName("expectedResultParam");
        ITestPolicyCmptTypeParameter paramChild = param.newTestPolicyCmptTypeParamChild();
        paramChild.setName("childParam");
        
        testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "PremiumCalculation");
        testCase.setTestCaseType(testCaseType.getName());
        
        ITestPolicyCmpt tpc = testCase.newTestPolicyCmpt();
        tpc.setTestPolicyCmptTypeParameter("expectedResultParam");
        testPcTypeRelation = tpc.newTestPolicyCmptRelation();
        testPcTypeRelation.setTestPolicyCmptTypeParameter("childParam");
    }
    
    public void testInitFromXml() {
        Element docEl = getTestDocument().getDocumentElement();
        Element paramEl = XmlUtil.getFirstElement(docEl);
        testPcTypeRelation.initFromXml(paramEl);
        assertEquals("relation1", testPcTypeRelation.getTestPolicyCmptTypeParameter());
        assertEquals("base.target1", testPcTypeRelation.getTarget());
    }

    public void testToXml() {
        testPcTypeRelation.setTestPolicyCmptTypeParameter("relation2");
        testPcTypeRelation.setTarget("base.target2");
        Element el = testPcTypeRelation.toXml(newDocument());
        testPcTypeRelation.setTestPolicyCmptTypeParameter("test1");
        testPcTypeRelation.setTarget("test2");
        testPcTypeRelation.initFromXml(el);
        assertEquals("relation2", testPcTypeRelation.getTestPolicyCmptTypeParameter());
        assertEquals("base.target2", testPcTypeRelation.getTarget());
    }
    
    public void testValidateTestCaseTypeParamNotFound() throws Exception{
        MessageList ml = testPcTypeRelation.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));

        testPcTypeRelation.setTestPolicyCmptTypeParameter("x");
        ml = testPcTypeRelation.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }

    public void testValidateAssoziationTargetNotInTestCase() throws Exception{
        testCase.newTestPolicyCmpt().setName("testPolicyCmptTarget");
        
        MessageList ml = testPcTypeRelation.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE));

        testPcTypeRelation.setTarget("x");
        ml = testPcTypeRelation.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_ASSOZIATION_TARGET_NOT_IN_TEST_CASE));
    }

    public void testValidateModelRelationNotFound() throws Exception{
        IPolicyCmptType policyCmptType = newPolicyCmptType(project, "policyCmptType");
        policyCmptType.newPolicyCmptTypeAssociation().setTargetRoleSingular("modelRelation");
        ITestPolicyCmptTypeParameter param = ((ITestPolicyCmpt)testPcTypeRelation.getParent()).findTestPolicyCmptTypeParameter(project);
        param.setPolicyCmptType("policyCmptType");
        ITestPolicyCmptTypeParameter paramChild = testPcTypeRelation.findTestPolicyCmptTypeParameter(project);
        paramChild.setRelation("modelRelation");

        MessageList ml = testPcTypeRelation.validate(project);
        assertNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MODEL_RELATION_NOT_FOUND));

        paramChild.setRelation("x");
        ml = testPcTypeRelation.validate(project);
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MODEL_RELATION_NOT_FOUND));
    }
}
