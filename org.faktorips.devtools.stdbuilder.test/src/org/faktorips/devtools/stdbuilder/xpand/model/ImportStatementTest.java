/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ImportStatementTest {

    @Test
    public void testGetQualifiedName_Class() throws Exception {
        ImportStatement importStatement = new ImportStatement(String.class);
        assertEquals("java.lang.String", importStatement.getQualifiedName());
    }

    @Test
    public void testGetQualifiedName_String() throws Exception {
        ImportStatement importStatement = new ImportStatement("java.lang.String");
        assertEquals("java.lang.String", importStatement.getQualifiedName());
    }

    @Test
    public void testGetQualifiedName_EmptyPackage() {
        ImportStatement importStatement = new ImportStatement("String");
        assertEquals("String", importStatement.getQualifiedName());
    }

    @Test
    public void testGetPackageName_Class() throws Exception {
        ImportStatement importStatement = new ImportStatement(String.class);
        assertEquals("java.lang", importStatement.getPackage());
    }

    @Test
    public void testGetPackageName_String() throws Exception {
        ImportStatement importStatement = new ImportStatement("java.lang.String");
        assertEquals("java.lang", importStatement.getPackage());
    }

    @Test
    public void testGetUnqualifiedName_Class() throws Exception {
        ImportStatement importStatement = new ImportStatement(String.class);
        assertEquals("String", importStatement.getUnqualifiedName());
    }

    @Test
    public void testGetUnqualifiedName_String() throws Exception {
        ImportStatement importStatement = new ImportStatement("java.lang.String");
        assertEquals("String", importStatement.getUnqualifiedName());
    }

}
