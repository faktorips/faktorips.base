/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
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
