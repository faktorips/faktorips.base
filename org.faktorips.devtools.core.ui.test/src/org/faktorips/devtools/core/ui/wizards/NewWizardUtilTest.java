/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Test;

public class NewWizardUtilTest extends AbstractIpsPluginTest {

    @Test
    public void testCreateDefaultCategoriesIfNecessary() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "Product");
        List<IProductCmptCategory> categories = productCmptType.getCategories();
        for (IProductCmptCategory category : categories) {
            category.delete();
        }

        NewWizardUtil.createDefaultCategoriesIfNecessary(productCmptType);

        assertEquals(4, productCmptType.getCategories().size());
        assertTrue(productCmptType.findDefaultCategoryForFormulaSignatureDefinitions(ipsProject) != null);
        assertTrue(productCmptType.findDefaultCategoryForPolicyCmptTypeAttributes(ipsProject) != null);
        assertTrue(productCmptType.findDefaultCategoryForProductCmptTypeAttributes(ipsProject) != null);
        assertTrue(productCmptType.findDefaultCategoryForTableStructureUsages(ipsProject) != null);
        assertTrue(productCmptType.findDefaultCategoryForValidationRules(ipsProject) != null);
    }

    @Test
    public void testCreateDefaultCategoriesIfNecessary_DoNotCreateCategoriesIfProductCmptTypeHasSupertype()
            throws CoreException {

        IIpsProject ipsProject = newIpsProject();
        IProductCmptType superProductCmptType = newProductCmptType(ipsProject, "SuperProduct");
        IProductCmptType productCmptType = newProductCmptType(superProductCmptType, "Product");

        NewWizardUtil.createDefaultCategoriesIfNecessary(productCmptType);

        assertEquals(0, productCmptType.getCategories().size());
    }

}
