/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.junit.Before;
import org.junit.Test;

public class OpenFixDifferencesToModelWizardActionTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot root;
    private OpenFixDifferencesToModelWizardAction openFixDifferencesToModelWizardAction;
    private Set<IFixDifferencesToModelSupport> ipsElementsToFix;
    private ProductCmptType productCmptType;
    private ProductCmpt productCmpt;
    private ProductCmpt productCmpt2;

    @Override
    @Before
    public void setUp() throws Exception {
        openFixDifferencesToModelWizardAction = new OpenFixDifferencesToModelWizardAction();
        ipsElementsToFix = new HashSet<IFixDifferencesToModelSupport>();
        ipsProject = newIpsProject();
        root = ipsProject.findIpsPackageFragmentRoot("productdef");

        productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        productCmpt = newProductCmpt(productCmptType, "a.b.ProductCmpt");
        productCmpt2 = newProductCmpt(productCmptType, "a.c.ProductCmpt");

        IAttribute newAttribute = productCmptType.newAttribute();
        newAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
        productCmptType.getIpsSrcFile().save(true, null);

        waitForIndexer();
    }

    @Test
    public void testAddElementToFix_IpsProject() throws CoreException {
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, ipsProject);

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertTrue(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_JavaProject() throws CoreException {
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, ipsProject.getJavaProject());

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertTrue(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_IpsPackageFragmentRoot() throws CoreException {
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, root);

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertTrue(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_DefaultIpsPackageFragment() throws CoreException {
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, root.getDefaultIpsPackageFragment());

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertTrue(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_PackageFragment() throws CoreException {
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, root.getIpsPackageFragment(""));

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertTrue(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_PackageFragment_FirstLevel() throws CoreException {
        IIpsPackageFragment ipsPackageFragment = root.getIpsPackageFragment("a");
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, ipsPackageFragment);

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertTrue(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_PackageFragment_SecondLevel_First() throws CoreException {
        IIpsPackageFragment ipsPackageFragment = root.getIpsPackageFragment("a.b");
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, ipsPackageFragment);

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertFalse(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_IpsSrcFile_SecondLevel_First() throws CoreException {
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, productCmpt.getIpsSrcFile());

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertFalse(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_PackageFragment_SecondLevel_Second() throws CoreException {
        IIpsPackageFragment ipsPackageFragment = root.getIpsPackageFragment("a.c");
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, ipsPackageFragment);

        assertFalse(ipsElementsToFix.contains(productCmpt));
        assertTrue(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_IpsSrcFile_SecondLevel_Second() throws CoreException {
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, productCmpt2.getIpsSrcFile());

        assertFalse(ipsElementsToFix.contains(productCmpt));
        assertTrue(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_ProductCmpt() throws CoreException {
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, productCmpt);

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertFalse(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_PackageFragment_IpsSrcFiles() throws CoreException {
        for (IIpsPackageFragment fragment : root.getIpsPackageFragments()) {
            for (IIpsElement element : fragment.getIpsSrcFiles()) {
                openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, element);
            }
        }

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertTrue(ipsElementsToFix.contains(productCmpt2));
    }

    @Test
    public void testAddElementToFix_IResource() throws CoreException {
        IResource resource = ipsProject.getProject();

        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, resource);

        assertTrue(ipsElementsToFix.contains(productCmpt));
        assertTrue(ipsElementsToFix.contains(productCmpt2));
    }

}