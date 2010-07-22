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

package org.faktorips.devtools.core.ui.table;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.util.ArgumentCheck;

/**
 * Cell modifier with bean setter and getter support.
 * 
 * @author Joerg Ortmann
 */
public class BeanTableCellModifier implements ICellModifier {
    private UIToolkit uiToolkit;

    // The table viewer this cell modifier belongs to
    private TableViewer tableViewer;

    // The parent control
    IDataChangeableReadWriteAccess parentControl;

    // the ips project in the context of which this object has been instantiated
    private IIpsProject ipsProject;

    // Listeners for the changes inside the cell
    private List<ColumnChangeListener> columnChangeListeners = new ArrayList<ColumnChangeListener>(1);

    // Cache
    private Map<Object, Object> propertyDescriptors = new HashMap<Object, Object>(4);
    private Map<Object, Object> columnIdentifers = new HashMap<Object, Object>(4);

    // Contains the delegate cell editors for each column,
    // only if at least one column specifies different cell editor for each row
    // otherwise the list is empty
    private Map<Integer, DelegateCellEditor> delegateCellEditor = new HashMap<Integer, DelegateCellEditor>(0);

    public BeanTableCellModifier(TableViewer tableViewer, IDataChangeableReadWriteAccess parentControl,
            IIpsProject ipsProject) {
        ArgumentCheck.notNull(tableViewer);
        this.tableViewer = tableViewer;
        this.parentControl = parentControl;
        ArgumentCheck.notNull(ipsProject, this);
        this.ipsProject = ipsProject;
    }

    /**
     * Inits (create and store) the cell editors for each row. The given value datatypes specifies
     * the type of the cell editor in the row.
     */
    public void initRowModifier(int column, ValueDatatype[] datatypesRows) {
        List<CellEditor> rowCellEditors = new ArrayList<CellEditor>(datatypesRows.length);
        DelegateCellEditor dm = delegateCellEditor.get(new Integer(column));
        if (dm == null) {
            // error wrong initializing of the delegate cell editor
            throw new RuntimeException("Wrong index of delegate cell editor!");
        }
        for (ValueDatatype datatypesRow : datatypesRows) {
            rowCellEditors.add(createCellEditor(uiToolkit, datatypesRow, dm.getColumn()));
        }
        dm.setCellEditors(rowCellEditors.toArray(new CellEditor[rowCellEditors.size()]));
    }

    /**
     * Initialize the modifiers with the given properties and datatypes. The datatypes specifies the
     * type of the cell editor which will adapt to the corresponding column. The cell editors will
     * be related to the column index in the same order like the datatypes (e.g. the first given
     * datatype specifies the cell editor type of the first column.
     */
    public void initModifier(UIToolkit uiToolkit, String[] properties, ValueDatatype[] datatypes) {
        ArgumentCheck.isTrue(properties.length == datatypes.length);
        this.uiToolkit = uiToolkit;
        ArrayList<CellEditor> cellEditors = new ArrayList<CellEditor>(datatypes.length);

        // create column identifier and cell editors
        for (int i = 0; i < properties.length; i++) {
            ColumnIdentifier ci = new ColumnIdentifier(properties[i], datatypes[i], i);
            // assert that a property could only be assigned to one column
            Assert.isTrue(columnIdentifers.get(properties[i]) == null, "A column modifier for property "
                    + properties[i] + " already exist!");
            columnIdentifers.put(properties[i], ci);
            // create cell modifier if enabled

            if (datatypes[i] != null) {
                CellEditor cellEditor = createCellEditor(uiToolkit, datatypes[i], i);
                cellEditors.add(cellEditor);
            } else {
                cellEditors.add(null);
            }
        }

        // connect to table viewer
        tableViewer.setColumnProperties(properties);
        tableViewer.setCellEditors(cellEditors.toArray(new CellEditor[cellEditors.size()]));
        tableViewer.setCellModifier(this);
    }

    /*
     * Returns a new cell editor for the given datatype.
     */
    private CellEditor createCellEditor(UIToolkit uiToolkit, ValueDatatype valueDatatype, int columnIndex) {
        if (valueDatatype == DelegateCellEditor.DELEGATE_VALUE_DATATYPE) {
            // the value datatype is the dummy delegate indicator
            // create a delegate cell editor which delegates to the corresponding editor depending
            // on the value datatype for each the row, the row cell editors will be created if the
            // content of the table is updated
            // @see this#initRowModifier
            DelegateCellEditor dm = new DelegateCellEditor(tableViewer, columnIndex);
            delegateCellEditor.put(new Integer(columnIndex), dm);
            return dm;
        } else {
            ValueDatatypeControlFactory factory = IpsUIPlugin.getDefault()
                    .getValueDatatypeControlFactory(valueDatatype);
            return factory.createCellEditor(uiToolkit, valueDatatype, null, tableViewer, columnIndex, ipsProject);
        }
    }

    /**
     * Adds a column change listener to the cell modifier.
     */
    public void addListener(ColumnChangeListener changeListener) {
        columnChangeListeners.add(changeListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canModify(Object element, String property) {
        return parentControl.isDataChangeable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue(Object element, String property) {
        try {
            PropertyDescriptor pd = getPropertyDescriptor(element, property);
            if (pd != null) {
                return (String)getPropertyValue(element, pd);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error resolving property methods for element " + element.getClass().getName());
        }
        throw new RuntimeException("Error resolving property method " + property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modify(Object element, String property, Object value) {
        try {
            ArgumentCheck.isInstanceOf(element, TableItem.class);
            element = ((TableItem)element).getData();
            PropertyDescriptor pd = getPropertyDescriptor(element, property);
            ColumnIdentifier ci = (ColumnIdentifier)columnIdentifers.get(property);
            if (pd != null && ci != null) {
                if (value == null || !value.equals(getPropertyValue(element, pd))) {
                    setPropertyValue(element, pd, value);
                    tableViewer.update(element, null);
                    notifyColumnChangeListener(ci, value);
                }
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error resolving property methods for element " + element.getClass().getName());
        }
        throw new RuntimeException("Error resolving property method " + property);
    }

    /*
     * Returns the property descriptor of the given property and element
     */
    private PropertyDescriptor getPropertyDescriptor(Object element, String property) throws IntrospectionException {
        PropertyDescriptor pd = (PropertyDescriptor)propertyDescriptors.get(element.getClass().getName() + property);
        if (pd != null) {
            return pd;
        }
        BeanInfo beanInfo = Introspector.getBeanInfo(element.getClass());
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertie : properties) {
            if (propertie.getName().equals(property)) {
                propertyDescriptors.put(element.getClass().getName() + property, propertie);
                return propertie;
            }
        }
        return null;
    }

    /*
     * Returns the value of the given property and element
     */
    private Object getPropertyValue(Object element, PropertyDescriptor property) {
        try {
            Method getter = property.getReadMethod();
            Object value = getter.invoke(element, new Object[0]);
            return value;
        } catch (Exception e) {
            throw new RuntimeException("Error getting property value " + property.getName());
        }
    }

    /*
     * Sets the value of the given property and element
     */
    private void setPropertyValue(Object element, PropertyDescriptor property, Object value) {
        try {
            Method setter = property.getWriteMethod();
            setter.invoke(element, new Object[] { value });
        } catch (Exception e) {
            throw new RuntimeException("Error setting property value " + property.getName(), e);
        }
    }

    private void notifyColumnChangeListener(ColumnIdentifier columnIdentifier, Object value) {
        for (ColumnChangeListener columnChangeListener : columnChangeListeners) {
            columnChangeListener.valueChanged(columnIdentifier, value);
        }
    }
}
