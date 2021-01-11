/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
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
    private IPolicyCmptTypeAssociation associationPolicy;

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

        associationPolicy = policyCmptType.newPolicyCmptTypeAssociation();
        associationPolicy.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        associationPolicy.setTarget(targetPolicyType.getQualifiedName());
        associationPolicy.setTargetRoleSingular("testRelationPolicySide");
        associationPolicy.setTargetRolePlural("testRelationsPolicySide");

        association = productCmptType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget(targetProductType.getQualifiedName());
        association.setTargetRoleSingular("testRelationProductSide");
        association.setTargetRolePlural("testRelationsProductSide");
        association.setMatchingAssociationName(associationPolicy.getName());

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
    public void testValidateTotalMin() {
        associationPolicy.setMinCardinality(5);
        associationPolicy.setMaxCardinality(Integer.MAX_VALUE);
        link1.setMaxCardinality(1);
        link2.setMaxCardinality(2);

        MessageList list = callValidator(gen);
        assertEquals(2, list.size());
        Message message = list.getMessage(0);
        assertEquals(Message.ERROR, message.getSeverity());
        assertEquals(IProductCmptLink.MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN, message.getCode());
        assertEquals(1, message.getInvalidObjectProperties().length);
        assertEquals(link1, message.getInvalidObjectProperties()[0].getObject());
        assertEquals(IProductCmptLink.PROPERTY_MIN_CARDINALITY, message.getInvalidObjectProperties()[0].getProperty());
        message = list.getMessage(1);
        assertEquals(Message.ERROR, message.getSeverity());
        assertEquals(IProductCmptLink.MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN, message.getCode());
        assertEquals(1, message.getInvalidObjectProperties().length);
        assertEquals(link2, message.getInvalidObjectProperties()[0].getObject());
        assertEquals(IProductCmptLink.PROPERTY_MIN_CARDINALITY, message.getInvalidObjectProperties()[0].getProperty());
    }

    @Test
    public void testValidateTotalMin_NoError() {
        associationPolicy.setMinCardinality(5);
        associationPolicy.setMaxCardinality(Integer.MAX_VALUE);
        link1.setMaxCardinality(Integer.MAX_VALUE);
        link2.setMaxCardinality(6);

        MessageList list = callValidator(gen);
        assertEquals(0, list.size());
    }

    @Test
    public void testValidateTotalMax() {
        associationPolicy.setMinCardinality(1);
        associationPolicy.setMaxCardinality(8);
        link1.setMinCardinality(1);
        link1.setMaxCardinality(Integer.MAX_VALUE);
        link2.setMinCardinality(2);
        link2.setMaxCardinality(8);

        MessageList list = callValidator(gen);
        assertEquals(2, list.size());
        Message message = list.getMessage(0);
        assertEquals(Message.ERROR, message.getSeverity());
        assertEquals(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX, message.getCode());
        assertEquals(1, message.getInvalidObjectProperties().length);
        assertEquals(link1, message.getInvalidObjectProperties()[0].getObject());
        assertEquals(IProductCmptLink.PROPERTY_MAX_CARDINALITY, message.getInvalidObjectProperties()[0].getProperty());
        message = list.getMessage(1);
        assertEquals(Message.ERROR, message.getSeverity());
        assertEquals(IProductCmptLink.MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX, message.getCode());
        assertEquals(1, message.getInvalidObjectProperties().length);
        assertEquals(link2, message.getInvalidObjectProperties()[0].getObject());
        assertEquals(IProductCmptLink.PROPERTY_MAX_CARDINALITY, message.getInvalidObjectProperties()[0].getProperty());
    }

    @Test
    public void testValidateTotalMax_NoError() {
        associationPolicy.setMinCardinality(1);
        associationPolicy.setMaxCardinality(8);
        link1.setMinCardinality(1);
        link1.setMaxCardinality(6);
        link2.setMinCardinality(2);
        link2.setMaxCardinality(7);

        MessageList list = callValidator(gen);
        assertEquals(0, list.size());
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
