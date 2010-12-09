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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * @author Alexander Weickmann
 */
public class RenameAssociationProcessorTest extends AbstractIpsRefactoringTest {

    private final static String POLICY_ROLE_SINGULAR = "PolicyRoleSingular";

    private final static String POLICY_ROLE_PLURAL = "PolicyRolePlural";

    private final static String PRODUCT_ROLE_SINGULAR = "ProductRoleSingular";

    private final static String PRODUCT_ROLE_PLURAL = "ProductRolePlural";

    private IPolicyCmptTypeAssociation policyToOtherPolicyAssociation;

    private IProductCmptTypeAssociation productToOtherProductAssociation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        policyToOtherPolicyAssociation = policyCmptType.newPolicyCmptTypeAssociation();
        policyToOtherPolicyAssociation.setTarget(otherPolicyCmptType.getQualifiedName());
        policyToOtherPolicyAssociation.setTargetRoleSingular(POLICY_ROLE_SINGULAR);
        policyToOtherPolicyAssociation.setTargetRolePlural(POLICY_ROLE_PLURAL);
        policyToOtherPolicyAssociation.setInverseAssociation(otherPolicyToPolicyAssociation.getName());
        otherPolicyToPolicyAssociation.setInverseAssociation(policyToOtherPolicyAssociation.getName());

        productToOtherProductAssociation = productCmptType.newProductCmptTypeAssociation();
        productToOtherProductAssociation.setTarget(otherProductCmptType.getQualifiedName());
        productToOtherProductAssociation.setTargetRoleSingular(PRODUCT_ROLE_SINGULAR);
        productToOtherProductAssociation.setTargetRolePlural(PRODUCT_ROLE_PLURAL);
    }

    public void testRenamePolicyCmptTypeAssociation() throws CoreException {
        String newAssociationName = "foo";
        String newPluralAssociationName = "bar";
        performRenameRefactoring(policyToOtherPolicyAssociation, newAssociationName, newPluralAssociationName);

        // Check for changed association roles
        assertNull(policyCmptType.getAssociation(POLICY_ROLE_SINGULAR));
        assertNull(policyCmptType.getAssociationByRoleNamePlural(POLICY_ROLE_PLURAL));
        assertNotNull(policyCmptType.getAssociation(newAssociationName));
        assertNotNull(policyCmptType.getAssociationByRoleNamePlural(newPluralAssociationName));
        assertTrue(policyToOtherPolicyAssociation.getTargetRoleSingular().equals(newAssociationName));
        assertTrue(policyToOtherPolicyAssociation.getTargetRolePlural().equals(newPluralAssociationName));

        // Check for inverse association update
        assertEquals(newAssociationName, otherPolicyToPolicyAssociation.getInverseAssociation());
    }

    public void testRenameProductCmptTypeAssociation() throws CoreException {
        String newAssociationName = "foo";
        String newPluralAssociationName = "bar";
        performRenameRefactoring(productToOtherProductAssociation, newAssociationName, newPluralAssociationName);

        // Check for changed association roles
        assertNull(productCmptType.getAssociation(PRODUCT_ROLE_SINGULAR));
        assertNull(productCmptType.getAssociationByRoleNamePlural(PRODUCT_ROLE_PLURAL));
        assertNotNull(productCmptType.getAssociation(newAssociationName));
        assertNotNull(productCmptType.getAssociationByRoleNamePlural(newPluralAssociationName));
        assertTrue(productToOtherProductAssociation.getTargetRoleSingular().equals(newAssociationName));
        assertTrue(productToOtherProductAssociation.getTargetRolePlural().equals(newPluralAssociationName));
    }

}
