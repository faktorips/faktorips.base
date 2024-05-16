/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsValidationRule;
import org.faktorips.runtime.model.annotation.IpsValidationRules;
import org.junit.Test;

public class ValidationRuleTest {

    private final PolicyCmptType superType = (PolicyCmptType)IpsModel.getType(SuperPolicy.class);
    private final ValidationRule rule = superType.getValidationRule("rule");
    private final PolicyCmptType type = (PolicyCmptType)IpsModel.getType(Policy.class);
    private final ValidationRule subRule = type.getValidationRule("rule");

    @Test
    public void testIsOverriding() throws Exception {
        assertFalse(rule.isOverriding());
        assertTrue(subRule.isOverriding());
    }

    @Test
    public void testGetSuperValidationRule() throws Exception {
        assertThat(rule.getSuperValidationRule(), is(nullValue()));
        assertThat(subRule.getSuperValidationRule(), is(rule));
    }

    @Test
    public void testGetDocumentation() {
        assertThat(rule.getDescription(Locale.GERMAN), is("Description of rule"));
        assertThat(subRule.getDescription(Locale.GERMAN), is("Description of rule in Policy"));
    }

    @IpsPolicyCmptType(name = "MySuperPolicy")
    @IpsValidationRules({ "rule" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class SuperPolicy implements IModelObject {

        @IpsValidationRule(name = "rule", msgCode = "", severity = Severity.ERROR)
        protected boolean rule(MessageList ml, IValidationContext context) {
            return true;
        }

        @Override
        public MessageList validate(IValidationContext context) {
            return null;
        }
    }

    @IpsPolicyCmptType(name = "MyPolicy")
    @IpsValidationRules({ "rule" })
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.type.test", defaultLocale = "de")
    private static class Policy extends SuperPolicy {

        @Override
        @IpsValidationRule(name = "rule", msgCode = "", severity = Severity.ERROR)
        protected boolean rule(MessageList ml, IValidationContext context) {
            return true;
        }
    }
}
