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

    private IPolicyCmptTypeAssociation policyToOtherPolicyAssociation;

    private IProductCmptTypeAssociation productToOtherProductAssociation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        policyToOtherPolicyAssociation = policyCmptType.newPolicyCmptTypeAssociation();
        policyToOtherPolicyAssociation.setTarget(otherPolicyCmptType.getQualifiedName());
        policyToOtherPolicyAssociation.setInverseAssociation(otherPolicyToPolicyAssociation.getName());
        otherPolicyToPolicyAssociation.setInverseAssociation(policyToOtherPolicyAssociation.getName());

        productToOtherProductAssociation = productCmptType.newProductCmptTypeAssociation();
        productToOtherProductAssociation.setTarget(otherProductCmptType.getQualifiedName());
    }

    public void testRenamePolicyCmptTypeAssociation() throws CoreException {
        String newAssociationName = "foo";
        String newPluralAssociationName = "bar";
        performRenameRefactoring(policyToOtherPolicyAssociation, newAssociationName, newPluralAssociationName);
    }

}
