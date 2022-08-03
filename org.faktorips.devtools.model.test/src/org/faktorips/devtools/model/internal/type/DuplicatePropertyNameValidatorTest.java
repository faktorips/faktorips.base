/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.junit.Before;
import org.junit.Test;

public class DuplicatePropertyNameValidatorTest extends AbstractIpsPluginTest {

    private static final String ID = "MY_ID";
    private DuplicatePropertyNameValidator validatorTest;
    private IPolicyCmptTypeAssociation toVA;
    private IPolicyCmptTypeAssociation toA;
    private IPolicyCmptTypeAssociation toB;
    private IPolicyCmptTypeAssociation toVB;
    private IPolicyCmptTypeAssociation toC;
    private IPolicyCmptTypeAssociation toVC;
    private PolicyCmptType policyCmptTypeA;
    private ObjectProperty opToVA;
    private ObjectProperty opToVB;
    private ObjectProperty opToVC;
    private IPolicyCmptTypeAssociation toO;
    private IPolicyCmptTypeAssociation toAO;
    private ObjectProperty opToVO;
    private IIpsProject ipsProject;
    private MessageList messageList = new MessageList();
    private PolicyCmptType policyCmptTypeOther;
    private PolicyCmptType policyCmptTypeV;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptTypeA = newPolicyCmptType(ipsProject, "pctA");
        PolicyCmptType policyCmptTypeB = newPolicyCmptType(ipsProject, "pctB");
        PolicyCmptType policyCmptTypeB1 = newPolicyCmptType(ipsProject, "pctB1");
        PolicyCmptType policyCmptTypeC = newPolicyCmptType(ipsProject, "pctC");
        policyCmptTypeV = newPolicyCmptType(ipsProject, "pctV");
        policyCmptTypeOther = newPolicyCmptType(ipsProject, "pctO");

        policyCmptTypeB.setSupertype(policyCmptTypeA.getName());
        policyCmptTypeC.setSupertype(policyCmptTypeB.getName());
        policyCmptTypeB1.setSupertype(policyCmptTypeA.getName());

        validatorTest = policyCmptTypeA.createDuplicatePropertyNameValidator(ipsProject);

        toA = (IPolicyCmptTypeAssociation)policyCmptTypeV.newAssociation();
        toA.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toA.setTarget(policyCmptTypeA.getName());
        toA.setTargetRoleSingular("toA");
        toA.setTargetRolePlural("toAs");

        toVA = (IPolicyCmptTypeAssociation)policyCmptTypeA.newAssociation();
        toVA.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toVA.setTarget(policyCmptTypeV.getName());
        toVA.setTargetRoleSingular("toV");
        toVA.setTargetRolePlural("toV");
        toVA.setInverseAssociation(toA.getName());

        toB = (IPolicyCmptTypeAssociation)policyCmptTypeV.newAssociation();
        toB.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toB.setTarget(policyCmptTypeA.getName());
        toB.setTargetRoleSingular("toB");
        toB.setTargetRolePlural("toBs");

        toVB = (IPolicyCmptTypeAssociation)policyCmptTypeB.newAssociation();
        toVB.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toVB.setTarget(policyCmptTypeV.getName());
        toVB.setTargetRoleSingular("toV");
        toVB.setTargetRolePlural("toV");
        toVB.setInverseAssociation(toB.getName());

        toC = (IPolicyCmptTypeAssociation)policyCmptTypeV.newAssociation();
        toC.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toC.setTarget(policyCmptTypeA.getName());
        toC.setTargetRoleSingular("toC");
        toC.setTargetRolePlural("toCs");

        toVC = (IPolicyCmptTypeAssociation)policyCmptTypeC.newAssociation();
        toVC.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toVC.setTarget(policyCmptTypeV.getName());
        toVC.setTargetRoleSingular("toV");
        toVC.setTargetRolePlural("toV");
        toVC.setInverseAssociation(toC.getName());

        toO = (IPolicyCmptTypeAssociation)policyCmptTypeV.newAssociation();
        toO.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        toO.setTarget(policyCmptTypeA.getName());
        toO.setTargetRoleSingular("toO");
        toO.setTargetRolePlural("toOs");

        toAO = (IPolicyCmptTypeAssociation)policyCmptTypeOther.newAssociation();
        toAO.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        toAO.setTarget(policyCmptTypeA.getName());
        toAO.setTargetRoleSingular("toV");
        toAO.setTargetRolePlural("toV");
        toAO.setInverseAssociation(toO.getName());

        opToVA = new ObjectProperty(toVA, "toV");
        opToVB = new ObjectProperty(toVB, "toV");
        opToVC = new ObjectProperty(toVC, "toV");
        opToVO = new ObjectProperty(toAO, "toV");

    }

    @Test
    public void testIgnore() {
        ObjectProperty[] objectProperties = { opToVB, opToVA };
        // both are not inverse of derived union - only one is valid
        assertFalse(validatorTest.ignore(policyCmptTypeA, objectProperties));
        toA.setDerivedUnion(true);
        assertTrue(validatorTest.ignore(policyCmptTypeA, objectProperties));

        objectProperties = new ObjectProperty[] { opToVC, opToVB, opToVA };
        // both toVC and toVB are no inverse of derived unions!
        assertFalse(validatorTest.ignore(policyCmptTypeA, objectProperties));
        toB.setDerivedUnion(true);
        assertTrue(validatorTest.ignore(policyCmptTypeA, objectProperties));

        objectProperties = new ObjectProperty[] { opToVO, opToVB, opToVA };
        assertFalse(validatorTest.ignore(policyCmptTypeA, objectProperties));

        objectProperties = new ObjectProperty[] { opToVA, opToVO };
        assertFalse(validatorTest.ignore(policyCmptTypeA, objectProperties));

        objectProperties = new ObjectProperty[] { opToVO, opToVA };
        assertFalse(validatorTest.ignore(policyCmptTypeA, objectProperties));

        objectProperties = new ObjectProperty[] { opToVO, opToVO };
        assertFalse(validatorTest.ignore(policyCmptTypeOther, objectProperties));
    }

    public void testIgnore_notInCurrentType() {
        ObjectProperty[] objectProperties = { opToVO, opToVO };
        assertTrue(validatorTest.ignore(policyCmptTypeA, objectProperties));
    }

    @Test
    public void testIgnore_notIgnored() {
        ObjectProperty[] objectProperties = { opToVB, opToVA };
        // both are not inverse of derived union - only one is valid
        assertFalse(validatorTest.ignore(policyCmptTypeA, objectProperties));
    }

    /**
     * The constrain is set correct but both associations are in the same type.
     */
    @Test
    public void testIgnore_constrainSameType() {
        ObjectProperty[] objectProperties = { new ObjectProperty(toB, "toB"),
                new ObjectProperty(toA, "toA") };
        toB.setConstrain(true);
        toB.setTargetRoleSingular("toA");
        toB.setTargetRolePlural("toAs");

        assertFalse(validatorTest.ignore(policyCmptTypeV, objectProperties));
    }

    /**
     * The constrain is set correct but both associations are in the same type.
     */
    @Test
    public void testIgnore_constrain() {
        ObjectProperty[] objectProperties = { opToVB, opToVA };
        toVB.setConstrain(true);
        toVB.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        assertTrue(validatorTest.ignore(policyCmptTypeA, objectProperties));
    }

    @Test
    public void testIgnore_wrongConstrain() {
        ObjectProperty[] objectProperties = { opToVB, opToVA };
        toVA.setConstrain(true);

        assertFalse(validatorTest.ignore(policyCmptTypeA, objectProperties));
    }

    @Test
    public void testAddInvalidPolicyAttributes() {
        IProductCmptType productCmptType = policyCmptTypeA.findProductCmptType(ipsProject);
        policyCmptTypeA.newPolicyCmptTypeAttribute(productCmptType.getUnqualifiedName());

        validatorTest.visit(policyCmptTypeA);
        validatorTest.addMessagesForDuplicates(policyCmptTypeA, messageList);

        assertEquals(1, messageList.size());
        assertEquals(IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE,
                messageList.getMessage(0).getInvalidObjectProperties().get(1).getProperty());
    }

    @Test
    public void testAddPolicyAndProductAttributes_PolicyCmptType() {
        IProductCmptType productCmptType = policyCmptTypeA.findProductCmptType(ipsProject);
        productCmptType.newProductCmptTypeAttribute(ID);
        policyCmptTypeA.newPolicyCmptTypeAttribute(ID);

        validatorTest.visit(policyCmptTypeA);
        validatorTest.addMessagesForDuplicates(policyCmptTypeA, messageList);

        assertEquals(1, messageList.size());
        assertEquals(IAssociation.PROPERTY_NAME,
                messageList.getMessage(0).getInvalidObjectProperties().get(0).getProperty());
    }

    @Test
    public void testAddPolicyAndProductAttributes_ProdCmptType() {
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "prodCmptType", policyCmptTypeA);
        productCmptType.newProductCmptTypeAttribute(ID);
        policyCmptTypeA.newPolicyCmptTypeAttribute(ID);
        validatorTest = productCmptType.createDuplicatePropertyNameValidator(ipsProject);

        validatorTest.visit(productCmptType);
        validatorTest.addMessagesForDuplicates(policyCmptTypeA, messageList);

        assertEquals(1, messageList.size());
        assertEquals(IAssociation.PROPERTY_NAME,
                messageList.getMessage(0).getInvalidObjectProperties().get(0).getProperty());
    }

    @Test
    public void testCreateMessage_SameITypeAndObjectPartContainer() {
        IPolicyCmptTypeAttribute attr1 = policyCmptTypeA.newPolicyCmptTypeAttribute(ID);
        IPolicyCmptTypeAttribute attr2 = policyCmptTypeA.newPolicyCmptTypeAttribute(ID);
        validatorTest.visit(policyCmptTypeA);
        validatorTest.addMessagesForDuplicates(policyCmptTypeA, messageList);
        ObjectProperty property2 = new ObjectProperty(attr1, IAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        ObjectProperty property1 = new ObjectProperty(attr2, IMethod.PROPERTY_NAME);
        ObjectProperty[] properties = { property1, property2 };

        Message message = validatorTest.createMessage(ID, properties);
        String text = NLS.bind(Messages.DuplicatePropertyNameValidator_msg, ID, StringUtils.EMPTY);
        assertTrue(message.getText().contains(text));
    }

    @Test
    public void testCreateMessage_SameITypeAndDifferentObjectPartContainer() {
        IPolicyCmptTypeAttribute attr = policyCmptTypeA.newPolicyCmptTypeAttribute(ID);
        IPolicyCmptTypeAssociation association = policyCmptTypeA.newPolicyCmptTypeAssociation();
        ObjectProperty property2 = new ObjectProperty(attr, IAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        ObjectProperty property1 = new ObjectProperty(association, IMethod.PROPERTY_NAME);
        ObjectProperty[] properties = { property1, property2 };

        Message message = validatorTest.createMessage(ID, properties);

        String text = NLS.bind(Messages.DuplicatePropertyNameValidator_msg_DifferentElementsSameType,
                Messages.DuplicatePropertyNameValidator_PluralAssociation,
                Messages.DuplicatePropertyNameValidator_PluralAttribute);
        assertTrue(message.getText().contains(StringUtils.capitalize(text)));
    }

    @Test
    public void testCreateMessage_SameITypeAndDifferentObjectPartContainer_ProdCmptValidator() {
        ProductCmptType productCmptType = newProductCmptType(ipsProject, ID);
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        ITableStructureUsage tableUsage = productCmptType.newTableStructureUsage();
        ObjectProperty property2 = new ObjectProperty(tableUsage, ITableStructureUsage.PROPERTY_NAME);
        ObjectProperty property1 = new ObjectProperty(method, IMethod.PROPERTY_NAME);
        ObjectProperty[] properties = { property2, property1 };
        validatorTest = productCmptType.createDuplicatePropertyNameValidator(ipsProject);

        Message message = validatorTest.createMessage(ID, properties);

        String text = NLS.bind(Messages.DuplicatePropertyNameValidator_msg_DifferentElementsSameType,
                org.faktorips.devtools.model.internal.productcmpttype.Messages.TableStructureUsage_msg_Plural,
                Messages.DuplicatePropertyNameValidator_PluralMethod);
        assertTrue(message.getText().contains(StringUtils.capitalize(text)));
    }

    @Test
    public void testcreateMessage_DifferentITypesAndDifferentObjectPartContainer_ProdCmptValidator() {
        ProductCmptType productCmptType = newProductCmptType(ipsProject, ID);
        productCmptType.setPolicyCmptType(policyCmptTypeA.getUnqualifiedName());
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        IAssociation association = policyCmptTypeA.newAssociation();
        ObjectProperty property2 = new ObjectProperty(association, IAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        ObjectProperty property1 = new ObjectProperty(method, IMethod.PROPERTY_NAME);
        ObjectProperty[] properties = { property1, property2 };
        validatorTest = productCmptType.createDuplicatePropertyNameValidator(ipsProject);

        Message message = validatorTest.createMessage(ID, properties);

        String text = NLS.bind(Messages.DuplicatePropertyNameValidator_msg_DifferentElementsAndITypes,
                Messages.DuplicatePropertyNameValidator_SingularAssociation, policyCmptTypeA.getName());
        assertTrue(message.getText().contains(text));
    }

    @Test
    public void testcreateMessage_PolicyAttrHasSameNameAsProdCmptType() {
        ProductCmptType productCmptType = newProductCmptType(ipsProject, ID);
        productCmptType.setPolicyCmptType(policyCmptTypeA.getUnqualifiedName());
        IAttribute attribute = policyCmptTypeA.newAttribute();
        ObjectProperty property2 = new ObjectProperty(attribute, IAttribute.PROPERTY_NAME);
        ObjectProperty property1 = new ObjectProperty(policyCmptTypeA, IPolicyCmptType.PROPERTY_PRODUCT_CMPT_TYPE);
        ObjectProperty[] properties = { property2, property1 };

        Message message = validatorTest.createMessage(ID, properties);

        String text = NLS.bind(Messages.DuplicatePropertyNameValidator_msg_DifferentElementsSameType,
                Messages.DuplicatePropertyNameValidator_PluralAttribute,
                org.faktorips.devtools.model.internal.productcmpttype.Messages.ProductCmptType_pluralCaption);
        assertTrue(message.getText().contains(StringUtils.capitalize(text)));
    }
}
