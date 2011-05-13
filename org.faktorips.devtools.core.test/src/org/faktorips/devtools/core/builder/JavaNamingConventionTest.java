/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Modifier;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IJavaNamingConvention;
import org.junit.Before;
import org.junit.Test;

public class JavaNamingConventionTest {

    private IJavaNamingConvention javaNamingConvention;

    @Before
    public void setUp() {
        javaNamingConvention = new JavaNamingConvention();
    }

    @Test
    public void testGetPublishedInterfaceName() {
        assertEquals("IConceptName", javaNamingConvention.getPublishedInterfaceName("ConceptName"));
    }

    @Test
    public void testGetImplementationClassName() {
        assertEquals("ConceptName", javaNamingConvention.getImplementationClassName("ConceptName"));
    }

    @Test
    public void testGetImplementationClassNameForPublishedInterfaceName() {
        assertEquals("ConceptName",
                javaNamingConvention.getImplementationClassNameForPublishedInterfaceName("IConceptName"));
    }

    @Test
    public void testGetConstantClassVarName() {
        assertEquals("CLASSVAR", javaNamingConvention.getConstantClassVarName("classVar"));
    }

    @Test
    public void testGetMemberVarName() {
        assertEquals("city", javaNamingConvention.getMemberVarName("City"));
        assertEquals("livingSpace", javaNamingConvention.getMemberVarName("LivingSpace"));
    }

    @Test
    public void testGetMultiValueMemberVarName() {
        assertEquals("cities", javaNamingConvention.getMemberVarName("Cities"));
        assertEquals("livingSpaces", javaNamingConvention.getMultiValueMemberVarName("LivingSpaces"));
    }

    @Test
    public void testGetGetterMethodNameUsingClass() {
        assertEquals("isOk", javaNamingConvention.getGetterMethodName("ok", Datatype.PRIMITIVE_BOOLEAN.getClass()));
        assertEquals("getOk", javaNamingConvention.getGetterMethodName("ok", Datatype.BOOLEAN.getClass()));
    }

    @Test
    public void testGetGetterMethodNameUsingDatatype() {
        assertEquals("isOk", javaNamingConvention.getGetterMethodName("ok", Datatype.PRIMITIVE_BOOLEAN));
        assertEquals("getOk", javaNamingConvention.getGetterMethodName("ok", Datatype.BOOLEAN));
    }

    @Test
    public void testGetSetterMethodName() {
        assertEquals("setOk", javaNamingConvention.getSetterMethodName("ok"));
    }

    @Test
    public void testGetModifierForPublicInterfaceMethod() {
        assertEquals(Modifier.PUBLIC, javaNamingConvention.getModifierForPublicInterfaceMethod());
    }

    @Test
    public void testGetTypeName() {
        assertEquals("TypeName", javaNamingConvention.getTypeName("TypeName"));
        assertEquals("TypeName", javaNamingConvention.getTypeName("typeName"));
    }

    @Test
    public void testGetEnumLiteral() {
        String literal = javaNamingConvention.getEnumLiteral("foo");
        assertEquals("FOO", literal);
    }

    @Test
    public void testGetEnumLiteralInvalidCharacters() {
        String literal = javaNamingConvention.getEnumLiteral("foo //%bar");
        assertEquals("FOO____BAR", literal);
    }

    @Test
    public void testGetEnumLiteralUmlaut() {
        String literal = javaNamingConvention.getEnumLiteral("fooÄbar");
        assertEquals("FOOAEBAR", literal);

        literal = javaNamingConvention.getEnumLiteral("fooäbar");
        assertEquals("FOOAEBAR", literal);

        literal = javaNamingConvention.getEnumLiteral("fooÖbar");
        assertEquals("FOOOEBAR", literal);

        literal = javaNamingConvention.getEnumLiteral("fooöbar");
        assertEquals("FOOOEBAR", literal);

        literal = javaNamingConvention.getEnumLiteral("fooÜbar");
        assertEquals("FOOUEBAR", literal);

        literal = javaNamingConvention.getEnumLiteral("fooübar");
        assertEquals("FOOUEBAR", literal);

        literal = javaNamingConvention.getEnumLiteral("fooßbar");
        assertEquals("FOOSSBAR", literal);
    }

    @Test
    public void testGetToDoMarker() {
        assertEquals("TODO", javaNamingConvention.getToDoMarker());
    }

}
