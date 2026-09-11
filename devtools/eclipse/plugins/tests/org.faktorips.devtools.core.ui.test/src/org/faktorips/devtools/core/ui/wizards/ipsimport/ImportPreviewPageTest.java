/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsimport;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.MessageList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImportPreviewPageTest extends AbstractIpsPluginTest {

    private static final String FILENAME = "test.csv"; //$NON-NLS-1$

    private IIpsProject ipsProject;
    private Shell shell;
    private TestPreviewTableFormat tableFormat;
    private ImportPreviewPage page;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        shell = new Shell(Display.getCurrent());
        tableFormat = new TestPreviewTableFormat();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        if (page != null) {
            page.dispose();
        }
        shell.dispose();
        super.tearDown();
    }

    private TableStructure newTwoColumnTableStructure() {
        TableStructure tableStructure = newTableStructure(ipsProject, "TestTable");
        for (String columnName : new String[] { "col1", "col2" }) {
            IColumn column = tableStructure.newColumn();
            column.setName(columnName);
            column.setDatatype(Datatype.STRING.getQualifiedName());
        }
        return tableStructure;
    }

    /** Creates, wires up and shows the page so that {@code fillPreview()} runs exactly once. */
    private Table showPage(IIpsObject structure) {
        return showPage(structure, true);
    }

    /** Creates, wires up and shows the page so that {@code fillPreview()} runs exactly once. */
    private Table showPage(IIpsObject structure, boolean includeLiteralName) {
        page = new ImportPreviewPage(FILENAME, tableFormat, structure, false, includeLiteralName);
        page.setWizard(new TestImportWizard());
        page.createControl(shell);
        page.validatePage();
        page.setVisible(true);

        Composite pageControl = (Composite)page.getControl();
        Composite previewGroup = (Composite)pageControl.getChildren()[1];
        return (Table)previewGroup.getChildren()[0];
    }

    @Test
    void testFillPreview_ValidRows_PageCompleteAndNoMessages() {
        tableFormat.setTablePreviewRows(List.of(new String[] { "a", "b" }, new String[] { "c", "d" }));

        Table table = showPage(newTwoColumnTableStructure());

        assertThat(table.getItemCount(), is(2));
        assertThat(table.getItem(0).getText(0), is("a"));
        assertThat(table.getItem(0).getText(1), is("b"));
        assertThat(table.getItem(1).getText(0), is("c"));
        assertThat(table.getItem(1).getText(1), is("d"));
        assertThat(page.getErrorMessage(), is(nullValue()));
        assertThat(page.isPageComplete(), is(true));
    }

    @Test
    void testFillPreview_RowWithErrorMarker_CellFlaggedAsBrokenButPageRemainsComplete() {
        tableFormat.setTablePreviewRows(
                Collections.singletonList(new String[] { ITableFormat.PREVIEW_ERROR_CELL_MARKER + "bad", "b" }));

        Table table = showPage(newTwoColumnTableStructure());

        assertThat(table.getItem(0).getText(0), is("bad"));
        assertThat(table.getItem(0).getBackground(0).getRGB(), is(ImportPreviewPage.ERROR_CELL_RGB));
        assertThat(page.getErrorMessage(), is(nullValue()));
        assertThat(page.getMessageType(), is(IMessageProvider.WARNING));
        assertThat(page.isPageComplete(), is(true));
    }

    @Test
    void testFillPreview_RowWithFewerColumnsThanStructure_TrailingCellsFlaggedAsBrokenButPageRemainsComplete() {
        // missing trailing cells can be corrected afterwards in the editor, so it is only a warning
        tableFormat.setTablePreviewRows(Collections.singletonList(new String[] { "a" }));

        Table table = showPage(newTwoColumnTableStructure());

        assertThat(table.getItem(0).getBackground(1).getRGB(), is(ImportPreviewPage.ERROR_CELL_RGB));
        assertThat(page.getErrorMessage(), is(nullValue()));
        assertThat(page.getMessageType(), is(IMessageProvider.WARNING));
        assertThat(page.isPageComplete(), is(true));
    }

    @Test
    void testFillPreview_RowWithMoreColumnsThanStructure_AllCellsFlaggedAsBrokenAndPageIncomplete() {
        // the file structure itself is ambiguous in this case, so import is blocked
        tableFormat.setTablePreviewRows(Collections.singletonList(new String[] { "a", "b", "c" }));

        Table table = showPage(newTwoColumnTableStructure());

        assertThat(table.getItem(0).getBackground(0).getRGB(), is(ImportPreviewPage.ERROR_CELL_RGB));
        assertThat(table.getItem(0).getBackground(1).getRGB(), is(ImportPreviewPage.ERROR_CELL_RGB));
        assertThat(page.getErrorMessage(), is(notNullValue()));
        assertThat(page.isPageComplete(), is(false));
    }

    @Test
    void testFillPreview_EmptyUniqueIdentifierColumn_WarningShownButPageRemainsComplete() {
        // newDefaultEnumType() creates [literalName, id (unique), name (unique)]; leave "id" empty.
        EnumType enumType = newDefaultEnumType(ipsProject, "TestEnum");
        tableFormat.setEnumPreviewRows(Collections.singletonList(new String[] { "LITERAL", "", "aName" }));

        Table table = showPage(enumType);

        assertThat(table.getItem(0).getBackground(1).getRGB(), is(ImportPreviewPage.ERROR_CELL_RGB));
        assertThat(page.getErrorMessage(), is(nullValue()));
        assertThat(page.getMessageType(), is(IMessageProvider.WARNING));
        assertThat(page.isPageComplete(), is(true));
    }

    @Test
    void testFillPreview_EnumContentImport_LiteralNameColumnNotExpectedAndPageComplete() {
        // newDefaultEnumType() creates [literalName, id (unique), name (unique)]; when importing
        // into an IEnumContent (includeLiteralName=false) the literal name is not a column, so a
        // row without it must not be flagged as having too few columns.
        EnumType enumType = newDefaultEnumType(ipsProject, "TestEnum");
        tableFormat.setEnumPreviewRows(Collections.singletonList(new String[] { "anId", "aName" }));

        Table table = showPage(enumType, false);

        assertThat(table.getItem(0).getText(0), is("anId"));
        assertThat(table.getItem(0).getText(1), is("aName"));
        assertThat(page.getErrorMessage(), is(nullValue()));
        assertThat(page.isPageComplete(), is(true));
    }

    @Test
    void testFillPreview_NoRows_ShowsInvalidFileWarning() {
        tableFormat.setTablePreviewRows(List.of());

        Table table = showPage(newTwoColumnTableStructure());

        assertThat(table.getItemCount(), is(0));
        assertThat(page.getMessageType(), is(IMessageProvider.WARNING));
    }

    /** Minimal {@link ITableFormat} double returning preconfigured preview rows. */
    private static final class TestPreviewTableFormat extends AbstractExternalTableFormat {

        private List<String[]> tablePreviewRows = List.of();
        private List<String[]> enumPreviewRows = List.of();

        void setTablePreviewRows(List<String[]> rows) {
            tablePreviewRows = rows;
        }

        void setEnumPreviewRows(List<String[]> rows) {
            enumPreviewRows = rows;
        }

        @Override
        public List<String[]> getImportTablePreview(ITableStructure structure,
                IPath filename,
                int maxNumberOfRows,
                boolean ignoreColumnHeaderRow,
                String nullRepresentation) {
            return tablePreviewRows;
        }

        @Override
        public List<String[]> getImportEnumPreview(IEnumType structure,
                IPath filename,
                int maxNumberOfRows,
                boolean ignoreColumnHeaderRow,
                String nullRepresentation) {
            return enumPreviewRows;
        }

        @Override
        public boolean isValidImportSource(String source) {
            return true;
        }

        @Override
        public boolean executeTableExport(ITableContents contents,
                IPath filename,
                String nullRepresentationString,
                boolean exportColumnHeaderRow,
                MessageList list,
                IProgressMonitor monitor) {
            return false;
        }

        @Override
        public void executeTableImport(ITableStructure structure,
                IPath filename,
                ITableRows targetGeneration,
                String nullRepresentationString,
                boolean ignoreColumnHeaderRow,
                MessageList list,
                boolean importIntoExisting) {
            // not used by this test
        }

        @Override
        public void executeEnumImport(IEnumValueContainer valueContainer,
                IPath filename,
                String nullRepresentationString,
                boolean ignoreColumnHeaderRow,
                MessageList list,
                boolean importIntoExisting) {
            // not used by this test
        }

        @Override
        public boolean executeEnumExport(IEnumValueContainer valueContainer,
                IPath filename,
                String nullRepresentationString,
                boolean exportColumnHeaderRow,
                MessageList list) {
            return false;
        }
    }

    /**
     * Minimal {@link IpsObjectImportWizard} double so {@code fillPreview()} can call
     * {@code getWizard()}.
     */
    private static final class TestImportWizard extends IpsObjectImportWizard {

        @Override
        protected String getDialogSettingsKey() {
            return "ImportPreviewPageTest"; //$NON-NLS-1$
        }

        @Override
        protected String getNullRepresentation() {
            return ""; //$NON-NLS-1$
        }

        @Override
        public boolean performFinish() {
            return false;
        }
    }
}
