/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.TextCellEditor;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;

/**
 * Enables cell editing for builder set properties.
 * @author Roman Grutza
 */
public class BuilderSetPropertyEditingSupport extends EditingSupport {

    private IIpsProject ipsProject;
    private IIpsArtefactBuilderSetConfigModel builderSetConfigModel;

    private TableViewer viewer;

    /**
     * @param viewer The viewer for which to enable editing support
     * @param ipsProject IPS project for which to configure buildersets 
     * @param builderSetConfigModel The builderset model (e.g. retrieved with <code>IIpsProject.getProperties().getBuilderSetConfig()</code>)
     * @param builderSetId Id of the builderset
     */
    public BuilderSetPropertyEditingSupport(TableViewer viewer, 
            IIpsProject ipsProject,  
            IIpsArtefactBuilderSetConfigModel builderSetConfigModel, 
            String builderSetId) {
        super(viewer);
        ArgumentCheck.notNull(viewer);
        ArgumentCheck.notNull(ipsProject);
        ArgumentCheck.notNull(builderSetConfigModel);
        ArgumentCheck.notNull(builderSetId);
        
        this.viewer = viewer;
        this.ipsProject = ipsProject;
        this.builderSetConfigModel = builderSetConfigModel;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean canEdit(Object element) {
        if (element instanceof IIpsBuilderSetPropertyDef) {
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
            if (propertyDef.isAvailable(ipsProject)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    protected CellEditor getCellEditor(Object element) {
        if (element instanceof IIpsBuilderSetPropertyDef) {
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
            String type = propertyDef.getType();
            UIToolkit toolkit = new UIToolkit(null);
            CellEditor editor = null;
            
            if(type.equals("string")){
                editor = new TextCellEditor(viewer, 0, new Text(viewer.getTable(), SWT.NONE));
            } else if (type.equals("boolean")) {
                Combo combo = toolkit.createComboForBoolean(viewer.getTable(), false, "true", "false");
                editor = new ComboCellEditor(viewer, 0, combo);
            } else if (type.equals("integer")) {
                editor = new TextCellEditor(viewer, 0, new Text(viewer.getTable(), SWT.NONE));
            } else if (type.equals("enum") || type.equals("extensionPoint")) {
                DefaultEnumType propertyDefEnum = new DefaultEnumType("builderSetPropertyType", AttributeType.class);
                Object[] discreteValues = propertyDef.getDiscreteValues();
                DefaultEnumValue[] values = new DefaultEnumValue[discreteValues.length];
                for (int i = 0; i < discreteValues.length; i++) {
                    values[i] = new DefaultEnumValue(propertyDefEnum, (String) discreteValues[i]);
                }
                Combo combo = toolkit.createCombo(viewer.getTable(), values);
                editor = new ComboCellEditor(viewer, 0, combo);
            }
            return editor;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected Object getValue(Object element) {
        if (element instanceof IIpsBuilderSetPropertyDef) {
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef) element;
            return builderSetConfigModel.getPropertyValue(propertyDef.getName());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected void setValue(Object element, Object value) {
        // TODO validation?
        if (element instanceof IIpsBuilderSetPropertyDef) {
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef) element;
            builderSetConfigModel.setPropertyValue(propertyDef.getName(), (String)value);
            viewer.update(element, null);
        }
    }
}
