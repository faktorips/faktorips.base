/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.ProductCmptPropertyType;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ValidationRuleConfigTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;

    private IValidationRule rule;
    private IValidationRuleConfig ruleConfig;
    private IProductCmptGeneration generation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        rule = policyCmptType.newRule();
        rule.setName("rule1");
        rule.setActivatedByDefault(true);

        productCmpt = newProductCmpt(productCmptType, "ProductA");
        generation = productCmpt.getProductCmptGeneration(0);
        ruleConfig = generation.newValidationRuleConfig(rule);
    }

    @Test
    public void testDefaultActivation() {
        assertTrue("ValidationRule is supposed to be configured as active.", ruleConfig.isActive());
    }

    @Test
    public void testInitFromXml() {
        Element el = getTestDocument().getDocumentElement();
        ruleConfig.initFromXml(el);
        assertTrue("ValidationRule is supposed to be configured as active.", ruleConfig.isActive());
        assertEquals("rule1", ruleConfig.getName());
    }

    @Test
    public void testInitFromXml_InactiveRule() {
        ruleConfig.setActive(false);
        Element el = ruleConfig.toXml(newDocument());
        ruleConfig.initFromXml(el);
        assertTrue("ValidationRule is NOT supposed to be configured as active.", !ruleConfig.isActive());
        assertEquals("rule1", ruleConfig.getName());
    }

    @Test
    public void testFindVRule() {
        IValidationRule rule = ruleConfig.findValidationRule(ipsProject);
        assertNotNull(rule);
        assertEquals(this.rule, rule);
    }

    @Test
    public void testToXml() {
        ruleConfig.setActive(false);
        ruleConfig.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        Element el = ruleConfig.toXml(newDocument());
        IValidationRuleConfig copy = generation.newValidationRuleConfig(rule);
        copy.initFromXml(el);
        assertThat(copy.isActive(), is(false));
        assertThat(copy.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
    }

    @Test
    public void testToXml_InheritedValue() {
        IValidationRuleConfig templateRuleConfig = addTemplateRuleConfig();
        assertThat(ruleConfig.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));

        templateRuleConfig.setActive(false);
        ruleConfig.setActive(true);

        Element el = ruleConfig.toXml(newDocument());

        IValidationRuleConfig copy = generation.newValidationRuleConfig(rule);
        copy.initFromXml(el);
        assertThat(copy.isActive(), is(false));
        assertThat(copy.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
    }

    @Test
    public void testFindValidationRule_InexistentRule() {
        IValidationRule rule = mock(IValidationRule.class);
        when(rule.getPropertyName()).thenReturn("newRule");
        when(rule.isActivatedByDefault()).thenReturn(false);
        when(rule.getProductCmptPropertyType()).thenReturn(ProductCmptPropertyType.VALIDATION_RULE);

        IValidationRuleConfig config = generation.newValidationRuleConfig(rule);
        IValidationRule foundRule = config.findValidationRule(ipsProject);
        assertNull(foundRule);
    }

    @Test
    public void testFindInexistentPcTypeOnFindVRule() {
        productCmptType.setPolicyCmptType("inexistentPCType");
        IValidationRule rule = ruleConfig.findValidationRule(ipsProject);
        assertNull(rule);
    }

    @Test
    public void testFindSupertypeHierarchie() {
        IPolicyCmptType subPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SubPolicy", "SubProdukt");
        subPolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        IProductCmptType subProductCmptType = subPolicyCmptType.findProductCmptType(ipsProject);
        subProductCmptType.setSupertype(productCmptType.getQualifiedName());

        IProductCmpt subProductCmpt = newProductCmpt(subProductCmptType, "SubProduct");
        IProductCmptGeneration subGen = subProductCmpt.getProductCmptGeneration(0);
        // configure supertype rule
        IValidationRuleConfig ruleConfig = subGen.newValidationRuleConfig(rule);

        IValidationRule foundRule = ruleConfig.findValidationRule(ipsProject);
        assertNotNull(foundRule);
        assertEquals(this.rule, foundRule);
    }

    @Test
    public void testIsActive_DefinedValue() {
        assertThat(ruleConfig.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));

        ruleConfig.setActive(false);
        assertThat(ruleConfig.isActive(), is(false));

        ruleConfig.setActive(true);
        assertThat(ruleConfig.isActive(), is(true));
    }

    @Test
    public void testIsActive_InheritedValue() {
        IValidationRuleConfig templateRuleConfig = addTemplateRuleConfig();
        assertThat(ruleConfig.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));

        ruleConfig.setActive(true);
        templateRuleConfig.setActive(false);

        assertThat(ruleConfig.isActive(), is(false));
    }

    @Test
    public void testIsActive_InheritedValueButTemplateIsMissing() {
        productCmpt.setTemplate("This isnt' the template you're looking for");
        ruleConfig.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        ruleConfig.setActive(true);

        assertThat(ruleConfig.isActive(), is(true));
    }

    private IValidationRuleConfig addTemplateRuleConfig() {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        IValidationRuleConfig templateRuleConfig = templateGen.newValidationRuleConfig(rule);

        productCmpt.setTemplate(template.getQualifiedName());
        ruleConfig.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        return templateRuleConfig;
    }

}
