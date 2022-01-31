/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.valuesets;

import org.eclipse.jface.viewers.CellEditor;
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
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.controls.Messages;
import org.faktorips.devtools.core.ui.controls.TableLayoutComposite;
import org.faktorips.devtools.core.ui.editors.TableMessageHoverService;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.runtime.MessageList;

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
    private final ValueDatatype valueDatatype;
    private final IIpsProject ipsProject;
    private ContentsChangeListener changeListener;
    private DisposeListener disposeListener;

    /**
     * Constructs a EnumValueSetEditControl and handles the type of the value set that is, if the
     * value set is of the wrong type, a new EnumValueEnumSet is created
     */
    public EnumValueSetEditControl(Composite parent, ValueDatatype valueDatatype, IIpsProject ipsProject) {
        super(parent, SWT.NONE);
        this.valueDatatype = valueDatatype;
        this.ipsProject = ipsProject;
        setUpChangeListener(ipsProject);
        setUpDisposeListener(ipsProject);
    }

    private void setUpChangeListener(final IIpsProject project) {
        changeListener = event -> {
            if (event.isAffected(valueSet)) {
                refresh();
            }
        };
        project.getIpsModel().addChangeListener(changeListener);
    }

    private void setUpDisposeListener(final IIpsProject project) {
        disposeListener = $ -> project.getIpsModel().removeChangeListener(changeListener);
        addDisposeListener(disposeListener);
    }

    @Override
    public void initialize(Object modelObject, String label) {
        String resultingLabel = label == null ? Messages.EnumValueSetEditControl_titleValues : label;
        super.initialize(modelObject, resultingLabel);
        GridLayout layout = (GridLayout)getLayout();
        layout.marginHeight = 10;

        new MessageService(getTableViewer());
    }

    @Override
    protected void initModelObject(Object modelObject) {
        valueSet = (IEnumValueSet)modelObject;
    }

    public IEnumValueSet getEnumValueSet() {
        return valueSet;
    }

    public void setEnumValueSet(IEnumValueSet valueSet) {
        this.valueSet = valueSet;
        getTableViewer().setInput(valueSet);
    }

    @Override
    public ValueSetType getValueSetType() {
        return ValueSetType.ENUM;
    }

    @Override
    public boolean canEdit(IValueSet valueSet, ValueDatatype valueDatatype) {
        if (valueSet == null) {
            return false;
        }
        if (valueDatatype == null) {
            return false;
        }
        return valueSet.isEnum() && !valueDatatype.isEnum();
    }

    @Override
    public IValueSet getValueSet() {
        return getEnumValueSet();
    }

    @Override
    public void setValueSet(IValueSet newSet, ValueDatatype valueDatatype) {
        setEnumValueSet((IEnumValueSet)newSet);
    }

    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new ContentProvider();
    }

    @Override
    protected ILabelProvider createLabelProvider() {
        return new TableLabelProvider();
    }

    @Override
    protected void createTableColumns(Table table) {
        new TableColumn(table, SWT.NONE).setResizable(false);
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault()
                .getValueDatatypeControlFactory(valueDatatype);
        new TableColumn(table, ctrlFactory.getDefaultAlignment()).setResizable(false);
    }

    @Override
    protected String[] getColumnPropertyNames() {
        return new String[] { Messages.EnumValueSetEditControl_colName_1, Messages.EnumValueSetEditControl_colName_2 };
    }

    @Override
    protected void addColumnLayoutData(TableLayoutComposite layouter) {
        // message image
        layouter.addColumnData(new ColumnPixelData(15, false));
        layouter.addColumnData(new ColumnWeightData(95, true));
    }

    @Override
    protected CellEditor[] createCellEditors() {
        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault()
                .getValueDatatypeControlFactory(valueDatatype);
        CellEditor[] editors = new CellEditor[2];
        // no editor for the message image column
        editors[0] = null;
        editors[1] = ctrlFactory.createTableCellEditor(getUiToolkit(), valueDatatype, null, getTableViewer(), 1,
                ipsProject);
        return editors;
    }

    @Override
    protected ICellModifier createCellModifier() {
        return new CellModifier();
    }

    @Override
    public Object addElement() {
        String newValue = ""; //$NON-NLS-1$
        valueSet.addValue(newValue);
        return newValue;
    }

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

    private MessageList validate(Object element) throws CoreRuntimeException {
        IndexValueWrapper wrapper = (IndexValueWrapper)element;
        if (valueSet == null) {
            return new MessageList();
        }
        return valueSet.validateValue(wrapper.index, valueSet.getIpsProject());
    }

    @Override
    public Composite getComposite() {
        return this;
    }

    private class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex != 0) {
                return null;
            }
            try {
                MessageList list = validate(element);
                return IpsUIPlugin.getImageHandling().getImage(IpsProblemOverlayIcon.getOverlay(list.getSeverity()),
                        false);
            } catch (CoreRuntimeException e) {
                IpsPlugin.log(e);
                return null;
            }
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return ""; //$NON-NLS-1$
            }
            String value = IpsUIPlugin.getDefault().getDatatypeFormatter()
                    .formatValue(valueDatatype, element.toString());
            return value;
        }
    }

    private class ContentProvider implements IStructuredContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            int len = valueSet.getValues().length;
            IndexValueWrapper[] wrappers = new IndexValueWrapper[len];
            for (int i = 0; i < len; i++) {
                wrappers[i] = new IndexValueWrapper(i);
            }
            return wrappers;
        }

        @Override
        public void dispose() {
            // nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // nothing to do
        }

    }

    private class CellModifier implements ICellModifier {

        @Override
        public boolean canModify(Object element, String property) {
            return isDataChangeable();
        }

        @Override
        public Object getValue(Object element, String property) {
            IndexValueWrapper wrapper = (IndexValueWrapper)element;
            return wrapper.getValueName();
        }

        @Override
        public void modify(Object element, String property, Object value) {
            if (element == null) {
                return;
            }
            Object resultingElement = element;
            if (element instanceof Item) {
                resultingElement = ((Item)element).getData();
            }
            IndexValueWrapper wrapper = (IndexValueWrapper)resultingElement;
            wrapper.setValueName((String)value);
            getTableViewer().update(resultingElement, null);
        }
    }

    private class IndexValueWrapper {
        private int index;

        IndexValueWrapper(int index) {
            this.index = index;
        }

        String getValueName() {
            String name = valueSet.getValue(index);
            return name;
        }

        void setValueName(String newName) {
            valueSet.setValue(index, newName);
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
        protected MessageList getMessagesFor(Object element) throws CoreRuntimeException {
            return validate(element);
        }

    }

}
