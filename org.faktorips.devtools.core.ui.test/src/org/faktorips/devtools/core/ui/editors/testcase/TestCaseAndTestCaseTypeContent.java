/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.testcase;

import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

/**
 * Class to create and provide the content for a given test case type and a given test case.<br>
 * TestCaseType TestCase<br>
 * testPolicyCmptType1 (TestPolicyCmptTypeParameter) <--- testPolicyCmptType1_label
 * (ITestPolicyCmpt)<br>
 * testPolicyCmptTypeRelation1 (TestPolicyCmptTypeParameter) <--- testPolicyCmptTypeRelation1
 * (ITestPolicyCmptRelation)<br>
 * \- testPolicyCmptType2_label (ITestPolicyCmpt)<br>
 * testPolicyCmptTypeRelation1 (ITestPolicyCmptRelation)<br>
 * \- testPolicyCmptType22_label (ITestPolicyCmpt)<br>
 * testPolicyCmptTypeRelation2 (TestPolicyCmptTypeParameter) <--- testPolicyCmptTypeRelation2
 * (ITestPolicyCmptRelation)<br>
 * \- testPolicyCmptType3_label (ITestPolicyCmpt)<br>
 * 
 * @author Joerg Ortmann
 */
public class TestCaseAndTestCaseTypeContent {

    ITestPolicyCmpt testPolicyCmpt1;
    ITestPolicyCmptLink link1;
    ITestPolicyCmpt testPolicyCmpt2;
    ITestPolicyCmpt testPolicyCmpt22;
    ITestPolicyCmptLink link2;
    ITestPolicyCmpt testPolicyCmpt3;

    ITestPolicyCmptTypeParameter testPolicyCmptType1;
    ITestPolicyCmptTypeParameter testPolicyCmptType2;
    ITestPolicyCmptTypeParameter testPolicyCmptType3;

    public TestCaseAndTestCaseTypeContent(ITestCase testCase, ITestCaseType testCaseType) {
        testCase.setTestCaseType("TestCaseType1");

        testCaseType.newInputTestPolicyCmptTypeParameter().setName("testPolicyCmptType1");

        (testPolicyCmpt1 = testCase.newTestPolicyCmpt()).setTestPolicyCmptTypeParameter("testPolicyCmptType1");
        testPolicyCmpt1.setName("testPolicyCmptType1_label");
        testPolicyCmpt1.setTestPolicyCmptTypeParameter("testPolicyCmptType1");

        link1 = testPolicyCmpt1.newTestPolicyCmptLink();
        link1.setTestPolicyCmptTypeParameter("testPolicyCmptTypeRelation1");

        testPolicyCmpt2 = link1.newTargetTestPolicyCmptChild();
        testPolicyCmpt2.setName("testPolicyCmptType2_label");
        testPolicyCmpt2.setTestPolicyCmptTypeParameter("testPolicyCmptTypeRelation1");

        link1 = testPolicyCmpt1.newTestPolicyCmptLink();
        link1.setTestPolicyCmptTypeParameter("testPolicyCmptTypeRelation1");

        testPolicyCmpt22 = link1.newTargetTestPolicyCmptChild();
        testPolicyCmpt22.setName("testPolicyCmptType22_label");
        testPolicyCmpt22.setTestPolicyCmptTypeParameter("testPolicyCmptTypeRelation1");

        link2 = testPolicyCmpt22.newTestPolicyCmptLink();
        link2.setTestPolicyCmptTypeParameter("testPolicyCmptTypeRelation2");

        testPolicyCmpt3 = link2.newTargetTestPolicyCmptChild();
        testPolicyCmpt3.setName("testPolicyCmptType3_label");
        testPolicyCmpt3.setTestPolicyCmptTypeParameter("testPolicyCmptTypeRelation2");

        testPolicyCmptType1 = testCaseType.newInputTestPolicyCmptTypeParameter();
        testPolicyCmptType1.setName("testPolicyCmptType2");

        testPolicyCmptType2 = testPolicyCmptType1.newTestPolicyCmptTypeParamChild();
        testPolicyCmptType2.setName("testPolicyCmptTypeRelation1");

        testPolicyCmptType3 = testPolicyCmptType2.newTestPolicyCmptTypeParamChild();
        testPolicyCmptType3.setName("testPolicyCmptTypeRelation2");
    }

    public ITestPolicyCmptLink getRelation1() {
        return link1;
    }

    public ITestPolicyCmptLink getRelation2() {
        return link2;
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
