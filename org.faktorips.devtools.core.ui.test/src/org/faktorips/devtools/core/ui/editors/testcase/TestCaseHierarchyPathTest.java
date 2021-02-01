/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.testcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.testcase.TestCaseHierarchyPath;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestCaseHierarchyPathTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private TestCaseAndTestCaseTypeContent testContent;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        ITestCase testCase = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "TestCase1");
        ITestCaseType testCaseType = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE,
                "TestCaseType1");
        testContent = new TestCaseAndTestCaseTypeContent(testCase, testCaseType);
    }

    @Test
    public void testPathForTestCase() {
        TestCaseHierarchyPath pathRelation = new TestCaseHierarchyPath(testContent.getRelation2(), true);
        assertEquals(
                "testPolicyCmptType1_label//testPolicyCmptTypeRelation1//testPolicyCmptType22_label//testPolicyCmptTypeRelation2",
                pathRelation.getHierarchyPath());
        assertTrue(pathRelation.count() == 4);

        TestCaseHierarchyPath path = new TestCaseHierarchyPath(testContent.getTestPolicyCmpt3());
        assertEquals(
                "testPolicyCmptType1_label//testPolicyCmptTypeRelation1//testPolicyCmptType22_label//testPolicyCmptTypeRelation2//testPolicyCmptType3_label",
                path.getHierarchyPath());
        assertEquals(
                "testPolicyCmptType1_label//testPolicyCmptTypeRelation1//testPolicyCmptType22_label//testPolicyCmptTypeRelation2",
                TestCaseHierarchyPath.getFolderName(path.getHierarchyPath()));
        assertEquals("testPolicyCmptType3_label", TestCaseHierarchyPath.unqualifiedName(path.getHierarchyPath()));
        assertTrue(path.count() == 5);
        assertTrue(path.hasNext());
        assertEquals("testPolicyCmptType1_label", path.next());
        assertEquals("testPolicyCmptTypeRelation1", path.next());
        assertEquals("testPolicyCmptType22_label", path.next());
        assertEquals("testPolicyCmptTypeRelation2", path.next());
        assertTrue(path.count() == 1);
        assertEquals("testPolicyCmptType3_label", path.next());
        assertFalse(path.hasNext());
        assertTrue(path.count() == 0);
    }

    @Test
    public void testPathForTestCaseType() {
        TestCaseHierarchyPath pathRelation = new TestCaseHierarchyPath(testContent.getRelation2(), false);
        assertEquals("testPolicyCmptType1//testPolicyCmptTypeRelation1//testPolicyCmptTypeRelation2",
                pathRelation.getHierarchyPath());
        assertTrue(pathRelation.count() == 3);

        TestCaseHierarchyPath path = new TestCaseHierarchyPath(testContent.getTestPolicyCmpt3(), false);
        assertEquals("testPolicyCmptType1//testPolicyCmptTypeRelation1//testPolicyCmptTypeRelation2",
                path.getHierarchyPath());
        assertTrue(path.count() == 3);
        assertEquals("testPolicyCmptType1", path.next());
        assertEquals("testPolicyCmptTypeRelation1", path.next());
        assertTrue(path.count() == 1);
        assertEquals("testPolicyCmptTypeRelation2", path.next());
        assertFalse(path.hasNext());
        assertTrue(path.count() == 0);
    }

    @Test
    public void testEvalTestPolicyCmptParamPath() {
        // eval path for testPolicyCmpt2 => testPolicyCmptType10.testPolicyCmptTypeRelation10
        String testPolicyCmptParamPath = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testContent.testPolicyCmpt2);
        assertEquals("testPolicyCmptType1#0.testPolicyCmptTypeRelation1#0", testPolicyCmptParamPath);
        // eval path for testPolicyCmpt22 => testPolicyCmptType10.testPolicyCmptTypeRelation11
        testPolicyCmptParamPath = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testContent.testPolicyCmpt22);
        assertEquals("testPolicyCmptType1#0.testPolicyCmptTypeRelation1#1", testPolicyCmptParamPath);
        // eval path for testPolicyCmpt3 =>
        // testPolicyCmptType10.testPolicyCmptTypeRelation11.testPolicyCmptTypeRelation20
        testPolicyCmptParamPath = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testContent.testPolicyCmpt3);
        assertEquals("testPolicyCmptType1#0.testPolicyCmptTypeRelation1#1.testPolicyCmptTypeRelation2#0",
                testPolicyCmptParamPath);
    }
}
