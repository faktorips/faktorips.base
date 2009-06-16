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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
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
    public boolean isFactoryFor(ValueDatatype datatype) {
        return datatype instanceof EnumDatatype && !(datatype instanceof EnumTypeDatatypeAdapter);
    }

    /**
     * {@inheritDoc}
     */
    public EditField createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Combo combo = toolkit.createCombo(parent);
        AbstractEnumDatatypeBasedField enumField = null;

        if (valueSet instanceof IEnumValueSet) {
            enumField = new EnumValueSetField(combo, (IEnumValueSet)valueSet, datatype);
        } else {
            enumField = new EnumDatatypeField(combo, (EnumDatatype)datatype);
        }

        return enumField;
    }

    /**
     * {@inheritDoc}
     */
    public Control createControl(UIToolkit toolkit, Composite parent, ValueDatatype datatype, IValueSet valueSet, IIpsProject ipsProject) {
        return createEditField(toolkit, parent, datatype, valueSet, ipsProject).getControl();
    }

    /**
     * Creates a <code>ComboCellEditor</code> for the given valueset and Datatype. The created
     * CellEditor contains a <code>Combo</code> control that is filled with the corresponding values
     * from the given <code>ValueSet</code>. If the given valueset is either not an
     * <code>EnumValueSet</code> or <code>null</code> a <code>ComboCellEditor</code> is created with
     * a <code>Combo</code> control for the given <code>DataType</code>. In this case the Combo
     * contains the value IDs (not the names) of the given <code>EnumDatatype</code> {@inheritDoc}
     */
    public TableCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            ValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        Combo comboControl;
        if (valueSet instanceof IEnumValueSet) {
            comboControl = toolkit.createCombo(tableViewer.getTable(), (IEnumValueSet)valueSet, (EnumDatatype)dataType);
        } else if (dataType instanceof EnumDatatype) {
            comboControl = toolkit.createCombo(tableViewer.getTable());
            initializeEnumCombo(comboControl, (EnumDatatype)dataType);
        } else {
            comboControl = toolkit.createIDCombo(tableViewer.getTable(), (EnumDatatype)dataType);
        }

        return new ComboCellEditor(tableViewer, columnIndex, comboControl);
    }

    private void initializeEnumCombo(Combo combo, EnumDatatype datatype) {
        // stores the enum datatype object as data object in the combo,
        // will be used to map between the displayed text and id
        EnumDatatypeField enumDatatypeField = new EnumDatatypeField(combo, datatype);
        combo.setData(enumDatatypeField);
    }

}
