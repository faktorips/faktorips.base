/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.ipsimport;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.TableFormatConfigurationCompositeFactory;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.tableconversion.ITableFormat;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Wizard Page to show a preview of a file to be imported into an IPS object (e.g. an
 * <code>ITableContents</code> or an <code>IEnumtType</code>). The page consists of a configuration
 * area which is specific to a certain <code>ITableFormat</code> and a preview area.
 * 
 * @author Roman Grutza
 */
public class ImportPreviewPage extends WizardPage implements ValueChangeListener {

    // max number of rows to consider for preview
    public static final int MAX_NUMBER_PREVIEW_ROWS = 8;

    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;

    // display preview for this filename using the table format and structure
    private String filename;
    private ITableFormat tableFormat;
    private IIpsObject structure;

    private UIToolkit toolkit = new UIToolkit(null);
    private TableColumn[] columns;

    // page control as defined by the wizard page class
    private Composite pageControl;
    private Group configurationGroup;
    private Group previewGroup;
    private Table previewTable;

    // creates configuration controls specific to a table format
    private TableFormatConfigurationCompositeFactory configCompositeFactory;

    private boolean ignoreColumnHeaderRow;

    /**
     * Displays a preview of the external file which optionally can be adjusted with custom
     * controls.
     * <p>
     * 
     * @param filename The name of the file to show the preview for.
     * @param tableFormat An <code>ITableFormat</code> instance. If custom controls are available,
     *            they are created using this table format by looking up the <code>guiClass</code>
     *            extension element of the
     *            <code>org.faktorips.devtools.core.externalTableFormat</code> extension point.
     * @param structure The structure against which to validate the file. Is an
     *            <code>IEnumType</code> instance when previewing an enum, or an
     *            <code>ITableStructure</code> instance for table previews.
     * @param ignoreColumnHeaderRow <code>true</code> if the first row contains column header and
     *            should be ignored <code>false</code> if the to be imported content contains no
     *            column header row.
     */
    public ImportPreviewPage(String filename, ITableFormat tableFormat, IIpsObject structure,
            boolean ignoreColumnHeaderRow) {
        super(Messages.ImportPreviewPage_pageName);

        ArgumentCheck.notNull(filename);
        ArgumentCheck.notNull(tableFormat);
        ArgumentCheck.notNull(structure);

        this.filename = filename;
        this.tableFormat = tableFormat;
        this.structure = structure;
        this.ignoreColumnHeaderRow = ignoreColumnHeaderRow;

        setPageComplete(false);
    }

    public void createControl(Composite parent) {
        validateInput = false;
        setTitle(Messages.ImportPreviewPage_pageTitle);

        pageControl = toolkit.createGridComposite(parent, 1, false, false);

        configurationGroup = toolkit.createGroup(pageControl, Messages.ImportPreviewPage_configurationGroupTitle);
        createTableFormatPropertiesControl(configurationGroup, toolkit);
        if (configurationGroup.getLayout() instanceof GridLayout) {
            final GridData layoutData = (GridData)configurationGroup.getLayoutData();
            layoutData.grabExcessVerticalSpace = false;
        }

        previewGroup = toolkit.createGroup(pageControl, Messages.ImportPreviewPage_livePreviewGroupTitle);
        createTable(previewGroup, toolkit);
        if (previewGroup.getLayout() instanceof GridLayout) {
            final GridData layoutData = (GridData)previewGroup.getLayoutData();
            layoutData.grabExcessVerticalSpace = true;
        }

        setControl(pageControl);
        validateInput = true;
    }

    private void createTable(Composite parent, UIToolkit toolkit) {
        previewTable = toolkit.createTable(parent, SWT.BORDER);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        previewTable.setLayoutData(data);
        previewTable.setHeaderVisible(true);
        previewTable.setLinesVisible(true);
    }

    private void createTableFormatPropertiesControl(Composite parent, UIToolkit toolkit) {
        try {
            configCompositeFactory = IpsUIPlugin.getDefault().getTableFormatPropertiesControlFactory(tableFormat);

            if (configCompositeFactory != null) {
                configCompositeFactory.createPropertyComposite(parent, toolkit);
                configCompositeFactory.addValueChangedListener(this);
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        configCompositeFactory.removeValueChangedListener(this);
    }

    @SuppressWarnings("unchecked")
    private void fillPreview() {
        previewTable.removeAll();

        if (filename == null || tableFormat == null) {
            return;
        }

        handleNumberOfTableColumnsChanged();

        String nullRepresentation = ((IpsObjectImportWizard)getWizard()).nullRepresentation;
        List<String[]> preview = Collections.emptyList();
        if (structure instanceof ITableStructure) {
            preview = tableFormat.getImportTablePreview((ITableStructure)structure, new Path(filename),
                    MAX_NUMBER_PREVIEW_ROWS, ignoreColumnHeaderRow, nullRepresentation);
        } else if (structure instanceof IEnumType) {
            preview = tableFormat.getImportEnumPreview((IEnumType)structure, new Path(filename),
                    MAX_NUMBER_PREVIEW_ROWS, ignoreColumnHeaderRow, nullRepresentation);
        }

        columns = previewTable.getColumns();
        int columnCount = columns.length;

        // set column header text
        for (int i = 0; i < columnCount; i++) {
            if (structure instanceof ITableStructure) {
                columns[i].setText(((ITableStructure)structure).getColumn(i).getName());
            } else if (structure instanceof IEnumType) {
                IEnumType type = (IEnumType)structure;
                String attributeName = type.getEnumAttributesIncludeSupertypeCopies(true).get(i).getName();
                columns[i].setText(attributeName);
            }
        }

        previewTable.removeAll();

        for (String[] row : preview) {
            TableItem item = new TableItem(previewTable, SWT.LEAD);
            for (int col = 0; col < Math.min(columnCount, row.length); col++) {
                item.setText(col, row[col]);
            }
        }

        if (previewTable.getItemCount() <= 0 && previewTable.getColumnCount() > 0) {
            String warningMsg = NLS.bind(Messages.ImportPreviewPage_warnFileInvalid, filename);
            setMessage(warningMsg, IStatus.WARNING);
        }

        refreshColumnWidths();
        pageControl.layout();
    }

    private void handleNumberOfTableColumnsChanged() {
        int previewTableColumnCount = previewTable.getColumnCount();

        int requestedColumns = 1;

        if (structure instanceof ITableStructure) {
            requestedColumns = ((ITableStructure)structure).getNumOfColumns();
        } else if (structure instanceof IEnumType) {
            requestedColumns = ((IEnumType)structure).getEnumAttributesCountIncludeSupertypeCopies(true);
        }
        if (requestedColumns < previewTableColumnCount) {
            // delete unnecessary columns in preview table
            for (int i = requestedColumns; i < previewTableColumnCount; i++) {
                previewTable.getColumn(i).dispose();
            }
        }
        if (requestedColumns > previewTableColumnCount) {
            // need to create new columns
            for (int i = previewTableColumnCount; i < requestedColumns; i++) {
                new TableColumn(previewTable, SWT.LEFT);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void valueChanged(FieldValueChangedEvent e) {
        if (validateInput) { // don't validate during control creating!
            validatePage();
        }
        updatePageComplete();
        fillPreview();
    }

    protected void updatePageComplete() {
        if (getErrorMessage() != null) {
            setPageComplete(false);
            previewTable.removeAll();
            return;
        }
        setPageComplete(true);
    }

    private void refreshColumnWidths() {
        int tableWidth = previewTable.getSize().x;
        int columnCount = previewTable.getColumnCount();
        int widthPerColumn = (tableWidth - 2) / ((columnCount == 0) ? 1 : columnCount);

        for (int i = 0; i < columnCount; i++) {
            previewTable.getColumn(i).setWidth(widthPerColumn);
        }
        pageControl.layout(true);
    }

    @Override
    public void setVisible(boolean visible) {
        fillPreview();
    }

    public void validatePage() {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage(null);
        if (!tableFormat.isValidImportSource(filename)) {
            String msg = NLS.bind(Messages.ImportPreviewPage_validationWarningInvalidFile, filename);
            setErrorMessage(msg);
        }

        if (configCompositeFactory != null) {
            MessageList ml = configCompositeFactory.validate();
            if (ml.containsErrorMsg()) {
                setErrorMessage(ml.getFirstMessage(Message.ERROR).getText());
            }
        }

        updatePageComplete();
    }

    /**
     * Reinitializes the contents of this page.
     * <p/>
     * 
     * @param filename The name of the file to show the preview for.
     * @param tableFormat An <code>ITableFormat</code> instance. If custom controls are available,
     *            they are created using this table format by looking up the <code>guiClass</code>
     *            extension element of the
     *            <code>org.faktorips.devtools.core.externalTableFormat</code> extension point.
     * @param structure The structure against which to validate the file. Is an
     *            <code>IEnumType</code> instance when previewing an enum, or an
     *            <code>ITableStructure</code> instance for table previews.
     * @param ignoreColumnHeaderRow <code>true</code> if the first row contains column header and
     *            should be ignored <code>false</code> if the to be imported content contains no
     *            column header row.
     */
    public void reinit(String filename, ITableFormat tableFormat, IIpsObject structure, boolean ignoreColumnHeaderRow) {
        ArgumentCheck.notNull(filename);
        ArgumentCheck.notNull(tableFormat);
        ArgumentCheck.notNull(structure);

        this.filename = filename;
        this.tableFormat = tableFormat;
        this.structure = structure;
        this.ignoreColumnHeaderRow = ignoreColumnHeaderRow;
    }
}
