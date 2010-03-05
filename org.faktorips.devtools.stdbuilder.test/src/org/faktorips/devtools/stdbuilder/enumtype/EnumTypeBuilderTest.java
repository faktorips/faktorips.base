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

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class EnumTypeBuilderTest extends AbstractStdBuilderTest {

    private final static String ENUM_TYPE_NAME = "TestEnumType";

    private StandardBuilderSet builderSet;

    private IEnumType enumType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        builderSet = (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet();
        enumType = newEnumType(ipsProject, ENUM_TYPE_NAME);
    }

    @SuppressWarnings("unused")
    // In work
    private IType getGeneratedJavaType() {
        return getGeneratedJavaType(ENUM_TYPE_NAME);
    }

    public void testGetGeneratedJavaElements() {
        List<IJavaElement> javaElements = builderSet.getGeneratedJavaElements(enumType);
        assertEquals(2, javaElements.size());
        assertEquals(IJavaElement.TYPE, javaElements.get(0).getElementType());
        assertEquals(IJavaElement.TYPE, javaElements.get(1).getElementType());
    }

}
