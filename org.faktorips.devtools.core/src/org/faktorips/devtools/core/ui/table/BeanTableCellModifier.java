/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.util.ArgumentCheck;

public class BeanTableCellModifier extends ValueCellModifier  {
    // The table viewer this cell modifier belongs to
    private TableViewer tableViewer;

    private List columnChangeListeners = new ArrayList(1);
    
    private HashMap propertyDescriptors = new HashMap(4);
    
    private HashMap columnIdentifers = new HashMap(4);
    
    
    
    public BeanTableCellModifier(TableViewer tableViewer) {
        ArgumentCheck.notNull(tableViewer);
        this.tableViewer = tableViewer;
    }
    
    public void initModifier(UIToolkit uiToolkit, String[] properties, ValueDatatype[] datatypes) {
        ArgumentCheck.isTrue(properties.length == datatypes.length);
        ArrayList cellEditors = new ArrayList(datatypes.length);
        
        // create column identifier and cell editors
        for (int i = 0; i < properties.length; i++) {
            ColumnIdentifier ci = new ColumnIdentifier(properties[i], datatypes[i], i);
            // assert that a property could only be assigned to one column
            Assert.isTrue(columnIdentifers.get(properties[i]) == null, "A column modifier for property " + properties[i] + " already exist!"); //$NON-NLS-1$ //$NON-NLS-2$
            columnIdentifers.put(properties[i], ci);
            // create cell modifier if enabled
            if (datatypes[i] != null){
                CellEditor cellEditor = createCellEditor(uiToolkit, datatypes[i], i);
                cellEditors.add(cellEditor);
            } else {
                cellEditors.add(null);
            }
        }

        // connect to table viewer
        tableViewer.setColumnProperties(properties);
        tableViewer.setCellEditors((CellEditor[])cellEditors.toArray(new CellEditor[cellEditors.size()]));
        tableViewer.setCellModifier(this);
    }
    
    /**
     * Returns a new cell editor for the given datatype.
     */
    public CellEditor createCellEditor(UIToolkit uiToolkit, ValueDatatype valueDatatype, int columnIndex) {
        ValueDatatypeControlFactory factory = IpsPlugin.getDefault().getValueDatatypeControlFactory(valueDatatype);
        return factory.createCellEditor(uiToolkit, valueDatatype, null, tableViewer, columnIndex);
    }
    
    /**
     * Adds a column change listener to the cell modifier.
     */
    public void addListener(ColumnChangeListener changeListener){
        columnChangeListeners.add(changeListener);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean canModify(Object element, String property) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected String getValueInternal(Object element, String property) {
        try {
            PropertyDescriptor pd = getPropertyDescriptor(element, property);
            if (pd != null){
                return (String) getPropertyValue(element, pd);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error resolving property methods for element " + element.getClass().getName()); //$NON-NLS-1$
        }
        throw new RuntimeException("Error resolving property method " + property); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void modifyInternal(Object element, String property, Object value) {
        try {
            ArgumentCheck.isInstanceOf(element, TableItem.class);
            element = ((TableItem)element).getData();
            PropertyDescriptor pd = getPropertyDescriptor(element, property);
            ColumnIdentifier ci = (ColumnIdentifier) columnIdentifers.get(property);
            if (pd != null && ci != null) {
                if (value == null || !value.equals(getPropertyValue(element, pd))) {
                    setPropertyValue(element, pd, value);
                    tableViewer.update(element, null);
                    notifyColumnChangeListener(ci, value);
                }
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error resolving property methods for element " + element.getClass().getName()); //$NON-NLS-1$
        }
        throw new RuntimeException("Error resolving property method " + property); //$NON-NLS-1$
    }
    
    /*
     * Returns the property descriptor of the given property and element 
     */
    private PropertyDescriptor getPropertyDescriptor(Object element, String property ) throws IntrospectionException{
        PropertyDescriptor pd = (PropertyDescriptor ) propertyDescriptors.get(element.getClass().getName() + property);
        if (pd != null){
            return pd;
        }
        BeanInfo beanInfo = Introspector.getBeanInfo(element.getClass());
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].getName().equals(property)) {
                propertyDescriptors.put(element.getClass().getName() + property, properties[i]);
                return properties[i];
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
            throw new RuntimeException("Error getting property value " + property.getName()); //$NON-NLS-1$
        }
    }
    
    /*
     * Sets the value of the given property and element
     */
    private void setPropertyValue(Object element, PropertyDescriptor property, Object value) {
        try {
            Method setter = property.getWriteMethod();
            setter.invoke(element, new Object[]{value});
        } catch (Exception e) {
            throw new RuntimeException("Error setting property value " + property.getName(), e); //$NON-NLS-1$
        }
    }
    
    private void notifyColumnChangeListener(ColumnIdentifier columnIdentifier, Object value){
        for (Iterator iter = columnChangeListeners.iterator(); iter.hasNext();) {
            ((ColumnChangeListener)iter.next()).valueChanged(columnIdentifier, value);
        }
    }    
}
