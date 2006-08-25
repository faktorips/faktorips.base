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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.DefaultTestContent;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestCaseAndTestCaseTypeTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    
    private ITestCase testCase;
    private ITestCaseType testCaseType;
    
    private DefaultTestContent content;
    
    private String pathToTestPolicyCmptInput;
    
    public void setUp() throws Exception{
        super.setUp();
        content = new DefaultTestContent();
        project = content.getProject();
        testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "TestCase1");  
        testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "TestCaseType1");  
        testCase.setTestCaseType(testCaseType.getQualifiedName());
        
        // search for the first relation in the default content
        // will be used in the test case type
        IPolicyCmptType pct = content.getContract();
        IRelation[] relations = pct.getRelations();
        if (relations == null || ! (relations.length > 0)){
            fail("Wrong test project content. No relation exists in default content.");
        }
        IRelation relation = null;
        for (int i = 0; i < relations.length; i++) {
            if (! relations[i].isAssoziation()){
                relation = relations[0];
                break;
            }
        }
        if (relation == null){
            fail("Wrong test project content. No composite relation exisits in default content.");
        }
        
        // create test case type side
        ITestPolicyCmptTypeParameter tp = testCaseType.newInputTestPolicyCmptTypeParameter();
        tp.setName("inputTestPolicyCmptTypeParam1");
        tp.setPolicyCmptType(pct.getQualifiedName());
        tp.setName(StringUtil.unqualifiedName(pct.getName()));
        ITestPolicyCmptTypeParameter tpChild = tp.newTestPolicyCmptTypeParamChild();
        tpChild.setRelation(relation.getName());
        tpChild.setName(relation.getName());
        testCaseType.newInputTestValueParameter().setName("inputValueParameter1");
        
        testCaseType.newExpectedResultPolicyCmptTypeParameter().setName("expectedResultTestPolicyCmptTypeParam1");
        testCaseType.newExpectedResultValueParameter().setName("expectedResultValueParameter1");
        testCaseType.newExpectedResultValueParameter().setName("expectedResultValueParameter2");
        testCaseType.newExpectedResultValueParameter().setName("expectedResultValueParameter3");
        testCaseType.newExpectedResultValueParameter().setName("expectedResultValueParameter4");
        
        // create test case side
        ITestPolicyCmpt pc = testCase.newTestPolicyCmpt();
        pc.setTestPolicyCmptTypeParameter("inputTestPolicyCmptTypeParam1");
        pc.setTestPolicyCmptTypeParameter(tp.getName());
        pc.setName(tp.getName());
        ITestPolicyCmptRelation pcr = pc.addTestPcTypeRelation(tpChild, "", "");
        ITestPolicyCmpt pcChild = pcr.findTarget();
        pathToTestPolicyCmptInput = new TestCaseHierarchyPath(pcChild, true).getHierarchyPath();
        testCase.newTestValue().setTestValueParameter("inputValueParameter1");
        
        testCase.newTestPolicyCmpt().setTestPolicyCmptTypeParameter("expectedResultTestPolicyCmptTypeParam1");

        testCase.newTestValue().setTestValueParameter("expectedResultValueParameter1");
        testCase.newTestValue().setTestValueParameter("expectedResultValueParameter2");
        testCase.newTestValue().setTestValueParameter("expectedResultValueParameter3");
        testCase.newTestValue().setTestValueParameter("expectedResultValueParameter4");
    }

    public void testContentProvider() throws CoreException{
        TestCaseContentProvider testCaseCntProviderIn = 
            new TestCaseContentProvider(TestCaseContentProvider.INPUT, testCase);
        TestCaseContentProvider testCaseContentProviderExp = 
        new TestCaseContentProvider(TestCaseContentProvider.EXPECTED_RESULT, testCase);

       assertEquals(2, testCaseCntProviderIn.getElements(testCase).length);
       assertEquals(5, testCaseContentProviderExp.getElements(testCase).length);
    }
    
    public void testValidateTestPolicyCmptRelation() throws CoreException {
        ITestPolicyCmpt pc = testCase.findTestPolicyCmpt(pathToTestPolicyCmptInput);
        ITestPolicyCmptRelation pcr = (ITestPolicyCmptRelation) pc.getParent();
        MessageList ml = pcr.validate();
        assertEquals(1, ml.getNoOfMessages());
        assertNotNull(ml.getMessageByCode(ITestPolicyCmpt.MSGCODE_POLICY_CMPT_TYPE_NOT_EXISTS));
        
        ITestPolicyCmptTypeParameter param = testCase.findTestPolicyCmptTypeParameter(pcr);
        param.setMinInstances(2);
        param.setMaxInstances(3);
        ml = pcr.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MIN_INSTANCES_NOT_REACHED));
        
        ITestPolicyCmpt parent = (ITestPolicyCmpt) pcr.getParent();
        parent.addTestPcTypeRelation(param, "", "");
        parent.addTestPcTypeRelation(param, "", "");
        parent.addTestPcTypeRelation(param, "", "");
        ml = pcr.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MAX_INSTANCES_REACHED));
        
        String prevRelation = param.getRelation();
        param.setRelation("none");
        ml = pcr.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_MODEL_RELATION_NOT_FOUND));
        
        param.setRelation(prevRelation);
        
        pcr.setTestPolicyCmptType("none");
        ml = pcr.validate();
        assertNotNull(ml.getMessageByCode(ITestPolicyCmptRelation.MSGCODE_TEST_CASE_TYPE_PARAM_NOT_FOUND));
    }
}
