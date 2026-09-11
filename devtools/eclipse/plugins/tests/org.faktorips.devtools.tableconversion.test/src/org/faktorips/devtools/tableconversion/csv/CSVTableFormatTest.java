/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.tableconversion.csv;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.tableconversion.AbstractTableTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CSVTableFormatTest extends AbstractTableTest {

    private CSVTableFormat format;
    private IIpsProject ipsProject;
    private File file;
    private IEnumType enumType;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = initializeIpsProject("test");

        format = new CSVTableFormat();
        format.setName("CSV");
        format.setDefaultExtension(".csv");
        format.addValueConverter(new StringValueConverter());

        // 4 columns: literalName, id0, id1, id2 (all String, see AbstractTableTest#stringDatatypes)
        enumType = createValidEnumTypeWithStringValues(ipsProject);

        file = new File("previewEnum" + format.getDefaultExtension());
        file.delete();
    }

    @Override
    protected void tearDownExtension() throws Exception {
        file.delete();
    }

    private List<String[]> preview(String csvContent) throws Exception {
        Files.writeString(file.toPath(), csvContent, StandardCharsets.UTF_8);
        IPath path = new Path(file.getAbsolutePath());
        return format.getImportEnumPreview(enumType, path, 10, true, "NULL");
    }

    @Test
    public void testPreview_LineWithUnterminatedQuote_OtherRowsUnaffectedAndBrokenCellMarked() throws Exception {
        String csv = """
                LITERAL_NAME,id0,id1,id2
                "L1","a","b","c"
                "L2","a","b","unterminated
                "L3","x","y","z"
                """;

        List<String[]> preview = preview(csv);

        assertThat(preview, hasSize(3));
        assertThat(preview.get(0), arrayContaining("L1", "a", "b", "c"));
        assertThat(preview.get(2), arrayContaining("L3", "x", "y", "z"));

        String[] brokenRow = preview.get(1);
        assertThat(brokenRow[0], equalTo("L2"));
        assertThat(brokenRow[1], equalTo("a"));
        assertThat(brokenRow[2], equalTo("b"));
        assertThat(brokenRow[3], equalTo(ITableFormat.PREVIEW_ERROR_CELL_MARKER + "unterminated"));
    }

    @Test
    public void testPreview_BrokenValueContainingSeparator_SplitAcrossColumns() throws Exception {
        String csv = "LITERAL_NAME,id0,id1,id2\n"
                + "\"L1\",\"a\",\"partOne,partTwo\n";

        List<String[]> preview = preview(csv);
        String[] brokenRow = preview.get(0);

        assertThat(brokenRow[0], equalTo("L1"));
        assertThat(brokenRow[1], equalTo("a"));
        assertThat(brokenRow[2], equalTo(ITableFormat.PREVIEW_ERROR_CELL_MARKER + "partOne"));
        assertThat(brokenRow[3], equalTo(ITableFormat.PREVIEW_ERROR_CELL_MARKER + "partTwo"));
    }

    @Test
    public void testPreview_MoreFieldsThanColumns_ExtraValuesAreKept() throws Exception {
        String csv = "LITERAL_NAME,id0,id1,id2\n"
                + "L1,a,b,c,extra\n";

        List<String[]> preview = preview(csv);
        String[] row = preview.get(0);

        assertThat(row.length, equalTo(5));
        assertThat(row, arrayContaining("L1", "a", "b", "c", "extra"));
    }

    @Test
    public void testPreview_ValidFile_StillReturnsExpectedRows() throws Exception {
        String csv = """
                LITERAL_NAME,id0,id1,id2
                L1,a,b,c
                L2,x,y,z
                """;

        List<String[]> preview = preview(csv);

        assertThat(preview, hasSize(2));
        assertThat(preview.get(0), arrayContaining("L1", "a", "b", "c"));
        assertThat(preview.get(1), arrayContaining("L2", "x", "y", "z"));
    }

    @Test
    public void testPreview_EscapedQuoteWithinQuotedField_NotTreatedAsFieldEnd() throws Exception {
        // a doubled quote ("") inside a quoted field represents a literal quote and must not be
        // mistaken for the closing quote of that field.
        String csv = "LITERAL_NAME,id0,id1,id2\n"
                + "\"L1\",\"a\"\"b\",\"c\n";

        List<String[]> preview = preview(csv);
        String[] row = preview.get(0);

        assertThat(row[0], equalTo("L1"));
        assertThat(row[1], equalTo("a\"b"));
        assertThat(row[2], equalTo(ITableFormat.PREVIEW_ERROR_CELL_MARKER + "c"));
    }

    @Test
    public void testPreview_EnumContentImport_LiteralNameColumnNotExpected() throws Exception {
        // when importing into an IEnumContent rather than the IEnumType itself, the literal name
        // attribute is not generated into code and therefore not a column in the file
        String csv = """
                id0,id1,id2
                a,b,c
                x,y,z
                """;
        Files.writeString(file.toPath(), csv, StandardCharsets.UTF_8);
        IPath path = new Path(file.getAbsolutePath());

        List<String[]> preview = format.getImportEnumPreview(enumType, path, 10, true, "NULL", false);

        assertThat(preview, hasSize(2));
        assertThat(preview.get(0), arrayContaining("a", "b", "c"));
        assertThat(preview.get(1), arrayContaining("x", "y", "z"));
    }

    @Test
    public void testPreview_UnterminatedQuoteBeyondDeclaredColumns_StillMarkedAsBroken() throws Exception {
        // regression test: the broken (unterminated-quote) cell used to be silently dropped
        // when it occurred in a column beyond the structure's declared column count, making
        // the row look error-free instead of broken.
        String csv = "LITERAL_NAME,id0,id1,id2\n"
                + "L1,a,b,c,\"extra\n";

        List<String[]> preview = preview(csv);
        String[] row = preview.get(0);

        assertThat(row.length, equalTo(5));
        assertThat(row[0], equalTo("L1"));
        assertThat(row[1], equalTo("a"));
        assertThat(row[2], equalTo("b"));
        assertThat(row[3], equalTo("c"));
        assertThat(row[4], equalTo(ITableFormat.PREVIEW_ERROR_CELL_MARKER + "extra"));
    }

}
