/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import com.google.common.base.Function;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
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
        EditField<?> editField = createActiveEditField();
        createTemplateStatusButton(editField);
        editFields.add(editField);
    }

    private CheckboxField createActiveEditField() {
        Checkbox checkbox = getToolkit().createCheckbox(this);
        checkbox.setChecked(getPropertyValue().isActive());
        checkbox.setText(IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(getPropertyValue()));
        checkbox.setToolTipText(getValidationRuleDescription());
        CheckboxField editField = new CheckboxField(checkbox);
        getBindingContext().bindContent(editField, getPropertyValue(), IValidationRuleConfig.PROPERTY_ACTIVE);
        return editField;
    }

    private String getValidationRuleDescription() {
        try {
            IValidationRule validationRule = getPropertyValue().findValidationRule(getPropertyValue().getIpsProject());
            if (validationRule != null) {
                return IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(validationRule);
            }
        } catch (CoreException ex) {
            throw new CoreRuntimeException(ex);
        }
        return StringUtils.EMPTY;
    }

    @Override
    protected Function<IValidationRuleConfig, String> getToolTipFormatter() {
        return new Function<IValidationRuleConfig, String>() {

            @Override
            public String apply(IValidationRuleConfig ruleConfig) {
                if (ruleConfig == null) {
                    return StringUtils.EMPTY;
                }
                if (ruleConfig.isActive()) {
                    return Messages.ValidationRuleConfigEditComposite_activated;
                } else {
                    return Messages.ValidationRuleConfigEditComposite_deactivated;
                }
            }
        };
    }

}
