/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.testcasetype;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.stringParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.runtime.test.IpsTestResult;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class TestCaseTypeClassBuilderTest extends AbstractStdBuilderTest {

    private final static String TEST_CASE_TYPE_NAME = "TestCaseType";

    private TestCaseTypeClassBuilder builder;

    private ITestCaseType testCaseType;

    private IType javaClass;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        testCaseType = newTestCaseType(ipsProject, TEST_CASE_TYPE_NAME);
        builder = new TestCaseTypeClassBuilder(builderSet);
        javaClass = getGeneratedJavaClass(testCaseType, false, TEST_CASE_TYPE_NAME);
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

}
