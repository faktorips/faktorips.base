/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.faktorips.abstracttest.core.AbstractCoreIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class IpsCompositeMoveRefactoringIntegrationTest extends AbstractCoreIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
    }

    @Test
    public void testMoveTwoRelatedIpsObjects() throws CoreRuntimeException {
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
