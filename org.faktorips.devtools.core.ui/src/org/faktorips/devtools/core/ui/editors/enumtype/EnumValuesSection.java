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
import org.eclipse.jface.action.IAction;
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
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enumtype.IEnumContent;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.enumtype.IEnumValue;
import org.faktorips.devtools.core.model.enumtype.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
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

    // Action to add new enum values
    private IAction newEnumValueAction;

    // Action to delete enum values
    private IAction deleteEnumValueAction;

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

        try {
            updateEnabledStates();
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
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
        newEnumValueAction = new NewEnumValueAction(enumValuesTableViewer);
        deleteEnumValueAction = new DeleteEnumValueAction(enumValuesTableViewer);
        toolBarManager.add(newEnumValueAction);
        toolBarManager.add(deleteEnumValueAction);

        // Update the toolbar with the new information
        toolBarManager.update(true);
    }

    /*
     * Updates the enabled states of the enum values table and its actions. The enabled states
     * result from the fact whether this enum values section is used for an enum type or for an enum
     * content and whether the (referenced) enum type defines its values in the model or not.
     */
    private void updateEnabledStates() throws CoreException {
        IEnumType enumType = enumValueContainer.findEnumType();
        boolean valuesArePartOfModel = enumType.getValuesArePartOfModel();

        if (enumValueContainer instanceof IEnumType) {
            newEnumValueAction.setEnabled(valuesArePartOfModel);
            deleteEnumValueAction.setEnabled(valuesArePartOfModel);
            enumValuesTable.setEnabled(valuesArePartOfModel);
            getSectionControl().setEnabled(valuesArePartOfModel);

        } else if (enumValueContainer instanceof IEnumContent) {
            newEnumValueAction.setEnabled(!(valuesArePartOfModel));
            deleteEnumValueAction.setEnabled(!(valuesArePartOfModel));
            enumValuesTable.setEnabled(!(valuesArePartOfModel));
            getSectionControl().setEnabled(!(valuesArePartOfModel));
        }
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
                addTableColumnToEnumValuesTable(columnName);
            }

        } else {

            // Create columns based upon enum attributes of the enum type
            IEnumType enumType = enumValueContainer.findEnumType();
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributes();
            for (IEnumAttribute currentEnumAttribute : enumAttributes) {
                String columnName = currentEnumAttribute.getName();
                addTableColumnToEnumValuesTable(columnName);
            }

        }
    }

    // Creates the table viewer for the enum values table widget
    private void createTableViewer() {
        // Create and setup general properties
        enumValuesTableViewer = new TableViewer(enumValuesTable);
        enumValuesTableViewer.setUseHashlookup(true);

        // Create cell editors and set column properties
        updateTableViewer();

        // Assign the content provider and the label provider
        enumValuesTableViewer.setContentProvider(new EnumValuesContentProvider());
        enumValuesTableViewer.setLabelProvider(new EnumValuesLabelProvider());
        enumValuesTableViewer.setInput(enumValueContainer);

        // Set the cell modifier
        enumValuesTableViewer.setCellModifier(new EnumCellModifier());
    }

    /*
     * Updates the cell editors for the enum values table viewer by creating them anew and
     * overwrites the column properties with actual data. Also refreshes the viewer with actual
     * model data.
     */
    private void updateTableViewer() {
        if (enumValuesTableViewer == null) {
            return;
        }

        String[] columnNames = enumValuesTableColumnNames.toArray(new String[enumValuesTableColumnNames.size()]);

        // Create cell editors
        CellEditor[] cellEditors = new CellEditor[columnNames.length];
        for (int i = 0; i < cellEditors.length; i++) {
            cellEditors[i] = new TextCellEditor(enumValuesTable);
        }

        // Assign the cell editors to the table viewer
        enumValuesTableViewer.setCellEditors(cellEditors);

        // Update column properties
        enumValuesTableViewer.setColumnProperties(columnNames);

        // Refresh the viewer
        enumValuesTableViewer.refresh();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

    // Adds a column to the enum values table
    private void addTableColumnToEnumValuesTable(String columnName) {
        TableColumn newColumn = new TableColumn(enumValuesTable, SWT.LEFT);
        newColumn.setText(columnName);
        newColumn.setWidth(200);
        enumValuesTableColumnNames.add(columnName);

        updateTableViewer();
    }

    // Removes the column identified by the given enum attribute from the enum values table
    private void removeTableColumnFromEnumValuesTable(IEnumAttribute enumAttribute) {
        String name = enumAttribute.getName();
        for (TableColumn currentColumn : enumValuesTable.getColumns()) {
            if (currentColumn.getText().equals(name)) {
                currentColumn.dispose();
            }
        }
        enumValuesTableColumnNames.remove(name);

        updateTableViewer();
    }

    // Updates the column in the enum values table that represents the given enum attribute
    private void updateTableColumnInEnumValuesTable(IEnumAttribute enumAttribute) {
        int index = ((IEnumType)enumAttribute.getParent()).getIndexOfEnumAttribute(enumAttribute);
        enumValuesTable.getColumn(index).setText(enumAttribute.getName());

        // Build new column names
        enumValuesTableColumnNames.clear();
        for (TableColumn currentTableColumn : enumValuesTable.getColumns()) {
            enumValuesTableColumnNames.add(currentTableColumn.getText());
        }

        updateTableViewer();
    }

    /*
     * The content provider for the enum values table viewer.
     */
    private class EnumValuesContentProvider implements IStructuredContentProvider, ContentsChangeListener {

        /**
         * Creates the content provider for the enum values table viewer and registers itself as
         * content change listener to the ips model.
         */
        public EnumValuesContentProvider() {
            enumValueContainer.getIpsModel().addChangeListener(this);
        }

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
            enumValueContainer.getIpsModel().removeChangeListener(this);
        }

        /**
         * {@inheritDoc}
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

        /**
         * {@inheritDoc}
         */
        public void contentsChanged(ContentChangeEvent event) {
            // Return if the content changed was not the enum value container to be edited
            if (!(event.getIpsSrcFile().equals(enumValueContainer.getIpsSrcFile()))) {
                return;
            }

            // Switch based upon event type
            switch (event.getEventType()) {
                case ContentChangeEvent.TYPE_PART_ADDED:
                    IIpsObjectPart addedPart = event.getPart();
                    if (addedPart != null) {
                        if (addedPart instanceof IEnumAttribute) {
                            IEnumAttribute addedEnumAttribute = (IEnumAttribute)addedPart;
                            EnumValuesSection.this.addTableColumnToEnumValuesTable(addedEnumAttribute.getName());
                        }
                    }

                    break;

                case ContentChangeEvent.TYPE_PROPERTY_CHANGED:
                    IIpsObjectPart modifiedPart = event.getPart();
                    if (modifiedPart != null) {
                        if (modifiedPart instanceof IEnumAttribute) {
                            IEnumAttribute modifiedEnumAttribute = (IEnumAttribute)modifiedPart;
                            EnumValuesSection.this.updateTableColumnInEnumValuesTable(modifiedEnumAttribute);
                        }
                    }

                    break;

                case ContentChangeEvent.TYPE_PART_REMOVED:
                    IIpsObjectPart removedPart = event.getPart();
                    if (removedPart != null) {
                        if (removedPart instanceof IEnumAttribute) {
                            IEnumAttribute removedEnumAttribute = (IEnumAttribute)removedPart;
                            enumAttributeRemoved(removedEnumAttribute);
                        }
                    }

                    break;

                case ContentChangeEvent.TYPE_WHOLE_CONTENT_CHANGED:
                    try {
                        IIpsObject changedIpsObject = event.getIpsSrcFile().getIpsObject();
                        if (changedIpsObject instanceof IEnumType) {
                            if ((IEnumType)changedIpsObject == enumValueContainer) {
                                updateEnabledStates();
                            }
                        }
                    } catch (CoreException e) {
                        IpsPlugin.logAndShowErrorDialog(e);
                    }

                    break;

                default:
                    break;
            }
        }

        // Things to do when an enum attribute has been removed from the referenced enum type
        private void enumAttributeRemoved(IEnumAttribute removedEnumAttribute) {
            // Delete referencing enum values if there are no more enum attributes
            IEnumType enumType = (IEnumType)removedEnumAttribute.getParent();
            if (enumType.getNumberEnumAttributes() == 0) {
                for (IEnumValue currentEnumValue : enumType.getEnumValues()) {
                    currentEnumValue.delete();
                }
                try {
                    for (IEnumContent currentEnumContent : enumType.findReferencingEnumContents()) {
                        for (IEnumValue currentEnumValue : currentEnumContent.getEnumValues()) {
                            currentEnumValue.delete();
                        }
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }

            // Remove the table column of the enum attribute
            EnumValuesSection.this.removeTableColumnFromEnumValuesTable(removedEnumAttribute);
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
            String columnText = null;
            if (enumValuesTableColumnNames.size() > 0) {
                IEnumValue enumValue = (IEnumValue)element;
                IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValues().get(columnIndex);
                columnText = enumAttributeValue.getValue();
            }

            return columnText;
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
