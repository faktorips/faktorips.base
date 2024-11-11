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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResource.AResourceType;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.FolderSelectionControl;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.ipsexport.IpsObjectExportPage;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.tablecontents.ITableContents;

/**
 * The {@code TableSelectionPage} class represents a wizard page for exporting table contents in the
 * IPS Object Export Wizard. It allows the user to select tables for export, choose an export
 * folder, specify the file format, and define null value representations.
 * <p>
 * This page validates user input, checks for folder existence, and ensures no duplicate table
 * content file names are selected.
 * </p>
 */
public class TableSelectionPage extends IpsObjectExportPage {

    @SuppressWarnings("hiding")
    public static final String PAGE_NAME = "TableSelectionPage";

    protected static final String FOLDER_PATH = PAGE_NAME + ".FOLDER_PATH"; //$NON-NLS-1$

    private CheckboxTableViewer tableViewer;

    private TextButtonField folderPathField;

    private Map<IIpsSrcFile, ITableContents> tableContents = new HashMap<>();

    /**
     * Constructs a {@code TableSelectionPage} instance, initializing it with the specified
     * structured selection.
     *
     * @param selection the structured selection to process and extract table contents from
     * @throws JavaModelException if an error occurs during resource extraction
     */
    public TableSelectionPage(IStructuredSelection selection) throws JavaModelException {
        super(Messages.TableExportPage_title, selection, true);
        for (Object item : selection.toArray()) {
            extractTableContents(item);
        }
    }

    /**
     * Extracts table contents from the specified selection object, resolving it to an
     * {@code IResource} if possible and processing it for table contents.
     *
     * @param selection the selected item to process
     * @throws JavaModelException if an error occurs during resource extraction
     */
    private void extractTableContents(Object selection) throws JavaModelException {
        IResource selectedResource = resolveResource(selection);
        if (selectedResource != null) {
            processResourceForTableContents(selectedResource);
        }
    }

    /**
     * Processes the specified resource and retrieves all valid table contents. Depending on the
     * resource type (file, folder, or project), it extracts the appropriate IPS source files.
     *
     * @param selectedResource the resource to process
     */
    public void processResourceForTableContents(IResource selectedResource) {

        IIpsElement ipsElement = IIpsModel.get().getIpsElement(Wrappers.wrap(selectedResource).as(AResource.class));
        if (ipsElement == null) {
            return;
        }

        if (isFolder(selectedResource)) {
            List<IIpsSrcFile> allIpsSrcFiles = getIpsSrcFilesFromFolder(ipsElement);
            allIpsSrcFiles.forEach(this::addTableContentIfValid);

        } else if (isFile(selectedResource)) {
            IIpsSrcFile ipsSrcFile = getIpsSrcFile(selectedResource);
            addTableContentIfValid(ipsSrcFile);

        } else if (isProject(selectedResource)) {
            List<IIpsSrcFile> allIpsSrcFiles = getIpsSrcFilesFromProject(ipsElement);
            allIpsSrcFiles.forEach(this::addTableContentIfValid);
        }
    }

    private boolean isFolder(IResource resource) {
        return (resource.getType() & IResource.FOLDER) != 0;
    }

    private boolean isFile(IResource resource) {
        return (resource.getType() & IResource.FILE) != 0;
    }

    private boolean isProject(IResource resource) {
        return (resource.getType() & IResource.PROJECT) != 0;
    }

    /**
     * Retrieves all IPS source files from the specified folder element and its subfolders.
     *
     * @param element the folder element to search in
     * @return a list of {@code IIpsSrcFile} objects found in the folder
     */
    private List<IIpsSrcFile> getIpsSrcFilesFromFolder(IIpsElement element) {
        List<IIpsSrcFile> srcFiles = new ArrayList<>();
        findIpsSrcFilesFromFolder(element, srcFiles);
        return srcFiles;
    }

    /**
     * Recursively finds all IPS source files in the specified folder element and adds them to the
     * specified list.
     *
     * @param element the folder element to search in
     * @param srcFiles the list to add the found IPS source files to
     */
    private void findIpsSrcFilesFromFolder(IIpsElement element, List<IIpsSrcFile> srcFiles) {
        if (element.getCorrespondingResource().getType().equals(AResourceType.FOLDER)) {
            for (IIpsElement child : element.getChildren()) {
                if (child.getCorrespondingResource().getType().equals(AResourceType.FOLDER)) {
                    findIpsSrcFilesFromFolder(child, srcFiles);
                } else {
                    IIpsSrcFile srcFile = getIpsSrcFileFromIpsElement(child);
                    if (srcFile != null && srcFile.getIpsObjectType().equals(IpsObjectType.TABLE_CONTENTS)) {
                        srcFiles.add(srcFile);
                    }
                }
            }
        }
    }

    /**
     * Retrieves all IPS source files of type {@code TABLE_CONTENTS} from the specified ips project.
     *
     * @param projectElement the project element to search in
     * @return a list of {@code IIpsSrcFile} objects found in the project
     */
    private List<IIpsSrcFile> getIpsSrcFilesFromProject(IIpsElement projectElement) {
        return projectElement.getIpsProject().findAllIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
    }

    /**
     * Adds the specified IPS source file to the {@code tableContents} map if it is of type
     * {@code TABLE_CONTENTS}.
     *
     * @param file the IPS source file to add
     */
    private void addTableContentIfValid(IIpsSrcFile file) {
        if (file != null && file.getIpsObjectType().equals(IpsObjectType.TABLE_CONTENTS)) {
            tableContents.put(file, findResourceTableContents(file));
        }
    }

    /**
     * Creates the table viewer to display the selected items.
     */
    private void createTableViewer() {
        setTableViewer(CheckboxTableViewer.newCheckList(pageControl, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL));
        getTableViewer().getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        getTableViewer().setContentProvider(ArrayContentProvider.getInstance());

        List<ITableContents> sortedContents = getTableContents().values().stream()
                .sorted(Comparator.comparing(ITableContents::getName))
                .toList();

        getTableViewer().setInput(sortedContents);
        getTableViewer().addCheckStateListener(event -> validatePage());
        getTableViewer().setAllChecked(true);
    }

    /**
     * Creates the folder path control for selecting the export folder.
     */
    private void createFolderControl(UIToolkit toolkit, Composite optionsComposite) {
        toolkit.createFormLabel(optionsComposite, Messages.TableSelectionPage_labelFolderField);
        setFolderPathField(new TextButtonField(new FolderSelectionDialog(optionsComposite, toolkit)));
        getFolderPathField().addChangeListener(this);
    }

    /**
     * Creates the layout of the UI page.
     */
    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        setTitle(Messages.TableExportPage_title);
        createPageControl(parent);
        createTableViewer();
        createSeparator(pageControl);

        Composite optionsComposite = toolkit.createLabelEditColumnComposite(pageControl);

        createFolderControl(toolkit, optionsComposite);
        createFileFormatControl(toolkit, optionsComposite);
        createNullPresentationControl(toolkit, optionsComposite);

        createColumHeaderCheckBox(toolkit, pageControl);

        setPageComplete(false);

        restoreWidgetValues();

        validatePage();

    }

    /**
     * Returns the {@link IIpsElement} associated with the given {@link IIpsSrcFile}.
     *
     * @param srcFile the source file to find the IPS element for
     * @return the corresponding IPS element, or {@code null} if none is found
     */
    public IIpsElement findIpsElement(IIpsSrcFile srcFile) {
        return IIpsModel.get().getIpsElement(srcFile.getCorrespondingResource());
    }

    /**
     * Finds and returns the table contents for the specified IPS source file.
     *
     * @param srcFile the IPS source file to search for
     * @return the associated {@code ITableContents} if found, or {@code null} if not
     */
    private ITableContents findResourceTableContents(IIpsSrcFile srcFile) {
        IpsObjectType ipsObjectType = srcFile.getIpsObjectType();
        if (ipsObjectType.equals(IpsObjectType.TABLE_CONTENTS)) {
            return (ITableContents)srcFile.getIpsObject();
        }
        return null;
    }

    public void setFolderPath(String path) {
        getFolderPathField().setText(path);
        validatePage();
    }

    public String getFolderPath() {
        return getFolderPathField().getText();
    }

    public IStructuredSelection getSelectedResources() {
        return getTableViewer() != null ? new StructuredSelection(getTableViewer().getCheckedElements())
                : StructuredSelection.EMPTY;
    }

    public Map<IIpsSrcFile, ITableContents> getTableContents() {
        return tableContents;
    }

    /**
     * Retrieves the checked items in the table viewer as a list of table contents.
     *
     * @return the list of checked IPS source files
     */
    public List<ITableContents> getTableViewerChosenTableContents() {
        return Arrays.stream(getTableViewer().getCheckedElements())
                .filter(ITableContents.class::isInstance)
                .map(ITableContents.class::cast)
                .toList();
    }

    public List<IIpsSrcFile> getTableViewerChosenIpsSrcFiles() {
        // Fetch all selected ITableContents
        List<ITableContents> selectedContents = getTableViewerChosenTableContents();

        // Find the corresponding IIpsSrcFile for each selected ITableContents
        return tableContents.entrySet().stream()
                .filter(entry -> selectedContents.contains(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).keySet().stream().toList();
    }

    /**
     * Generates a filename for the specified IPS source file, appending the specified file
     * extension.
     *
     * @param srcFile the IPS source file
     * @param extension the file extension
     * @return the generated filename
     */
    public String getFilename(IIpsSrcFile srcFile, String extension) {
        String contentsName = tableContents.get(srcFile).getName();
        return contentsName + extension;
    }

    public TextButtonField getFolderPathField() {
        return folderPathField;
    }

    public void setFolderPathField(TextButtonField folderPathField) {
        this.folderPathField = folderPathField;
    }

    public CheckboxTableViewer getTableViewer() {
        return tableViewer;
    }

    public void setTableViewer(CheckboxTableViewer tableViewer) {
        this.tableViewer = tableViewer;
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == getFolderPathField()) {
            validatePage();
        }

        updateSelectionPageCompletion();
    }

    /**
     * Sets the page as complete if there are no errors and required selections are made. Checks
     * that a folder path, table selection, and file format are specified.
     */
    private void updateSelectionPageCompletion() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }

        boolean hasFolderSelected = getFolderPathField().getText().trim().length() > 0;
        boolean hasTableSelected = getTableViewer().getCheckedElements().length > 0;
        boolean hasFileFormatSelected = getFileFormatControl().getSelectionIndex() != -1;
        setPageComplete(hasFolderSelected && hasTableSelected && hasFileFormatSelected);
    }

    @Override
    protected boolean allowNewContainerName() {
        return false;
    }

    /**
     * Validates that the folder path is not empty and exists on the file system.
     */
    private void validateFolder() {
        if (getFolderPathField() != null) {
            if (getFolderPath().equals("")) {
                setErrorMessage(Messages.TableSelectionPage_msgFolderPathEmpty);
                return;
            }
            File folder = new File(getFolderPath());
            if (!folder.exists() && !folder.isDirectory()) {
                setErrorMessage(Messages.TableSelectionPage_msgFolderNonExisting);
            }
        }
    }

    @Override
    protected void validateObjectToExport() {
        tableContents.values().forEach(this::validateTableContent);
    }

    /**
     * Checks for duplicate table content names among the checked items in the table viewer. If
     * duplicates are found, an error message is set, and a map of duplicate names and their
     * corresponding table contents is returned.
     *
     * @return a map where each key is a duplicate table content name, and each value is a list of
     *             {@code ITableContents} objects with that name
     */
    private Map<String, List<ITableContents>> validateNoDuplicateSrcFileNames() {
        Map<String, List<ITableContents>> nameToTableContentsMap = new HashMap<>();

        for (ITableContents content : getTableViewerChosenTableContents()) {
            String name = content.getName();
            nameToTableContentsMap.computeIfAbsent(name, k -> new ArrayList<>()).add(content);
        }

        List<String> duplicateNames = nameToTableContentsMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .toList();

        if (!duplicateNames.isEmpty()) {
            setErrorMessage(Messages.TableSelectionPage_msgDuplicateFileNames + String.join(", ", duplicateNames));
        }

        return nameToTableContentsMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Highlights duplicate entries in red font color for each checked, duplicate content in the
     * viewer.
     */
    private void highlightDuplicateEntries(Map<String, List<ITableContents>> duplicatesMap) {

        resetTableViewerColors();

        duplicatesMap.forEach((name, contentsList) -> {
            List<ITableContents> checkedContents = contentsList.stream()
                    .filter(content -> getTableViewer().getChecked(content))
                    .toList();
            if (checkedContents.size() > 1) {
                checkedContents.forEach(this::highlightContentInRed);
            }
        });
    }

    /**
     * Resets all items in the table viewer to their default font color.
     */
    private void resetTableViewerColors() {

        if (getTableViewer().getTable() != null) {
            TableItem[] items = getTableViewer().getTable().getItems();
            for (TableItem item : items) {
                item.setForeground(null);
            }
        }

    }

    private void highlightContentInRed(ITableContents content) {

        if (getTableViewer().getTable() != null) {
            TableItem[] items = getTableViewer().getTable().getItems();
            for (TableItem item : items) {
                if (item.getData().equals(content)) {
                    item.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
                    break;
                }
            }
        }
    }

    /**
     * Validates the page and generates error messages if needed.
     */
    @Override
    protected void validatePage() {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage(null);

        Map<String, List<ITableContents>> duplicatesMap = validateNoDuplicateSrcFileNames();
        if (getErrorMessage() != null) {
            highlightDuplicateEntries(duplicatesMap);
            updateSelectionPageCompletion();
            return;
        }

        resetTableViewerColors();

        validateFolder();
        if (getErrorMessage() != null) {
            updateSelectionPageCompletion();
            return;
        }

        validateObjectToExport();

        updateSelectionPageCompletion();
    }

    @Override
    protected IpsObjectRefControl createExportedIpsObjectRefControlWithLabel(UIToolkit toolkit, Composite parent) {
        return null;
    }

    @Override
    protected void setDefaults(IResource selectedResource) {
        // Nothing to do

    }

    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }
        exportWithColumnHeaderRowField.getCheckbox().setChecked(settings.getBoolean(EXPORT_WITH_COLUMN_HEADER));
        nullRepresentation.setText(settings.get(NULL_REPRESENTATION));
        setFolderPath(settings.get(FOLDER_PATH));

    }

    @Override
    public void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }
        settings.put(EXPORT_WITH_COLUMN_HEADER, exportWithColumnHeaderRowField.getCheckbox().isChecked());
        settings.put(NULL_REPRESENTATION, nullRepresentation.getText());
        settings.put(FOLDER_PATH, getFolderPath());

    }

    /**
     * An inner class that extends {@code FolderSelectionControl} and provides a button for browsing
     * the file system to select an export folder.
     */
    public class FolderSelectionDialog extends FolderSelectionControl {

        public FolderSelectionDialog(Composite parent, UIToolkit toolkit) {
            super(parent, toolkit, Messages.TableSelectionPage_labelFolderSelectionButton);
        }

        @Override
        protected void buttonClicked() {
            openDirectoryDialog();
        }

        private void openDirectoryDialog() {
            DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
            directoryDialog.setText(Messages.TableSelectionPage_folderSelectionText);
            directoryDialog.setMessage(Messages.TableSelectionPage_folderSelectionMessage);

            String selectedFolder = directoryDialog.open();
            if (selectedFolder != null) {
                setFolderPath(selectedFolder);
            }
        }
    }
}
