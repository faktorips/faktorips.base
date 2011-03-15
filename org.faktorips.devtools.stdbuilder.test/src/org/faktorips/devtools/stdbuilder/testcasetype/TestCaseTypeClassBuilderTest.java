/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.testcasetype;

import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class TestCaseTypeClassBuilderTest extends AbstractStdBuilderTest {

    private final static String TEST_CASE_TYPE_NAME = "TestCaseType";

    private TestCaseTypeClassBuilder builder;

    private ITestCaseType testCaseType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        testCaseType = newTestCaseType(ipsProject, TEST_CASE_TYPE_NAME);
        builder = new TestCaseTypeClassBuilder(builderSet, DefaultBuilderSet.KIND_TEST_CASE_TYPE_CLASS);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(testCaseType);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass()));
    }

    private IType getGeneratedJavaClass() {
        return getGeneratedJavaType(testCaseType, false, builder.getKindId(), TEST_CASE_TYPE_NAME);
    }

}
