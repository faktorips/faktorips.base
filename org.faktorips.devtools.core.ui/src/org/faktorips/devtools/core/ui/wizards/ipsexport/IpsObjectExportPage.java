/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.ipsexport;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.FileSelectionControl;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.tableconversion.ITableFormat;
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
    private ComboField fileFormatField;

    protected ITableFormat[] formats;

    protected TextButtonField exportedIpsObjectField;
    protected IpsObjectRefControl exportedIpsObjectControl;

    protected IResource selectedResource;
    protected boolean validateInput = true;

    /**
     * Creates a label and an IPS object reference control for the <code>IIpsObject</code> to
     * export.
     * 
     * @param toolkit A form toolkit
     * @param parent The parent composite
     * @return A concrete <code>IpsObjectRefControl</code> instance, like TableContentsRefControl,
     *         ...
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

    public IpsObjectExportPage(String pageName) {
        super(pageName);
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
        return "".equals(projectField.getText()) ? null : //$NON-NLS-1$
                IpsPlugin.getDefault().getIpsModel().getIpsProject(projectField.getText());
    }

    protected void validateFormat() {
        // must not be empty
        if (fileFormatControl.getSelectionIndex() == -1) {
            setErrorMessage(Messages.IpsObjectExportPage_msgMissingFileFormat);
            return;
        }
    }

    /**
     * The method validates the filename.
     * <p>
     * Subclasses may extend this method to perform their own validation.
     * </p>
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
            return;
        }
    }

    /*
     * File selection with default name. The default name will be derived from the current selected
     * table contents name.
     */
    protected class FileSelectionDialogWithDefault extends FileSelectionControl {
        public FileSelectionDialogWithDefault(Composite parent, UIToolkit toolkit) {
            super(parent, toolkit, SWT.SAVE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buttonClicked() {
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
        validateFilename();
        if (getErrorMessage() != null) {
            return;
        }
        validateFormat();
        if (getErrorMessage() != null) {
            return;
        }
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

    /**
     * {@inheritDoc}
     */
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
            exportedIpsObjectControl.setIpsProject(null);
            return;
        }
        IIpsProject project = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectField.getText());
        if (project.exists()) {
            exportedIpsObjectControl.setIpsProject(project);
            return;
        }
        exportedIpsObjectControl.setIpsProject(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }
        settings.put(EXPORT_WITH_COLUMN_HEADER, exportWithColumnHeaderRowField.getCheckbox().isChecked());
        settings.put(NULL_REPRESENTATION, nullRepresentation.getText());
    }

    /**
     * {@inheritDoc}
     */
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
        fileFormatField = new ComboField(fileFormatControl);
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
        exportedIpsObjectControl.setIpsProject(project);
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
        if (validateInput) { // don't validate during control creating!
            validatePage();
        }
        updatePageComplete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean allowNewContainerName() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(Event event) {

    }

    public String getNullRepresentation() {
        return nullRepresentation.getText();
    }

    public boolean isExportColumnHeaderRow() {
        return exportWithColumnHeaderRowField.getCheckbox().isChecked();
    }

}
