/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.testcase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;
import org.junit.Test;

public class TestCaseBuilderTest extends AbstractStdBuilderTest {

    private static final String TEST_POLICY_TYPE = "TestPolicyType";

    private ITestCaseType testCaseType;

    private IIpsProject otherProject;

    private TestCaseBuilder builder;

    private IPolicyCmptType newPolicyCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testCaseType = (ITestCaseType)newIpsObject(ipsProject, IpsObjectType.TEST_CASE_TYPE, "testCaseType");
        testCaseType.newInputTestValueParameter().setName("testValueParam1");
        testCaseType.newInputTestValueParameter().setName("testValueParam1");
        testCaseType.getIpsSrcFile().save(true, null);

        builder = new TestCaseBuilder(builderSet);

        otherProject = newIpsProject();
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(otherProject);
        ipsProject.setIpsObjectPath(ipsObjectPath);
        newPolicyCmptType = newPolicyCmptType(otherProject, TEST_POLICY_TYPE);

    }

    @Test
    public void testBuildInvalidTestCase() throws CoreRuntimeException {
        ITestCase testCase = (ITestCase)newIpsObject(ipsProject, IpsObjectType.TEST_CASE, "testCase");
        testCase.setTestCaseType(testCaseType.getQualifiedName());
        testCase.newTestValue().setTestValueParameter("testValueParam1");
        testCase.getIpsSrcFile().save(true, null);
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

    @Test
    public void testGetQualifiedNameFromTestPolicyCmptParam() throws Exception {
        String qualifiedName = builder.getQualifiedClassName(newPolicyCmptType);

        assertThat(qualifiedName, is("org.faktorips.sample.model.internal.TestPolicyType"));
    }

    @Test
    public void testGetQualifiedNameFromTestPolicyCmptParam_DifferentProjectSettings() throws Exception {
        setGeneratorProperty(ipsProject, StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES,
                Boolean.FALSE.toString());
        String qualifiedName = builder.getQualifiedClassName(newPolicyCmptType);

        assertThat(qualifiedName, is("org.faktorips.sample.model.internal.TestPolicyType"));
    }

    @Test
    public void testGetQualifiedNameFromTestPolicyCmptParam_OtherDifferentProjectSettings() throws Exception {
        setGeneratorProperty(otherProject, StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES,
                Boolean.FALSE.toString());
        String qualifiedName = builder.getQualifiedClassName(newPolicyCmptType);

        assertThat(qualifiedName, is("org.faktorips.sample.model.TestPolicyType"));
    }

}
