/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.enumtype;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class EnumXmlAdapterBuilderTest extends AbstractStdBuilderTest {

    private final static String ENUM_TYPE_NAME = "TestEnumType";

    private EnumXmlAdapterBuilder builder;

    private IEnumType enumType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        StandardBuilderSet builderSet = (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet();
        EnumTypeBuilder enumTypeBuilder = new EnumTypeBuilder(builderSet);
        builder = new EnumXmlAdapterBuilder(builderSet, enumTypeBuilder);
        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
    }

    private IType getGeneratedJavaXmlAdapter() {
        return getGeneratedJavaType(enumType, true, true, ENUM_TYPE_NAME + "XmlAdapter");
    }

    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(enumType);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaXmlAdapter()));
    }

}
