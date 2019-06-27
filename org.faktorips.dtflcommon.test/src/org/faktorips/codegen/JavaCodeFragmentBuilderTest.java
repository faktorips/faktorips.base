/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

import org.apache.commons.lang.SystemUtils;
import org.junit.Test;

public class JavaCodeFragmentBuilderTest {
    @Test
    public void testAnnotation_AsOneString() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute"); //$NON-NLS-1$
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@XmlAttribute" + SystemUtils.LINE_SEPARATOR, code.getSourcecode()); //$NON-NLS-1$

        builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute(name=\"parent-id\")"); //$NON-NLS-1$
        code = builder.getFragment();
        assertEquals("@XmlAttribute(name=\"parent-id\")" + SystemUtils.LINE_SEPARATOR, code.getSourcecode()); //$NON-NLS-1$
        ImportDeclaration imports = new ImportDeclaration();
        imports.add("javax.xml.bind.annotation.XmlAttribute"); //$NON-NLS-1$
        assertEquals(imports, code.getImportDeclaration());
    }

    @Test
    public void testAnnotation_ClassName_Params() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute", "name=\"parent-id\""); //$NON-NLS-1$ //$NON-NLS-2$
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@XmlAttribute(name=\"parent-id\")" + SystemUtils.LINE_SEPARATOR, code.getSourcecode()); //$NON-NLS-1$
        ImportDeclaration imports = new ImportDeclaration();
        imports.add("javax.xml.bind.annotation.XmlAttribute"); //$NON-NLS-1$
        assertEquals(imports, code.getImportDeclaration());
    }

    @Test
    public void testAnnotation_AnnotationString_ParamName_ParamValue() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute", "name", "parent-id"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@XmlAttribute(name=\"parent-id\")" + SystemUtils.LINE_SEPARATOR, code.getSourcecode()); //$NON-NLS-1$
        ImportDeclaration imports = new ImportDeclaration();
        imports.add("javax.xml.bind.annotation.XmlAttribute"); //$NON-NLS-1$
        assertEquals(imports, code.getImportDeclaration());
    }

    @Test
    public void testAnnotation_Class_Params() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn(Override.class, "someParameters"); //$NON-NLS-1$
        JavaCodeFragment code = builder.getFragment();
        assertEquals("@Override(someParameters)" + SystemUtils.LINE_SEPARATOR, code.getSourcecode()); //$NON-NLS-1$
        ImportDeclaration imports = new ImportDeclaration();
        imports.add(Override.class);
        assertEquals(imports, code.getImportDeclaration());
    }

    @Test
    public void testMethodBeginIntStringStringStringArrayStringArray() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.methodBegin(Modifier.PUBLIC, String[].class, "validate", new String[0], new Class[0]); //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append("public String[] validate()"); //$NON-NLS-1$
        buf.append(SystemUtils.LINE_SEPARATOR);
        buf.append("{"); //$NON-NLS-1$
        assertEquals(buf.toString(), builder.toString().trim());
    }

    @Test
    public void testAnnotationClassValueLn() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationClassValueLn("AnAnnotation", "value", List.class.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("@AnAnnotation(value=List.class)" + SystemUtils.LINE_SEPARATOR, builder.getFragment() //$NON-NLS-1$
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
        String expected = "{" + SystemUtils.LINE_SEPARATOR + "    {" + SystemUtils.LINE_SEPARATOR + "        blabla" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + SystemUtils.LINE_SEPARATOR + "    }" + SystemUtils.LINE_SEPARATOR + "}" + SystemUtils.LINE_SEPARATOR; //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(expected, builder.getFragment().getSourcecode());
    }

    @Test
    public void testAddGenerics() {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.appendGenerics(Integer.class.getName(), "xxx1234", String.class.getName());
        assertEquals("<Integer, xxx1234, String>", builder.getFragment().getSourcecode());
        assertFalse(builder.getFragment().getImportDeclaration().isCovered("xxx1234"));
        assertTrue(builder.getFragment().getImportDeclaration().isCovered(Integer.class));
        assertTrue(builder.getFragment().getImportDeclaration().isCovered(String.class));
    }

}
