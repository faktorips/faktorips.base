/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.editors.pctype.AttributeEditDialog.RuleUIModel;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.junit.Before;
import org.junit.Test;

public class AttributeEditDialogRuleUIModelTest extends AbstractIpsPluginTest {

    private IPolicyCmptTypeAttribute attribute;
    private RuleUIModel ruleUIModel;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject ipsProject = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "Policy");
        attribute = policyCmptType.newPolicyCmptTypeAttribute("attribute");
        ruleUIModel = new RuleUIModel(attribute);
    }

    @Test
    public void testSetValidationRule() {
        IValidationRule rule = attribute.createValueSetRule();
        List<PropertyChangeEvent> events = new ArrayList<>();
        ruleUIModel.addPropertyChangeListener(events::add);

        ruleUIModel.setValidationRule(rule);

        assertThat(ruleUIModel.getValidationRule(), is(sameInstance(rule)));
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getPropertyName(), is(RuleUIModel.PROPERTY_ENABLED));
        assertThat(events.get(0).getOldValue(), is(false));
        assertThat(events.get(0).getNewValue(), is(true));
        assertThat(events.get(1).getPropertyName(), is(RuleUIModel.PROPERTY_VALIDATION_RULE));
        assertThat(events.get(1).getOldValue(), is(nullValue()));
        assertThat(events.get(1).getNewValue(), is(rule));
    }

    @Test
    public void testFireRuleChange() {
        IValidationRule rule = attribute.createValueSetRule();
        ruleUIModel.setValidationRule(rule);
        List<PropertyChangeEvent> events = new ArrayList<>();
        ruleUIModel.addPropertyChangeListener(events::add);

        ruleUIModel.fireRuleChange();

        assertThat(events.size(), is(1));
        assertThat(events.get(0).getPropertyName(), is(RuleUIModel.PROPERTY_VALIDATION_RULE));
        assertThat(events.get(0).getOldValue(), is(rule));
        assertThat(events.get(0).getNewValue(), is(rule));
    }

    @Test
    public void testSetEnabled_True() {
        List<PropertyChangeEvent> events = new ArrayList<>();
        ruleUIModel.addPropertyChangeListener(events::add);

        ruleUIModel.setEnabled(true);

        assertThat(ruleUIModel.isEnabled(), is(true));
        IValidationRule rule = ruleUIModel.getValidationRule();
        assertThat(rule, is(notNullValue()));
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getPropertyName(), is(RuleUIModel.PROPERTY_ENABLED));
        assertThat(events.get(0).getOldValue(), is(false));
        assertThat(events.get(0).getNewValue(), is(true));
        assertThat(events.get(1).getPropertyName(), is(RuleUIModel.PROPERTY_VALIDATION_RULE));
        assertThat(events.get(1).getOldValue(), is(nullValue()));
        assertThat(events.get(1).getNewValue(), is(rule));
    }

    @Test
    public void testSetEnabled_True_DisablesGeneric() {
        attribute.setGenericValidationEnabled(true);
        List<PropertyChangeEvent> events = new ArrayList<>();
        ruleUIModel.addPropertyChangeListener(events::add);

        ruleUIModel.setEnabled(true);

        assertThat(ruleUIModel.isEnabled(), is(true));
        IValidationRule rule = ruleUIModel.getValidationRule();
        assertThat(rule, is(notNullValue()));
        assertThat(attribute.isGenericValidationEnabled(), is(false));
        assertThat(events.size(), is(3));
        assertThat(events.get(0).getPropertyName(), is(RuleUIModel.PROPERTY_GENERIC));
        assertThat(events.get(0).getOldValue(), is(true));
        assertThat(events.get(0).getNewValue(), is(false));
        assertThat(events.get(1).getPropertyName(), is(RuleUIModel.PROPERTY_ENABLED));
        assertThat(events.get(1).getOldValue(), is(false));
        assertThat(events.get(1).getNewValue(), is(true));
        assertThat(events.get(2).getPropertyName(), is(RuleUIModel.PROPERTY_VALIDATION_RULE));
        assertThat(events.get(2).getOldValue(), is(nullValue()));
        assertThat(events.get(2).getNewValue(), is(rule));
    }

    @Test
    public void testSetEnabled_False() {
        ruleUIModel.setEnabled(true);
        List<PropertyChangeEvent> events = new ArrayList<>();
        ruleUIModel.addPropertyChangeListener(events::add);
        IValidationRule rule = ruleUIModel.getValidationRule();

        ruleUIModel.setEnabled(false);

        assertThat(ruleUIModel.isEnabled(), is(false));
        assertThat(ruleUIModel.getValidationRule(), is(nullValue()));
        assertThat(events.size(), is(2));
        assertThat(events.get(0).getPropertyName(), is(RuleUIModel.PROPERTY_ENABLED));
        assertThat(events.get(0).getOldValue(), is(true));
        assertThat(events.get(0).getNewValue(), is(false));
        assertThat(events.get(1).getPropertyName(), is(RuleUIModel.PROPERTY_VALIDATION_RULE));
        assertThat(events.get(1).getOldValue(), is(rule));
        assertThat(events.get(1).getNewValue(), is(nullValue()));
    }

    @Test
    public void testIsEnabled() {
        assertThat(ruleUIModel.isEnabled(), is(false));

        IValidationRule rule = attribute.createValueSetRule();
        ruleUIModel.setValidationRule(rule);

        assertThat(ruleUIModel.isEnabled(), is(true));
    }

    @Test
    public void testIsGeneric() {
        assertThat(ruleUIModel.isGeneric(), is(false));

        attribute.setGenericValidationEnabled(true);

        assertThat(ruleUIModel.isGeneric(), is(true));
    }

    @Test
    public void testSetGeneric_True() {
        List<PropertyChangeEvent> events = new ArrayList<>();
        ruleUIModel.addPropertyChangeListener(events::add);

        ruleUIModel.setGeneric(true);

        assertThat(attribute.isGenericValidationEnabled(), is(true));
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getPropertyName(), is(RuleUIModel.PROPERTY_GENERIC));
        assertThat(events.get(0).getOldValue(), is(false));
        assertThat(events.get(0).getNewValue(), is(true));
    }

    @Test
    public void testSetGeneric_True_DisablesRule() {
        ruleUIModel.setEnabled(true);
        List<PropertyChangeEvent> events = new ArrayList<>();
        ruleUIModel.addPropertyChangeListener(events::add);
        IValidationRule rule = ruleUIModel.getValidationRule();

        ruleUIModel.setGeneric(true);

        assertThat(attribute.isGenericValidationEnabled(), is(true));
        assertThat(events.size(), is(3));
        assertThat(events.get(0).getPropertyName(), is(RuleUIModel.PROPERTY_GENERIC));
        assertThat(events.get(0).getOldValue(), is(false));
        assertThat(events.get(0).getNewValue(), is(true));
        assertThat(ruleUIModel.isEnabled(), is(false));
        assertThat(ruleUIModel.getValidationRule(), is(nullValue()));
        assertThat(events.get(1).getPropertyName(), is(RuleUIModel.PROPERTY_ENABLED));
        assertThat(events.get(1).getOldValue(), is(true));
        assertThat(events.get(1).getNewValue(), is(false));
        assertThat(events.get(2).getPropertyName(), is(RuleUIModel.PROPERTY_VALIDATION_RULE));
        assertThat(events.get(2).getOldValue(), is(rule));
        assertThat(events.get(2).getNewValue(), is(nullValue()));
    }

    @Test
    public void testSetGeneric_False() {
        attribute.setGenericValidationEnabled(true);
        List<PropertyChangeEvent> events = new ArrayList<>();
        ruleUIModel.addPropertyChangeListener(events::add);

        ruleUIModel.setGeneric(false);

        assertThat(attribute.isGenericValidationEnabled(), is(false));
        assertThat(events.size(), is(1));
        assertThat(events.get(0).getPropertyName(), is(RuleUIModel.PROPERTY_GENERIC));
        assertThat(events.get(0).getOldValue(), is(true));
        assertThat(events.get(0).getNewValue(), is(false));
    }
}
