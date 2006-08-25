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

import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptRelation;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * Class to create and provide the content for a given test case type and a given test case.<br>
 * TestCaseType                                                          TestCase<br>
 *   testPolicyCmptType1 (TestPolicyCmptTypeParameter)              <---    testPolicyCmptType1_label (ITestPolicyCmpt)<br>
 *     testPolicyCmptTypeRelation1 (TestPolicyCmptTypeParameter)    <---      testPolicyCmptTypeRelation1 (ITestPolicyCmptRelation)<br>
 *                                                                    \-        testPolicyCmptType2_label (ITestPolicyCmpt)<br>
 *       testPolicyCmptTypeRelation2 (TestPolicyCmptTypeParameter)  <---          testPolicyCmptTypeRelation2 (ITestPolicyCmptRelation)<br>
 *                                                                    \-            testPolicyCmptType3_label (ITestPolicyCmpt)<br>
 * @author Joerg Ortmann
 */
public class TestCaseAndTestCaseTypeContent {    
    ITestPolicyCmpt testPolicyCmpt1;
    ITestPolicyCmptRelation relation1;
    ITestPolicyCmpt testPolicyCmpt2;
    ITestPolicyCmptRelation relation2;
    ITestPolicyCmpt testPolicyCmpt3;
    
    ITestPolicyCmptTypeParameter testPolicyCmptType1;
    ITestPolicyCmptTypeParameter testPolicyCmptType2;
    ITestPolicyCmptTypeParameter testPolicyCmptType3;
    
    public TestCaseAndTestCaseTypeContent(ITestCase testCase, ITestCaseType testCaseType) {
        testCase.setTestCaseType("TestCaseType1");
        
        testCaseType.newInputTestPolicyCmptTypeParameter().setName("inputTestPolicyCmptTypeParam1");
        
        (testPolicyCmpt1 = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("inputTestPolicyCmptTypeParam1");
        testPolicyCmpt1.setName("testPolicyCmptType1_label");
        testPolicyCmpt1.setTestPolicyCmptTypeParameter("testPolicyCmptType1");
        
        relation1 = testPolicyCmpt1.newTestPolicyCmptRelation();
        relation1.setTestPolicyCmptType("testPolicyCmptTypeRelation1");
        
        testPolicyCmpt2 = relation1.newTargetTestPolicyCmptChild();
        testPolicyCmpt2.setName("testPolicyCmptType2_label");
        testPolicyCmpt2.setTestPolicyCmptTypeParameter("testPolicyCmptTypeRelation1");
        
        relation2 = testPolicyCmpt2.newTestPolicyCmptRelation();
        relation2.setTestPolicyCmptType("testPolicyCmptTypeRelation2");
        
        testPolicyCmpt3 = relation2.newTargetTestPolicyCmptChild();
        testPolicyCmpt3.setName("testPolicyCmptType3_label");        
        testPolicyCmpt3.setTestPolicyCmptTypeParameter("testPolicyCmptTypeRelation2");
        
        
        testPolicyCmptType1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        testPolicyCmptType1.setName("testPolicyCmptType1");
        
        testPolicyCmptType2 = testPolicyCmptType1.newTestPolicyCmptTypeParamChild();
        testPolicyCmptType2.setName("testPolicyCmptTypeRelation1"); 
        
        testPolicyCmptType3 = testPolicyCmptType2.newTestPolicyCmptTypeParamChild();
        testPolicyCmptType3.setName("testPolicyCmptTypeRelation2");         
    }

    public ITestPolicyCmptRelation getRelation1() {
        return relation1;
    }

    public ITestPolicyCmptRelation getRelation2() {
        return relation2;
    }

    public ITestPolicyCmpt getTestPolicyCmpt1() {
        return testPolicyCmpt1;
    }

    public ITestPolicyCmpt getTestPolicyCmpt2() {
        return testPolicyCmpt2;
    }

    public ITestPolicyCmpt getTestPolicyCmpt3() {
        return testPolicyCmpt3;
    }

    public ITestPolicyCmptTypeParameter getTestPolicyCmptType1() {
        return testPolicyCmptType1;
    }

    public ITestPolicyCmptTypeParameter getTestPolicyCmptType2() {
        return testPolicyCmptType2;
    }

    public ITestPolicyCmptTypeParameter getTestPolicyCmptType3() {
        return testPolicyCmptType3;
    }
}
