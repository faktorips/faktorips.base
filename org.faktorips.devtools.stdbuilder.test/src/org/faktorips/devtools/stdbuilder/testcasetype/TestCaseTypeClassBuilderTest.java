/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.testcasetype;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.stringParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.runtime.test.IpsTestResult;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class TestCaseTypeClassBuilderTest extends AbstractStdBuilderTest {

    private static final String TEST_CASE_TYPE_NAME = "TestCaseType";

    private static final String TEST_POLICY_TYPE = "TestPolicyType";

    private TestCaseTypeClassBuilder builder;

    private ITestCaseType testCaseType;

    private IType javaClass;

    private IIpsProject projectDependsIpsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        testCaseType = newTestCaseType(ipsProject, TEST_CASE_TYPE_NAME);
        builder = new TestCaseTypeClassBuilder(builderSet);
        javaClass = getGeneratedJavaClass(testCaseType, false, TEST_CASE_TYPE_NAME);

        projectDependsIpsProject = newIpsProject();
        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(projectDependsIpsProject);
        ipsProject.setIpsObjectPath(ipsObjectPath);
        newPolicyCmptType(projectDependsIpsProject, TEST_POLICY_TYPE);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(testCaseType);

        expectType(javaClass);
        expectMethod(javaClass, javaClass.getElementName(), stringParam());
        expectMethod(javaClass, builder.getMethodNameExecuteBusinessLogic());
        expectMethod(javaClass, builder.getMethodNameExecuteAsserts(), unresolvedParam(IpsTestResult.class));
        expectMethod(javaClass, builder.getMethodNameInitInputFromXml(), unresolvedParam(Element.class));
        expectMethod(javaClass, builder.getMethodNameInitExpectedResultFromXml(), unresolvedParam(Element.class));
    }

    @Test
    public void testGetQualifiedNameFromTestPolicyCmptParam() throws Exception {
        ITestPolicyCmptTypeParameter testPolicyTypeParam = testCaseType.newCombinedPolicyCmptTypeParameter();
        testPolicyTypeParam.setPolicyCmptType(TEST_POLICY_TYPE);

        String qualifiedNameFromTestPolicyCmptParam = builder
                .getQualifiedNameFromTestPolicyCmptParam(testPolicyTypeParam);

        assertThat(qualifiedNameFromTestPolicyCmptParam, is("org.faktorips.sample.model.internal.TestPolicyType"));
    }

    @Test
    public void testGetQualifiedNameFromTestPolicyCmptParam_DifferentProjectSettings() throws Exception {
        setGeneratorProperty(ipsProject, StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES,
                Boolean.FALSE.toString());
        ITestPolicyCmptTypeParameter testPolicyTypeParam = testCaseType.newCombinedPolicyCmptTypeParameter();
        testPolicyTypeParam.setPolicyCmptType(TEST_POLICY_TYPE);

        String qualifiedNameFromTestPolicyCmptParam = builder
                .getQualifiedNameFromTestPolicyCmptParam(testPolicyTypeParam);

        assertThat(qualifiedNameFromTestPolicyCmptParam, is("org.faktorips.sample.model.internal.TestPolicyType"));
    }

    @Test
    public void testGetQualifiedNameFromTestPolicyCmptParam_ProjectDependsIpsProject_DifferentProjectSettings()
            throws Exception {
        setGeneratorProperty(projectDependsIpsProject, StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES,
                Boolean.FALSE.toString());
        ITestPolicyCmptTypeParameter testPolicyTypeParam = testCaseType.newCombinedPolicyCmptTypeParameter();
        testPolicyTypeParam.setPolicyCmptType(TEST_POLICY_TYPE);

        String qualifiedNameFromTestPolicyCmptParam = builder
                .getQualifiedNameFromTestPolicyCmptParam(testPolicyTypeParam);

        assertThat(qualifiedNameFromTestPolicyCmptParam, is("org.faktorips.sample.model.TestPolicyType"));
    }

}
