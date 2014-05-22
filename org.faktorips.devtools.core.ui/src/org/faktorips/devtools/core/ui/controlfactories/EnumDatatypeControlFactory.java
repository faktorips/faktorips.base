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
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.EnumumerationField;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.TextCellEditor;

/**
 * A control factory for the datytpes enumeration.
 * 
 * @author Joerg Ortmann
 */
public class EnumDatatypeControlFactory extends ValueDatatypeControlFactory {

    public EnumDatatypeControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        if (datatype == null) {
            return false;
        }
        return datatype.isEnum() && !(datatype instanceof EnumTypeDatatypeAdapter);
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
        } else {
            Text text = toolkit.createText(parent);
            return new EnumumerationField(text, (EnumDatatype)datatype, getNullStringRepresentation(valueSet));
        }
    }

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

        IpsCellEditor cellEditor = createComboCellEditor(toolkit, datatype, valueSet, tableViewer.getTable());
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    private IpsCellEditor createComboCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            Composite parent) {

        Combo comboControl;
        if (valueSet instanceof IEnumValueSet) {
            comboControl = toolkit.createCombo(parent, (IEnumValueSet)valueSet, (EnumDatatype)datatype);
            return new ComboCellEditor(comboControl);
        } else {
            Text text = toolkit.createText(parent);
            initializeEnumCombo(text, (EnumDatatype)datatype, getNullStringRepresentation(valueSet));
            return new TextCellEditor(text);
        }
    }

    protected void initializeEnumCombo(Text text, EnumDatatype datatype, String nullStringRepresentation) {
        // stores the enum datatype object as data object in the combo,
        // will be used to map between the displayed text and id
        EnumumerationField enumDatatypeField = new EnumumerationField(text, datatype, nullStringRepresentation);
        text.setData(enumDatatypeField);
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.LEFT;
    }

}
