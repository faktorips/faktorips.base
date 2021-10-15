/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import static org.faktorips.testsupport.IpsMatchers.containsErrorMessage;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.IAttribute;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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
        super.setUp();
        openFixDifferencesToModelWizardAction = new OpenFixDifferencesToModelWizardAction();
        ipsElementsToFix = new HashSet<>();
        ipsProject = newIpsProject();
        root = ipsProject.findIpsPackageFragmentRoot("productdef");

        productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        productCmpt = newProductCmpt(productCmptType, "a.b.ProductCmpt");
        productCmpt2 = newProductCmpt(productCmptType, "a.c.ProductCmpt");

        IAttribute newAttribute = productCmptType.newAttribute();
        newAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
        productCmptType.getIpsSrcFile().save(true, null);

        while (productCmptType.getIpsSrcFile().isDirty()) {
            waitForIndexer();
        }
    }

    @Test
    public void testAddElementToFix_IpsProject() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt, containsDifferenceToModel(ipsProject));
        assertThat(productCmpt2, containsDifferenceToModel(ipsProject));

        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, ipsProject);

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, hasItem(productCmpt2));
    }

    @Test
    public void testAddElementToFix_JavaProject() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt, containsDifferenceToModel(ipsProject));
        assertThat(productCmpt2, containsDifferenceToModel(ipsProject));

        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, ipsProject.getJavaProject());

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, hasItem(productCmpt2));
    }

    @Test
    public void testAddElementToFix_IpsPackageFragmentRoot() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt, containsDifferenceToModel(ipsProject));
        assertThat(productCmpt2, containsDifferenceToModel(ipsProject));

        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, root);

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, hasItem(productCmpt2));
    }

    @Test
    public void testAddElementToFix_DefaultIpsPackageFragment() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt, containsDifferenceToModel(ipsProject));
        assertThat(productCmpt2, containsDifferenceToModel(ipsProject));

        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, root.getDefaultIpsPackageFragment());

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, hasItem(productCmpt2));
    }

    @Test
    public void testAddElementToFix_PackageFragment() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt, containsDifferenceToModel(ipsProject));
        assertThat(productCmpt2, containsDifferenceToModel(ipsProject));

        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, root.getIpsPackageFragment(""));

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, hasItem(productCmpt2));
    }

    @Test
    public void testAddElementToFix_PackageFragment_FirstLevel() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt, containsDifferenceToModel(ipsProject));
        assertThat(productCmpt2, containsDifferenceToModel(ipsProject));

        IIpsPackageFragment ipsPackageFragment = root.getIpsPackageFragment("a");
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, ipsPackageFragment);

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, hasItem(productCmpt2));
    }

    @Test
    public void testAddElementToFix_PackageFragment_SecondLevel_First() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt, containsDifferenceToModel(ipsProject));

        IIpsPackageFragment ipsPackageFragment = root.getIpsPackageFragment("a.b");
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, ipsPackageFragment);

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, not(hasItem(productCmpt2)));
    }

    @Test
    public void testAddElementToFix_IpsSrcFile_SecondLevel_First() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt, containsDifferenceToModel(ipsProject));

        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, productCmpt.getIpsSrcFile());

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, not(hasItem(productCmpt2)));
    }

    @Test
    public void testAddElementToFix_PackageFragment_SecondLevel_Second() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt2, containsDifferenceToModel(ipsProject));

        IIpsPackageFragment ipsPackageFragment = root.getIpsPackageFragment("a.c");
        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, ipsPackageFragment);

        assertThat(ipsElementsToFix, not(hasItem(productCmpt)));
        assertThat(ipsElementsToFix, hasItem(productCmpt2));
    }

    @Test
    public void testAddElementToFix_IpsSrcFile_SecondLevel_Second() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt2, containsDifferenceToModel(ipsProject));

        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, productCmpt2.getIpsSrcFile());

        assertThat(ipsElementsToFix, not(hasItem(productCmpt)));
        assertThat(ipsElementsToFix, hasItem(productCmpt2));
    }

    @Test
    public void testAddElementToFix_ProductCmpt() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt, containsDifferenceToModel(ipsProject));

        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, productCmpt);

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, not(hasItem(productCmpt2)));
    }

    @Test
    public void testAddElementToFix_PackageFragment_IpsSrcFiles() throws CoreException {
        for (IIpsPackageFragment fragment : root.getIpsPackageFragments()) {
            for (IIpsElement element : fragment.getIpsSrcFiles()) {
                openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, element);
            }
        }

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, hasItem(productCmpt2));
    }

    @Test
    public void testAddElementToFix_IResource() throws CoreException {
        // check preconditions, because this test fails randomly
        assertThat("Only when the ipsProject is valid can the differences be added", ipsProject.validate(),
                not(containsErrorMessage()));
        assertThat(productCmpt, containsDifferenceToModel(ipsProject));
        assertThat(productCmpt2, containsDifferenceToModel(ipsProject));

        IResource resource = ipsProject.getProject();

        openFixDifferencesToModelWizardAction.addElementToFix(ipsElementsToFix, resource);

        assertThat(ipsElementsToFix, hasItem(productCmpt));
        assertThat(ipsElementsToFix, hasItem(productCmpt2));
    }

    private static Matcher<IFixDifferencesToModelSupport> containsDifferenceToModel(final IIpsProject ipsProject) {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("contain a difference to the model");
            }

            @Override
            protected boolean matchesSafely(IFixDifferencesToModelSupport arg0) {
                try {
                    return arg0.containsDifferenceToModel(ipsProject);
                } catch (CoreException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
    }

}