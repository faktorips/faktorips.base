/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.editors.CategoryPmo;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.type.IProductCmptProperty;
import org.faktorips.util.ArgumentCheck;

/**
 * Edit dialog to edit the table structure usages.
 */
public class TableStructureUsageEditDialog extends IpsPartEditDialog2 {

    private TextField nameField;
    private CheckboxField mandatoryTableContentField;
    private ComboViewerField<IProductCmptCategory> categoryField;

    private ITableStructureUsage tableStructureUsage;

    private TableViewer viewer;

    private Button btnAdd;
    private Button btnRemove;
    private Button btnUp;
    private Button btnDown;

    private CategoryPmo categoryPmo;

    private ExtensionPropertyControlFactory extFactory;

    public TableStructureUsageEditDialog(ITableStructureUsage tblStructureUsage, Shell parentShell) {
        super(tblStructureUsage, parentShell, Messages.TblsStructureUsageEditDialog_title, true);
        tableStructureUsage = tblStructureUsage;
        extFactory = new ExtensionPropertyControlFactory(tblStructureUsage);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;

        TabItem page = new TabItem(folder, SWT.NONE);
        page.setText(Messages.TblsStructureUsageEditDialog_propertiesPageTtitle);
        page.setControl(createGeneralPage(folder));

        bindContent();
        nameField.getControl().setFocus();
        return folder;
    }

    private Control createGeneralPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = getToolkit().createLabelEditColumnComposite(c);

        getToolkit().createFormLabel(workArea, Messages.TblsStructureUsageEditDialog_rolenameLabel);
        Text nameText = getToolkit().createText(workArea);
        nameField = new TextField(nameText);

        getToolkit().createFormLabel(workArea, Messages.TblsStructureUsageEditDialog_contentRequiredLabel);
        Checkbox mandatoryTableContent = getToolkit().createCheckbox(workArea);
        mandatoryTableContentField = new CheckboxField(mandatoryTableContent);

        getToolkit().createFormLabel(workArea, Messages.TableStructureUsageEditDialog_categoryLabel);
        createCategoryCombo(workArea);

        createChangingOverTimeCheckbox(workArea);

        createExtFactoryControls(workArea);

        getToolkit().createVerticalSpacer(c, 10);

        Group grp = getToolkit()
                .createGridGroup(c, Messages.TblsStructureUsageEditDialog_tableStructuresGroup, 1, true);
        createTableStructureComposite(grp);

        return c;
    }

    private void createChangingOverTimeCheckbox(Composite workArea) {
        getToolkit().createFormLabel(workArea, ""); //$NON-NLS-1$
        Button changeOverTimeCheckbox = getToolkit().createButton(
                workArea,
                NLS.bind(Messages.AttributeEditDialog_changeOverTimeCheckbox, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural()),
                SWT.CHECK);
        getBindingContext().bindContent(changeOverTimeCheckbox, tableStructureUsage,
                ITableStructureUsage.PROPERTY_CHANGING_OVER_TIME);
    }

    private void createCategoryCombo(Composite workArea) {
        Combo categoryCombo = getToolkit().createCombo(workArea);
        categoryField = new ComboViewerField<>(categoryCombo, IProductCmptCategory.class);

        categoryPmo = new CategoryPmo(tableStructureUsage);
        categoryField.setInput(categoryPmo.getCategories());
        categoryField.setAllowEmptySelection(true);
        categoryField.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                IProductCmptCategory category = (IProductCmptCategory)element;
                return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(category);
            }
        });

    }

    private void createExtFactoryControls(Composite workArea) {
        if (extFactory.needsToCreateControlsFor(IExtensionPropertyDefinition.POSITION_BOTTOM)) {
            extFactory.createControls(workArea, getToolkit(), tableStructureUsage,
                    IExtensionPropertyDefinition.POSITION_BOTTOM);
        }
        extFactory.bind(getBindingContext());
    }

    private Control createTableStructureComposite(Composite cmp) {
        Composite composite = getToolkit().createGridComposite(cmp, 2, false, true);

        createTable(composite);
        createButtons(composite);

        updateButtonsEnabledState();

        return composite;
    }

    private void createTable(Composite composite) {
        Table table = new Table(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.TblsStructureUsageEditDialog_tableStructure);

        // Create the viewer and connect it to the view
        viewer = new TableViewer(table);
        viewer.setContentProvider(new TableStructureContentProvider());
        viewer.setLabelProvider(new DefaultLabelProvider());
        viewer.setInput(tableStructureUsage);

        viewer.addSelectionChangedListener($ -> updateButtonsEnabledState());
        repackTable(viewer);
    }

    /**
     * Repacks the columns in the test attribute table
     */
    private void repackTable(TableViewer tableViewer) {
        for (int i = 0, n = tableViewer.getTable().getColumnCount(); i < n; i++) {
            tableViewer.getTable().getColumn(i).pack();
        }
    }

    private void bindContent() {
        getBindingContext().bindContent(nameField, tableStructureUsage, ITableStructureUsage.PROPERTY_ROLENAME);
        getBindingContext().bindContent(mandatoryTableContentField, tableStructureUsage,
                ITableStructureUsage.PROPERTY_MANDATORY_TABLE_CONTENT);
        getBindingContext().bindContent(categoryField, categoryPmo, IProductCmptProperty.PROPERTY_CATEGORY);
    }

    private void createButtons(Composite composite) {
        Composite buttonComposite = getToolkit().createGridComposite(composite, 1, true, false);
        buttonComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        btnAdd = getToolkit().createButton(buttonComposite, Messages.TblsStructureUsageEditDialog_addButton);
        btnRemove = getToolkit().createButton(buttonComposite, Messages.TblsStructureUsageEditDialog_removeButton);
        btnUp = getToolkit().createButton(buttonComposite, Messages.TblsStructureUsageEditDialog_upButton);
        btnDown = getToolkit().createButton(buttonComposite, Messages.TblsStructureUsageEditDialog_downButton);

        btnAdd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnRemove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnUp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        btnAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addClicked();
            }
        });
        btnRemove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                removedClicked();
            }
        });
        btnUp.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveClicked(true);
            }
        });
        btnDown.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveClicked(false);
            }
        });
    }

    private void addClicked() {
        ITableStructure tableStructure;
        try {
            tableStructure = selectTableStructureByDialog();
            if (tableStructure != null) {
                tableStructureUsage.addTableStructure(tableStructure.getQualifiedName());
                refresh();
                selectInTable(tableStructure.getQualifiedName());
                viewer.getTable().setFocus();
                updateButtonsEnabledState();
            }
        } catch (IpsException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private void removedClicked() {
        ISelection selection = viewer.getSelection();
        if (selection != null && selection instanceof IStructuredSelection structSelection) {
            for (Object name : structSelection) {
                String element = (String)name;
                tableStructureUsage.removeTableStructure(element);
            }
            refresh();
            updateButtonsEnabledState();
        }
    }

    private void moveClicked(boolean up) {
        int[] newSelection = tableStructureUsage.moveTableStructure(viewer.getTable().getSelectionIndices(), up);
        refresh();
        viewer.getTable().setSelection(newSelection);
        viewer.getTable().setFocus();
        updateButtonsEnabledState();
    }

    private void selectInTable(String qualifiedName) {
        viewer.setSelection(new StructuredSelection(new String[] { qualifiedName }));
    }

    private void updateButtonsEnabledState() {
        if (!isDataChangeable()) {
            getToolkit().setDataChangeable(btnAdd, false);
            getToolkit().setDataChangeable(btnRemove, false);
            getToolkit().setDataChangeable(btnUp, false);
            getToolkit().setDataChangeable(btnDown, false);
            return;
        }
        boolean enabled = false;
        btnAdd.setEnabled(true);
        ISelection selection = viewer.getSelection();
        if (selection != null && selection instanceof IStructuredSelection && !selection.isEmpty()) {
            enabled = true;
        }
        btnRemove.setEnabled(enabled);
        btnUp.setEnabled(enabled);
        btnDown.setEnabled(enabled);
    }

    /**
     * Displays a dialog to select a table structure. Returns the selected table structure or
     * <code>null</code> if the user select nothing.
     */
    private ITableStructure selectTableStructureByDialog() {
        ElementListSelectionDialog selectDialog = new ElementListSelectionDialog(getShell(),
                new DefaultLabelProvider());
        selectDialog.setTitle(Messages.TblsStructureUsageEditDialog_selectStructurDialogTitle);
        selectDialog.setMessage(Messages.TblsStructureUsageEditDialog_selectStructurDialogMessage);

        IIpsSrcFile[] tableStructures = tableStructureUsage.getIpsProject().findIpsSrcFiles(
                IpsObjectType.TABLE_STRUCTURE);
        selectDialog.setElements(tableStructures);
        if (selectDialog.open() == Window.OK) {
            if (selectDialog.getResult().length > 0) {
                return (ITableStructure)((IIpsSrcFile)selectDialog.getResult()[0]).getIpsObject();
            }
        }
        return null;
    }

    private void refresh() {
        viewer.refresh();
        repackTable(viewer);
        updateButtonsEnabledState();
        updateTitleInTitleArea();
    }

    /**
     * Simple array content provider to provide all table structure references inside a table
     * structure usage object as String array
     */
    private static class TableStructureContentProvider extends ArrayContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            ArgumentCheck.isInstanceOf(inputElement, ITableStructureUsage.class);
            return ((ITableStructureUsage)inputElement).getTableStructures();
        }
    }

}
