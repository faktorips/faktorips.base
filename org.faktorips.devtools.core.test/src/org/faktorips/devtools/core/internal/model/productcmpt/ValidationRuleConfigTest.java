/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Assert;
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
    public void testInitFromXmlInactive() {
        ruleConfig.setActive(false);
        Element el = ruleConfig.toXml(newDocument());
        ruleConfig.initFromXml(el);
        assertTrue("ValidationRule is NOT supposed to be configured as active.", !ruleConfig.isActive());
        assertEquals("rule1", ruleConfig.getName());
    }

    @Test
    public void testFindVRule() throws CoreException {
        IValidationRule rule = ruleConfig.findValidationRule(ipsProject);
        Assert.assertNotNull(rule);
        Assert.assertEquals(this.rule, rule);
    }

    @Test
    public void testFindInexistentVRule() throws CoreException {
        IValidationRuleConfig config = generation.newValidationRuleConfig();
        IValidationRule rule = config.findValidationRule(ipsProject);
        Assert.assertNull(rule);
    }

    @Test
    public void testFindInexistentPcTypeOnFindVRule() throws CoreException {
        productCmptType.setPolicyCmptType("inexistentPCType");
        IValidationRule rule = ruleConfig.findValidationRule(ipsProject);
        Assert.assertNull(rule);
    }

    @Test
    public void testFindSupertypeHierarchie() throws CoreException {
        IPolicyCmptType subPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SubPolicy", "SubProdukt");
        subPolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        IProductCmptType subProductCmptType = subPolicyCmptType.findProductCmptType(ipsProject);
        subProductCmptType.setSupertype(productCmptType.getQualifiedName());

        IProductCmpt subProductCmpt = newProductCmpt(subProductCmptType, "SubProduct");
        IProductCmptGeneration subGen = subProductCmpt.getProductCmptGeneration(0);
        // configure supertype rule
        IValidationRuleConfig ruleConfig = subGen.newValidationRuleConfig(rule);

        IValidationRule foundRule = ruleConfig.findValidationRule(ipsProject);
        Assert.assertNotNull(foundRule);
        Assert.assertEquals(this.rule, foundRule);
    }

}
