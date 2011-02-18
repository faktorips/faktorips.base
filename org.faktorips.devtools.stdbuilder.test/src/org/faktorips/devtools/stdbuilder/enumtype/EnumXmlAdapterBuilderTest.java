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

package org.faktorips.devtools.stdbuilder.enumtype;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class EnumXmlAdapterBuilderTest extends AbstractStdBuilderTest {

    private final static String ENUM_TYPE_NAME = "TestEnumType";

    private EnumXmlAdapterBuilder builder;

    private IEnumType enumType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        EnumTypeBuilder enumTypeBuilder = new EnumTypeBuilder(builderSet);
        builder = new EnumXmlAdapterBuilder(builderSet, enumTypeBuilder);
        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
    }

    private IType getGeneratedJavaXmlAdapter() {
        return getGeneratedJavaType(enumType, true, true, ENUM_TYPE_NAME + "XmlAdapter");
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(enumType);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaXmlAdapter()));
    }

}
