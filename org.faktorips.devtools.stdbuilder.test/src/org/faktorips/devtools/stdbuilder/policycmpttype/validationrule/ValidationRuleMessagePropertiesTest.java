/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.policycmpttype.validationrule.ValidationRuleMessageProperties.RuleKeyParts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleMessagePropertiesTest {

    private static final String QNAME1 = "myQName";

    private static final String QNAME2 = "myQName2";

    private static final String RULE1 = "rule1";

    private static final String RULE2 = "rule2";

    private static final String RULE3 = "rule3";

    @Mock
    private IPolicyCmptType pcType1;

    @Mock
    private IPolicyCmptType pcType2;

    @Mock
    private IValidationRule rule1;

    @Mock
    private IValidationRule rule2;

    @Mock
    private IValidationRule rule3;

    @Before
    public void initPcType() {
        when(pcType1.getQualifiedName()).thenReturn(QNAME1);
        when(pcType2.getQualifiedName()).thenReturn(QNAME2);
    }

    private void mockRuleProperties(IValidationRule rule, IPolicyCmptType pcType, String ruleName) {
        when(rule.getName()).thenReturn(ruleName);
        String ruleQName = getRuleQName(pcType.getQualifiedName(), ruleName);
        when(rule.getQualifiedRuleName()).thenReturn(ruleQName);
        when(rule.getIpsObject()).thenReturn(pcType);
    }

    private String getRuleQName(String policyCmptType, String ruleName) {
        return policyCmptType + IValidationRule.QNAME_SEPARATOR + ruleName;
    }

    @Test
    public void testInitMessagesForPcTypes() throws Exception {
        mockRuleProperties(rule1, pcType1, RULE1);
        mockRuleProperties(rule2, pcType1, RULE2);
        mockRuleProperties(rule3, pcType2, RULE3);
        ValidationRuleMessageProperties validationMessages = new ValidationRuleMessageProperties();
        validationMessages.put(rule1, "123");
        validationMessages.put(rule2, "123");
        validationMessages.put(rule3, "312");

        validationMessages.initMessagesForPcTypes();

        Collection<RuleKeyParts> rules = validationMessages.getKeysForPolicyCmptType(QNAME1);
        assertEquals(2, rules.size());
        assertThat(rules, hasItem(new RuleKeyParts(QNAME1, RULE1, getRuleQName(QNAME1, RULE1))));
        assertThat(rules, hasItem(new RuleKeyParts(QNAME1, RULE2, getRuleQName(QNAME1, RULE2))));
        rules = validationMessages.getKeysForPolicyCmptType(QNAME2);
        assertEquals(1, validationMessages.getKeysForPolicyCmptType(QNAME2).size());
        assertThat(rules, hasItem(new RuleKeyParts(QNAME2, RULE3, getRuleQName(QNAME2, RULE3))));
    }

}
