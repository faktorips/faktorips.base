/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.table.AbstractTraversalStrategy;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;

/**
 * Enables cell editing for builder set properties.
 * 
 * @author Roman Grutza
 */
public class BuilderSetPropertyEditingSupport extends EditingSupport {

    private IIpsProject ipsProject;
    private IIpsArtefactBuilderSetConfigModel builderSetConfigModel;

    private TableViewer viewer;

    /**
     * @param viewer The viewer for which to enable editing support
     * @param ipsProject IPS project for which to configure buildersets
     * @param builderSetConfigModel The builderset model (e.g. retrieved with
     *            <code>IIpsProject.getProperties().getBuilderSetConfig()</code>)
     * @param builderSetId Id of the builderset
     */
    public BuilderSetPropertyEditingSupport(TableViewer viewer, IIpsProject ipsProject,
            IIpsArtefactBuilderSetConfigModel builderSetConfigModel, String builderSetId) {

        super(viewer);
        ArgumentCheck.notNull(viewer);
        ArgumentCheck.notNull(ipsProject);
        ArgumentCheck.notNull(builderSetConfigModel);
        ArgumentCheck.notNull(builderSetId);

        this.viewer = viewer;
        this.ipsProject = ipsProject;
        this.builderSetConfigModel = builderSetConfigModel;
    }

    @Override
    protected boolean canEdit(Object element) {
        if (element instanceof IIpsBuilderSetPropertyDef propertyDef) {
            if (propertyDef.isAvailable(ipsProject)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        if (element instanceof IIpsBuilderSetPropertyDef propertyDef) {
            String type = propertyDef.getType();
            UIToolkit toolkit = new UIToolkit(null);
            CellEditor editor = null;

            switch (type) {
                case "string": //$NON-NLS-1$
                    editor = new TextCellEditor(viewer.getTable());
                    break;
                case "boolean": //$NON-NLS-1$
                    Combo booleanCombo = toolkit.createComboForBoolean(viewer.getTable(), false, "true", "false"); //$NON-NLS-1$ //$NON-NLS-2$
                    editor = getCellEditorInternal(booleanCombo);
                    break;
                case "integer": //$NON-NLS-1$
                    editor = new TextCellEditor(viewer.getTable());
                    editor.setValidator(value -> {
                        if (value == null || !value.toString().matches("[0-9]+")) { //$NON-NLS-1$
                            return Messages.BuilderSetPropertyEditingSupport_validatorErrorMessage;
                        }
                        return null;
                    });
                    break;
                case "enum": //$NON-NLS-1$
                case "extensionPoint": //$NON-NLS-1$
                    Combo combo = toolkit.createCombo(viewer.getTable());
                    combo.setItems(propertyDef.getDiscreteValues());
                    editor = getCellEditorInternal(combo);
                    break;
            }
            return editor;
        }
        return null;
    }

    /**
     * override traverse strategy in ComboCellEditor since we use Eclipse 3.3
     * CellViewerEditorActivationStrategy this makes it possible to jump between editable cells and
     * leave the tableViewer with CTRL-Tab or CTRL-Shift-Tab
     */
    private ComboCellEditor getCellEditorInternal(Combo combo) {
        ComboCellEditor cellEditor = new ComboCellEditor(combo);
        cellEditor.setTraversalStrategy(new AbstractTraversalStrategy(cellEditor) {
            @Override
            public void keyTraversed(TraverseEvent e) {
                if ((e.stateMask & SWT.CTRL) != 0) {
                    getCellEditor().deactivate();
                }
            }
        });
        return cellEditor;
    }

    @Override
    protected Object getValue(Object element) {
        if (element instanceof IIpsBuilderSetPropertyDef propertyDef) {
            return builderSetConfigModel.getPropertyValue(propertyDef.getName());
        }
        return null;
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (element instanceof IIpsBuilderSetPropertyDef) {
            if (value == null) {
                return;
            }
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
            builderSetConfigModel.setPropertyValue(propertyDef.getName(), value.toString(),
                    propertyDef.getDescription());
            viewer.update(element, null);
        }
    }
}
