/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.testcasetype;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;

public class TestCaseTypeClassBuilderTest extends AbstractStdBuilderTest {

    private final static String TEST_CASE_TYPE_NAME = "TestCaseType";

    private TestCaseTypeClassBuilder builder;

    private ITestCaseType testCaseType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        testCaseType = newTestCaseType(ipsProject, TEST_CASE_TYPE_NAME);
        builder = new TestCaseTypeClassBuilder(builderSet, DefaultBuilderSet.KIND_TEST_CASE_TYPE_CLASS);
    }

    private IType getGeneratedJavaClass() {
        return getGeneratedJavaType(testCaseType, false, true, TEST_CASE_TYPE_NAME);
    }

    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(testCaseType);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass()));
    }

}
