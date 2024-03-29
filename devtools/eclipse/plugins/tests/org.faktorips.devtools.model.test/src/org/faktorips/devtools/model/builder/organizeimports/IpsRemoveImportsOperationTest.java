/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.organizeimports;

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
                "org/faktorips/devtools/model/builder/organizeimports/ImportTestSource.java.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        while (bufferedReader.ready()) {
            builder.append(bufferedReader.readLine() + "\n");
        }
        String source = builder.toString();

        builder = new StringBuilder();
        stream = getClass().getClassLoader().getResourceAsStream(
                "org/faktorips/devtools/model/builder/organizeimports/ImportTestExpected.java.txt");
        bufferedReader = new BufferedReader(new InputStreamReader(stream));
        while (bufferedReader.ready()) {
            builder.append(bufferedReader.readLine() + "\n");
        }
        String expected = builder.toString();

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedAndDuplicateImports(source);

        assertEquals(expected, removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-541
     */
    @Test
    public void testRemoveMinimalStringConstants() throws Exception {
        String input = """
                import asd.asdf.Table;
                import package.nblöab.UniqueConstraint;

                @Table(name = "BON_BONUSPRODUKT", uniqueConstraints = { @UniqueConstraint(columnNames = { "RUNTIME_ID", "GUELTIG_VON" }) })
                weotereasdf sadfo asdof asdölkf n""";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedAndDuplicateImports(input);

        assertEquals(input, removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-541
     */
    @Test
    public void testUglyFormattedImportStatement() throws Exception {
        String input = """
                import asd.asdf.Table;
                import package.nblöab.
                UniqueConstraint;
                import pack.bla.Test
                ; import asdad.Test2;
                           import blablabl
                           .Test;
                 import unnötig.bläblablü1; import lasdü.DasBrauchIch; import muadsa.asfa.asf;
                DasBrauchIch""";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedAndDuplicateImports(input);

        assertEquals("import lasdü.DasBrauchIch; DasBrauchIch", removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-541
     */
    @Test
    public void testWithEscapedQuotationMarks() throws Exception {
        String input = """
                import blablabla.blas.DasBrauchIch;

                "bla\\""DasBrauchIch\"\"""";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedAndDuplicateImports(input);

        assertEquals(input, removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-10633
     */
    @Test
    public void testRemoveUnusedImports_DuplicateImport() throws Exception {
        String input = """
                import foo.Bar;
                import foo.Foo;
                import foo.Bar;
                var foobar = Foo + Bar;
                """;

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedAndDuplicateImports(input);

        assertEquals("import foo.Bar;\nimport foo.Foo;\nvar foobar = Foo + Bar;\n", removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-3327
     */
    @Test
    public void testRemoveUnusedImports_PackageNameContainsImport() throws Exception {
        String input = """
                package importschnittstelle;
                import lasdü.DasBrauchIch;
                DasBrauchIch""";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedAndDuplicateImports(input);

        assertEquals("package importschnittstelle;\nimport lasdü.DasBrauchIch;\nDasBrauchIch", removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-3327
     */
    @Test
    public void testRemoveUnusedImports_PackageNameContainsImport2() throws Exception {
        String input = """
                package test.versimport .schnittstelle;
                import lasdü.DasBrauchIch;
                DasBrauchIch""";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedAndDuplicateImports(input);

        assertEquals("package test.versimport .schnittstelle;\nimport lasdü.DasBrauchIch;\nDasBrauchIch",
                removeUnusedImports);
    }

    @Test
    public void testJavaDocImportReference() throws Exception {
        String input = """
                import blablabla.blas.DasBrauchIch;
                import blablabla.blas.DasNicht;

                /**DasBrauchIch*/
                /* *DasNicht*/""";

        String expected = """
                import blablabla.blas.DasBrauchIch;

                /**DasBrauchIch*/
                /* *DasNicht*/""";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedAndDuplicateImports(input);

        assertEquals(expected, removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-1436
     */
    @Test
    public void testWith_WindowsLineEndings() throws Exception {
        String input = """
                import blablabla.blas.DasBrauchIch;\r
                import blablabla.blas.DasBrauchIchNicht;\r
                import blablabla.blas.DasBrauchIch2;\r
                \r
                DasBrauchIch und DasBrauchIch2""";

        String expected = """
                import blablabla.blas.DasBrauchIch;\r
                import blablabla.blas.DasBrauchIch2;\r
                \r
                DasBrauchIch und DasBrauchIch2""";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedAndDuplicateImports(input);

        assertEquals(expected, removeUnusedImports);
    }

    /**
     * Testing the problem occurred with FIPS-1436
     */
    @Test
    public void testWith_MacLineEndings() throws Exception {
        String input = """
                import blablabla.blas.DasBrauchIch;\r\
                import blablabla.blas.DasBrauchIchNicht;\r\
                import blablabla.blas.DasBrauchIch2;\r\
                \r\
                DasBrauchIch und DasBrauchIch2""";

        String expected = """
                import blablabla.blas.DasBrauchIch;\r\
                import blablabla.blas.DasBrauchIch2;\r\
                \r\
                DasBrauchIch und DasBrauchIch2""";

        IpsRemoveImportsOperation ipsRemoveImportsOperation = new IpsRemoveImportsOperation();
        String removeUnusedImports = ipsRemoveImportsOperation.removeUnusedAndDuplicateImports(input);

        assertEquals(expected, removeUnusedImports);
    }

}
