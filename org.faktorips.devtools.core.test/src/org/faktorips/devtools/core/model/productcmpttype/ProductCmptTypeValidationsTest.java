/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.productcmpttype;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptTypeValidationsTest {

    private String policyCmptType = "pcType1";

    private String superPolicyCmptType = "superPcType1";

    @Mock
    private IProductCmptType superProductCmptType;

    @Mock
    private IPolicyCmptType foundSuperPolicyCmptType;

    @Mock
    private IIpsProject ipsProject;

    @Before
    public void setUpIpsProject() throws CoreException {
        when(ipsProject.findPolicyCmptType(superPolicyCmptType)).thenReturn(foundSuperPolicyCmptType);
    }

    @Test
    public void testIsConsistentHierarchy_noSuperPolicyType() throws Exception {
        String policyCmptTypeOfSupertype = "";
        when(superProductCmptType.isConfigurationForPolicyCmptType()).thenReturn(true);

        boolean constistent = ProductCmptTypeValidations.isConsistentHierarchy(policyCmptType, superPolicyCmptType,
                policyCmptTypeOfSupertype, superProductCmptType, ipsProject);

        assertFalse(constistent);
    }

    @Test
    public void testIsConsistentHierarchy_configuredSuperPolicyType() throws Exception {
        String policyCmptTypeOfSupertype = superPolicyCmptType;
        when(superProductCmptType.isConfigurationForPolicyCmptType()).thenReturn(true);
        when(foundSuperPolicyCmptType.isConfigurableByProductCmptType()).thenReturn(true);

        boolean constistent = ProductCmptTypeValidations.isConsistentHierarchy(policyCmptType, superPolicyCmptType,
                policyCmptTypeOfSupertype, superProductCmptType, ipsProject);

        assertTrue(constistent);
    }

    @Test
    public void testIsConsistentHierarchy_noPolicySuperType() throws Exception {
        String policyCmptTypeOfSupertype = "";
        foundSuperPolicyCmptType = null;
        superPolicyCmptType = "";
        when(superProductCmptType.isConfigurationForPolicyCmptType()).thenReturn(false);

        boolean constistent = ProductCmptTypeValidations.isConsistentHierarchy(policyCmptType, superPolicyCmptType,
                policyCmptTypeOfSupertype, superProductCmptType, ipsProject);

        assertTrue(constistent);
    }

    @Test
    public void testIsConsistentHierarchy_superTypeConfiguresSameType() throws Exception {
        String policyCmptTypeOfSupertype = policyCmptType;

        boolean constistent = ProductCmptTypeValidations.isConsistentHierarchy(policyCmptType, superPolicyCmptType,
                policyCmptTypeOfSupertype, superProductCmptType, ipsProject);

        assertTrue(constistent);
    }

}
