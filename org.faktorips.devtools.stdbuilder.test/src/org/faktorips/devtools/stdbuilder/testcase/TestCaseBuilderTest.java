/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.testcase;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class TestCaseBuilderTest extends AbstractStdBuilderTest {

    private ITestCaseType testCaseType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testCaseType = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "testCaseType");
        testCaseType.newInputTestValueParameter().setName("testValueParam1");
        testCaseType.newInputTestValueParameter().setName("testValueParam1");
        testCaseType.getIpsSrcFile().save(true, null);
    }

    @Test
    public void testBuildInvalidTestCase() throws CoreException {
        ITestCase testCase = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "testCase");
        testCase.setTestCaseType(testCaseType.getQualifiedName());
        testCase.newTestValue().setTestValueParameter("testValueParam1");
        testCase.getIpsSrcFile().save(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
