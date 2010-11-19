/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.table.AbstractTraversalStrategy;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
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
        if (element instanceof IIpsBuilderSetPropertyDef) {
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
            if (propertyDef.isAvailable(ipsProject)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        if (element instanceof IIpsBuilderSetPropertyDef) {
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
            String type = propertyDef.getType();
            UIToolkit toolkit = new UIToolkit(null);
            CellEditor editor = null;

            if (type.equals("string")) { //$NON-NLS-1$
                editor = new TextCellEditor(viewer.getTable());
            } else if (type.equals("boolean")) { //$NON-NLS-1$
                Combo combo = toolkit.createComboForBoolean(viewer.getTable(), false, "true", "false"); //$NON-NLS-1$ //$NON-NLS-2$
                editor = getCellEditorInternal(combo);
            } else if (type.equals("integer")) { //$NON-NLS-1$
                editor = new TextCellEditor(viewer.getTable());
                editor.setValidator(new ICellEditorValidator() {
                    @Override
                    public String isValid(Object value) {
                        if (value == null || !value.toString().matches("[0-9]+")) { //$NON-NLS-1$
                            return Messages.BuilderSetPropertyEditingSupport_validatorErrorMessage;
                        }
                        return null;
                    }
                });
            } else if (type.equals("enum") || type.equals("extensionPoint")) { //$NON-NLS-1$ //$NON-NLS-2$
                DefaultEnumType propertyDefEnum = new DefaultEnumType("builderSetPropertyType", DefaultEnumValue.class); //$NON-NLS-1$
                Object[] discreteValues = propertyDef.getDiscreteValues();
                DefaultEnumValue[] values = new DefaultEnumValue[discreteValues.length];
                for (int i = 0; i < discreteValues.length; i++) {
                    values[i] = new DefaultEnumValue(propertyDefEnum, (String)discreteValues[i]);
                }
                Combo combo = toolkit.createCombo(viewer.getTable(), values);
                editor = getCellEditorInternal(combo);
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
        if (element instanceof IIpsBuilderSetPropertyDef) {
            IIpsBuilderSetPropertyDef propertyDef = (IIpsBuilderSetPropertyDef)element;
            String propertyValue = builderSetConfigModel.getPropertyValue(propertyDef.getName());
            return propertyValue;
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
