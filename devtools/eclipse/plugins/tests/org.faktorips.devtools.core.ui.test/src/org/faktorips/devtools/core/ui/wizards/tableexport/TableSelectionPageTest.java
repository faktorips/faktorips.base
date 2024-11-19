/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tableexport;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;

public class TableSelectionPageTest extends AbstractIpsPluginTest {

    private IStructuredSelection selection;
    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot ipsRootFolder;
    private IIpsPackageFragment ipsFolder1;
    private IIpsPackageFragment ipsFolder2;
    private ITableContents tableContents1;
    private ITableContents tableContents2;
    private ITableStructure tableStructure1;
    private ITableStructure tableStructure2;

    private Path tempDir;

    Composite parentComposite;

    private TableSelectionPage tableSelectionPage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = this.newIpsProject("TestProject");
        ipsRootFolder = ipsProject.getIpsPackageFragmentRoots()[0];

        ipsFolder1 = ipsRootFolder.createPackageFragment("folder1", true, null);
        ipsFolder2 = ipsRootFolder.createPackageFragment("folder2", true, null);

        tableContents1 = setupTableContent(tableStructure1, "TestTableContents1", "testStructre1", ipsFolder1);
        tableContents2 = setupTableContent(tableStructure2, "TestTableContents2", "testStructre2", ipsFolder2);

    }

    private ITableContents setupTableContent(ITableStructure tableStructure,
            String contentName,
            String structureName,
            IIpsPackageFragment ipsPackage) throws Exception {
        tableStructure = (ITableStructure)newIpsObject(ipsPackage, IpsObjectType.TABLE_STRUCTURE,
                structureName);
        IColumn column0 = tableStructure.newColumn();
        column0.setDatatype("Integer");
        IColumn column1 = tableStructure.newColumn();
        column1.setDatatype("Integer");
        IColumn column2 = tableStructure.newColumn();
        column2.setDatatype("Integer");

        ITableContents tableContents = (ITableContents)newIpsObject(ipsPackage, IpsObjectType.TABLE_CONTENTS,
                contentName);
        tableContents.setTableStructure(tableStructure.getQualifiedName());

        tableContents.newColumn("1", "");
        tableContents.newColumn("2", "");
        tableContents.newColumn("3", "");

        tempDir = Files.createTempDirectory("testDir");

        return tableContents;

    }

    @Test
    public void testInitializeTableContents_selectedFiles() throws Exception {

        selection = new StructuredSelection(List.of(tableContents1, tableContents2));

        tableSelectionPage = new TableSelectionPage(selection);

        List<ITableContents> contents = tableSelectionPage.getTableContents().values().stream().toList();
        assertThat(contents.size(), is(2));
        assertThat(contents.get(0).getQualifiedName(), is(tableContents2.getQualifiedName()));
        assertThat(contents.get(1).getQualifiedName(), is(tableContents1.getQualifiedName()));
    }

    @Test
    public void testInitializeTableContents_selectedFolder() throws Exception {

        selection = new StructuredSelection(List.of(ipsFolder1));

        tableSelectionPage = new TableSelectionPage(selection);

        List<ITableContents> contents = tableSelectionPage.getTableContents().values().stream().toList();
        assertThat(contents.size(), is(1));
        assertThat(contents.get(0).getQualifiedName(), is(tableContents1.getQualifiedName()));

    }

    @Test
    public void testInitializeTableContents_selectedProject() throws Exception {

        selection = new StructuredSelection(List.of(ipsProject));

        tableSelectionPage = new TableSelectionPage(selection);

        List<ITableContents> contents = tableSelectionPage.getTableContents().values().stream().toList();
        assertThat(contents.size(), is(2));
        assertThat(contents.get(0).getQualifiedName(), is(tableContents2.getQualifiedName()));
        assertThat(contents.get(1).getQualifiedName(), is(tableContents1.getQualifiedName()));
    }

    @Test
    public void testValidateNoDuplicateSrcFileNames_withDuplicates() throws Exception {

        ITableContents duplicateTable = setupTableContent(tableStructure1, "TestTableContents1", "testStructre1",
                ipsFolder2);

        selection = new StructuredSelection(List.of(tableContents1, duplicateTable));

        tableSelectionPage = new TableSelectionPage(selection);
        CheckboxTableViewer tableviewer = mock(CheckboxTableViewer.class);
        when(tableviewer.getCheckedElements()).thenReturn(selection.toArray());

        TextButtonField folderPathField = mock(TextButtonField.class);
        when(folderPathField.getText()).thenReturn(tempDir.toString());

        Combo fileFormat = mock(Combo.class);
        when(fileFormat.getSelectionIndex()).thenReturn(2);

        tableSelectionPage.setFolderPathField(folderPathField);
        tableSelectionPage.setTableViewer(tableviewer);
        tableSelectionPage.setFileFormatControl(fileFormat);

        tableSelectionPage.validatePage();

        assertTrue(tableSelectionPage.getErrorMessage().contains(Messages.TableSelectionPage_msgDuplicateFileNames));
    }

    @Test
    public void testValidatePage_noFolderPath() throws Exception {
        selection = new StructuredSelection(List.of(tableContents1, tableContents2));
        tableSelectionPage = new TableSelectionPage(selection);

        // Mock the table viewer and file format
        CheckboxTableViewer tableviewer = mock(CheckboxTableViewer.class);
        when(tableviewer.getCheckedElements()).thenReturn(selection.toArray());
        Combo fileFormat = mock(Combo.class);
        when(fileFormat.getSelectionIndex()).thenReturn(1);

        // Folder path field has no text to simulate missing folder path
        TextButtonField folderPathField = mock(TextButtonField.class);
        when(folderPathField.getText()).thenReturn("");

        tableSelectionPage.setFolderPathField(folderPathField);
        tableSelectionPage.setTableViewer(tableviewer);
        tableSelectionPage.setFileFormatControl(fileFormat);

        tableSelectionPage.validatePage();

        assertTrue(tableSelectionPage.getErrorMessage().contains(Messages.TableSelectionPage_msgFolderPathEmpty));
    }

    @Test
    public void testValidatePage_FolderPathNotExisting() throws Exception {
        selection = new StructuredSelection(List.of(tableContents1, tableContents2));
        tableSelectionPage = new TableSelectionPage(selection);

        // Mock the table viewer and file format
        CheckboxTableViewer tableviewer = mock(CheckboxTableViewer.class);
        when(tableviewer.getCheckedElements()).thenReturn(selection.toArray());
        Combo fileFormat = mock(Combo.class);
        when(fileFormat.getSelectionIndex()).thenReturn(1);

        // Folder path field has no text to simulate missing folder path
        TextButtonField folderPathField = mock(TextButtonField.class);
        when(folderPathField.getText()).thenReturn("invalid");

        tableSelectionPage.setFolderPathField(folderPathField);
        tableSelectionPage.setTableViewer(tableviewer);
        tableSelectionPage.setFileFormatControl(fileFormat);

        tableSelectionPage.validatePage();

        assertTrue(tableSelectionPage.getErrorMessage().contains(Messages.TableSelectionPage_msgFolderNonExisting));
    }

    @Test
    public void testValidatePage_invalidTableContent() throws Exception {

        ITableContents invalidtableContents = (ITableContents)newIpsObject(ipsRootFolder, IpsObjectType.TABLE_CONTENTS,
                "invalid");
        selection = new StructuredSelection(List.of(invalidtableContents, tableContents2));
        tableSelectionPage = new TableSelectionPage(selection);

        CheckboxTableViewer tableviewer = mock(CheckboxTableViewer.class);
        when(tableviewer.getCheckedElements()).thenReturn(selection.toArray());

        Combo fileFormat = mock(Combo.class);
        when(fileFormat.getSelectionIndex()).thenReturn(1);

        TextButtonField folderPathField = mock(TextButtonField.class);
        when(folderPathField.getText()).thenReturn(tempDir.toString());

        tableSelectionPage.setFolderPathField(folderPathField);
        tableSelectionPage.setTableViewer(tableviewer);
        tableSelectionPage.setFileFormatControl(fileFormat);

        tableSelectionPage.validatePage();

        assertTrue(tableSelectionPage.getErrorMessage().contains(
                org.faktorips.devtools.core.ui.wizards.tableexport.Messages.TableExportPage_msgNonExisitingStructure));
    }

    @Test
    public void testValidatePage_validPage() throws Exception {

        selection = new StructuredSelection(List.of(tableContents1, tableContents2));

        tableSelectionPage = new TableSelectionPage(selection);

        CheckboxTableViewer tableviewer = mock(CheckboxTableViewer.class);
        when(tableviewer.getCheckedElements()).thenReturn(selection.toArray());

        TextButtonField folderPathField = mock(TextButtonField.class);
        when(folderPathField.getText()).thenReturn(tempDir.toString());

        Combo fileFormat = mock(Combo.class);
        when(fileFormat.getSelectionIndex()).thenReturn(2);

        tableSelectionPage.setFolderPathField(folderPathField);
        tableSelectionPage.setTableViewer(tableviewer);
        tableSelectionPage.setFileFormatControl(fileFormat);
        tableSelectionPage.validatePage();

        assertNull(tableSelectionPage.getErrorMessage());
    }

}
