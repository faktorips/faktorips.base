/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import static org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus.DEFINED;
import static org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus.INHERITED;
import static org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus.UNDEFINED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.values.Decimal;
import org.junit.Test;

public class InferTemplateProcessorTest extends AbstractIpsPluginTest {

    private static final Cardinality CARDINALITY_OPTIONAL_DEFAULT_1 = new Cardinality(0, 1, 1);
    private static final Cardinality CARDINALITY_OPTIONAL_DEFAULT_0 = new Cardinality(0, 1, 0);
    private static final Cardinality CARDINALITY_MANDATORY = new Cardinality(1, 1, 1);
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
    public void testRun_NullValueIsSetInTemplateTableContentUsage() {
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
    public void testRun_ValidationRuleConfigsAreSetInTemplate() {
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

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration,
                Lists.newArrayList(product1, product2, product3));
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
    public void testRun_ValidationRuleConfigsAreInheritedInProducts() {
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

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration,
                Lists.newArrayList(product1, product2, product3));
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
    public void testRun_ProductAndGenerationLinksAreInferred() {
        IIpsProject project = newIpsProject();
        setLinkThreshold(project, Decimal.valueOf(1));

        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);
        IProductCmptTypeAssociation association = type.newProductCmptTypeAssociation();
        association.setTarget(PRODUCT_TYPE_QNAME);
        association.setTargetRoleSingular(PRODUCT_ASSOCIATION);
        IProductCmptTypeAssociation genAssociation = type.newProductCmptTypeAssociation();
        genAssociation.setTarget(PRODUCT_TYPE_QNAME);
        genAssociation.setTargetRoleSingular(GENERATION_ASSOCIATION);
        genAssociation.setChangingOverTime(true);

        IProductCmpt product1 = newProductCmpt(type, PRODUCT_1_QNAME);
        IProductCmptGeneration gen1 = product1.getLatestProductCmptGeneration();

        IProductCmpt product2 = newProductCmpt(type, PRODUCT_2_QNAME);
        IProductCmptGeneration gen2 = product2.getLatestProductCmptGeneration();

        Cardinality cardinality = CARDINALITY_MANDATORY;

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

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration,
                Lists.newArrayList(product1, product2));
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
    public void testRun_UsesThresholdFromIpsProjectPropertiesForPopertyValues() {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);
        IProductCmptTypeAttribute[] attributes = new IProductCmptTypeAttribute[11];
        for (int i = 1; i <= 10; i++) {
            attributes[i] = type.newProductCmptTypeAttribute();
            attributes[i].setName("a" + i);
            attributes[i].setDatatype(Datatype.STRING.getQualifiedName());
            attributes[i].setModifier(Modifier.PUBLISHED);
        }

        IProductCmpt[] products = new IProductCmpt[10];
        for (int i = 0; i < 10; i++) {
            products[i] = newProductCmpt(type, "Product" + i);
            for (int j = 1; j <= 10; j++) {
                IAttributeValue propertyValue = products[i].newPropertyValue(attributes[j], IAttributeValue.class);
                propertyValue
                        .setValueHolder(new SingleValueHolder(propertyValue, "v" + (j >= 10 - i ? j : j + "_" + i)));
            }
        }

        for (int t = 1; t <= 10; t++) {
            Decimal threshold = Decimal.valueOf(t, 1);
            setPropertyValueThreshold(project, threshold);

            IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME + t);
            for (int j = 1; j <= 10; j++) {
                // initialize empty values
                template.newPropertyValues(attributes[j]);
            }
            IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();

            InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration, Arrays.asList(products));
            processor.run(new NullProgressMonitor());

            for (int j = 1; j <= 10; j++) {
                assertThat(
                        "attribute a" + j + " has the same value for " + j
                                + " products and should therefor be in the template for a threshold of " + threshold,
                        template.getAttributeValue("a" + j).getTemplateValueStatus(),
                        j >= t ? is(TemplateValueStatus.DEFINED) : is(TemplateValueStatus.UNDEFINED));
            }
        }

    }

    @Test
    public void testRun_UsesThresholdFromIpsProjectPropertiesForLinkCardinalities() {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);
        IProductCmpt[] products = new IProductCmpt[10];
        for (int i = 0; i < 10; i++) {
            products[i] = newProductCmpt(type, "Product" + i);
            for (int j = 1; j <= 10; j++) {
                newLink(products[i], PRODUCT_ASSOCIATION + j, "a", new Cardinality(1, (j >= 10 - i ? 1 : i + 20), 1));
            }
        }

        for (int t = 1; t <= 10; t++) {
            Decimal threshold = Decimal.valueOf(t, 1);
            setLinkThreshold(project, threshold);

            IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME + t);
            IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();

            InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration, Arrays.asList(products));
            processor.run(new NullProgressMonitor());

            for (int j = 1; j <= 10; j++) {
                assertThat(
                        PRODUCT_ASSOCIATION + j + " has the same cardinality for " + j
                                + " products and should therefor be in the template for a threshold of " + threshold,
                        template.getLinksAsList(PRODUCT_ASSOCIATION + j).size(), is(j >= t ? 1 : 0));
            }
        }
    }

    @Test
    public void testRun_UsesThresholdFromIpsProjectPropertiesForLinkTargets() {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);

        for (int t = 1; t <= 10; t++) {
            List<IProductCmpt> products = addProductsAndLinks(type, t);
            Decimal threshold = Decimal.valueOf(t, 1);
            setLinkThreshold(project, threshold);

            IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME + t);
            IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();

            InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration, products);
            processor.run(new NullProgressMonitor());

            for (int j = 1; j <= 10; j++) {
                if (j >= t) { // there should be at least t products with the same target
                    if (t == 1) { // there is exactly one target that is referenced t+ times
                        assertThat(PRODUCT_ASSOCIATION + j + " has the same target for " + j
                                + " products and should therefor be in the template for a threshold of " + threshold,
                                template.getLinksAsList(PRODUCT_ASSOCIATION + j).size(), is(11 - j));
                    } else { // as the threshold is so low, all different targets are valid
                        assertThat(PRODUCT_ASSOCIATION + j + " has the same target for " + j
                                + " products and should therefor be in the template for a threshold of " + threshold,
                                template.getLinksAsList(PRODUCT_ASSOCIATION + j).size(), is(1));
                    }
                } else { // no target reaches the threshold
                    assertThat(PRODUCT_ASSOCIATION + j + " has the same target for " + j
                            + " products and should therefor not be in the template for a threshold of " + threshold,
                            template.getLinksAsList(PRODUCT_ASSOCIATION + j).size(), is(0));
                }
            }
        }
    }

    @Test
    public void testRun_UsesThresholdFromIpsProjectPropertiesForLinkCardinalitiesAndTargets() {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);
        IProductCmpt product1 = newProductCmpt(type, "Product1");
        newLink(product1, PRODUCT_ASSOCIATION, "a", CARDINALITY_MANDATORY);
        newLink(product1, PRODUCT_ASSOCIATION, "b", CARDINALITY_OPTIONAL_DEFAULT_0);
        newLink(product1, PRODUCT_ASSOCIATION, "c", CARDINALITY_MANDATORY);
        IProductCmpt product2 = newProductCmpt(type, "Product2");
        newLink(product2, PRODUCT_ASSOCIATION, "a", CARDINALITY_MANDATORY);
        newLink(product2, PRODUCT_ASSOCIATION, "b", CARDINALITY_OPTIONAL_DEFAULT_0);
        newLink(product2, PRODUCT_ASSOCIATION, "c", CARDINALITY_OPTIONAL_DEFAULT_1);
        IProductCmpt product3 = newProductCmpt(type, "Product3");
        newLink(product3, PRODUCT_ASSOCIATION, "a", CARDINALITY_OPTIONAL_DEFAULT_1);
        newLink(product3, PRODUCT_ASSOCIATION, "b", CARDINALITY_MANDATORY);
        newLink(product3, PRODUCT_ASSOCIATION, "c", CARDINALITY_OPTIONAL_DEFAULT_0);
        IProductCmpt product4 = newProductCmpt(type, "Product4");
        newLink(product4, PRODUCT_ASSOCIATION, "b", CARDINALITY_MANDATORY);

        setLinkThreshold(project, Decimal.valueOf(5, 1));

        IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME);
        IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration,
                Arrays.asList(product1, product2, product3, product4));
        processor.run(new NullProgressMonitor());

        assertThat(template.getLinksAsList(PRODUCT_ASSOCIATION).size(), is(2));
        IProductCmptLink linkA = template.getLinksAsList(PRODUCT_ASSOCIATION).get(0);
        assertThat(linkA.getTarget(), is("a"));
        assertThat(linkA.getCardinality(), is(CARDINALITY_MANDATORY));
        IProductCmptLink linkB = template.getLinksAsList(PRODUCT_ASSOCIATION).get(1);
        assertThat(linkB.getTarget(), is("b"));
        // this is the cardinality of the first 2 products in the list; the second 2 would qualify
        // as well, but the "best" hit is the first
        assertThat(linkB.getCardinality(), is(CARDINALITY_OPTIONAL_DEFAULT_0));
    }

    private IProductCmptLink newLink(IProductCmpt productCmpt,
            String association,
            String target,
            Cardinality cardinality) {
        IProductCmptLink link = productCmpt.newLink(association);
        link.setTarget(target);
        link.setCardinality(cardinality);
        return link;
    }

    private List<IProductCmpt> addProductsAndLinks(IProductCmptType type, int t) {
        List<IProductCmpt> products = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            ProductCmpt prod = newProductCmpt(type, "Product" + t + '_' + i);
            products.add(prod);
            for (int j = 1; j <= 10; j++) {
                String target = "a" + (j >= 10 - i ? j : j + "_" + i);
                newLink(prod, PRODUCT_ASSOCIATION + j, target, CARDINALITY_MANDATORY);
            }
        }
        return products;
    }

    @Test
    public void testRun_LinkIsInferredAndTemplateValueStatesAreAdjusted() {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);
        IProductCmptTypeAssociation association = type.newProductCmptTypeAssociation();
        association.setTarget(PRODUCT_TYPE_QNAME);
        association.setTargetRoleSingular(PRODUCT_ASSOCIATION);

        IProductCmpt product1 = newProductCmpt(type, "Product1");
        IProductCmptLink link1 = newLink(product1, PRODUCT_ASSOCIATION, "a", CARDINALITY_MANDATORY);

        IProductCmpt product2 = newProductCmpt(type, "Product2");
        IProductCmptLink link2 = newLink(product2, PRODUCT_ASSOCIATION, "a", CARDINALITY_MANDATORY);

        IProductCmpt product3 = newProductCmpt(type, "Product3");
        IProductCmptLink link3 = newLink(product3, PRODUCT_ASSOCIATION, "a", CARDINALITY_OPTIONAL_DEFAULT_1);

        IProductCmpt product4 = newProductCmpt(type, "Product4");

        setLinkThreshold(project, Decimal.valueOf(5, 1));

        IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME);
        IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration,
                Arrays.asList(product1, product2, product3, product4));
        processor.run(new NullProgressMonitor());

        assertThat(template.getLinksAsList(PRODUCT_ASSOCIATION).size(), is(1));
        IProductCmptLink templateLink = template.getLinksAsList(PRODUCT_ASSOCIATION).get(0);
        assertThat(templateLink.getTarget(), is("a"));
        assertThat(templateLink.getCardinality(), is(CARDINALITY_MANDATORY));

        assertThat(link1.getTemplateValueStatus(), is(INHERITED));
        assertThat(link1.getTarget(), is("a"));
        assertThat(link1.getCardinality(), is(CARDINALITY_MANDATORY));
        assertThat(link2.getTemplateValueStatus(), is(INHERITED));
        assertThat(link2.getTarget(), is("a"));
        assertThat(link2.getCardinality(), is(CARDINALITY_MANDATORY));
        assertThat(link3.getTemplateValueStatus(), is(DEFINED));
        assertThat(link3.getTarget(), is("a"));
        assertThat(link3.getCardinality(), is(CARDINALITY_OPTIONAL_DEFAULT_1));

        assertThat(product4.getLinksAsList(PRODUCT_ASSOCIATION).size(), is(1));
        assertThat(product4.getLinksAsList(PRODUCT_ASSOCIATION).get(0).getTemplateValueStatus(), is(UNDEFINED));
        assertThat(product4.getLinksAsList(PRODUCT_ASSOCIATION).get(0).getTarget(), is("a"));
        assertThat(product4.getLinksAsList(PRODUCT_ASSOCIATION).get(0).getCardinality(), is(CARDINALITY_MANDATORY));
    }

    @Test
    public void testRun_MissingLinksAreIgnoredForThresholdCalculation() {
        IIpsProject project = newIpsProject();
        IProductCmptType type = newProductCmptType(project, PRODUCT_TYPE_QNAME);

        IProductCmpt product1 = newProductCmpt(type, "Product1");
        newLink(product1, PRODUCT_ASSOCIATION, "a", CARDINALITY_MANDATORY);

        IProductCmpt product2 = newProductCmpt(type, "Product2");
        newLink(product2, PRODUCT_ASSOCIATION, "a", CARDINALITY_MANDATORY);

        IProductCmpt product3 = newProductCmpt(type, "Product3");
        IProductCmpt product4 = newProductCmpt(type, "Product4");
        IProductCmpt product5 = newProductCmpt(type, "Product5");

        setLinkThreshold(project, Decimal.valueOf(4, 1));

        IProductCmpt template = newProductTemplate(type, TEMPLATE_QNAME);
        IProductCmptGeneration templateGeneration = template.getLatestProductCmptGeneration();

        InferTemplateProcessor processor = new InferTemplateProcessor(templateGeneration,
                Arrays.asList(product1, product2, product3, product4, product5));
        processor.run(new NullProgressMonitor());

        assertThat(template.getLinksAsList(PRODUCT_ASSOCIATION).size(), is(1));
        IProductCmptLink templateLink = template.getLinksAsList(PRODUCT_ASSOCIATION).get(0);
        assertThat(templateLink.getTarget(), is("a"));
        assertThat(templateLink.getCardinality(), is(CARDINALITY_MANDATORY));
    }

    private void setPropertyValueThreshold(IIpsProject project, Decimal threshold) {
        IIpsProjectProperties properties = project.getProperties();
        properties.setInferredTemplatePropertyValueThreshold(threshold);
        project.setProperties(properties);
    }

    private void setLinkThreshold(IIpsProject project, Decimal threshold) {
        IIpsProjectProperties properties = project.getProperties();
        properties.setInferredTemplateLinkThreshold(threshold);
        project.setProperties(properties);
    }

}
