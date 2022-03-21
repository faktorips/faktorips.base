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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.DeltaType;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IProductCmptLinkContainer;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Test;

public class ProductCmptToTypeDeltaTest extends AbstractIpsPluginTest {

    private static final String TEST_VALUE = "TestValue";
    private static final String ATTRIBUTE_NAME = "test";
    private static final String TARGET1 = "Target1";
    private static final String TARGET2 = "Target2";
    private static final String ASSOCIATION = "Association1";

    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private PolicyCmptType policyCmptType;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "MyPolicyCmptType", "MyProductCmptType");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "MyProductCmpt");
    }

    @Test
    public void testDelta_empty() throws Exception {

        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertTrue(productCmptToTypeDelta.isEmpty());
    }

    @Test
    public void testDelta_unusedGenerations_onlyOneGeneration() throws Exception {
        productCmptType.setChangingOverTime(false);

        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertTrue(productCmptToTypeDelta.isEmpty());
    }

    @Test
    public void testDelta_unusedGenerations_needToFix() throws Exception {
        productCmpt.newGeneration(new GregorianCalendar(2001, 0, 1));
        productCmptType.setChangingOverTime(false);

        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertFalse(productCmptToTypeDelta.isEmpty());
        assertEquals(1, productCmptToTypeDelta.getEntries(DeltaType.INVALID_GENERATIONS).length);
    }

    @Test
    public void testCreateEntriesForTemplateLinks_Empty() throws Exception {
        ProductCmpt productTemplate = setUpTemplateAndAssociation();
        createLink(productCmpt, ASSOCIATION, TARGET1);
        createLink(productCmpt, ASSOCIATION, TARGET2);
        createLink(productTemplate, ASSOCIATION, TARGET1);
        createLink(productTemplate, ASSOCIATION, TARGET2);
        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertTrue(productCmptToTypeDelta.isEmpty());
    }

    @Test
    public void testCreateEntriesForTemplateLinks_MissingLink() throws Exception {
        ProductCmpt productTemplate = setUpTemplateAndAssociation();
        createLink(productCmpt, ASSOCIATION, TARGET1);
        createLink(productTemplate, ASSOCIATION, TARGET1);
        createLink(productTemplate, ASSOCIATION, TARGET2);
        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertThat(productCmptToTypeDelta.getEntries()[0].getDeltaType(), is(DeltaType.MISSING_TEMPLATE_LINK));
    }

    @Test
    public void testCreateEntriesForTemplateLinks_DeletedInheritedLink() throws Exception {
        ProductCmpt productTemplate = setUpTemplateAndAssociation();
        createLink(productCmpt, ASSOCIATION, TARGET1);
        IProductCmptLink link = createLink(productCmpt, ASSOCIATION, TARGET2);
        link.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        createLink(productTemplate, ASSOCIATION, TARGET1);
        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertThat(productCmptToTypeDelta.getEntries()[0].getDeltaType(), is(DeltaType.REMOVED_TEMPLATE_LINK));
    }

    @Test
    public void testCreateEntriesForTemplateLinks_DeletUndefinedLink() throws Exception {
        ProductCmpt productTemplate = setUpTemplateAndAssociation();
        createLink(productCmpt, ASSOCIATION, TARGET1);
        IProductCmptLink link = createLink(productCmpt, ASSOCIATION, TARGET2);
        link.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        createLink(productTemplate, ASSOCIATION, TARGET1);
        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertTrue(productCmptToTypeDelta.isEmpty());
    }

    @Test
    public void testCreateEntriesForTemplateLinks_NotDeletDefinedLink() throws Exception {
        ProductCmpt productTemplate = setUpTemplateAndAssociation();
        createLink(productCmpt, ASSOCIATION, TARGET1);
        IProductCmptLink link = createLink(productCmpt, ASSOCIATION, TARGET2);
        link.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        createLink(productTemplate, ASSOCIATION, TARGET1);
        ProductCmptToTypeDelta productCmptToTypeDelta = new ProductCmptToTypeDelta(productCmpt, ipsProject);

        assertTrue(productCmptToTypeDelta.isEmpty());
    }

    protected ProductCmpt setUpTemplateAndAssociation() {
        IProductCmptTypeAssociation asso = productCmptType.newProductCmptTypeAssociation();
        asso.setTargetRoleSingular(ASSOCIATION);
        asso.setChangingOverTime(false);
        ProductCmpt productTemplate = newProductTemplate(ipsProject, "Template");
        productTemplate.setProductCmptType(productCmptType.getQualifiedName());
        productCmpt.setTemplate(productTemplate.getQualifiedName());
        return productTemplate;
    }

    private IProductCmptLink createLink(IProductCmptLinkContainer container, String association, String target) {
        IProductCmptLink newLink = container.newLink(association);
        newLink.setTarget(target);
        return newLink;
    }

    @Test
    public void testFindAndSetPredecessors() throws Exception {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute(ATTRIBUTE_NAME);
        policyCmptTypeAttribute.setDatatype(ValueDatatype.STRING.getName());
        policyCmptTypeAttribute.setValueSetConfiguredByProduct(true);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        IProductCmptGeneration generation = productCmpt.getLatestProductCmptGeneration();
        generation.getPropertyValue(ATTRIBUTE_NAME, IConfiguredDefault.class).setValue(TEST_VALUE);
        ((EnumValueSet)generation.getPropertyValue(ATTRIBUTE_NAME, IConfiguredValueSet.class)
                .changeValueSetType(ValueSetType.ENUM)).addValue(TEST_VALUE);

        policyCmptTypeAttribute.setChangingOverTime(false);
        productCmpt.fixAllDifferencesToModel(ipsProject);

        assertThat(productCmpt.getPropertyValue(ATTRIBUTE_NAME, IConfiguredDefault.class).getValue(), is(TEST_VALUE));
        assertThat(((EnumValueSet)productCmpt.getPropertyValue(ATTRIBUTE_NAME, IConfiguredValueSet.class).getValueSet())
                .getValues()[0], is(TEST_VALUE));

    }

}
