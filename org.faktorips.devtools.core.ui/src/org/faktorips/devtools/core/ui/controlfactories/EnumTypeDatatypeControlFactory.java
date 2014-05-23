/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.EnumerationField;
import org.faktorips.devtools.core.ui.table.EditFieldCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;

/**
 * A control factory for the {@link IEnumType} which implements the {@link EnumDatatype} interface.
 * 
 * @author Peter Kuntz
 */
public class EnumTypeDatatypeControlFactory extends ValueDatatypeControlFactory {

    public EnumTypeDatatypeControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return datatype instanceof EnumTypeDatatypeAdapter;
    }

    @Override
    public EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {

        if (valueSet != null && valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            Combo combo = toolkit.createCombo(parent);
            return new EnumValueSetField(combo, (IEnumValueSet)valueSet, datatype, isControlForDefaultValue(valueSet));
        }
        EnumTypeDatatypeAdapter datatypeAdapter = (EnumTypeDatatypeAdapter)datatype;
        Text text = toolkit.createText(parent);
        return new EnumerationField(text, datatypeAdapter, getNullStringRepresentation(valueSet));
    }

    /**
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    @Override
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        return createEditField(toolkit, parent, datatype, valueSet, ipsProject).getControl();
    }

    /**
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    @Override
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        return createTableCellEditor(toolkit, datatype, valueSet, tableViewer, columnIndex, ipsProject);
    }

    /**
     * Creates a <code>ComboCellEditor</code> for the given valueset and Datatype. The created
     * CellEditor contains a <code>Combo</code> control that is filled with the corresponding values
     * from the given <code>ValueSet</code>. If the given valueset is either not an
     * <code>EnumValueSet</code> or <code>null</code> a <code>ComboCellEditor</code> is created with
     * a <code>Combo</code> control for the given <code>DataType</code>. In this case the Combo
     * contains the value IDs (not the names) of the given <code>EnumDatatype</code> {@inheritDoc}
     */
    @Override
    public IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        EditField<String> editField = createEditField(toolkit, tableViewer.getTable(), datatype, valueSet, ipsProject);
        editField.getControl().setData(editField);
        EditFieldCellEditor cellEditor = new EditFieldCellEditor(editField);
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.LEFT;
    }

}
