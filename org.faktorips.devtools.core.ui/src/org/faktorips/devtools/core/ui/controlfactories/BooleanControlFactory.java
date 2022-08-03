/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import java.util.LinkedHashMap;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.PrimitiveBooleanDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BooleanDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.Messages;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.RadioButtonGroupField;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;

/**
 * A control factory for the data types boolean and primitive boolean. Creates radio buttons and a
 * {@link RadioButtonGroupField}.
 */
public class BooleanControlFactory extends ValueDatatypeControlFactory {

    public BooleanControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return Datatype.BOOLEAN.equals(datatype) || isPrimitiveBoolean(datatype);
    }

    @Override
    public EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        RadioButtonGroup<String> radioButtonGroup = createControls(toolkit, parent, valueSet, datatype);
        return new RadioButtonGroupField<>(radioButtonGroup);
    }

    private RadioButtonGroup<String> createControls(UIToolkit toolkit,
            Composite parent,
            IValueSet valueSet,
            ValueDatatype datatype) {
        LinkedHashMap<String, String> optionsMap = initOptions(valueSet, datatype);
        RadioButtonGroup<String> radioButtonGroup = toolkit.createRadioButtonGroup(parent, optionsMap);
        radioButtonGroup.getComposite().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        if (valueSet != null) {
            registerUpdateListener(valueSet.getValueSetOwner(), datatype, radioButtonGroup);
        }
        updateButtonEnablement(valueSet, datatype, radioButtonGroup);
        return radioButtonGroup;
    }

    private void registerUpdateListener(final IValueSetOwner valueSetOwner,
            final ValueDatatype datatype,
            final RadioButtonGroup<String> radioButtonGroup) {
        final ContentsChangeListener contentChangeListener = event -> {
            if (event.isAffected(valueSetOwner)) {
                updateButtonEnablement(valueSetOwner.getValueSet(), datatype, radioButtonGroup);
            }
        };
        valueSetOwner.getIpsModel().addChangeListener(contentChangeListener);
        radioButtonGroup.getComposite()
                .addDisposeListener(e -> valueSetOwner.getIpsModel().removeChangeListener(contentChangeListener));
    }

    protected LinkedHashMap<String, String> initOptions(IValueSet valueSet, ValueDatatype datatype) {
        LinkedHashMap<String, String> optionsMap = new LinkedHashMap<>();
        optionsMap.put(Boolean.TRUE.toString(), getTrueValue());
        optionsMap.put(Boolean.FALSE.toString(), getFalseValue());
        if (!isPrimitiveBoolean(datatype)) {
            optionsMap.put(null, getUndefinedLabel(valueSet, datatype));
        }
        return optionsMap;
    }

    private String getUndefinedLabel(IValueSet valueSet, ValueDatatype datatype) {
        if (isControlForDefaultValue(valueSet, datatype)) {
            return Messages.DefaultValueRepresentation_RadioButtonGroup;
        } else {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
    }

    private void updateButtonEnablement(IValueSet valueSet,
            ValueDatatype datatype,
            RadioButtonGroup<String> radioButtonGroup) {
        if (!isControlForDefaultValue(valueSet, datatype) && valueSet != null) {
            disableButtonIfValueNotAvailable(valueSet, radioButtonGroup, Boolean.TRUE.toString());
            disableButtonIfValueNotAvailable(valueSet, radioButtonGroup, Boolean.FALSE.toString());
        }
    }

    private void disableButtonIfValueNotAvailable(IValueSet valueSet,
            RadioButtonGroup<String> radioButtonGroup,
            String valueId) {
        Button buttonForId = radioButtonGroup.getRadioButton(valueId);
        if (buttonForId != null && !buttonForId.isDisposed()) {
            buttonForId.setEnabled(valueSetContainsId(valueSet, valueId));
        }
    }

    private boolean valueSetContainsId(IValueSet valueSet, String valueId) {
        IIpsProject ipsProject = valueSet.getIpsProject();
        return valueSet.containsValue(valueId, ipsProject);
    }

    @Override
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        return parent;
    }

    private Combo createComboControl(UIToolkit toolkit, Composite parent, ValueDatatype datatype) {
        return toolkit.createComboForBoolean(parent, isNullIncluded(datatype), getTrueValue(), getFalseValue());
    }

    private boolean isNullIncluded(ValueDatatype datatype) {
        return !isPrimitiveBoolean(datatype);
    }

    /**
     * @deprecated use {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *                 instead.
     */
    @Deprecated
    @Override
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        return createTableCellEditor(toolkit, dataType, valueSet, tableViewer, columnIndex, ipsProject);
    }

    /**
     * Creates a <code>ComboCellEditor</code> containing a <code>Combo</code> using
     * {@link #createControl(UIToolkit, Composite, ValueDatatype, IValueSet, IIpsProject)}.
     */
    @Override
    public IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        IpsCellEditor cellEditor = createComboCellEditor(toolkit, dataType, tableViewer.getTable());
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    private IpsCellEditor createComboCellEditor(UIToolkit toolkit, ValueDatatype dataType, Composite parent) {
        Combo comboControl = createComboControl(toolkit, parent, dataType);
        IpsCellEditor tableCellEditor = new ComboCellEditor(comboControl);
        // stores the boolean datatype object as data object in the combo,
        // to indicate that the to be displayed data will be mapped as boolean
        if (isPrimitiveBoolean(dataType)) {
            comboControl.setData(new PrimitiveBooleanDatatype());
        } else {
            comboControl.setData(new BooleanDatatype());
        }
        return tableCellEditor;
    }

    private boolean isPrimitiveBoolean(ValueDatatype dataType) {
        return Datatype.PRIMITIVE_BOOLEAN.equals(dataType);
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.LEFT;
    }

    public static String getTrueValue() {
        return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(Datatype.PRIMITIVE_BOOLEAN,
                Boolean.TRUE.toString());
    }

    public static String getFalseValue() {
        return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(Datatype.PRIMITIVE_BOOLEAN,
                Boolean.FALSE.toString());
    }

}
