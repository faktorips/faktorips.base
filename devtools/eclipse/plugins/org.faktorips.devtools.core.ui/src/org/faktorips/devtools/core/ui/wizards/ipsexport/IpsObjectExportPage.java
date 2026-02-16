/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsexport;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.tableconversion.ITableFormat;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.StringValueComboField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.FileSelectionControl;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileExternal;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.StringUtil;

/**
 * Base wizard page for configuring an IPS object for export.
 * <p>
 * Despite a filename and the IPS object to export this page allows to set advanced parameters for
 * the target format of the export.
 *
 * @author Roman Grutza
 */
public abstract class IpsObjectExportPage extends WizardDataTransferPage implements ValueChangeListener {

    public static final String PAGE_NAME = "IpsObjectExportPage"; //$NON-NLS-1$

    /** The maximum number of columns allowed in an Excel sheet. */
    protected static final short MAX_EXCEL_COLUMNS = Short.MAX_VALUE;
    protected static final String EXPORT_WITH_COLUMN_HEADER = PAGE_NAME + ".EXPORT_WITH_COLUMN_HEADER"; //$NON-NLS-1$
    protected static final String NULL_REPRESENTATION = PAGE_NAME + ".NULL_REPRESENTATION"; //$NON-NLS-1$
    protected static final String EXPORT_ENUM_AS_NAME_AND_ID = PAGE_NAME + ".EXPORT_ENUM_AS_NAME_AND_ID"; //$NON-NLS-1$

    protected Composite pageControl;

    protected IpsProjectRefControl projectControl;
    private Combo fileFormatControl;
    protected Text nullRepresentation;
    protected TextButtonField filenameField;
    protected TextButtonField projectField;
    protected CheckboxField exportWithColumnHeaderRowField;
    protected CheckboxField exportEnumAsNameAndIdField;
    protected StringValueComboField fileFormatField;

    protected ITableFormat[] formats;

    protected TextButtonField exportedIpsObjectField;
    protected IpsObjectRefControl exportedIpsObjectControl;

    protected IResource selectedResource;

    protected IIpsSrcFile selectedIpsSrcFile;

    private boolean validateInput;

    public IpsObjectExportPage(String pageName, IStructuredSelection selection, boolean isMassExport)
            throws JavaModelException {
        super(pageName);
        validateInput = true;
        if (selection.getFirstElement() != null && !isMassExport) {
            selectedResource = resolveResource(selection.getFirstElement());
        } else {
            selectedResource = null;
        }
    }

    protected IResource resolveResource(Object selection) throws JavaModelException {
        return switch (selection) {
            case IResource resource -> resource;
            case IJavaElement javaElement -> javaElement.getCorrespondingResource();
            case IIpsElement ipsElement -> {
                if (ipsElement instanceof IIpsObject) {
                    selectedIpsSrcFile = ((IIpsObject)ipsElement).getIpsSrcFile();
                }
                yield ipsElement.getEnclosingResource().unwrap();
            }
            default -> null;
        };
    }

    /**
     * Creates a label and an IPS object reference control for the <code>IIpsObject</code> to
     * export.
     *
     * @param toolkit A form toolkit
     * @param parent The parent composite
     * @return A concrete <code>IpsObjectRefControl</code> instance, like TableContentsRefControl,
     *             ...
     */
    protected abstract IpsObjectRefControl createExportedIpsObjectRefControlWithLabel(UIToolkit toolkit,
            Composite parent);

    /**
     * Validate the IPS object (like enum type/content or table contents) to export.
     */
    protected abstract void validateObjectToExport();

    /**
     * Derives the default values for source folder and package from the selected resource.
     *
     * @param selectedResource The resource that was selected in the current selection when the
     *            wizard was opened.
     */
    protected abstract void setDefaults(IResource selectedResource);

    /**
     * Gets the {@link IpsSrcFile} which contains the data required for the export.
     *
     * @param selectedResource The currently selected resource
     * @return The required IIpsSrcFile or null if is does not exist
     */
    protected IIpsSrcFile getIpsSrcFile(IResource selectedResource) {
        IIpsElement srcElement = null;
        if (selectedIpsSrcFile != null) {
            srcElement = selectedIpsSrcFile;

        } else if (selectedResource != null) {
            srcElement = IIpsModel.get().getIpsElement(Wrappers.wrap(selectedResource).as(AResource.class));
        } else {
            return null;
        }

        return getIpsSrcFileFromIpsElement(srcElement);
    }

    public void setFilename(String newName) {
        filenameField.setText(newName);
    }

    public String getFilename() {
        return filenameField.getText();
    }

    protected IIpsSrcFile getIpsSrcFileFromIpsElement(IIpsElement ipsElement) {
        return switch (ipsElement) {
            case IpsSrcFileExternal external -> external.getMutableIpsSrcFile();
            case IIpsSrcFile ipssrcFile -> ipssrcFile;
            default -> null;
        };
    }

    public Combo getFileFormatControl() {
        return fileFormatControl;
    }

    public void setFileFormatControl(Combo fileFormatControl) {
        this.fileFormatControl = fileFormatControl;
    }

    public ITableFormat getFormat() {
        if (getFileFormatControl().getSelectionIndex() == -1) {
            return null;
        }
        return formats[getFileFormatControl().getSelectionIndex()];
    }

    public IIpsProject getIpsProject() {
        return "".equals(projectField.getText()) ? null //$NON-NLS-1$
                : IIpsModel.get().getIpsProject(projectField.getText());
    }

    protected void validateTableContent(ITableContents contents) {

        if (contents == null || !contents.exists()) {
            setErrorMessage(
                    org.faktorips.devtools.core.ui.wizards.tableexport.Messages.TableExportPage_msgNonExisitingContents);
            return;
        }
        ITableStructure structure = contents.findTableStructure(contents.getIpsProject());
        if (structure == null || !structure.exists()) {
            setErrorMessage(
                    org.faktorips.devtools.core.ui.wizards.tableexport.Messages.TableExportPage_msgNonExisitingStructure);
            return;
        }
        MessageList structureValidationMessages = structure.validate(structure.getIpsProject());
        removeVersionFormatValidation(structureValidationMessages);
        if (structureValidationMessages.containsErrorMsg()) {
            setWarningMessage(
                    org.faktorips.devtools.core.ui.wizards.tableexport.Messages.TableExportPage_msgStructureNotValid);
        } else {
            clearWarningMessage();
        }
        if (structure.getNumOfColumns() > MAX_EXCEL_COLUMNS) {
            Object[] objects = new Object[3];
            objects[0] = Integer.valueOf(structure.getNumOfColumns());
            objects[1] = structure;
            objects[2] = Short.valueOf(MAX_EXCEL_COLUMNS);
            String text = NLS
                    .bind(org.faktorips.devtools.model.tablecontents.Messages.TableExportOperation_errStructureTooMuchColumns,
                            objects);
            setErrorMessage(text);
        }
    }

    protected void validateFormat() {
        // must not be empty
        if (getFileFormatControl().getSelectionIndex() == -1) {
            setErrorMessage(Messages.IpsObjectExportPage_msgMissingFileFormat);
        }
    }

    /**
     * The method validates the filename.
     * <p>
     * Subclasses may extend this method to perform their own validation.
     */
    protected void validateFilename() {
        String filename = filenameField.getText();
        // must not be empty
        if (filename.length() == 0) {
            setErrorMessage(Messages.IpsObjectExportPage_msgEmptyName);
            return;
        }
        File file = new File(filename);
        if (file.isDirectory()) {
            setErrorMessage(Messages.IpsObjectExportPage_msgFilenameIsDirectory);
            return;
        }
        ITableFormat format = getFormat();
        if (format != null) {
            String formatExtension = format.getDefaultExtension();
            if (!filename.endsWith(formatExtension)) {
                String errorMessage = NLS.bind(Messages.IpsObjectExportPage_msgMissingFileExtension,
                        filename, formatExtension);
                setErrorMessage(errorMessage);
                return;
            }
        }
        if (file.exists()) {
            setMessage(Messages.IpsObjectExportPage_msgFileAlreadyExists, IMessageProvider.WARNING);
        }
    }

    protected void validateProject() {
        if (projectField.getText().equals("")) { //$NON-NLS-1$
            setErrorMessage(Messages.IpsObjectExportPage_msgProjectEmpty);
            return;
        }
        IIpsProject project = getIpsProject();
        if (project == null) {
            setErrorMessage(Messages.IpsObjectExportPage_msgNonExistingProject);
            return;
        }
        if (!project.exists()) {
            setErrorMessage(Messages.IpsObjectExportPage_msgNonExistingProject);
        }
    }

    /**
     * Checks whether the displayed qualified name is unique. If not, a hint is shown which
     * describes the selected {@link IpsObjectType}.
     * <p>
     * This check does not affect the validation process since it is only used for improving the
     * usability.
     *
     * @throws IpsException if an error occurs during the validation
     */
    protected void validateObjectToExportUniqueness() {
        String qualifiedName = exportedIpsObjectField.getText();
        if (!exportedIpsObjectControl.checkIpsObjectUniqueness(qualifiedName)) {
            IpsObjectType selectedObjectType = exportedIpsObjectControl.getSelectedObjectType();
            String message = NLS.bind(Messages.IpsObjectExportPage_msgDuplicateQualifiedName,
                    qualifiedName, selectedObjectType.getDisplayName());
            setMessage(message, IMessageProvider.INFORMATION);
        }
    }

    /**
     * Validates the page and generates error messages if needed. Can be overridden in subclasses to
     * add specific validation logics.
     */
    protected void validatePage() {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage(null);
        validateProject();
        if (getErrorMessage() != null) {
            return;
        }
        validateObjectToExport();
        if (getErrorMessage() != null) {
            return;
        }
        // First, check the selected file format
        validateFormat();
        if (getErrorMessage() != null) {
            return;
        }
        // Then, check the filename including the expected file format extension.
        validateFilename();
        if (getErrorMessage() != null) {
            return;
        }
        validateObjectToExportUniqueness();
        updatePageComplete();
    }

    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(projectField.getText()) //$NON-NLS-1$
                && !"".equals(filenameField.getText()) //$NON-NLS-1$
                && !"".equals(exportedIpsObjectField.getText()) //$NON-NLS-1$
                && getFileFormatControl().getSelectionIndex() != -1;
        setPageComplete(complete);
    }

    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }
        exportWithColumnHeaderRowField.getCheckbox().setChecked(settings.getBoolean(EXPORT_WITH_COLUMN_HEADER));
        exportEnumAsNameAndIdField.getCheckbox().setChecked(settings.getBoolean(EXPORT_ENUM_AS_NAME_AND_ID));
        nullRepresentation.setText(settings.get(NULL_REPRESENTATION));

    }

    protected void projectChanged() {
        if ("".equals(projectField.getText())) { //$NON-NLS-1$
            exportedIpsObjectControl.setIpsProjects();
            return;
        }
        IIpsProject project = IIpsModel.get().getIpsProject(projectField.getText());
        if (project.exists()) {
            exportedIpsObjectControl.setIpsProjects(project);
            return;
        }
        exportedIpsObjectControl.setIpsProjects();
    }

    @Override
    public void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }
        settings.put(EXPORT_WITH_COLUMN_HEADER, exportWithColumnHeaderRowField.getCheckbox().isChecked());
        settings.put(EXPORT_ENUM_AS_NAME_AND_ID, exportEnumAsNameAndIdField.getCheckbox().isChecked());
        settings.put(NULL_REPRESENTATION, nullRepresentation.getText());

    }

    private void createProjectFieldComposite(UIToolkit toolkit) {
        Composite locationComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.IpsObjectExportPage_labelProject);
        projectControl = toolkit.createIpsProjectRefControl(locationComposite);
        projectField = new TextButtonField(projectControl);
        projectField.addChangeListener(this);
    }

    private void createFileNameControl(UIToolkit toolkit, Composite lowerComposite) {
        toolkit.createFormLabel(lowerComposite, Messages.IpsObjectExportPage_labelName);
        filenameField = new TextButtonField(new FileSelectionDialogWithDefault(lowerComposite, toolkit));
        filenameField.addChangeListener(this);
    }

    private void creatIpsObjectControl(UIToolkit toolkit, Composite lowerComposite) {
        exportedIpsObjectControl = createExportedIpsObjectRefControlWithLabel(toolkit, lowerComposite);
        exportedIpsObjectField = new TextButtonField(exportedIpsObjectControl);
        exportedIpsObjectField.addChangeListener(this);
    }

    protected void createPageControl(Composite parent) {
        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);
    }

    protected void createSeparator(Composite composite) {
        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    protected void createNullPresentationControl(UIToolkit toolkit, Composite composite) {
        toolkit.createFormLabel(composite, Messages.IpsObjectExportPage_labelNullRepresentation);
        nullRepresentation = toolkit.createText(composite);
        nullRepresentation.setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
    }

    protected void createFileFormatControl(UIToolkit toolkit, Composite composite) {
        toolkit.createFormLabel(composite, Messages.IpsObjectExportPage_labelFileFormat);
        setFileFormatControl(toolkit.createCombo(composite));
        formats = IpsPlugin.getDefault().getExternalTableFormats();
        for (ITableFormat format : formats) {
            getFileFormatControl().add(format.getName());
        }
        getFileFormatControl().select(0);
        fileFormatField = new StringValueComboField(getFileFormatControl());
        fileFormatField.addChangeListener(this);
    }

    protected void createColumHeaderCheckBox(UIToolkit toolkit, Composite composite) {
        Checkbox withColumnHeaderRow = toolkit.createCheckbox(composite,
                Messages.IpsObjectExportPage_firstRowContainsHeader);
        exportWithColumnHeaderRowField = new CheckboxField(withColumnHeaderRow);
        exportWithColumnHeaderRowField.addChangeListener(this);
        withColumnHeaderRow.setChecked(true);
    }

    protected void createEnumNameAndIdCheckBox(UIToolkit toolkit, Composite composite) {
        Checkbox exportEnumAsNameAndId = toolkit.createCheckbox(composite,
                Messages.IpsObjectExportPage_exportEnumAsNameAndId);
        exportEnumAsNameAndIdField = new CheckboxField(exportEnumAsNameAndId);
        exportEnumAsNameAndIdField.addChangeListener(this);
        exportEnumAsNameAndId.setChecked(false);
    }

    public boolean isExportEnumAsNameAndId() {
        return exportEnumAsNameAndIdField.getCheckbox().isChecked();
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.IpsObjectExportPage_pageTitle);

        createPageControl(parent);

        createProjectFieldComposite(toolkit);
        createSeparator(pageControl);

        Composite lowerComposite = toolkit.createLabelEditColumnComposite(pageControl);

        creatIpsObjectControl(toolkit, lowerComposite);
        createFileFormatControl(toolkit, lowerComposite);
        createFileNameControl(toolkit, lowerComposite);
        createNullPresentationControl(toolkit, lowerComposite);

        createColumHeaderCheckBox(toolkit, pageControl);
        createEnumNameAndIdCheckBox(toolkit, pageControl);

        setDefaults(selectedResource);

        validateInput = true;

        restoreWidgetValues();

        validatePage();
    }

    public void setIpsProject(IIpsProject project) {
        projectControl.setIpsProject(project);
        exportedIpsObjectControl.setIpsProjects(project);
    }

    protected void filenameChanged() {
        for (int i = 0; i < formats.length; i++) {
            ITableFormat format = formats[i];
            if (filenameField.getText().endsWith(format.getDefaultExtension())) {
                fileFormatField.getCombo().select(i);
            }
        }
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == projectField) {
            projectChanged();
        }
        if (e.field == filenameField) {
            filenameChanged();
        }
        if (validateInput) {
            // don't validate during control creating!
            validatePage();
        }
        updatePageComplete();
    }

    @Override
    protected boolean allowNewContainerName() {
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        // Nothing to do
    }

    public String getNullRepresentation() {
        return nullRepresentation.getText();
    }

    public boolean isExportColumnHeaderRow() {
        return exportWithColumnHeaderRowField.getCheckbox().isChecked();
    }

    protected void setWarningMessage(String newMessage) {
        setMessage(newMessage, IMessageProvider.WARNING);
    }

    protected void clearWarningMessage() {
        if (getMessageType() == IMessageProvider.WARNING) {
            setMessage(null);
        }
    }

    // FIPS-4865: the version format is not relevant to the export
    protected void removeVersionFormatValidation(MessageList messages) {
        for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();) {
            if (IIpsObjectPartContainer.MSGCODE_INVALID_VERSION_FORMAT.equals(iterator.next().getCode())) {
                iterator.remove();
            }
        }
    }

    /**
     * File selection with default name. The default name will be derived from the current selected
     * table contents name.
     */
    protected class FileSelectionDialogWithDefault extends FileSelectionControl {

        public FileSelectionDialogWithDefault(Composite parent, UIToolkit toolkit) {
            super(parent, toolkit, SWT.SAVE);
        }

        @Override
        protected void buttonClicked() {
            initializeExtensionFilter();

            String previousFilename = getFilename();

            // if there is no previous filename use the default filename
            setFilename(IpsStringUtils.isEmpty(previousFilename) ? getDefaultFilename() : previousFilename);

            // if no file was selected (e.g. cancel clicked)
            // set the previous filename
            if (askForFilename() == null) {
                setFilename(previousFilename);
            }
        }

        private String getDefaultFilename() {
            String contentsName = exportedIpsObjectField.getText();
            ITableFormat format = getFormat();
            String extension = ""; //$NON-NLS-1$
            if (format != null) {
                extension = format.getDefaultExtension();
            }
            return StringUtil.unqualifiedName(contentsName) + extension;
        }

        /**
         * Adds a filter to the dialog for just showing the files with the required file format.
         */
        private void initializeExtensionFilter() {
            ITableFormat selectedFormat = getFormat();
            if (selectedFormat != null) {
                String[] availableExtensions = { selectedFormat.getDefaultExtensionWildcard() };
                String[] availableExtensionsNames = { selectedFormat.getName() };
                setDialogFilterExtensions(availableExtensionsNames, availableExtensions);
            } else {
                clearDialogFilterExtensions();
            }
        }
    }

}
