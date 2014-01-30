/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptLinkContainerValidatorIntegrationTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IIpsProject ipsProject;

    private IPolicyCmptType targetPolicyType;
    private IProductCmptType targetProductType;
    private IProductCmptTypeAssociation association;
    private IProductCmptTypeAssociation staticAssociation;
    private IProductCmpt target1;
    private IProductCmpt target2;
    private IProductCmptLink link1;
    private IProductCmptLink link2;
    private IProductCmptLink staticLink;
    private ProductCmptLinkContainerValidator validator;
    private IProductCmptGeneration gen;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");

        targetPolicyType = newPolicyAndProductCmptType(ipsProject, "TargetPolicyType", "TargetProductType");
        targetProductType = targetPolicyType.findProductCmptType(ipsProject);
        target1 = newProductCmpt(targetProductType, "TargetProduct");
        target2 = newProductCmpt(targetProductType, "TargetProduct2");

        association = productCmptType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget(targetProductType.getQualifiedName());
        association.setTargetRoleSingular("testRelationProductSide");
        association.setTargetRolePlural("testRelationsProductSide");

        staticAssociation = productCmptType.newProductCmptTypeAssociation();
        staticAssociation.setAssociationType(AssociationType.AGGREGATION);
        staticAssociation.setTarget(targetProductType.getQualifiedName());
        staticAssociation.setTargetRoleSingular("testStaticRelationProductSide");
        staticAssociation.setTargetRolePlural("testStaticRelationsProductSide");
        staticAssociation.setChangingOverTime(false);

        gen = productCmpt.getProductCmptGeneration(0);
        link1 = gen.newLink(association.getTargetRoleSingular());
        link2 = gen.newLink(association.getTargetRoleSingular());
        link1.setTarget(target1.getQualifiedName());
        link2.setTarget(target2.getQualifiedName());
        staticLink = productCmpt.newLink(staticAssociation.getTargetRoleSingular());
        staticLink.setTarget(target2.getQualifiedName());

    }

    private MessageList callValidator(IProductCmptLinkContainer container) {
        MessageList list = new MessageList();
        validator = new ProductCmptLinkContainerValidator(ipsProject, container);
        validator.startAndAddMessagesToList(productCmptType, list);
        return list;
    }

    @Test
    public void testValidateMinumumCardinality() {
        association.setMinCardinality(3);
        association.setMaxCardinality(5);

        MessageList list = callValidator(gen);
        assertEquals(1, list.size());
        Message message = list.getMessage(0);
        assertEquals(Message.ERROR, message.getSeverity());
        assertEquals(IProductCmptLinkContainer.MSGCODE_NOT_ENOUGH_RELATIONS, message.getCode());
    }

    @Test
    public void testValidateMaximumCardinality() {
        association.setMinCardinality(0);
        association.setMaxCardinality(1);

        MessageList list = callValidator(gen);
        assertEquals(1, list.size());
        Message message = list.getMessage(0);
        assertEquals(Message.ERROR, message.getSeverity());
        assertEquals(IProductCmptLinkContainer.MSGCODE_TOO_MANY_RELATIONS, message.getCode());
    }

    @Test
    public void testValidateDuplicateTarget() {
        link2.setTarget(target1.getQualifiedName());

        MessageList list = callValidator(gen);
        assertEquals(1, list.size());
        Message message = list.getMessage(0);
        assertEquals(Message.ERROR, message.getSeverity());
        assertEquals(IProductCmptLinkContainer.MSGCODE_DUPLICATE_RELATION_TARGET, message.getCode());
    }

    @Test
    public void testGenertaionIgnoresStaticAssociation() {
        staticAssociation.setMinCardinality(2);
        staticAssociation.setMaxCardinality(5);
        MessageList list = callValidator(gen);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testProdCmptIgnoresChangingAssociation() {
        association.setMinCardinality(3);
        association.setMaxCardinality(5);
        MessageList list = callValidator(productCmpt);
        assertTrue(list.isEmpty());
    }

}
