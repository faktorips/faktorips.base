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

package org.faktorips.devtools.stdbuilder.testcase;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;

public class TestCaseBuilderTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private ITestCaseType testCaseType;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        testCaseType = (ITestCaseType)newIpsObject(project, IpsObjectType.TEST_CASE_TYPE, "testCaseType");
        testCaseType.newInputTestValueParameter().setName("testValueParam1");
        testCaseType.newInputTestValueParameter().setName("testValueParam1");
        testCaseType.getIpsSrcFile().save(true, null);
    }

    public void testBuildInvalidTestCase() throws CoreException{
        ITestCase testCase = (ITestCase)newIpsObject(project, IpsObjectType.TEST_CASE, "testCase");
        testCase.setTestCaseType(testCaseType.getQualifiedName());
        testCase.newTestValue().setTestValueParameter("testValueParam1");
        testCase.getIpsSrcFile().save(true, null);
        project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }
}
