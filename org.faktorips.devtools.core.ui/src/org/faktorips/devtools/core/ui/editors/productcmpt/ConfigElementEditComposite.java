/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.TimedEnumDatatypeUtil;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetFilter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.CompositeUIController;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.PreviewTextButtonField;
import org.faktorips.util.message.ObjectProperty;

/**
 * Allows the user to edit the value set and the default value of a configuration element.
 * 
 * @see IValueSet
 * @see IConfigElement
 * 
 * @author Alexander Weickmann
 */
public final class ConfigElementEditComposite extends
        EditPropertyValueComposite<IPolicyCmptTypeAttribute, IConfigElement> {

    public ConfigElementEditComposite(IPolicyCmptTypeAttribute property, IConfigElement propertyValue,
            ProductCmptPropertySection propertySection, Composite parent, CompositeUIController uiMasterController,
            UIToolkit toolkit) {

        super(property, propertyValue, propertySection, parent, uiMasterController, toolkit);
        initControls();
    }

    @Override
    protected void setLayout() {
        super.setLayout();
        GridLayout clientLayout = (GridLayout)getLayout();
        clientLayout.numColumns = 2;
    }

    @Override
    protected void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties) {
        createValueSetEditField(editFieldsToObjectProperties);
        createDefaultValueEditField(editFieldsToObjectProperties);
    }

    private void createDefaultValueEditField(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties) {
        createLabelWithWidthHint(Messages.ConfigElementEditComposite_defaultValue);

        ValueDatatype datatype = findDatatypeForDefaultValueEditField();
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);

        IValueSet sourceSet = ValueSetFilter.filterValueSet(getPropertyValue().getValueSet(), datatype, getGeneration()
                .getValidFrom(), getGeneration().getValidTo(),
                TimedEnumDatatypeUtil.ValidityCheck.SOME_TIME_OF_THE_PERIOD);
        EditField<String> editField = controlFactory.createEditField(getToolkit(), this, datatype, sourceSet,
                getGeneration().getIpsProject());

        editFieldsToObjectProperties.put(editField, new ObjectProperty(getPropertyValue(),
                IConfigElement.PROPERTY_VALUE));
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
        if (datatype == null) {
            // No datatype found - use String as default
            datatype = Datatype.STRING;
        }
        return datatype;
    }

    private void createValueSetEditField(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties) {
        if (areRangeValueEditFieldsRequired()) {
            createValueSetEditFieldForRange(editFieldsToObjectProperties);
        } else {
            createValueSetEditFieldForOtherThanRange(editFieldsToObjectProperties);
        }
    }

    private boolean areRangeValueEditFieldsRequired() {
        return getProperty().getValueSet() != null ? getProperty().getValueSet().isRange() : getPropertyValue()
                .getValueSet().isRange();
    }

    @Override
    protected int getFirstControlMarginHeight() {
        return areRangeValueEditFieldsRequired() ? 4 : 0;
    }

    private void createValueSetEditFieldForRange(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties) {
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

        getToolkit().getFormToolkit().paintBordersFor(rangeComposite);

        editFieldsToObjectProperties.put(upperField, new ObjectProperty(range, IRangeValueSet.PROPERTY_UPPERBOUND));
        editFieldsToObjectProperties.put(lowerField, new ObjectProperty(range, IRangeValueSet.PROPERTY_LOWERBOUND));
        editFieldsToObjectProperties.put(stepField, new ObjectProperty(range, IRangeValueSet.PROPERTY_STEP));
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

    private void createValueSetEditFieldForOtherThanRange(Map<EditField<?>, ObjectProperty> editFieldsToObjectProperties) {
        createLabelWithWidthHint(Messages.ConfigElementEditComposite_valueSet);

        AnyValueSetControl valueSetControl = new AnyValueSetControl(this, getToolkit(), getPropertyValue(), getShell(),
                getController());
        valueSetControl.setDataChangeable(getProductCmptPropertySection().isDataChangeable());
        valueSetControl.setText(IpsUIPlugin.getDefault().getDatatypeFormatter()
                .formatValueSet(getPropertyValue().getValueSet()));
        ((GridData)valueSetControl.getLayoutData()).widthHint = UIToolkit.DEFAULT_WIDTH;

        PreviewTextButtonField editField = new PreviewTextButtonField(valueSetControl);

        editFieldsToObjectProperties.put(editField, new ObjectProperty(getPropertyValue(),
                IConfigElement.PROPERTY_VALUE_SET));
    }

    /**
     * Creates a label so that it's width corresponds to the width of the broadest label of this
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

    private IProductCmptGeneration getGeneration() {
        return getPropertyValue().getProductCmptGeneration();
    }

}
