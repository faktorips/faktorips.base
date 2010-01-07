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

package org.faktorips.devtools.core.ui.controls.valuesets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.controls.Messages;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.util.message.MessageList;

/**
 * A control to define an enum value set by specifying the values in it.
 * <p>
 * It consists of a table control and of buttons to add and remove values and to change the order of
 * the values in the set. The control modifies the EnumValueSet and makes no temporary copy of it.
 * To implement undo operation, the EnumValueSet must be stored localy before calling
 * EnumValueSetEditControl.
 */
public class EnumValueSetEditControl extends EditTableControl implements IValueSetEditControl {

    // The value set being edited
    private IEnumValueSet valueSet;

    /**
     * Constructs a EnumValueSetEditControl and handles the type of the value set that is, if the
     * value set is of the wrong type, a new EnumValueEnumSet is created
     */
    public EnumValueSetEditControl(IEnumValueSet valueSet, Composite parent, String label) {
        super(valueSet, parent, SWT.NONE, label);
        GridLayout layout = (GridLayout)getLayout();
        layout.marginHeight = 10;
        new MessageService(getTableViewer());
    }

    /**
     * Constructs a EnumValueSetEditControl and handles the type of the value set that is, if the
     * value set is of the wrong type, a new EnumValueEnumSet is created. Labels the control with
     * default-text ("Values") in english.
     */
    public EnumValueSetEditControl(IEnumValueSet valueSet, Composite parent) {
        this(valueSet, parent, Messages.EnumValueSetEditControl_titleValues);
    }

    @Override
    protected void initModelObject(Object modelObject) {
        valueSet = (EnumValueSet)modelObject;
    }

    public IEnumValueSet getEnumValueSet() {
        return valueSet;
    }

    public void setEnumValueSet(IEnumValueSet valueSet) {
        this.valueSet = valueSet;
        getTableViewer().setInput(valueSet);
    }

    /**
     * {@inheritDoc}
     */
    public ValueSetType getValueSetType() {
        return ValueSetType.ENUM;
    }

    /**
     * {@inheritDoc}
     */
    public boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype) {
        if (valueSet == null) {
            return false;
        }
        if (valueDatatype == null) {
            return false;
        }
        return valueSet.isEnum() && !valueDatatype.isEnum();
    }

    /**
     * {@inheritDoc}
     */
    public IValueSet getValueSet() {
        return getEnumValueSet();
    }

    /**
     * {@inheritDoc}
     */
    public void setValueSet(IValueSet newSet, ValueDatatype valueDatatype) {
        setEnumValueSet((IEnumValueSet)newSet);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#createContentProvider()
     */
    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new ContentProvider();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#createLabelProvider()
     */
    @Override
    protected ILabelProvider createLabelProvider() {
        return new TableLabelProvider();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#createTableColumns(org.eclipse.swt.widgets.TableContentsGeneration)
     */
    @Override
    protected void createTableColumns(Table table) {
        new TableColumn(table, SWT.NONE).setResizable(false);
        new TableColumn(table, SWT.NONE).setResizable(false);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#getColumnPropertyNames()
     */
    @Override
    protected String[] getColumnPropertyNames() {
        return new String[] { Messages.EnumValueSetEditControl_colName_1, Messages.EnumValueSetEditControl_colName_2 };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addColumnLayoutData(TableLayoutComposite layouter) {
        layouter.addColumnData(new ColumnPixelData(10, false)); // message image
        layouter.addColumnData(new ColumnWeightData(100, true));
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#createCellEditors()
     */
    @Override
    protected UnfocusableTextCellEditor[] createCellEditors() {
        UnfocusableTextCellEditor[] editors = new UnfocusableTextCellEditor[2];
        editors[0] = null; // no editor for the message image column
        editors[1] = new UnfocusableTextCellEditor(getTable());
        return editors;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#createCellModifier()
     */
    @Override
    protected ICellModifier createCellModifier() {
        return new CellModifier();
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.ui.controls.EditTableControl#addElement()
     */
    @Override
    public Object addElement() {
        String newValue = ""; //$NON-NLS-1$
        valueSet.addValue(newValue);
        return newValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeElement(int index) {
        valueSet.removeValue(index);
        getTableViewer().refresh();
    }

    @Override
    protected void swapElements(int index1, int index2) {
        String name1 = valueSet.getValue(index1);
        String name2 = valueSet.getValue(index2);
        valueSet.setValue(index1, name2);
        valueSet.setValue(index2, name1);
    }

    private MessageList validate(Object element) throws CoreException {
        IndexValueWrapper wrapper = (IndexValueWrapper)element;
        if (valueSet == null) {
            return new MessageList();
        }
        return valueSet.validateValue(wrapper.index, valueSet.getIpsProject());
    }

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex != 0) {
                return null;
            }
            try {
                MessageList list = validate(element);
                return IpsUIPlugin.getImageHandling().getImage(IpsProblemOverlayIcon.getOverlay(list.getSeverity()));
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return null;
            }
        }

        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return ""; //$NON-NLS-1$
            }
            return element.toString();
        }
    }

    private class ContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            int len = valueSet.getValues().length;
            IndexValueWrapper[] wrappers = new IndexValueWrapper[len];
            for (int i = 0; i < len; i++) {
                wrappers[i] = new IndexValueWrapper(i);
            }
            return wrappers;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private class CellModifier implements ICellModifier {

        public boolean canModify(Object element, String property) {
            return isDataChangeable();
        }

        public Object getValue(Object element, String property) {
            IndexValueWrapper wrapper = (IndexValueWrapper)element;
            return wrapper.getValueName();
        }

        public void modify(Object element, String property, Object value) {
            if (element == null) {
                return;
            }
            if (element instanceof Item) {
                element = ((Item)element).getData();
            }
            IndexValueWrapper wrapper = (IndexValueWrapper)element;
            wrapper.setValueName((String)value);
            getTableViewer().update(element, null);
        }
    }

    private class IndexValueWrapper {
        private int index;

        IndexValueWrapper(int index) {
            this.index = index;
        }

        String getValueName() {
            String name = valueSet.getValue(index);
            if (name == null) {
                name = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            }
            return name;
        }

        void setValueName(String newName) {
            if (newName.equals(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation())) {
                valueSet.setValue(index, null);
            } else {
                valueSet.setValue(index, newName);
            }
        }

        @Override
        public String toString() {
            return getValueName();
        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IndexValueWrapper)) {
                return false;
            }
            return index == ((IndexValueWrapper)o).index;
        }
    }

    private class MessageService extends TableMessageHoverService {

        public MessageService(TableViewer viewer) {
            super(viewer);
        }

        @Override
        protected MessageList getMessagesFor(Object element) throws CoreException {
            return validate(element);
        }

    }

}
