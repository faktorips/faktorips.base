/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptLinkContainerValidatorIntegrationTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IIpsProject ipsProject;

    private IPolicyCmptType targetPolicyType;
    private IProductCmptType targetProductType;
    private IProductCmptTypeAssociation association;
    private IProductCmpt target1;
    private IProductCmpt target2;
    private IProductCmptLink link1;
    private IProductCmptLink link2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);

        targetPolicyType = newPolicyAndProductCmptType(ipsProject, "TargetPolicyType", "TargetProductType");
        targetProductType = targetPolicyType.findProductCmptType(ipsProject);
        target1 = newProductCmpt(targetProductType, "TargetProduct");
        target2 = newProductCmpt(targetProductType, "TargetProduct2");

        association = productCmptType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setTarget(targetProductType.getQualifiedName());
        association.setTargetRoleSingular("testRelationProductSide");
        association.setTargetRolePlural("testRelationsProductSide");

        link1 = productCmpt.newLink("testRelationProductSide");
        link2 = productCmpt.newLink("testRelationProductSide");
        link1.setTarget(target1.getQualifiedName());
        link2.setTarget(target2.getQualifiedName());
    }

    private MessageList callValidator() {
        MessageList list = new MessageList();
        ProductCmptLinkContainerValidator validator = new ProductCmptLinkContainerValidator(ipsProject, productCmpt,
                list);
        try {
            validator.start(productCmptType);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return list;
    }

    @Test
    public void testValidateMinumumCardinality() {
        association.setMinCardinality(3);
        association.setMaxCardinality(5);

        MessageList list = callValidator();
        assertEquals(1, list.size());
    }

    private List<IProductCmptLink> list(IProductCmptLink... links) {
        return Arrays.asList(links);
    }

    @Test
    public void testValidateMaximumCardinality() {

    }

    @Test
    public void testValidateDuplicateTarget() {

    }

    @Test
    public void testValidateChangingOverTime() {

    }
}
