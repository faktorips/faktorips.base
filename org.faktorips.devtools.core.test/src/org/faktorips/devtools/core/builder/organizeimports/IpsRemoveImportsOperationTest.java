/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder.organizeimports;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

public class IpsRemoveImportsOperationTest {

    @Test
    public void testRemoveUnusedImports() throws Exception {
        // reading test resource
        StringBuilder builder = new StringBuilder();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(
                "org/faktorips/devtools/core/builder/organizeimports/ImportTestSource.java.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        while (bufferedReader.ready()) {
            builder.append(bufferedReader.readLine() + "\n");
        }
        String source = builder.toString();

        builder = new StringBuilder();
        stream = getClass().getClassLoader().getResourceAsStream(
                "org/faktorips/devtools/core/builder/organizeimports/ImportTestExpected.java.txt");
        bufferedReader = new BufferedReader(new InputStreamReader(stream));
        while (bufferedReader.ready()) {
            builder.append(bufferedReader.readLine() + "\n");
        }
        String expected = builder.toString();

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedImports(source);

        assertEquals(expected, removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-541
     */
    @Test
    public void testRemoveMinimalStringConstants() throws Exception {
        String input = "import asd.asdf.Table;\n" //
                + "import package.nblöab.UniqueConstraint;\n"
                + "\n" //
                + "@Table(name = \"BON_BONUSPRODUKT\", uniqueConstraints = { @UniqueConstraint(columnNames = { \"RUNTIME_ID\", \"GUELTIG_VON\" }) })\n" //
                + "weotereasdf sadfo asdof asdölkf n";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedImports(input);

        assertEquals(input, removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-541
     */
    @Test
    public void testUglyFormattedImportStatement() throws Exception {
        String input = "import asd.asdf.Table;\n" //
                + "import package.nblöab.\n" + //
                "UniqueConstraint;\n" + //
                "import pack.bla.Test\n" + //
                "; import asdad.Test2;\n" + //
                "           import blablabl\n" + //
                "           .Test;\n" + //
                " import unnötig.bläblablü1; import lasdü.DasBrauchIch; import muadsa.asfa.asf;\n" + //
                "DasBrauchIch";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedImports(input);

        assertEquals("import lasdü.DasBrauchIch; DasBrauchIch", removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-541
     */
    @Test
    public void testWithEscapedQuotationMarks() throws Exception {
        String input = "import blablabla.blas.DasBrauchIch;\n" + //
                "\n" + //
                "\"bla\\\"\"DasBrauchIch\"\"";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedImports(input);

        assertEquals(input, removeUnusedImports);
    }

    @Test
    public void testJavaDocImportReference() throws Exception {
        String input = "import blablabla.blas.DasBrauchIch;\n" + //
                "import blablabla.blas.DasNicht;\n" + //
                "\n" + //
                "/**DasBrauchIch*/\n" + //
                "/* *DasNicht*/";

        String expected = "import blablabla.blas.DasBrauchIch;\n" + //
                "\n" + //
                "/**DasBrauchIch*/\n" + //
                "/* *DasNicht*/";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedImports(input);

        assertEquals(expected, removeUnusedImports);
    }

}
