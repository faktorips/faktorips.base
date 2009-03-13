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

package org.faktorips.devtools.core.ui.wizards.tableimport;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.tableconversion.AbstractExternalTableFormat;
import org.faktorips.devtools.tableconversion.ITableFormat;

/**
 * Wizard Page to show a preview of the table contents to be imported.
 * 
 * @author Roman Grutza
 */
public class TablePreviewPage extends WizardPage implements ValueChangeListener {

    // max number of rows to consider for preview
    public static final int MAX_NUMBER_PREVIEW_ROWS = 8;
    
    // a leading and trailing column with no data, prevents artifacts when resizing the dialog
    private final static int TABLE_PADDING_LEFT_RIGHT = 20;

    // the resource that was selected in the workbench or null if none.
    private IResource selectedResource;

    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;

    // page control as defined by the wizard page class
    private Composite pageControl;

    // display preview for this filename using the tableFormat
    private String filename;
    private ITableFormat tableFormat;

    private TableColumn[] columns;

    private Table previewTable;

    private ITableStructure tableStructure;

    private Group configurationGroup;

    /**
     * Initializes the preview dialog using the given selection.
     * <p>
     * Note that in order to correctly display a preview one must also set a filename and a table
     * format using {@link #setFilename(String)} and
     * {@link #setTableFormat(AbstractExternalTableFormat)}.
     * 
     * @param selection A selection
     * @throws JavaModelException if the resource corresponding to the selection does not exist or
     *             an Exception happens during the access of the resource
     */
    public TablePreviewPage(IStructuredSelection selection) throws JavaModelException {
        super("Table preview");

        if (selection.getFirstElement() instanceof IResource) {
            selectedResource = (IResource)selection.getFirstElement();
        } else if (selection.getFirstElement() instanceof IJavaElement) {
            selectedResource = ((IJavaElement)selection.getFirstElement()).getCorrespondingResource();
        } else if (selection.getFirstElement() instanceof IIpsElement) {
            selectedResource = ((IIpsElement)selection.getFirstElement()).getEnclosingResource();
        } else {
            selectedResource = null;
        }
        // TODO: make "finish" appear on all pages
        //        setPageComplete(false);
        setPageComplete(true);
    }

    /**
     * Set the filename to display the preview for. Aside from setting the filename one must also
     * set the corresponding table format.
     * 
     * @see #setTableFormat(AbstractExternalTableFormat)
     */
    public void setFilename(String filename) {
        // This can not happen during construction time because the wizard pages for table import
        // are created in advance.
        this.filename = filename;
    }

    /**
     * Set the table format corresponding with the filename for which to display the preview.
     * 
     * @see #setFilename(String)
     */
    public void setTableFormat(ITableFormat tableFormat) {
        this.tableFormat = tableFormat;
    }

    /**
     * TODO
     * @param tableStructure
     */
    public void setTableStructure(ITableStructure tableStructure) {
        this.tableStructure = tableStructure;
    }

    /**
     * {@inheritDoc}
     */
    public void valueChanged(FieldValueChangedEvent e) {
        if (validateInput) { // don't validate during control creating!
            validatePage();    
        }
        updatePageComplete();
    }

    protected void updatePageComplete() {
        if (getErrorMessage()!=null) {
            setPageComplete(false);
            return;
        }
        setPageComplete(true);
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        try {
            
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle("Table preview");

        pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout pageLayout = new GridLayout(1, true);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);

        configurationGroup = toolkit.createGroup(pageControl, "Configuration");
        configurationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        GridLayout pageLayout2 = new GridLayout(1, true);
        pageControl.setLayout(pageLayout2);
        // fill the created Group
        createTableFormatConfigurationControl(configurationGroup);
        
        // create Group for Table Preview
        Group previewGroup = toolkit.createGroup(pageControl, "Preview of data being imported");
        previewTable = toolkit.createTable(previewGroup, SWT.BORDER | SWT.NO_SCROLL);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        previewTable.setLayoutData(data);
        previewTable.setHeaderVisible(true);
        previewTable.setLinesVisible(true);
        previewTable.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                updateColumnWidths();
            }
        });

        fillPreviewTableContents();
//      setDefaults(selectedResource);
        setControl(pageControl);
        
        validateInput = true;
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hook to override control creation in subclasses. Intended to be overridden.
     * @param parent
     */
    protected void createTableFormatConfigurationControl(Composite parent) {
        if (tableFormat == null) {
            // don't know which configuration pane to create, since table format was not set
            return;
        }
        tableFormat.createTableFormatConfigurationControl(parent);
    }

    
    private void fillPreviewTableContents() {
        if (this.filename == null || this.tableFormat == null) {
            return;
        }

        List preview = tableFormat.getImportTablePreview(
                tableStructure, new Path(filename), MAX_NUMBER_PREVIEW_ROWS);
        
        previewTable.removeAll();
        
        if (columns != null) {
            for (int colCount = 0; colCount < columns.length; colCount++) {
                if (columns[colCount] != null) {
                    columns[colCount].dispose();
                }
            }
        }
        
        int numberOfColumns = preview.isEmpty() ? 0 : 
            ((String[])preview.get(0)).length;
        
        
        // take empty leading and trailing column into account
        columns = new TableColumn[numberOfColumns + 2];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = new TableColumn(previewTable, SWT.LEFT, i);
        }

        for (int i = 1; i < columns.length - 1; i++) {
            columns[i].setText(tableStructure.getColumn(i - 1).getName());
        }
        
        
        for (Iterator iterator = preview.iterator(); iterator.hasNext();) {
            String[] row = (String[])iterator.next();
            TableItem item = new TableItem(previewTable, SWT.LEAD);
            for (int col = 1; col  < previewTable.getColumnCount() - 1; col++) {
                item.setText(col, row[col - 1]);
            }
        }
    }

    public void setVisible(boolean visible)  {
        super.setVisible(visible);

        if (visible) {
            createTableFormatConfigurationControl(configurationGroup); // TODO: does not belong here
            fillPreviewTableContents();
            updateColumnWidths();
        }
    }

    private void updateColumnWidths() {
        if (previewTable.getColumnCount() > 2) {

            int tableWidth = previewTable.getSize().x;
            int newColumnWidth = (tableWidth - 2 * TABLE_PADDING_LEFT_RIGHT) 
                / (previewTable.getColumnCount() - 2);

            columns[0].setWidth(TABLE_PADDING_LEFT_RIGHT);
            for (int i = 1; i < previewTable.getColumnCount() - 2; i++) {
                columns[i].setWidth(newColumnWidth);
            }
            
            // the last column containing data will grab the whole remaining space
            columns[previewTable.getColumnCount() - 2].setWidth(
                    (previewTable.getSize().x - 2 * TABLE_PADDING_LEFT_RIGHT) 
                    - (previewTable.getColumnCount() - 3) * newColumnWidth);
            columns[previewTable.getColumnCount() - 1].setWidth(TABLE_PADDING_LEFT_RIGHT);
        }
        pageControl.layout();
    }

    protected void validatePage() {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
        setErrorMessage(null);
        
        // TODO: do validation of members set through setter moethods
        
        updatePageComplete();
    }

}
