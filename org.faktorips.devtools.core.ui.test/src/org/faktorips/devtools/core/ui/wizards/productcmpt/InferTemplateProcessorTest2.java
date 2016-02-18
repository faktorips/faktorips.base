/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus.DEFINED;
import static org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus.INHERITED;
import static org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus.UNDEFINED;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.Cardinality;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.junit.Test;

public class InferTemplateProcessorTest2 extends AbstractIpsPluginTest {

    private static final String PRODUCT_TYPE_QNAME = "ProductType";
    private static final String POLICY_TYPE_QNAME = "PolicyType";
    private static final String PRODUCT_1_QNAME = "Product1";
    private static final String PRODUCT_2_QNAME = "Product2";
    private static final String PRODUCT_3_QNAME = "Product3";
    private static final String TEMPLATE_QNAME = "Template";
    private static final String TABLE_ROLE_1 = "TableUsage";
    private static final String RULE_1_NAME = "Rule1";
    private static final String RULE_2_NAME = "Rule2";
    private static final String RULE_3_NAME = "Rule3";
    private static final String PRODUCT_ASSOCIATION = "ProductAssociation";
    private static final String GENERATION_ASSOCIATION = "GenerationAssociation";

    @Test
    public void testRun_NullValueIsSetInTemplateTableContentUsage() throws CoreException {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);
        ITableStructureUsage usage = type.newTableStructureUsage();
        usage.setRoleName(TABLE_ROLE_1);

        // Product with null table content usage
        IProductCmpt product = newProductCmpt(type, PRODUCT_1_QNAME);
        IProductCmptGeneration productGen = product.getLatestProductCmptGeneration();
        ITableContentUsage productTableContentUsage = productGen.newTableContentUsage(usage);
        productTableContentUsage.setStructureUsage(TABLE_ROLE_1);
        productTableContentUsage.setTableContentName(null);

        // Template with some table content usage
        IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME);
        IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();
        ITableContentUsage templateTableContentUsage = templateGeneration.newTableContentUsage(usage);
        templateTableContentUsage.setStructureUsage(TABLE_ROLE_1);
        templateTableContentUsage.setTableContentName("should be overwritten");

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration, Lists.newArrayList(product));
        processor.run(new NullProgressMonitor());

        assertThat(templateTableContentUsage.getTableContentName(), is((String)null));
    }

    @Test
    public void testRun_ValidationRuleConfigsAreSetInTemplate() throws CoreException {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);
        IPolicyCmptType policyType = newPolicyCmptType(project, POLICY_TYPE_QNAME);

        type.setPolicyCmptType(POLICY_TYPE_QNAME);
        policyType.setProductCmptType(PRODUCT_TYPE_QNAME);

        IValidationRule rule1 = policyType.newRule();
        IValidationRule rule2 = policyType.newRule();
        IValidationRule rule3 = policyType.newRule();

        rule1.setName(RULE_1_NAME);
        rule2.setName(RULE_2_NAME);
        rule3.setName(RULE_3_NAME);

        // Products and template with rule configs
        IProductCmpt product1 = newProductCmpt(type, PRODUCT_1_QNAME);
        IProductCmpt product2 = newProductCmpt(type, PRODUCT_2_QNAME);
        IProductCmpt product3 = newProductCmpt(type, PRODUCT_3_QNAME);
        IProductCmptGeneration productGen1 = product1.getLatestProductCmptGeneration();
        IProductCmptGeneration productGen2 = product2.getLatestProductCmptGeneration();
        IProductCmptGeneration productGen3 = product3.getLatestProductCmptGeneration();

        productGen1.newValidationRuleConfig(rule1).setActive(true);
        productGen2.newValidationRuleConfig(rule1).setActive(true);
        productGen3.newValidationRuleConfig(rule1).setActive(true);

        productGen1.newValidationRuleConfig(rule2).setActive(false);
        productGen2.newValidationRuleConfig(rule2).setActive(false);
        productGen3.newValidationRuleConfig(rule2).setActive(false);

        productGen1.newValidationRuleConfig(rule3).setActive(true);
        productGen2.newValidationRuleConfig(rule3).setActive(false);
        productGen3.newValidationRuleConfig(rule3).setActive(true);

        IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME);
        IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();
        templateGeneration.newValidationRuleConfig(rule1);
        templateGeneration.newValidationRuleConfig(rule2);
        templateGeneration.newValidationRuleConfig(rule3);

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration, Lists.newArrayList(product1,
                product2, product3));
        processor.run(new NullProgressMonitor());

        IValidationRuleConfig ruleConfig1 = templateGeneration.getValidationRuleConfig(RULE_1_NAME);
        IValidationRuleConfig ruleConfig2 = templateGeneration.getValidationRuleConfig(RULE_2_NAME);
        IValidationRuleConfig ruleConfig3 = templateGeneration.getValidationRuleConfig(RULE_3_NAME);

        assertThat(ruleConfig1.getTemplateValueStatus(), is(DEFINED));
        assertThat(ruleConfig1.isActive(), is(true));

        assertThat(ruleConfig2.getTemplateValueStatus(), is(DEFINED));
        assertThat(ruleConfig2.isActive(), is(false));

        assertThat(ruleConfig3.getTemplateValueStatus(), is(UNDEFINED));
    }

    @Test
    public void testRun_ValidationRuleConfigsAreInheritedInProducts() throws CoreException {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);
        IPolicyCmptType policyType = newPolicyCmptType(project, POLICY_TYPE_QNAME);

        type.setPolicyCmptType(POLICY_TYPE_QNAME);
        policyType.setProductCmptType(PRODUCT_TYPE_QNAME);

        IValidationRule rule1 = policyType.newRule();
        IValidationRule rule2 = policyType.newRule();
        IValidationRule rule3 = policyType.newRule();

        rule1.setName(RULE_1_NAME);
        rule2.setName(RULE_2_NAME);
        rule3.setName(RULE_3_NAME);

        // Products and template with rule configs
        IProductCmpt product1 = newProductCmpt(type, PRODUCT_1_QNAME);
        IProductCmpt product2 = newProductCmpt(type, PRODUCT_2_QNAME);
        IProductCmpt product3 = newProductCmpt(type, PRODUCT_3_QNAME);
        IProductCmptGeneration gen1 = product1.getLatestProductCmptGeneration();
        IProductCmptGeneration gen2 = product2.getLatestProductCmptGeneration();
        IProductCmptGeneration gen3 = product3.getLatestProductCmptGeneration();

        gen1.newValidationRuleConfig(rule1).setActive(true);
        gen2.newValidationRuleConfig(rule1).setActive(true);
        gen3.newValidationRuleConfig(rule1).setActive(true);

        gen1.newValidationRuleConfig(rule2).setActive(false);
        gen2.newValidationRuleConfig(rule2).setActive(false);
        gen3.newValidationRuleConfig(rule2).setActive(false);

        gen1.newValidationRuleConfig(rule3).setActive(true);
        gen2.newValidationRuleConfig(rule3).setActive(false);
        gen3.newValidationRuleConfig(rule3).setActive(true);

        IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME);
        IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();
        templateGeneration.newValidationRuleConfig(rule1);
        templateGeneration.newValidationRuleConfig(rule2);
        templateGeneration.newValidationRuleConfig(rule3);

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration, Lists.newArrayList(product1,
                product2, product3));
        processor.run(new NullProgressMonitor());

        assertThat(gen1.getValidationRuleConfig(RULE_1_NAME).getTemplateValueStatus(), is(INHERITED));
        assertThat(gen1.getValidationRuleConfig(RULE_2_NAME).getTemplateValueStatus(), is(INHERITED));
        assertThat(gen1.getValidationRuleConfig(RULE_3_NAME).getTemplateValueStatus(), is(DEFINED));

        assertThat(gen2.getValidationRuleConfig(RULE_1_NAME).getTemplateValueStatus(), is(INHERITED));
        assertThat(gen2.getValidationRuleConfig(RULE_2_NAME).getTemplateValueStatus(), is(INHERITED));
        assertThat(gen2.getValidationRuleConfig(RULE_3_NAME).getTemplateValueStatus(), is(DEFINED));

        assertThat(gen3.getValidationRuleConfig(RULE_1_NAME).getTemplateValueStatus(), is(INHERITED));
        assertThat(gen3.getValidationRuleConfig(RULE_2_NAME).getTemplateValueStatus(), is(INHERITED));
        assertThat(gen3.getValidationRuleConfig(RULE_3_NAME).getTemplateValueStatus(), is(DEFINED));
    }

    @Test
    public void testRun_LinksAreInheritedFromTemplate() throws CoreException {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);

        IProductCmpt product1 = newProductCmpt(type, PRODUCT_1_QNAME);
        IProductCmptGeneration gen1 = product1.getLatestProductCmptGeneration();

        IProductCmpt product2 = newProductCmpt(type, PRODUCT_2_QNAME);
        IProductCmptGeneration gen2 = product2.getLatestProductCmptGeneration();

        Cardinality cardinality = new Cardinality(1, 1, 1);

        // Links on products and generations with same association, target and cardinality
        IProductCmptLink product1Link = product1.newLink(PRODUCT_ASSOCIATION);
        product1Link.setTarget(PRODUCT_3_QNAME);
        product1Link.setCardinality(cardinality);
        IProductCmptLink product2Link = product2.newLink(PRODUCT_ASSOCIATION);
        product2Link.setTarget(PRODUCT_3_QNAME);
        product2Link.setCardinality(cardinality);

        IProductCmptLink gen1Link = gen1.newLink(GENERATION_ASSOCIATION);
        gen1Link.setTarget(PRODUCT_3_QNAME);
        gen1Link.setCardinality(cardinality);
        IProductCmptLink gen2Link = gen2.newLink(GENERATION_ASSOCIATION);
        gen2Link.setTarget(PRODUCT_3_QNAME);
        gen2Link.setCardinality(cardinality);

        IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME);
        IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration, Lists.newArrayList(product1,
                product2));
        processor.run(new NullProgressMonitor());

        // Assert that links are added in the template
        assertThat(template.getLinksAsList(PRODUCT_ASSOCIATION).size(), is(1));
        assertThat(template.getLinksAsList(PRODUCT_ASSOCIATION).get(0).getTarget(), is(PRODUCT_3_QNAME));
        assertThat(template.getLinksAsList(PRODUCT_ASSOCIATION).get(0).getCardinality(), is(cardinality));
        assertThat(template.getLinksAsList(PRODUCT_ASSOCIATION).get(0).getTemplateValueStatus(), is(DEFINED));
        assertThat(template.getLinksAsList(GENERATION_ASSOCIATION).size(), is(0));

        assertThat(templateGeneration.getLinksAsList(GENERATION_ASSOCIATION).size(), is(1));
        assertThat(templateGeneration.getLinksAsList(GENERATION_ASSOCIATION).get(0).getTarget(), is(PRODUCT_3_QNAME));
        assertThat(templateGeneration.getLinksAsList(GENERATION_ASSOCIATION).get(0).getCardinality(), is(cardinality));
        assertThat(templateGeneration.getLinksAsList(GENERATION_ASSOCIATION).get(0).getTemplateValueStatus(),
                is(DEFINED));
        assertThat(templateGeneration.getLinksAsList(PRODUCT_ASSOCIATION).size(), is(0));

        // Assert that links in the product components are INHERITED
        assertThat(product1Link.getTemplateValueStatus(), is(INHERITED));
        assertThat(product2Link.getTemplateValueStatus(), is(INHERITED));
        assertThat(gen1Link.getTemplateValueStatus(), is(INHERITED));
        assertThat(gen2Link.getTemplateValueStatus(), is(INHERITED));
    }

    @Test
    public void testRun_OnlyLinksIdenticalInAllComponentsAreInherited() throws CoreException {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);

        IProductCmpt product1 = newProductCmpt(type, PRODUCT_1_QNAME);
        IProductCmptGeneration gen1 = product1.getLatestProductCmptGeneration();

        IProductCmpt product2 = newProductCmpt(type, PRODUCT_2_QNAME);
        IProductCmptGeneration gen2 = product2.getLatestProductCmptGeneration();

        // Links on products with same association and target but different cardinality
        IProductCmptLink product1Link = product1.newLink(PRODUCT_ASSOCIATION);
        product1Link.setTarget("a");
        product1Link.setCardinality(new Cardinality(1, 1, 1));
        IProductCmptLink product2Link = product2.newLink(PRODUCT_ASSOCIATION);
        product2Link.setTarget("a");
        product2Link.setCardinality(new Cardinality(0, 1, 1));

        // Links on generations with same association but different target
        IProductCmptLink gen1Link = gen1.newLink(GENERATION_ASSOCIATION);
        gen1Link.setTarget("a");
        IProductCmptLink gen2Link = gen2.newLink(GENERATION_ASSOCIATION);
        gen2Link.setTarget("b");

        IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME);
        IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration, Lists.newArrayList(product1,
                product2));
        processor.run(new NullProgressMonitor());

        // Assert that no links are added to the template
        assertThat(template.getLinksAsList().size(), is(0));
        assertThat(templateGeneration.getLinksAsList().size(), is(0));

        // Assert that links in the product components are still DEFINED
        assertThat(product1Link.getTemplateValueStatus(), is(DEFINED));
        assertThat(product2Link.getTemplateValueStatus(), is(DEFINED));
        assertThat(gen1Link.getTemplateValueStatus(), is(DEFINED));
        assertThat(gen2Link.getTemplateValueStatus(), is(DEFINED));
    }

}
