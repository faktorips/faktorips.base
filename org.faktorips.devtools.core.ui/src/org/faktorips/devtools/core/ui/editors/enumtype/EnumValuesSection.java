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

package org.faktorips.devtools.core.ui.editors.enumtype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.faktorips.devtools.core.model.enumtype.IEnumValueContainer;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * The ui section for the enum type pages and the enum content page that contains the enum values to
 * be edited.
 * 
 * @see org.faktorips.devtools.core.ui.editors.enumtype.EnumTypeStructurePage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumValuesSection extends IpsSection {

    // The enum value container holding the enum values to be edited
    private IEnumValueContainer enumValueContainer;

    // The ui table widget
    private Table enumValuesTable;

    // The jface table viewer linking the ui table with the model data
    private TableViewer enumValuesTableViewer;

    // The names of the columns for the enum values table ui widget
    private List<String> enumValuesTableColumnNames;

    /**
     * Creates a new <code>EnumValuesSection</code> containing the enum values of the given enum
     * value container.
     * 
     * @param enumValueContainer The enum values of this enum value container will be shown.
     * @param parent The parent ui composite.
     * @param toolkit The ui toolkit that shall be used to create ui elements.
     * 
     * @throws NullPointerException If enumValueContainer is <code>null</code>.
     */
    public EnumValuesSection(IEnumValueContainer enumValueContainer, Composite parent, UIToolkit toolkit) {
        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

        ArgumentCheck.notNull(enumValueContainer);
        this.enumValueContainer = enumValueContainer;
        this.enumValuesTableColumnNames = new ArrayList<String>(4);

        initControls();
        createToolbar();

        setText(Messages.EnumValuesSection_title);
    }

    // Creates the section's toolbar
    // TODO align the toolbar at the right of the section title bar
    private void createToolbar() {
        // Create the toolbar
        Section section = getSectionControl();
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        ToolBar toolbar = toolBarManager.createControl(section);
        toolbar.setLocation(220, toolbar.getLocation().y + 1);

        // Create and add the actions to the toolbar
        toolBarManager.add(new NewEnumValueAction(enumValuesTableViewer));
        toolBarManager.add(new DeleteEnumValueAction(enumValuesTableViewer));

        // Update the toolbar with the new information
        toolBarManager.update(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        try {
            createTable(client, toolkit);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        createTableViewer();
    }

    // Creates the ui table for editing enum values
    private void createTable(Composite parent, UIToolkit toolkit) throws CoreException {
        enumValuesTable = toolkit.createTable(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.SINGLE
                | SWT.FULL_SELECTION);
        enumValuesTable.setHeaderVisible(true);
        enumValuesTable.setLinesVisible(true);

        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.widthHint = parent.getClientArea().width;
        tableGridData.heightHint = parent.getClientArea().height;
        enumValuesTable.setLayoutData(tableGridData);;

        /*
         * Create the columns based upon the enum attribute values of the first enum value if there
         * are any enum values yet. If there are no enum values yet we create the columns based on
         * the enum attributes of the enum type.
         */
        if (enumValueContainer.getNumberEnumValues() > 0) {

            // Create columns based upon enum attribute values
            IEnumValue enumValue = enumValueContainer.getEnumValues().get(0);
            List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();
            for (IEnumAttributeValue currentEnumAttributeValue : enumAttributeValues) {
                IEnumAttribute currentEnumAttribute = currentEnumAttributeValue.findEnumAttribute();
                String columnName = (currentEnumAttribute != null) ? currentEnumAttribute.getName() : "";
                TableColumn currentColumn = new TableColumn(enumValuesTable, SWT.LEFT);
                currentColumn.setText(columnName);
                currentColumn.setWidth(300);
                enumValuesTableColumnNames.add(columnName);
            }

        } else {

            // Create columns based upon enum attributes of the enum type
            IEnumType enumType = enumValueContainer.findEnumType();
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributes();
            for (IEnumAttribute currentEnumAttribute : enumAttributes) {
                String columnName = currentEnumAttribute.getName();
                TableColumn currentColumn = new TableColumn(enumValuesTable, SWT.LEFT);
                currentColumn.setText(columnName);
                currentColumn.setWidth(300);
                enumValuesTableColumnNames.add(columnName);
            }

        }
    }

    // Creates the table viewer for the enum values table widget
    private void createTableViewer() {
        // Create and setup general properties
        String[] columnNames = enumValuesTableColumnNames.toArray(new String[enumValuesTableColumnNames.size()]);
        enumValuesTableViewer = new TableViewer(enumValuesTable);
        enumValuesTableViewer.setUseHashlookup(true);
        enumValuesTableViewer.setColumnProperties(columnNames);

        // Create cell editors
        CellEditor[] cellEditors = new CellEditor[columnNames.length];
        for (int i = 0; i < cellEditors.length; i++) {
            cellEditors[i] = new TextCellEditor(enumValuesTable);
        }

        // Assign the cell editors to the table viewer
        enumValuesTableViewer.setCellEditors(cellEditors);

        // Assign the content provider and the label provider
        enumValuesTableViewer.setContentProvider(new EnumValuesContentProvider());
        enumValuesTableViewer.setLabelProvider(new EnumValuesLabelProvider());
        enumValuesTableViewer.setInput(enumValueContainer);

        // Set the cell modifier
        enumValuesTableViewer.setCellModifier(new EnumCellModifier());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    /*
     * The content provider for the enum values table viewer.
     */
    private class EnumValuesContentProvider implements IStructuredContentProvider {

        /**
         * {@inheritDoc}
         */
        public Object[] getElements(Object inputElement) {
            return enumValueContainer.getEnumValues().toArray();
        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {

        }

        /**
         * {@inheritDoc}
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

    }

    /*
     * The label provider for the enum values table viewer.
     */
    private class EnumValuesLabelProvider implements ITableLabelProvider {

        /**
         * {@inheritDoc}
         */
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public String getColumnText(Object element, int columnIndex) {
            IEnumValue enumValue = (IEnumValue)element;
            IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValues().get(columnIndex);
            return enumAttributeValue.getValue();
        }

        /**
         * {@inheritDoc}
         */
        public void addListener(ILabelProviderListener listener) {

        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {

        }

        /**
         * {@inheritDoc}
         */
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public void removeListener(ILabelProviderListener listener) {

        }

    }

    /*
     * The cell modifier for the enum values table viewer.
     */
    private class EnumCellModifier implements ICellModifier {

        /**
         * {@inheritDoc}
         */
        public boolean canModify(Object element, String property) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public Object getValue(Object element, String property) {
            if (element instanceof IEnumValue) {
                IEnumValue enumValue = (IEnumValue)element;

                int columnIndex = enumValuesTableColumnNames.indexOf(property);
                if (columnIndex != -1) {
                    return enumValue.getEnumAttributeValues().get(columnIndex).getValue();
                }
            }

            return null;
        }

        /**
         * {@inheritDoc}
         */
        public void modify(Object element, String property, Object value) {
            IEnumValue enumValue = null;
            if (element instanceof IEnumValue) {
                enumValue = (IEnumValue)element;
            }
            if (element instanceof Item) {
                enumValue = (IEnumValue)((Item)element).getData();
            } else {
                return;
            }

            int columnIndex = enumValuesTableColumnNames.indexOf(property);
            if (columnIndex != -1) {
                enumValue.getEnumAttributeValues().get(columnIndex).setValue((String)value);
                enumValuesTableViewer.refresh(true);
            }
        }

    }

}
