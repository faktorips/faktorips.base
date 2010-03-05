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

public class EnumTypeBuilderTest extends AbstractStdBuilderTest {

    private final static String ENUM_TYPE_NAME = "TestEnumType";

    private EnumTypeBuilder builder;

    private IEnumType enumType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        StandardBuilderSet builderSet = (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet();
        builder = new EnumTypeBuilder(builderSet);
        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
    }

    private IType getGeneratedJavaEnum() {
        return getGeneratedJavaType(enumType, false, false, ENUM_TYPE_NAME);
    }

    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(enumType);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaEnum()));
    }

}
