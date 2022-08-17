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

import org.apache.commons.lang.StringUtils;
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
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
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

    protected Composite pageControl;

    protected IpsProjectRefControl projectControl;
    protected Combo fileFormatControl;
    protected Text nullRepresentation;
    protected TextButtonField filenameField;
    protected TextButtonField projectField;
    protected CheckboxField exportWithColumnHeaderRowField;
    protected StringValueComboField fileFormatField;

    protected ITableFormat[] formats;

    protected TextButtonField exportedIpsObjectField;
    protected IpsObjectRefControl exportedIpsObjectControl;

    protected IResource selectedResource;

    protected IIpsSrcFile selectedIpsSrcFile;

    private boolean validateInput;

    public IpsObjectExportPage(String pageName, IStructuredSelection selection) throws JavaModelException {
        super(pageName);
        validateInput = true;
        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();
        } else if (selection.getFirstElement() instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)selection.getFirstElement();
            if (ipsElement instanceof IIpsObject) {
                selectedIpsSrcFile = ((IIpsObject)ipsElement).getIpsSrcFile();
            }
            selectedResource = ipsElement.getEnclosingResource().unwrap();
        } else {
            selectedResource = null;
        }
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

        if (srcElement instanceof IpsSrcFileExternal) {
            return ((IpsSrcFileExternal)srcElement).getMutableIpsSrcFile();
        } else if (srcElement instanceof IIpsSrcFile) {
            return (IIpsSrcFile)srcElement;
        }

        return null;
    }

    public void setFilename(String newName) {
        filenameField.setText(newName);
    }

    public String getFilename() {
        return filenameField.getText();
    }

    public ITableFormat getFormat() {
        if (fileFormatControl.getSelectionIndex() == -1) {
            return null;
        }
        return formats[fileFormatControl.getSelectionIndex()];
    }

    public IIpsProject getIpsProject() {
        return "".equals(projectField.getText()) ? null //$NON-NLS-1$
                : IIpsModel.get().getIpsProject(projectField.getText());
    }

    protected void validateFormat() {
        // must not be empty
        if (fileFormatControl.getSelectionIndex() == -1) {
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
                && fileFormatControl.getSelectionIndex() != -1;
        setPageComplete(complete);
    }

    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }
        exportWithColumnHeaderRowField.getCheckbox().setChecked(settings.getBoolean(EXPORT_WITH_COLUMN_HEADER));
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
        settings.put(NULL_REPRESENTATION, nullRepresentation.getText());
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.IpsObjectExportPage_pageTitle);

        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);

        Composite locationComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.IpsObjectExportPage_labelProject);
        projectControl = toolkit.createIpsProjectRefControl(locationComposite);
        projectField = new TextButtonField(projectControl);
        projectField.addChangeListener(this);

        Label line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite lowerComposite = toolkit.createLabelEditColumnComposite(pageControl);

        // this creates a label and the control
        exportedIpsObjectControl = createExportedIpsObjectRefControlWithLabel(toolkit, lowerComposite);
        exportedIpsObjectField = new TextButtonField(exportedIpsObjectControl);
        exportedIpsObjectField.addChangeListener(this);

        toolkit.createFormLabel(lowerComposite, Messages.IpsObjectExportPage_labelFileFormat);
        fileFormatControl = toolkit.createCombo(lowerComposite);

        formats = IpsPlugin.getDefault().getExternalTableFormats();
        for (ITableFormat format : formats) {
            fileFormatControl.add(format.getName());
        }
        fileFormatControl.select(0);
        fileFormatField = new StringValueComboField(fileFormatControl);
        fileFormatField.addChangeListener(this);

        toolkit.createFormLabel(lowerComposite, Messages.IpsObjectExportPage_labelName);
        filenameField = new TextButtonField(new FileSelectionDialogWithDefault(lowerComposite, toolkit));
        filenameField.addChangeListener(this);

        toolkit.createFormLabel(lowerComposite, Messages.IpsObjectExportPage_labelNullRepresentation);
        nullRepresentation = toolkit.createText(lowerComposite);
        nullRepresentation.setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());

        Checkbox withColumnHeaderRow = toolkit.createCheckbox(pageControl,
                Messages.IpsObjectExportPage_firstRowContainsHeader);
        exportWithColumnHeaderRowField = new CheckboxField(withColumnHeaderRow);
        exportWithColumnHeaderRowField.addChangeListener(this);
        withColumnHeaderRow.setChecked(true);

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
            setFilename(StringUtils.isEmpty(previousFilename) ? getDefaultFilename() : previousFilename);

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
