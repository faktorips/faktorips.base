/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.RadioButtonGroupField;
import org.faktorips.devtools.core.ui.controller.fields.ValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.ValueSetFormat;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Provides controls that allow the user to edit the value set and the default value of an
 * {@link IConfigElement}.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 * 
 * @see IConfigElement
 * @see IValueSet
 */
public class ConfigElementEditComposite extends EditPropertyValueComposite<IPolicyCmptTypeAttribute, IConfigElement> {

    private ExtensionPropertyControlFactory extPropControlFactory;

    private AnyValueSetControl valueSetControl;

    public ConfigElementEditComposite(IPolicyCmptTypeAttribute property, IConfigElement propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {

        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        extPropControlFactory = new ExtensionPropertyControlFactory(propertyValue);
        initControls();
    }

    @Override
    protected void setLayout() {
        super.setLayout();
        GridLayout clientLayout = (GridLayout)getLayout();
        clientLayout.numColumns = 2;
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) {
        if (isRangeValueEditFieldsRequired()) {
            createEditFieldsForRange(editFields);
        } else {
            if (isBooleanDatatype()) {
                createEditFieldForBoolean(editFields);
            } else {
                createEditFieldForOthers(editFields);
            }
        }
        createEditFieldsForExtensionProperties();
    }

    private void createEditFieldsForRange(List<EditField<?>> editFields) {
        createValueSetEditFieldForRange(editFields);
        createDefaultValueEditField(editFields);
    }

    private void createEditFieldForBoolean(List<EditField<?>> editFields) {
        final BooleanValueSetPMO pmo = new BooleanValueSetPMO(getPropertyValue());
        createValueSetEditFieldForBoolean(editFields, pmo);
        EditField<String> editField = createDefaultValueEditField(editFields);
        if (editField instanceof RadioButtonGroupField) {
            final RadioButtonGroupField<String> radioButtonGroupField = (RadioButtonGroupField<String>)editField;

            getBindingContext().bindEnabled(radioButtonGroupField.getButton(Boolean.TRUE.toString()), pmo,
                    BooleanValueSetPMO.PROPERTY_TRUE);
            getBindingContext().bindEnabled(radioButtonGroupField.getButton(Boolean.FALSE.toString()), pmo,
                    BooleanValueSetPMO.PROPERTY_FALSE);
            Button nullButton = radioButtonGroupField.getButton(null);
            if (nullButton != null) {
                getBindingContext().bindEnabled(nullButton, pmo, BooleanValueSetPMO.PROPERTY_NULL);
            }
        }
    }

    private void createEditFieldForOthers(List<EditField<?>> editFields) {
        createValueSetEditFieldForOtherThanRange(editFields);
        createDefaultValueEditField(editFields);
    }

    private EditField<String> createDefaultValueEditField(List<EditField<?>> editFields) {
        createLabelWithWidthHint(Messages.ConfigElementEditComposite_defaultValue);

        ValueDatatype datatype = getProperty() == null ? null : findDatatypeForDefaultValueEditField();
        if (datatype == null) {
            // No datatype found - use String as default
            datatype = Datatype.STRING;
        }
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        EditField<String> editField = controlFactory.createEditField(getToolkit(), this, datatype, getPropertyValue()
                .getValueSet(), getPropertyValue().getIpsProject());
        editFields.add(editField);
        getBindingContext().bindContent(editField, getPropertyValue(), IConfigElement.PROPERTY_VALUE);
        return editField;
    }

    private ValueDatatype findDatatypeForDefaultValueEditField() {
        ValueDatatype datatype = null;
        try {
            datatype = getProperty().findDatatype(getPropertyValue().getIpsProject());
        } catch (CoreException e) {
            // Exception while searching for datatype, log exception and use String as default
            IpsPlugin.log(e);
            datatype = Datatype.STRING;
        }
        return datatype;
    }

    private boolean isRangeValueEditFieldsRequired() {
        IValueSet valueSet = getProperty() == null ? null : getProperty().getValueSet();
        return valueSet != null ? valueSet.isRange() : getPropertyValue().getValueSet().isRange();
    }

    private boolean isBooleanDatatype() {
        String datatype = getProperty() == null ? null : getProperty().getDatatype();
        return datatype != null ? datatype.equals(Datatype.PRIMITIVE_BOOLEAN.getQualifiedName())
                || datatype.equals(Datatype.BOOLEAN.getQualifiedName()) : false;
    }

    private void createValueSetEditFieldForRange(List<EditField<?>> editFields) {
        RangeValueSet range = (RangeValueSet)getPropertyValue().getValueSet();
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                range.getValueDatatype());

        createLabelWithWidthHint(Messages.ConfigElementEditComposite_minMaxStepLabel);
        Composite rangeComposite = getToolkit().createGridComposite(this, 3, false, false);

        // Add margin so the borders of the text controls are shown
        ((GridLayout)rangeComposite.getLayout()).marginWidth = 1;
        ((GridLayout)rangeComposite.getLayout()).marginHeight = 2;

        EditField<String> lowerField = createRangeEditField(controlFactory, rangeComposite, range);
        EditField<String> upperField = createRangeEditField(controlFactory, rangeComposite, range);
        EditField<String> stepField = createRangeEditField(controlFactory, rangeComposite, range);

        editFields.add(lowerField);
        editFields.add(upperField);
        editFields.add(stepField);

        getBindingContext().bindContent(upperField, range, IRangeValueSet.PROPERTY_UPPERBOUND);
        getBindingContext().bindContent(lowerField, range, IRangeValueSet.PROPERTY_LOWERBOUND);
        getBindingContext().bindContent(stepField, range, IRangeValueSet.PROPERTY_STEP);

        getToolkit().getFormToolkit().paintBordersFor(rangeComposite);
    }

    private void createValueSetEditFieldForBoolean(final List<EditField<?>> editFields, BooleanValueSetPMO pmo) {
        createLabelWithWidthHint(Messages.ConfigElementEditComposite_valueSet);

        BooleanValueSetControl booleanValueSetControl = new BooleanValueSetControl(this, getToolkit(), getProperty(),
                getPropertyValue());
        booleanValueSetControl.setDataChangeable(getProductCmptPropertySection().isDataChangeable());

        CheckboxField trueField = new CheckboxField(booleanValueSetControl.getTrueCheckBox());
        CheckboxField falseField = new CheckboxField(booleanValueSetControl.getFalseCheckBox());
        editFields.add(trueField);
        editFields.add(falseField);

        getBindingContext().bindContent(trueField, pmo, BooleanValueSetPMO.PROPERTY_TRUE);
        getBindingContext().bindContent(falseField, pmo, BooleanValueSetPMO.PROPERTY_FALSE);
        if (booleanValueSetControl.getNullCheckBox() != null) {
            CheckboxField nullField = new CheckboxField(booleanValueSetControl.getNullCheckBox());
            editFields.add(nullField);
            getBindingContext().bindContent(nullField, pmo, BooleanValueSetPMO.PROPERTY_NULL);
        }
    }

    private EditField<String> createRangeEditField(ValueDatatypeControlFactory controlFactory,
            Composite rangeComposite,
            RangeValueSet range) {

        EditField<String> editField = controlFactory.createEditField(getToolkit(), rangeComposite,
                range.getValueDatatype(), range, range.getIpsProject());
        initTextField(editField.getControl(), 50);
        return editField;
    }

    private void initTextField(Control control, int widthHint) {
        if (control.getLayoutData() instanceof GridData) {
            ((GridData)control.getLayoutData()).widthHint = widthHint;
        }
    }

    private void createValueSetEditFieldForOtherThanRange(List<EditField<?>> editFields) {
        createLabelWithWidthHint(Messages.ConfigElementEditComposite_valueSet);

        valueSetControl = new AnyValueSetControl(this, getToolkit(), getPropertyValue(), getShell());
        valueSetControl.setDataChangeable(getProductCmptPropertySection().isDataChangeable());
        valueSetControl.setText(IpsUIPlugin.getDefault().getDatatypeFormatter()
                .formatValueSet(getPropertyValue().getValueSet()));
        ((GridData)valueSetControl.getLayoutData()).widthHint = UIToolkit.DEFAULT_WIDTH;

        ValueSetField editField = new ValueSetField(valueSetControl, ValueSetFormat.newInstance(getPropertyValue()));
        editFields.add(editField);
        getBindingContext().bindContent(editField, getPropertyValue(), IConfigElement.PROPERTY_VALUE_SET);
    }

    /**
     * Creates a {@link Label} whose width corresponds to the width of the broadest label of this
     * section.
     */
    private void createLabelWithWidthHint(String text) {
        Label label = getToolkit().createLabel(this, text);

        int width1 = getLabelWidthForText(label, Messages.ConfigElementEditComposite_defaultValue);
        int width2 = getLabelWidthForText(label, Messages.ConfigElementEditComposite_minMaxStepLabel);
        int width3 = getLabelWidthForText(label, Messages.ConfigElementEditComposite_valueSet);
        int widthHint = Math.max(Math.max(width1, width2), width3);

        label.setText(text);
        ((GridData)label.getLayoutData()).widthHint = widthHint;
    }

    private int getLabelWidthForText(Label label, String text) {
        label.setText(text);
        return label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
    }

    private void createEditFieldsForExtensionProperties() {
        extPropControlFactory.createControls(this, getToolkit(), getPropertyValue(),
                IExtensionPropertyDefinition.POSITION_TOP);
        extPropControlFactory.createControls(this, getToolkit(), getPropertyValue(),
                IExtensionPropertyDefinition.POSITION_BOTTOM);
        extPropControlFactory.bind(getBindingContext());
    }

    @Override
    protected int getFirstControlMarginHeight() {
        return isRangeValueEditFieldsRequired() ? 4 : 0;
    }

    public void setEnumValueSetProvider(IEnumValueSetProvider enumValueSetProvider) {
        // anyValueSetControl might be null in case of a Range ValueSet
        if (valueSetControl != null) {
            valueSetControl.setEnumValueSetProvider(enumValueSetProvider);
        }
    }
}
