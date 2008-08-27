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
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.util.ArgumentCheck;

/**
 * Enables cell editing for builder set properties.
 * @author Roman Grutza
 */
public class BuilderSetPropertyEditingSupport extends EditingSupport {

    private IIpsProject ipsProject;
    private IIpsArtefactBuilderSetConfigModel builderSetConfigModel;

    private ColumnViewer viewer;
    private UIToolkit toolkit;
    

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
        this.toolkit = new UIToolkit(null);
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
            ValueDatatypeControlFactory datatypeFactory = IpsPlugin.getDefault().getValueDatatypeControlFactory(
                    propertyDef.getType());
            TableCellEditor cellEditor = datatypeFactory.createCellEditor(toolkit, propertyDef.getType(), null,
                    (TableViewer)viewer, 0);
            return cellEditor;
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
