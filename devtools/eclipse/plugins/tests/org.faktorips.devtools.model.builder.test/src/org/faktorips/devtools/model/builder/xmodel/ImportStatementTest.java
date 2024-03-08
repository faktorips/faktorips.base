/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

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
