/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Modifier;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.builder.JavaNamingConvention;
import org.faktorips.devtools.model.ipsproject.IJavaNamingConvention;
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

    @Test
    public void testGetValidProjectName_NotValidCharacter() {
        String projectName = javaNamingConvention.getValidJavaIdentifier("&23&& 78");
        assertEquals("_23___78", projectName);
    }

    @Test
    public void testGetValidProjectName_NotValidCharacter_NummberAsFirsCharacter() {
        String projectName = javaNamingConvention.getValidJavaIdentifier("123");
        assertEquals("_123", projectName);
    }

    @Test
    public void testGetValidProjectName() {
        String projectName = javaNamingConvention.getValidJavaIdentifier("$123");
        assertEquals("$123", projectName);
    }

    @Test
    public void testGetValidProjectName_NotValidHyphen() {
        String projectName = javaNamingConvention.getValidJavaIdentifier("$-123");
        assertEquals("$_123", projectName);
    }

    @Test
    public void testGetValidJavaIdentifier() throws Exception {
        assertEquals("ABC", javaNamingConvention.getEnumLiteral("abc"));
        assertEquals("ABC123", javaNamingConvention.getEnumLiteral("abc123"));
        assertEquals("AEUEI", javaNamingConvention.getEnumLiteral("äüi"));
        assertEquals("_1", javaNamingConvention.getEnumLiteral("1"));
        assertEquals("_123", javaNamingConvention.getEnumLiteral("123"));
        assertEquals("ASD_", javaNamingConvention.getEnumLiteral("asd+"));
        assertEquals("$____", javaNamingConvention.getEnumLiteral("$%%&&"));
        assertEquals("___", javaNamingConvention.getEnumLiteral("%%&"));
    }

}
