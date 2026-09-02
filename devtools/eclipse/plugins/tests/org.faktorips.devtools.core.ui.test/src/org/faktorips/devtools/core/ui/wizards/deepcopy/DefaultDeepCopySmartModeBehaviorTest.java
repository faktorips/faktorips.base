/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultDeepCopySmartModeBehaviorTest extends AbstractIpsPluginTest {

    private final DefaultDeepCopySmartModeBehavior behavior = new DefaultDeepCopySmartModeBehavior();

    private IIpsProject ipsProject;
    private ProductCmptType mainType;
    private ProductCmpt mainCmpt;
    private IIpsPackageFragmentRoot root;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        mainType = newProductCmptType(ipsProject, "MainType");
        ProductCmptType coverageType = newProductCmptType(ipsProject, "CoverageType");
        IProductCmptTypeAssociation association = mainType.newProductCmptTypeAssociation();
        association.setTarget(coverageType.getQualifiedName());
        association.setTargetRoleSingular("Coverage");

        mainCmpt = newProductCmpt(mainType, "produkte.hr_kompakt.HR-Kompakt");
        root = mainCmpt.getIpsPackageFragment().getRoot();
    }

    @Test
    public void testGetCopyOrLink_SubPackageOfRoot_IsCopy() throws Exception {
        ProductCmpt coverage = newProductCmpt(mainType, "produkte.hr_kompakt.deckungen.Grunddeckung");
        IProductCmptStructureReference reference = linkAndGetReference(coverage);

        assertThat(behavior.getCopyOrLink(root, reference), is(CopyOrLink.COPY));
    }

    @Test
    public void testGetCopyOrLink_SamePackageAsRoot_IsCopy() throws Exception {
        ProductCmpt coverage = newProductCmpt(mainType, "produkte.hr_kompakt.Grunddeckung");
        IProductCmptStructureReference reference = linkAndGetReference(coverage);

        assertThat(behavior.getCopyOrLink(root, reference), is(CopyOrLink.COPY));
    }

    @Test
    public void testGetCopyOrLink_SiblingPackageOfRoot_IsLink() throws Exception {
        ProductCmpt coverage = newProductCmpt(mainType, "produkte.hr_zusatzdeckungen.Zusatzdeckung");
        IProductCmptStructureReference reference = linkAndGetReference(coverage);

        assertThat(behavior.getCopyOrLink(root, reference), is(CopyOrLink.LINK));
    }

    @Test
    public void testGetCopyOrLink_ParentPackageOfRoot_IsLink() throws Exception {
        ProductCmpt coverage = newProductCmpt(mainType, "produkte.Coverage");
        IProductCmptStructureReference reference = linkAndGetReference(coverage);

        assertThat(behavior.getCopyOrLink(root, reference), is(CopyOrLink.LINK));
    }

    private IProductCmptStructureReference linkAndGetReference(ProductCmpt target) throws Exception {
        IProductCmptGeneration generation = mainCmpt.getProductCmptGeneration(0);
        IProductCmptLink link = generation.newLink("Coverage");
        link.setTarget(target.getQualifiedName());
        mainCmpt.getIpsSrcFile().save(null);

        DeepCopyPresentationModel presentationModel = new DeepCopyPresentationModel(generation);
        IProductCmptTreeStructure structure = presentationModel.getStructure();
        for (IProductCmptStructureReference candidate : structure.toSet(false)) {
            if (candidate instanceof IProductCmptReference productCmptReference
                    && productCmptReference.getProductCmpt().equals(target)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No reference found for " + target);
    }

}
