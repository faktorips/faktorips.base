/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.Test;

public class JavaCodeFragmentBuilderTest {
    @Test
    public void testAnnotation_AsOneString() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute"); //$NON-NLS-1$
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@XmlAttribute" + System.lineSeparator(), code.getSourcecode()); //$NON-NLS-1$

        builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute(name=\"parent-id\")"); //$NON-NLS-1$
        code = builder.getFragment();
        assertEquals("@XmlAttribute(name=\"parent-id\")" + System.lineSeparator(), code.getSourcecode()); //$NON-NLS-1$
        ImportDeclaration imports = new ImportDeclaration();
        imports.add("javax.xml.bind.annotation.XmlAttribute"); //$NON-NLS-1$
        assertEquals(imports, code.getImportDeclaration());
    }

    @Test
    public void testAnnotation_ClassName_Params() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute", "name=\"parent-id\""); //$NON-NLS-1$ //$NON-NLS-2$
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@XmlAttribute(name=\"parent-id\")" + System.lineSeparator(), code.getSourcecode()); //$NON-NLS-1$
        ImportDeclaration imports = new ImportDeclaration();
        imports.add("javax.xml.bind.annotation.XmlAttribute"); //$NON-NLS-1$
        assertEquals(imports, code.getImportDeclaration());
    }

    @Test
    public void testAnnotation_AnnotationString_ParamName_ParamValue() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute", "name", "parent-id"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@XmlAttribute(name=\"parent-id\")" + System.lineSeparator(), code.getSourcecode()); //$NON-NLS-1$
        ImportDeclaration imports = new ImportDeclaration();
        imports.add("javax.xml.bind.annotation.XmlAttribute"); //$NON-NLS-1$
        assertEquals(imports, code.getImportDeclaration());
    }

    @Test
    public void testAnnotation_Class_Params() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn(Override.class, "someParameters"); //$NON-NLS-1$
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@Override(someParameters)" + System.lineSeparator(), code.getSourcecode()); //$NON-NLS-1$
        ImportDeclaration imports = new ImportDeclaration();
        imports.add(Override.class);
        assertEquals(imports, code.getImportDeclaration());
    }

    @Test
    public void testMethodBeginIntStringStringStringArrayStringArray() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.methodBegin(Modifier.PUBLIC, String[].class, "validate", new String[0], new Class[0]); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        sb.append("public String[] validate()"); //$NON-NLS-1$
        sb.append(System.lineSeparator());
        sb.append("{"); //$NON-NLS-1$
        assertEquals(sb.toString(), builder.toString().trim());
    }

    @Test
    public void testAnnotationClassValueLn() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationClassValueLn("AnAnnotation", "value", List.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("@AnAnnotation(value=List.class)" + System.lineSeparator(), builder.getFragment() //$NON-NLS-1$
                .getSourcecode());
        assertTrue(builder.getFragment().getImportDeclaration().isCovered(List.class));
    }

    @Test
    public void testOpenCloseBracket() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder(true);
        assertEquals(0, builder.getFragment().getIndentationLevel());
        builder.openBracket();
        assertEquals(1, builder.getFragment().getIndentationLevel());
        builder.openBracket();
        assertEquals(2, builder.getFragment().getIndentationLevel());
        builder.append("blabla"); //$NON-NLS-1$
        builder.closeBracket();
        assertEquals(1, builder.getFragment().getIndentationLevel());
        builder.closeBracket();
        assertEquals(0, builder.getFragment().getIndentationLevel());
        String expected = "{" + System.lineSeparator() + "    {" + System.lineSeparator() + "        blabla" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + System.lineSeparator() + "    }" + System.lineSeparator() + "}" + System.lineSeparator(); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(expected, builder.getFragment().getSourcecode());
    }

    @Test
    public void testAddGenerics() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.appendGenerics(Integer.class.getName(), "xxx1234", String.class.getName()); //$NON-NLS-1$
        assertEquals("<Integer, xxx1234, String>", builder.getFragment().getSourcecode()); //$NON-NLS-1$
        assertFalse(builder.getFragment().getImportDeclaration().isCovered("xxx1234")); //$NON-NLS-1$
        assertTrue(builder.getFragment().getImportDeclaration().isCovered(Integer.class));
        assertTrue(builder.getFragment().getImportDeclaration().isCovered(String.class));
    }

}
