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
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;

/**
 * 
 * @author Joerg Ortmann
 */
public class TestCaseHierarchyPathTest extends AbstractIpsPluginTest {
    
    private IIpsProject ipsProject;
    
    private TestCaseAndTestCaseTypeContent testContent;
    
    /**
     * {@inheritDoc}
     */
    public void setUp() throws Exception{
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        ITestCase testCase = (ITestCase) newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "TestCase1");
        ITestCaseType testCaseType = (ITestCaseType) newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "TestCaseType1");
        testContent = new TestCaseAndTestCaseTypeContent(testCase, testCaseType);
    }
    
    public void testPathForTestCase(){
        TestCaseHierarchyPath pathRelation = new TestCaseHierarchyPath(testContent.getRelation2(), true);
        assertEquals("testPolicyCmptType1_label//testPolicyCmptTypeRelation1//testPolicyCmptType22_label//testPolicyCmptTypeRelation2", pathRelation.getHierarchyPath());
        assertTrue(pathRelation.count() == 4);
        
        TestCaseHierarchyPath path = new TestCaseHierarchyPath(testContent.getTestPolicyCmpt3());
        assertEquals("testPolicyCmptType1_label//testPolicyCmptTypeRelation1//testPolicyCmptType22_label//testPolicyCmptTypeRelation2//testPolicyCmptType3_label", path.getHierarchyPath());
        assertEquals("testPolicyCmptType1_label//testPolicyCmptTypeRelation1//testPolicyCmptType22_label//testPolicyCmptTypeRelation2", TestCaseHierarchyPath.getFolderName(path.getHierarchyPath()));
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
    
    public void testPathForTestCaseType(){
        TestCaseHierarchyPath pathRelation = new TestCaseHierarchyPath(testContent.getRelation2(), false);
        assertEquals("testPolicyCmptType1//testPolicyCmptTypeRelation1//testPolicyCmptTypeRelation2", pathRelation.getHierarchyPath());
        assertTrue(pathRelation.count() == 3);
        
        TestCaseHierarchyPath path = new TestCaseHierarchyPath(testContent.getTestPolicyCmpt3(), false);
        assertEquals("testPolicyCmptType1//testPolicyCmptTypeRelation1//testPolicyCmptTypeRelation2", path.getHierarchyPath());
        assertTrue(path.count() == 3);
        assertEquals("testPolicyCmptType1", path.next());
        assertEquals("testPolicyCmptTypeRelation1", path.next());
        assertTrue(path.count() == 1);
        assertEquals("testPolicyCmptTypeRelation2", path.next());
        assertFalse(path.hasNext());
        assertTrue(path.count() == 0);
    }
    
    public void testEvalTestPolicyCmptParamPath() throws CoreException{
        // eval path for testPolicyCmpt2 => testPolicyCmptType10.testPolicyCmptTypeRelation10
        String testPolicyCmptParamPath = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testContent.testPolicyCmpt2);
        assertEquals("testPolicyCmptType10.testPolicyCmptTypeRelation10", testPolicyCmptParamPath);
        // eval path for testPolicyCmpt22 => testPolicyCmptType10.testPolicyCmptTypeRelation11
        testPolicyCmptParamPath = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testContent.testPolicyCmpt22);
        assertEquals("testPolicyCmptType10.testPolicyCmptTypeRelation11", testPolicyCmptParamPath);  
        // eval path for testPolicyCmpt3 =>  testPolicyCmptType10.testPolicyCmptTypeRelation11.testPolicyCmptTypeRelation20
        testPolicyCmptParamPath = TestCaseHierarchyPath.evalTestPolicyCmptParamPath(testContent.testPolicyCmpt3);
        assertEquals("testPolicyCmptType10.testPolicyCmptTypeRelation11.testPolicyCmptTypeRelation20", testPolicyCmptParamPath);  
    }
}
