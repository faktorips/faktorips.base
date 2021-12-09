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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.productcmpt.treestructure.ProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.internal.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.junit.Before;
import org.junit.Test;

public class DeepCopyTreeStatusTest extends AbstractIpsPluginTest {

    private DeepCopyTreeStatus deepCopyTreeStatus;
    private IProductCmptType[] types;
    private IProductCmptTypeAssociation[] associations;
    private IProductCmpt[] productCmpts;
    private IProductCmptLink[] links;
    private IProductCmptGeneration[] productCmptGenerations;
    private ProductCmptTreeStructure structure;
    private IpsPreferences ipsPreferences;
    private String mode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();
        getCopyWizardMode();
    }

    private void getCopyWizardMode() {
        if (ipsPreferences.isCopyWizardModeCopy()) {
            mode = IpsPreferences.COPY_WIZARD_MODE_COPY;
        } else if (ipsPreferences.isCopyWizardModeLink()) {
            mode = IpsPreferences.COPY_WIZARD_MODE_LINK;
        } else {
            mode = IpsPreferences.COPY_WIZARD_MODE_SMARTMODE;
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        ipsPreferences.setCopyWizardMode(mode);
    }

    private void mockProducts() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        mockProducts(ipsProject, ipsProject);
    }

    private void mockProducts(IIpsProject rootIpsProject, IIpsProject childIpsProject) throws Exception {
        mockProducts(rootIpsProject, childIpsProject, false);
    }

    private void mockProducts(IIpsProject rootIpsProject, IIpsProject childIpsProject, boolean alternatePackageRoots)
            throws Exception {
        types = new IProductCmptType[6];
        for (int i = 0; i < types.length; i++) {
            types[i] = mock(IProductCmptType.class);
            when(types[i].getName()).thenReturn("type" + i);
        }

        associations = new IProductCmptTypeAssociation[4];
        for (int i = 0; i < associations.length; i++) {
            associations[i] = mock(IProductCmptTypeAssociation.class);
            when(associations[i].getName()).thenReturn("association" + i);
            when(associations[i].isRelevant()).thenReturn(true);
        }

        when(types[0].findAllNotDerivedAssociations(any(IIpsProject.class)))
                .thenReturn(Arrays.asList(associations[0], associations[1]));
        when(types[1].findAllNotDerivedAssociations(any(IIpsProject.class)))
                .thenReturn(Arrays.asList(associations[2]));
        when(types[2].findAllNotDerivedAssociations(any(IIpsProject.class)))
                .thenReturn(Arrays.asList(associations[3]));

        IIpsPackageFragmentRoot[] packageRoots = mockPackageRoots(alternatePackageRoots);
        IIpsPackageFragmentRoot[] childPackageRoots = childIpsProject == rootIpsProject ? packageRoots
                : mockPackageRoots(alternatePackageRoots);
        productCmpts = new IProductCmpt[6];
        links = new IProductCmptLink[6];
        productCmptGenerations = new IProductCmptGeneration[6];
        for (int i = 0; i < productCmpts.length; i++) {
            productCmpts[i] = mock(IProductCmpt.class);
            when(productCmpts[i].getName()).thenReturn(Integer.toString(i));
            productCmptGenerations[i] = mock(IProductCmptGeneration.class);
            when(productCmptGenerations[i].getParent()).thenReturn(productCmpts[i]);
            ArrayList<IIpsObjectGeneration> generations = new ArrayList<>();
            generations.add(productCmptGenerations[i]);
            when(productCmpts[i].getGenerations()).thenReturn(generations);
            when(productCmpts[i].getGenerationEffectiveOn(any(GregorianCalendar.class)))
                    .thenReturn(productCmptGenerations[i]);
            when(productCmpts[i].findProductCmptType(any(IIpsProject.class))).thenReturn(types[i]);
            when(productCmpts[i].getTableContentUsages()).thenReturn(new ITableContentUsage[0]);
            IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
            when(productCmpts[i].getIpsSrcFile()).thenReturn(srcFile);
            IIpsProject ipsProject = i < 3 ? rootIpsProject : childIpsProject;
            when(productCmpts[i].getIpsProject()).thenReturn(ipsProject);
            when(srcFile.getIpsProject()).thenReturn(ipsProject);
            IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
            IIpsPackageFragmentRoot packageFragmentRoot = i < 3 ? packageRoots[i % packageRoots.length]
                    : childPackageRoots[i % childPackageRoots.length];
            when(packageFragment.getRoot()).thenReturn(packageFragmentRoot);
            when(srcFile.getIpsPackageFragment()).thenReturn(packageFragment);
            when(productCmpts[i].getIpsPackageFragment()).thenReturn(packageFragment);
            when(productCmptGenerations[i].getTableContentUsages()).thenReturn(new ITableContentUsage[0]);
            links[i] = mock(IProductCmptLink.class);
        }

        /* @formatter:off
         * P1: P2  packageRoot
         * 0_:
         * |\:\_
         * | :\ \
         * 2 : 1_\ rootProject
         * --------------------
         * | : | 3 childProject
         * 4 : 5
         * @formatter:on */
        mockLink(links[0], associations[0], productCmpts[0], productCmpts[1]);
        mockLink(links[1], associations[0], productCmpts[0], productCmpts[2]);
        mockLink(links[2], associations[1], productCmpts[0], productCmpts[3]);
        mockLink(links[3], associations[2], productCmpts[1], productCmpts[5]);
        mockLink(links[4], associations[2], productCmpts[1], productCmpts[3]);
        mockLink(links[5], associations[3], productCmpts[2], productCmpts[4]);

        mockLinks(productCmptGenerations[0], links[0], links[1], links[2]);
        mockLinks(productCmptGenerations[1], links[3], links[4]);
        mockLinks(productCmptGenerations[2], links[5]);
        mockLinks(productCmptGenerations[3]);
        mockLinks(productCmptGenerations[4]);
        mockLinks(productCmptGenerations[5]);

        List<IValidationRuleConfig> ruleConfigs = new ArrayList<>();
        ruleConfigs.add(mock(IValidationRuleConfig.class));
        ruleConfigs.add(mock(IValidationRuleConfig.class));
        when(ruleConfigs.get(0).getName()).thenReturn("Rule1");
        when(ruleConfigs.get(1).getName()).thenReturn("RuleTwo");

        when(productCmptGenerations[0].getValidationRuleConfigs()).thenReturn(ruleConfigs);
        when(productCmptGenerations[1].getValidationRuleConfigs()).thenReturn(new ArrayList<IValidationRuleConfig>());
        when(productCmptGenerations[2].getValidationRuleConfigs()).thenReturn(new ArrayList<IValidationRuleConfig>());
        when(productCmptGenerations[3].getValidationRuleConfigs()).thenReturn(new ArrayList<IValidationRuleConfig>());
        when(productCmptGenerations[4].getValidationRuleConfigs()).thenReturn(new ArrayList<IValidationRuleConfig>());
        when(productCmptGenerations[5].getValidationRuleConfigs()).thenReturn(new ArrayList<IValidationRuleConfig>());
    }

    private void initDeepCopyTreeStatusWithStructure() throws CycleInProductStructureException {
        initDeepCopyTreeStatusWithStructure(new DefaultDeepCopySmartModeBehavior());
    }

    private void initDeepCopyTreeStatusWithStructure(IDeepCopySmartModeBehavior deepCopySmartModeBehavior)
            throws CycleInProductStructureException {
        structure = new ProductCmptTreeStructure(productCmpts[0], new GregorianCalendar(), mock(IIpsProject.class));

        deepCopyTreeStatus = new DeepCopyTreeStatus(ipsPreferences, deepCopySmartModeBehavior);
        deepCopyTreeStatus.initialize(structure);
    }

    private IIpsPackageFragmentRoot[] mockPackageRoots(boolean alternatePackageRoots) {
        int number = alternatePackageRoots ? 2 : 1;
        IIpsPackageFragmentRoot[] packageFragmentRoots = new IIpsPackageFragmentRoot[number];
        for (int i = 0; i < number; i++) {
            packageFragmentRoots[i] = mock(IIpsPackageFragmentRoot.class);
        }
        return packageFragmentRoots;
    }

    private void mockLinks(IProductCmptGeneration generation, IProductCmptLink... links) {
        when(generation.getLinksIncludingProductCmpt()).thenReturn(Arrays.asList(links));
    }

    private void mockLink(IProductCmptLink link,
            IProductCmptTypeAssociation association,
            IProductCmpt source,
            IProductCmpt target) throws CoreRuntimeException {
        when(link.findAssociation(any(IIpsProject.class))).thenReturn(association);
        when(link.getProductCmpt()).thenReturn(source);
        when(link.getIpsObject()).thenReturn(source);
        when(link.findTarget(any(IIpsProject.class))).thenReturn(target);
    }

    @Test
    public void testIsEnabled() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_COPY);
        mockProducts();
        initDeepCopyTreeStatusWithStructure();
        Set<IProductCmptStructureReference> allCopyEnabledElements = deepCopyTreeStatus
                .getAllEnabledElements(CopyOrLink.COPY, structure, true);
        // 6 product components + 1 is referenced twice (productCmpts[3])
        assertEquals(7, allCopyEnabledElements.size());
        Set<IProductCmptStructureReference> allLinkEnabledElements = deepCopyTreeStatus
                .getAllEnabledElements(CopyOrLink.LINK, structure, true);
        assertEquals(0, allLinkEnabledElements.size());

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            assertTrue(deepCopyTreeStatus.isEnabled(reference));
        }

        IProductCmptStructureReference r1 = structure.getRoot().getChildren()[0].getChildren()[1];
        deepCopyTreeStatus.setCopyOrLink(r1, CopyOrLink.LINK);
        allCopyEnabledElements = deepCopyTreeStatus.getAllEnabledElements(CopyOrLink.COPY, structure, true);
        assertEquals(5, allCopyEnabledElements.size());
        allLinkEnabledElements = deepCopyTreeStatus.getAllEnabledElements(CopyOrLink.LINK, structure, true);
        assertEquals(1, allLinkEnabledElements.size());
        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference.getParent() == r1 || (!reference.isRoot() && reference.getParent().getParent() == r1)) {
                assertFalse(deepCopyTreeStatus.isEnabled(reference));
            } else {
                assertTrue(deepCopyTreeStatus.isEnabled(reference));
            }
        }

        deepCopyTreeStatus.setChecked(r1, false);
        allCopyEnabledElements = deepCopyTreeStatus.getAllEnabledElements(CopyOrLink.COPY, structure, true);
        assertEquals(5, allCopyEnabledElements.size());
        allLinkEnabledElements = deepCopyTreeStatus.getAllEnabledElements(CopyOrLink.LINK, structure, true);
        assertEquals(0, allLinkEnabledElements.size());
        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference == r1 || reference.getParent() == r1
                    || (!reference.isRoot() && reference.getParent().getParent() == r1)) {
                assertFalse(deepCopyTreeStatus.isEnabled(reference));
            } else {
                assertTrue(deepCopyTreeStatus.isEnabled(reference));
            }
        }
    }

    @Test
    public void testGetCopyOrLink_CopyModus() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_COPY);
        mockProducts();
        initDeepCopyTreeStatusWithStructure();

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference instanceof IProductCmptTypeAssociationReference) {
                assertEquals(CopyOrLink.UNDEFINED, deepCopyTreeStatus.getCopyOrLink(reference));
            } else {
                assertEquals(CopyOrLink.COPY, deepCopyTreeStatus.getCopyOrLink(reference));
            }
        }
    }

    @Test
    public void testGetCopyOrLink_LinkModus() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_LINK);
        mockProducts();
        initDeepCopyTreeStatusWithStructure();
        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference.isRoot()) {
                assertEquals(CopyOrLink.COPY, deepCopyTreeStatus.getCopyOrLink(reference));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertEquals(CopyOrLink.UNDEFINED, deepCopyTreeStatus.getCopyOrLink(reference));
            } else {
                assertEquals(CopyOrLink.LINK, deepCopyTreeStatus.getCopyOrLink(reference));
            }
        }
    }

    @Test
    public void testGetCopyOrLink_SmartModusSameIpsProject() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        mockProducts();
        initDeepCopyTreeStatusWithStructure();

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference.isRoot()) {
                assertEquals(CopyOrLink.COPY, deepCopyTreeStatus.getCopyOrLink(reference));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertEquals(CopyOrLink.UNDEFINED, deepCopyTreeStatus.getCopyOrLink(reference));
            } else {
                assertEquals(CopyOrLink.COPY, deepCopyTreeStatus.getCopyOrLink(reference));
            }
        }
    }

    @Test
    public void testGetCopyOrLink_SmartModusCustomSmartModeBehavior() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        mockProducts();
        mockTableContentUsage();
        IDeepCopySmartModeBehavior testDeepCopySmartModeBehavior = mock(IDeepCopySmartModeBehavior.class);
        when(testDeepCopySmartModeBehavior.getCopyOrLink(any(IIpsPackageFragmentRoot.class),
                any(IProductCmptStructureReference.class))).thenReturn(CopyOrLink.LINK);

        initDeepCopyTreeStatusWithStructure(testDeepCopySmartModeBehavior);

        // 6 product references and 1 table content usage
        verify(testDeepCopySmartModeBehavior, times(7)).getCopyOrLink(any(IIpsPackageFragmentRoot.class),
                any(IProductCmptStructureReference.class));
        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference.isRoot()) {
                assertEquals(CopyOrLink.COPY, deepCopyTreeStatus.getCopyOrLink(reference));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertEquals(CopyOrLink.UNDEFINED, deepCopyTreeStatus.getCopyOrLink(reference));
            } else {
                assertEquals(CopyOrLink.LINK, deepCopyTreeStatus.getCopyOrLink(reference));
            }
        }
    }

    private void mockTableContentUsage() throws CoreRuntimeException {
        ITableContentUsage tableContentUsage = mock(ITableContentUsage.class);
        when(tableContentUsage.getIpsObject()).thenReturn(productCmpts[0]);
        when(productCmpts[0].getTableContentUsages()).thenReturn(new ITableContentUsage[] { tableContentUsage });
        ITableContents tableContents = mock(ITableContents.class);
        when(tableContentUsage.findTableContents(any(IIpsProject.class))).thenReturn(tableContents);
        when(tableContentUsage.getTableContentName()).thenReturn("MyTableContent");
    }

    @Test
    public void testGetCopyOrLink_SmartModusOtherIpsProject() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        IIpsProject rootIpsProject = mock(IIpsProject.class);
        IIpsProject childIpsProject = mock(IIpsProject.class);

        mockProducts(rootIpsProject, childIpsProject);
        initDeepCopyTreeStatusWithStructure();

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference.isRoot()) {
                assertEquals(CopyOrLink.COPY, deepCopyTreeStatus.getCopyOrLink(reference));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertEquals(CopyOrLink.UNDEFINED, deepCopyTreeStatus.getCopyOrLink(reference));
            } else {

                /* @formatter:off
                 * 0_
                 * |\\_
                 * | \ \
                 * 2  1_\ rootProject
                 * -------------------
                 * |  | 3 childProject
                 * 4  5
                 * @formatter:on */
                if (Integer.parseInt(reference.getWrappedIpsObject().getName()) < 3) {
                    assertEquals(CopyOrLink.COPY, deepCopyTreeStatus.getCopyOrLink(reference));
                } else {
                    assertEquals(CopyOrLink.LINK, deepCopyTreeStatus.getCopyOrLink(reference));
                }
            }
        }
    }

    @Test
    public void testGetCopyOrLink_SmartModusOtherPackageRoot() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        IIpsProject rootIpsProject = mock(IIpsProject.class);

        mockProducts(rootIpsProject, rootIpsProject, true);
        initDeepCopyTreeStatusWithStructure();

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            CopyOrLink copyOrLink = deepCopyTreeStatus.getCopyOrLink(reference);
            if (reference.isRoot()) {
                assertEquals(CopyOrLink.COPY, deepCopyTreeStatus.getCopyOrLink(reference));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertEquals(CopyOrLink.UNDEFINED, deepCopyTreeStatus.getCopyOrLink(reference));
            } else {

                /* @formatter:off
                 * P1: P2
                 * 0_:
                 * |\:\_
                 * | :\ \
                 * 2 : 1_\
                 * | : | 3
                 * 4 : 5
                 * @formatter:on */

                int number = Integer.parseInt(reference.getWrappedIpsObject().getName());
                boolean isInSamePackageRoot = number % 2 == 0;
                if (isInSamePackageRoot) {
                    assertEquals(number + " should be copied", CopyOrLink.COPY, copyOrLink);
                } else {
                    assertEquals(number + " should be linked", CopyOrLink.LINK, copyOrLink);
                }
            }
        }
    }

    @Test
    public void testGetCopyOrLink_SmartModusOtherProjectOrPackageRoot() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        IIpsProject rootIpsProject = mock(IIpsProject.class);
        IIpsProject childIpsProject = mock(IIpsProject.class);

        mockProducts(rootIpsProject, childIpsProject, true);
        initDeepCopyTreeStatusWithStructure();

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            CopyOrLink copyOrLink = deepCopyTreeStatus.getCopyOrLink(reference);
            if (reference.isRoot()) {
                assertEquals(CopyOrLink.COPY, deepCopyTreeStatus.getCopyOrLink(reference));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertEquals(CopyOrLink.UNDEFINED, deepCopyTreeStatus.getCopyOrLink(reference));
            } else {

                /* @formatter:off
                 * P1: P2  packageRoot
                 * 0_:
                 * |\:\_
                 * | :\ \
                 * 2 : 1_\ rootProject
                 * --------------------
                 * | : | 3 childProject
                 * 4 : 5
                 * @formatter:on */

                int number = Integer.parseInt(reference.getWrappedIpsObject().getName());
                boolean isInSamePackageRoot = number % 2 == 0;
                boolean isInSameProject = number < 3;
                if (isInSamePackageRoot && isInSameProject) {
                    assertEquals(number + " should be copied", CopyOrLink.COPY, copyOrLink);
                } else {
                    assertEquals(number + " should be linked", CopyOrLink.LINK, copyOrLink);
                }
            }
        }
    }

    @Test
    public void testGetCopyOrLink_SmartModusOtherProjectOrPackageRoot_InvalidProdutCmpt() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        mockProducts();
        // null out the package fragment
        when(productCmpts[1].getIpsPackageFragment()).thenReturn(null);

        initDeepCopyTreeStatusWithStructure();
        // test ok if the initialization passes without exceptions
    }

    @Test
    public void testGetCopyOrLink_TableUsageWithoutTable() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        IIpsProject rootIpsProject = mock(IIpsProject.class);
        IIpsProject childIpsProject = mock(IIpsProject.class);

        mockProducts(rootIpsProject, childIpsProject, true);
        ITableContentUsage tableContentUsage = mock(ITableContentUsage.class);
        when(tableContentUsage.getIpsObject()).thenReturn(productCmpts[0]);
        when(productCmpts[0].getTableContentUsages()).thenReturn(new ITableContentUsage[] { tableContentUsage });
        initDeepCopyTreeStatusWithStructure();

        // check that the usage is included in the structure even without a referenced table content
        IProductCmptStructureReference[] children = structure.getRoot().getChildren();
        assertThat(children.length, is(5));
        assertThat(children[4], is(instanceOf(ProductCmptStructureTblUsageReference.class)));
        ProductCmptStructureTblUsageReference tblUsageReference = (ProductCmptStructureTblUsageReference)children[4];
        assertThat(tblUsageReference.getTableContentUsage(), is(sameInstance(tableContentUsage)));

        // as there is no target table, the reference should not be in the elements of the
        // DeepCopyTreeStatus
        assertThat(deepCopyTreeStatus.getAllElements(CopyOrLink.COPY, structure, true).contains(tblUsageReference),
                is(false));
        assertThat(deepCopyTreeStatus.getAllElements(CopyOrLink.LINK, structure, true).contains(tblUsageReference),
                is(false));
        assertThat(deepCopyTreeStatus.getAllElements(CopyOrLink.UNDEFINED, structure, true).contains(tblUsageReference),
                is(false));

        // but when it is asked for a copy-or-link-status, CopyOrLink#UNDEFINED should be returned
        // without
        // an Exception
        assertThat(deepCopyTreeStatus.getCopyOrLink(tblUsageReference), is(CopyOrLink.UNDEFINED));

        // and the reference should be treated as checked
        // This is called from
        // org.faktorips.devtools.core.ui.wizards.deepcopy.SourcePage.updateCheckedAndGrayStatus(IProductCmptStructureReference)
        // for all references, independent of whether they are visible elements in the tree
        assertThat(deepCopyTreeStatus.isChecked(tblUsageReference), is(true));
    }

}
