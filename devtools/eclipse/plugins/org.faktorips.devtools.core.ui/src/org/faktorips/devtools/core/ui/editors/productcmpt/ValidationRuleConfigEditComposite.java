/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;

/**
 * Provides controls that allow the user to edit an {@link IValidationRuleConfig}.
 * 
 * @since 3.6
 * 
 * @see IValidationRuleConfig
 */
public class ValidationRuleConfigEditComposite
        extends EditPropertyValueComposite<IValidationRule, IValidationRuleConfig> {

    public ValidationRuleConfigEditComposite(IValidationRule property, IValidationRuleConfig propertyValue,
            IpsSection parentSection, Composite parent, BindingContext bindingContext, UIToolkit toolkit) {

        super(property, propertyValue, parentSection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(List<EditField<?>> editFields) {
        EditField<?> editField = createActiveEditField();
        createTemplateStatusButton(editField);
        addChangingOverTimeDecorationIfRequired(editField);
        editFields.add(editField);
    }

    private CheckboxField createActiveEditField() {
        Checkbox checkbox = getToolkit().createCheckbox(this);
        checkbox.setChecked(getPropertyValue().isActive());
        checkbox.setText(IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(getPropertyValue()));
        checkbox.setToolTipText(getValidationRuleDescription());
        CheckboxField editField = new CheckboxField(checkbox);
        getBindingContext().bindContent(editField, getPropertyValue(), IValidationRuleConfig.PROPERTY_ACTIVE);
        return editField;
    }

    private String getValidationRuleDescription() {
        IValidationRule validationRule = getPropertyValue().findValidationRule(getPropertyValue().getIpsProject());
        if (validationRule != null) {
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedDescription(validationRule);
        }
        return StringUtils.EMPTY;
    }

    @Override
    protected Function<IValidationRuleConfig, String> getToolTipFormatter() {
        return PropertyValueFormatter.VALIDATION_RULE_CONFIG;
    }

}
