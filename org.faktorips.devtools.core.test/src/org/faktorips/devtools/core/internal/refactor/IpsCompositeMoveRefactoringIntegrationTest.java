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

package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class IpsCompositeMoveRefactoringIntegrationTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
    }

    @Test
    public void testMoveTwoRelatedIpsObjects() throws CoreException {
        // Create IPS project with packages
        IIpsPackageFragment originalIpsPackageFragment = ipsProject.getIpsPackageFragmentRoots()[0]
                .createPackageFragment("pack1", true, null);
        IIpsPackageFragment targetIpsPackageFragment = ipsProject.getIpsPackageFragmentRoots()[0]
                .createPackageFragment("pack2", true, null);

        // Create policy product component types
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "pack1.Policy");
        policyCmptType.setConfigurableByProductCmptType(true);
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "pack1.Product");
        productCmptType.setConfigurationForPolicyCmptType(true);

        // Relate the policy and the product component type with each other
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());

        // Execute the composite move refactoring
        performCompositeMoveRefactoring(new LinkedHashSet<IIpsObject>(Arrays.asList(policyCmptType, productCmptType)),
                targetIpsPackageFragment);

        assertSourceFilesNonExistent(originalIpsPackageFragment);
        assertSourceFilesExistent(targetIpsPackageFragment);

        IPolicyCmptType newPolicyCmptType = (IPolicyCmptType)targetIpsPackageFragment.getIpsSrcFile("Policy",
                IpsObjectType.POLICY_CMPT_TYPE).getIpsObject();
        IProductCmptType newProductCmptType = (IProductCmptType)targetIpsPackageFragment.getIpsSrcFile("Product",
                IpsObjectType.PRODUCT_CMPT_TYPE).getIpsObject();

        printValidationResult(newPolicyCmptType);
        printValidationResult(newProductCmptType);
        assertTrue(newPolicyCmptType.isValid(ipsProject));
        assertTrue(newProductCmptType.isValid(ipsProject));
    }

    private void assertSourceFilesNonExistent(IIpsPackageFragment ipsPackageFragment) {
        assertFalse(ipsPackageFragment.getIpsSrcFile("Policy", IpsObjectType.POLICY_CMPT_TYPE).exists());
        assertFalse(ipsPackageFragment.getIpsSrcFile("Product", IpsObjectType.PRODUCT_CMPT_TYPE).exists());
    }

    private void assertSourceFilesExistent(IIpsPackageFragment ipsPackageFragment) {
        assertTrue(ipsPackageFragment.getIpsSrcFile("Policy", IpsObjectType.POLICY_CMPT_TYPE).exists());
        assertTrue(ipsPackageFragment.getIpsSrcFile("Product", IpsObjectType.PRODUCT_CMPT_TYPE).exists());
    }

}
