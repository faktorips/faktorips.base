/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.AbstractEnumDatatypeBasedField;
import org.faktorips.devtools.core.ui.controller.fields.EnumDatatypeField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.devtools.core.ui.table.TableTraversalStrategy;

/**
 * A control factory for the datytpes enumeration.
 * 
 * @author Joerg Ortmann
 */
public class EnumDatatypeControlFactory extends ValueDatatypeControlFactory {

    public EnumDatatypeControlFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        if (datatype == null) {
            return false;
        }
        return datatype.isEnum() && !(datatype instanceof EnumTypeDatatypeAdapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EditField createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Combo combo = toolkit.createCombo(parent);
        AbstractEnumDatatypeBasedField enumField = null;

        if (valueSet != null && valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            enumField = new EnumValueSetField(combo, (IEnumValueSet)valueSet, datatype);
        } else {
            enumField = new EnumDatatypeField(combo, (EnumDatatype)datatype);
        }

        return enumField;
    }

    /**
     * {@inheritDoc}
     */
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
    public TableCellEditor createCellEditor(UIToolkit toolkit,
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
    public TableCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        ComboCellEditor cellEditor = createComboCellEditor(toolkit, datatype, valueSet, tableViewer.getTable());
        cellEditor.setTraversalStrategy(new TableTraversalStrategy(cellEditor, tableViewer, columnIndex));
        return cellEditor;
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
    public TableCellEditor createGridCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            ColumnViewer columnViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        return createComboCellEditor(toolkit, datatype, valueSet, (Composite)columnViewer.getControl());
    }

    private ComboCellEditor createComboCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            Composite parent) {
        Combo comboControl;
        if (valueSet instanceof IEnumValueSet) {
            comboControl = toolkit.createCombo(parent, (IEnumValueSet)valueSet, (EnumDatatype)datatype);
        } else if (datatype.isEnum()) {
            comboControl = toolkit.createCombo(parent);
            initializeEnumCombo(comboControl, (EnumDatatype)datatype);
        } else {
            comboControl = toolkit.createIDCombo(parent, (EnumDatatype)datatype);
        }

        return new ComboCellEditor(comboControl);
    }

    protected void initializeEnumCombo(Combo combo, EnumDatatype datatype) {
        // stores the enum datatype object as data object in the combo,
        // will be used to map between the displayed text and id
        EnumDatatypeField enumDatatypeField = new EnumDatatypeField(combo, datatype);
        combo.setData(enumDatatypeField);
    }

}
