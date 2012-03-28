/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

public class MultiValueEditTableControl extends EditTableControl {

    private IAttributeValue attributeValue;
    private List<SingleValueHolder> valueList;
    private ValueDatatype valueDatatype;
    private IProductCmptTypeAttribute attribute;

    public MultiValueEditTableControl(Composite parent) {
        super(parent, SWT.NONE);
    }

    @Override
    public void initialize(Object modelObject, String label) {
        super.initialize(modelObject, label);
        getTable().setHeaderVisible(false);
    }

    @Override
    protected void initModelObject(Object modelObject) {
        attributeValue = (IAttributeValue)modelObject;
        try {
            attribute = getAttributeValue().findAttribute(getAttributeValue().getIpsProject());
            valueDatatype = attribute.findDatatype(getAttributeValue().getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        valueList = ((MultiValueHolder)getAttributeValue().getValueHolder()).getValue();
    }

    protected IAttributeValue getAttributeValue() {
        return attributeValue;
    }

    @Override
    protected CellEditor[] createCellEditors() {
        IpsCellEditor cellEditor = IpsUIPlugin
                .getDefault()
                .getValueDatatypeControlFactory(valueDatatype)
                .createTableCellEditor(getUiToolkit(), valueDatatype, attribute.getValueSet(), getTableViewer(), 0,
                        getAttributeValue().getIpsProject());
        return new CellEditor[] { cellEditor };
    }

    @Override
    protected ICellModifier createCellModifier() {
        return new CellModifier();
    }

    private class CellModifier implements ICellModifier {

        @Override
        public boolean canModify(Object element, String property) {
            return isDataChangeable();
        }

        @Override
        public Object getValue(Object element, String property) {
            SingleValueHolder value = (SingleValueHolder)element;
            return value.getValue();
        }

        @Override
        public void modify(Object element, String property, Object newValue) {
            SingleValueHolder value = (SingleValueHolder)element;
            value.setValue((String)newValue);
            getTableViewer().update(value, null);
        }
    }

    @Override
    protected String[] getColumnPropertyNames() {
        return new String[] { Messages.MultiValueEditTableControl_ColumnName };
    }

    @Override
    protected ILabelProvider createLabelProvider() {
        return new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((SingleValueHolder)element).getValue();
            }
        };
    }

    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new IStructuredContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing to do
            }

            @Override
            public void dispose() {
                // nothing to do
            }

            @Override
            public Object[] getElements(Object inputElement) {
                MultiValueHolder value = (MultiValueHolder)((IAttributeValue)inputElement).getValueHolder();
                return value.getValue().toArray();
            }
        };
    }

    @Override
    protected void createTableColumns(Table table) {
        TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setText(Messages.MultiValueEditTableControl_ColumnName);
    }

    @Override
    protected void addColumnLayoutData(TableLayoutComposite layouter) {
        // nothing to do
    }

    @Override
    protected Object addElement() {
        valueList.add(new SingleValueHolder(getAttributeValue(), IpsPlugin.getDefault().getIpsPreferences()
                .getNullPresentation()));
        return valueList;
    }

    @Override
    protected void removeElement(int index) {
        valueList.remove(index);
    }

    @Override
    protected void swapElements(int index1, int index2) {
        SingleValueHolder firstValue = valueList.get(index1);
        valueList.set(index1, valueList.get(index2));
        valueList.set(index2, firstValue);
    }

    public List<SingleValueHolder> getValues() {
        return valueList;
    }
}
