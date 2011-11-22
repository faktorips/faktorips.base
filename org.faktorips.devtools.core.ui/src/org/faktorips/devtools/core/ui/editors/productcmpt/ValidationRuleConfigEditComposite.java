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

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.util.message.ObjectProperty;

/**
 * Allows the user to activate / deactivate a validation rule.
 * 
 * @see IValidationRuleConfig
 * 
 * @author Alexander Weickmann
 */
public final class ValidationRuleConfigEditComposite extends
        EditPropertyValueComposite<IValidationRule, IValidationRuleConfig> {

    public ValidationRuleConfigEditComposite(IValidationRule property, IValidationRuleConfig propertyValue,
            ProductCmptPropertySection propertySection, Composite parent, BindingContext bindingContext,
            UIToolkit toolkit) {

        super(property, propertyValue, propertySection, parent, bindingContext, toolkit);
        initControls();
    }

    @Override
    protected void createEditFields(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties) {
        createActiveEditField(editFieldsToEditedProperties);
    }

    private void createActiveEditField(Map<EditField<?>, ObjectProperty> editFieldsToEditedProperties) {
        Checkbox checkbox = getToolkit().createCheckbox(this);
        checkbox.setChecked(getPropertyValue().isActive());
        IpsPlugin.getDefault();
        checkbox.setText(IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(getPropertyValue()));
        CheckboxField editField = new CheckboxField(checkbox);

        editFieldsToEditedProperties.put(editField, new ObjectProperty(getPropertyValue(),
                IValidationRuleConfig.PROPERTY_ACTIVE));
    }
}
