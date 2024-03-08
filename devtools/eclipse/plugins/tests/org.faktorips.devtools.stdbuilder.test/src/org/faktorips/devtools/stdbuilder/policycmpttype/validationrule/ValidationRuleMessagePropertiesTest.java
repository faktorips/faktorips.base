/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;

import org.faktorips.devtools.model.builder.propertybuilder.PropertyKey;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.policycmpttype.validationrule.ValidationRuleMessageProperties.RuleKeyParts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
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
        ValidationRuleMessageProperties validationMessages = new ValidationRuleMessageProperties(false);
        validationMessages.put(rule1, "123");
        validationMessages.put(rule2, "123");
        validationMessages.put(rule3, "312");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        validationMessages.store(outputStream);
        validationMessages.load(new ByteArrayInputStream(outputStream.toByteArray()));

        Collection<PropertyKey> rules = validationMessages.getKeysForPolicyCmptType(new QualifiedNameType(QNAME1,
                IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(2, rules.size());
        assertThat(rules, hasItem(new RuleKeyParts(QNAME1, RULE1, getRuleQName(QNAME1, RULE1))));
        assertThat(rules, hasItem(new RuleKeyParts(QNAME1, RULE2, getRuleQName(QNAME1, RULE2))));
        rules = validationMessages.getKeysForPolicyCmptType(new QualifiedNameType(QNAME2,
                IpsObjectType.POLICY_CMPT_TYPE));
        assertEquals(
                1,
                validationMessages.getKeysForPolicyCmptType(
                        new QualifiedNameType(QNAME2, IpsObjectType.POLICY_CMPT_TYPE)).size());
        assertThat(rules, hasItem(new RuleKeyParts(QNAME2, RULE3, getRuleQName(QNAME2, RULE3))));
    }

}
