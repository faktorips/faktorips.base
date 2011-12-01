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

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Provides controls that allow the user to edit an {@link IValidationRuleConfig}.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann, Faktor Zehn AG
 * 
 * @see IValidationRuleConfig
 */
public class ValidationRuleConfigEditComposite extends
        EditPropertyValueComposite<IValidationRule, IValidationRuleConfig> {

    public ValidationRuleConfigEditComposite(IValidationRule property, IValidationRuleConfig propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {

        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) {
        createActiveEditField(editFields);
    }

    private void createActiveEditField(List<EditField<?>> editFields) {
        Checkbox checkbox = getToolkit().createCheckbox(this);
        checkbox.setChecked(getPropertyValue().isActive());
        checkbox.setText(IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(getPropertyValue()));

        CheckboxField editField = new CheckboxField(checkbox);
        editFields.add(editField);
        getBindingContext().bindContent(editField, getPropertyValue(), IValidationRuleConfig.PROPERTY_ACTIVE);
    }

}
