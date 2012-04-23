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
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.MultiValueAttributeControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Provides controls that allow the user to edit the an {@link IAttributeValue}.
 * <p>
 * For attributes that do not change over time, a decoration marker is attached to the edit control.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann, Faktor Zehn AG
 * 
 * @see IAttributeValue
 */
public class AttributeValueEditComposite extends EditPropertyValueComposite<IProductCmptTypeAttribute, IAttributeValue> {

    public AttributeValueEditComposite(IProductCmptTypeAttribute property, IAttributeValue propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {

        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) throws CoreException {
        createValueEditField(editFields);
    }

    private void createValueEditField(List<EditField<?>> editFields) throws CoreException {
        ValueDatatype datatype = getProperty() == null ? null : getProperty().findDatatype(
                getPropertyValue().getIpsProject());
        IValueSet valueSet = getProperty() == null ? null : getProperty().getValueSet();
        EditField<String> editField = createEditField(datatype, valueSet);
        registerAndBindEditField(editFields, editField);
    }

    protected EditField<String> createEditField(ValueDatatype datatype, IValueSet valueSet) {
        EditField<String> editField;
        if (getPropertyValue().getValueHolder() instanceof MultiValueHolder) {
            MultiValueAttributeControl control = new MultiValueAttributeControl(this, getToolkit(), getPropertyValue(),
                    valueSet);
            editField = new TextButtonField(control);
            ValueHolderToFormattedStringWrapper wrapper = ValueHolderToFormattedStringWrapper
                    .createWrapperFor(getPropertyValue());
            getBindingContext().bindContent(editField, wrapper,
                    ValueHolderToFormattedStringWrapper.PROPERTY_FORMATTED_VALUE);
            getBindingContext().bindProblemMarker(editField, getPropertyValue().getValueHolder(),
                    MultiValueHolder.PROPERTY_VALUE);
            return editField;
        } else if (getPropertyValue().getValueHolder() instanceof SingleValueHolder) {
            ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                    datatype);
            editField = controlFactory.createEditField(getToolkit(), this, datatype, valueSet, getPropertyValue()
                    .getIpsProject());
            getBindingContext().bindContent(editField, getPropertyValue().getValueHolder(),
                    SingleValueHolder.PROPERTY_VALUE);
            return editField;
        }
        throw new RuntimeException("Illegal value holder isntance in attribute " + getProperty().getName()); //$NON-NLS-1$
    }

    protected void registerAndBindEditField(List<EditField<?>> editFields, EditField<String> editField) {
        editFields.add(editField);
        if (getProperty() != null && !getProperty().isChangingOverTime()) {
            addNotChangingOverTimeControlDecoration(editField);
        }
    }

    private void addNotChangingOverTimeControlDecoration(EditField<?> editField) {
        ControlDecoration controlDecoration = new ControlDecoration(editField.getControl(), SWT.LEFT | SWT.TOP,
                this.getParent());
        controlDecoration.setDescriptionText(NLS.bind(
                Messages.AttributeValueEditComposite_attributeNotChangingOverTimeDescription, IpsPlugin.getDefault()
                        .getIpsPreferences().getChangesOverTimeNamingConvention().getGenerationConceptNamePlural()));
        controlDecoration.setImage(IpsUIPlugin.getImageHandling().getImage(OverlayIcons.NOT_CHANGEOVERTIME_OVR_DESC));
        controlDecoration.setMarginWidth(1);
    }

}