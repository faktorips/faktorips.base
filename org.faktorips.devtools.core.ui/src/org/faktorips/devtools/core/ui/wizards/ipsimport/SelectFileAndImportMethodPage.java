/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsimport;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.ComboField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.FileSelectionControl;
import org.faktorips.devtools.core.ui.controls.Radiobutton;
import org.faktorips.devtools.tableconversion.ITableFormat;

/**
 * Page to select a source file, the table format and the destination IPS object (like a table
 * content or an enum type/content) to import into.
 * 
 * @author Thorsten Waertel
 * @author Roman Grutza
 */
public abstract class SelectFileAndImportMethodPage extends WizardDataTransferPage implements ValueChangeListener {

    public static final String PAGE_NAME = "SelectFileAndImportMethodPage"; //$NON-NLS-1$

    // Stored widget contents
    private static final String REPLACE_CONTENT = PAGE_NAME + ".REPLACE_CONTENT"; //$NON-NLS-1$
    private static final String FIRST_ROW_HAS_COLUMN_NAMES = PAGE_NAME + ".SELECTED_TREE_ELEMENTS"; //$NON-NLS-1$
    private static final String NULL_REPRESENTATION = PAGE_NAME + ".NULL_REPRESENTATION"; //$NON-NLS-1$

    private Text nullRepresentation;

    // edit fields
    private TextButtonField filenameField;
    private CheckboxField importIntoExistingField;
    private CheckboxField importIntoNewField;
    private CheckboxField importExistingReplaceField;
    private CheckboxField importExistingAppendField;
    private CheckboxField importIgnoreColumnHeaderRowField;
    private ComboField fileFormatField;

    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;

    // page control as defined by the wizard page class
    private Composite pageControl;

    private ITableFormat[] formats;

    private IResource initialSelection;

    private boolean importIntoExisting;

    public SelectFileAndImportMethodPage(IStructuredSelection selection) {
        super(Messages.SelectFileAndImportMethodPage_title);
        if (selection != null && selection.getFirstElement() instanceof IResource) {
            initialSelection = (IResource)selection.getFirstElement();
        }
        setPageComplete(false);
        formats = IpsPlugin.getDefault().getExternalTableFormats();
    }

    @Override
    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field == filenameField) {
            filenameChanged();
        } else if (e.field == importIntoExistingField) {
            importIntoExistingChanged();
        } else if (e.field == importIntoNewField) {
            importIntoNewChanged();
        } else if (e.field == importExistingAppendField) {
            importExistingAppendChanged();
        } else if (e.field == importExistingReplaceField) {
            importExistingReplaceChanged();
        } else if (e.field == fileFormatField) {
            // TODO rg: update preview
        }

        if (validateInput) { // don't validate during control creating!
            validatePage();
        }
        updatePageComplete();
    }

    protected void filenameChanged() {
        for (int i = 0; i < formats.length; i++) {
            ITableFormat format = formats[i];
            if (filenameField.getText().endsWith(format.getDefaultExtension())) {
                fileFormatField.getCombo().select(i);
            }
        }
    }

    protected void importIntoExistingChanged() {
        if (importIntoExistingField.getCheckbox().isChecked()) {
            importIntoNewField.getCheckbox().setChecked(false);
            importExistingReplaceField.getCheckbox().setEnabled(true);
            importExistingAppendField.getCheckbox().setEnabled(true);
            if (!importExistingAppendField.getCheckbox().isChecked() && !importIntoNewField.getCheckbox().isChecked()) {
                importExistingAppendField.getCheckbox().setChecked(true);
            }
        }
    }

    protected void importIntoNewChanged() {
        if (importIntoNewField.getCheckbox().isChecked()) {
            importIntoExistingField.getCheckbox().setChecked(false);
            importExistingReplaceField.getCheckbox().setEnabled(false);
            importExistingAppendField.getCheckbox().setEnabled(false);
        }
    }

    protected void importExistingAppendChanged() {
        importExistingReplaceField.getCheckbox().setChecked(!importExistingAppendField.getCheckbox().isChecked());
    }

    protected void importExistingReplaceChanged() {
        importExistingAppendField.getCheckbox().setChecked(!importExistingReplaceField.getCheckbox().isChecked());
    }

    /**
     * Validates the page and generates error messages if needed. Can be overridden in subclasses to
     * add specific validation logic.s
     */
    protected void validatePage() {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage(null);
        validateFilename();
        if (getErrorMessage() != null) {
            return;
        }
        validateImportMethod();
        if (getErrorMessage() != null) {
            return;
        }
        validateImportMethod();
        if (getErrorMessage() != null) {
            return;
        }
        validateFormat();
        if (getErrorMessage() != null) {
            return;
        }
        validateImportExistingMethod();
    }

    protected void validateFormat() {
        // must not be empty
        if (fileFormatField.getCombo().getSelectionIndex() == -1) {
            setErrorMessage(Messages.SelectFileAndImportMethodPage_msgMissingFileFormat);
            return;
        }
    }

    private void validateImportExistingMethod() {
        if (importIntoExistingField.getCheckbox().isChecked() && !importExistingAppendField.getCheckbox().isChecked()
                && !importExistingReplaceField.getCheckbox().isChecked()) {
            setErrorMessage(Messages.SelectFileAndImportMethodPage_msgMissingImportExistingMethod);
        }
    }

    private void validateImportMethod() {
        if (!importIntoExistingField.getCheckbox().isChecked() && !importIntoNewField.getCheckbox().isChecked()) {
            setErrorMessage(Messages.SelectFileAndImportMethodPage_msgMissingImportMethod);
        }
    }

    private void validateFilename() {
        String filename = filenameField.getText();
        if (filename.length() == 0) {
            setErrorMessage(Messages.SelectFileAndImportMethodPage_msgEmptyFilename);
            return;
        }
        File file = new File(filename);
        if (file.isDirectory()) {
            setErrorMessage(Messages.SelectFileAndImportMethodPage_msgFilenameIsDirectory);
        }
        if (!(new File(filename).exists())) {
            setErrorMessage(Messages.SelectFileAndImportMethodPage_msgFileDoesNotExist);
        }
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(Messages.SelectFileAndImportMethodPage_title);

        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        setControl(pageControl);

        Composite filenameComposite = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(filenameComposite, Messages.SelectFileAndImportMethodPage_labelName);
        filenameField = new TextButtonField(new FileSelectionControl(filenameComposite, toolkit, SWT.OPEN));
        filenameField.addChangeListener(this);

        toolkit.createFormLabel(filenameComposite, Messages.SelectFileAndImportMethodPage_labelFileFormat);
        Combo fileFormatControl = toolkit.createCombo(filenameComposite);
        for (ITableFormat format : formats) {
            fileFormatControl.add(format.getName());
        }
        fileFormatControl.select(0);
        fileFormatField = new ComboField(fileFormatControl);
        fileFormatField.addChangeListener(this);

        Composite importMethodComposite = new Composite(pageControl, SWT.NONE);
        importMethodComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout importExistingLayout = new GridLayout(1, false);
        importExistingLayout.marginWidth = 0;
        importMethodComposite.setLayout(importExistingLayout);

        Radiobutton importExistingRb = toolkit.createRadiobutton(importMethodComposite,
                getLabelForImportIntoExistingIpsObject());
        importIntoExistingField = new CheckboxField(importExistingRb);
        importIntoExistingField.addChangeListener(this);

        Group importExistingModeGroup = toolkit.createGroup(importMethodComposite, ""); //$NON-NLS-1$
        Radiobutton importExistingReplaceRb = toolkit.createRadiobutton(importExistingModeGroup,
                Messages.SelectFileAndImportMethodPage_labelImportExistingReplace);
        importExistingReplaceField = new CheckboxField(importExistingReplaceRb);
        importExistingReplaceField.addChangeListener(this);
        importExistingReplaceRb.setEnabled(false);
        Radiobutton importExistingAppendRb = toolkit.createRadiobutton(importExistingModeGroup,
                Messages.SelectFileAndImportMethodPage_labelImportExistingAppend);
        importExistingAppendField = new CheckboxField(importExistingAppendRb);
        importExistingAppendField.addChangeListener(this);
        importExistingAppendRb.setEnabled(false);

        toolkit.createVerticalSpacer(importMethodComposite, 5);
        Radiobutton importNewRb = toolkit.createRadiobutton(importMethodComposite, getLabelForImportIntoNewIpsObject());
        importIntoNewField = new CheckboxField(importNewRb);
        importIntoNewField.addChangeListener(this);
        importNewRb.setChecked(true);

        setDefaults(initialSelection);

        Checkbox ignoreColumnHeaderRow = toolkit.createCheckbox(pageControl,
                Messages.SelectFileAndImportMethodPage_labelFirstRowContainsColumnHeader);
        importIgnoreColumnHeaderRowField = new CheckboxField(ignoreColumnHeaderRow);
        importIgnoreColumnHeaderRowField.addChangeListener(this);
        importIgnoreColumnHeaderRowField.getCheckbox().setChecked(true);

        Composite additionals = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createLabel(additionals, Messages.SelectFileAndImportMethodPage_labelNullRepresentation);
        nullRepresentation = toolkit.createText(additionals);
        nullRepresentation.setText(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());

        validateInput = true;

        // init controls
        importIntoExistingField.getCheckbox().setChecked(importIntoExisting);
        importIntoExistingChanged();

        restoreWidgetValues();

        validatePage();
    }

    /**
     * Derives the default values for source folder and package from the selected resource.
     * 
     * @param selectedResource The resource that was selected in the current selection when the
     *            wizard was opened.
     */
    protected void setDefaults(IResource selectedResource) {
        if (selectedResource == null) {
            return;
        }
        if (getFormat().isValidImportSource(selectedResource.getRawLocation().toOSString())) {
            setFilename(selectedResource.getRawLocation().toOSString());
        }
        validatePage();
        updatePageComplete();
    }

    public String getFilename() {
        return filenameField.getText();
    }

    protected void setFilename(String newName) {
        filenameField.setText(newName);
    }

    public ITableFormat getFormat() {
        return formats[fileFormatField.getCombo().getSelectionIndex()];
    }

    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(filenameField.getText()) //$NON-NLS-1$
                && fileFormatField.getCombo().getSelectionIndex() != -1;
        setPageComplete(complete);
        if (getContainer() != null) {
            getContainer().updateButtons();
        }
    }

    public boolean isImportIntoExisting() {
        return importIntoExistingField.getCheckbox().isChecked();
    }

    public boolean isImportExistingReplace() {
        if (isImportIntoExisting()) {
            return importExistingReplaceField.getCheckbox().isChecked();
        }
        return false;
    }

    public boolean isImportExistingAppend() {
        if (isImportIntoExisting()) {
            return importExistingAppendField.getCheckbox().isChecked();
        }
        return false;
    }

    public boolean isImportIgnoreColumnHeaderRow() {
        return importIgnoreColumnHeaderRowField.getCheckbox().isChecked();
    }

    public String getNullRepresentation() {
        return nullRepresentation.getText();
    }

    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }
        importExistingReplaceField.getCheckbox().setChecked(settings.getBoolean(REPLACE_CONTENT));
        importIgnoreColumnHeaderRowField.getCheckbox().setChecked(settings.getBoolean(FIRST_ROW_HAS_COLUMN_NAMES));
        nullRepresentation.setText(settings.get(NULL_REPRESENTATION));

        importExistingReplaceChanged();
    }

    @Override
    public void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings == null) {
            return;
        }
        settings.put(REPLACE_CONTENT, importExistingReplaceField.getCheckbox().isChecked());
        settings.put(FIRST_ROW_HAS_COLUMN_NAMES, importIgnoreColumnHeaderRowField.getCheckbox().isChecked());
        settings.put(NULL_REPRESENTATION, nullRepresentation.getText());
    }

    @Override
    protected boolean allowNewContainerName() {
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        // Nothing to do
    }

    /**
     * Sets the checkbox import into existing table.
     */
    public void setImportIntoExisting(boolean importIntoExisting) {
        this.importIntoExisting = importIntoExisting;
    }

    /**
     * Returns the label for the widget which is enabled when the destination IPS object (which can
     * be a table content or an enum type/value) should be newly created.
     */
    protected abstract String getLabelForImportIntoNewIpsObject();

    /**
     * Returns the label for the widget which is enabled when the destination IPS object (which can
     * be a table content or an enum type/value) is an existing type.
     */
    protected abstract String getLabelForImportIntoExistingIpsObject();

}
