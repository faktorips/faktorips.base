/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.deepcopy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
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

    private void initStructure() throws Exception {
        IIpsProject ipsProject = mock(IIpsProject.class);
        initStructure(ipsProject, ipsProject);
    }

    private void initStructure(IIpsProject rootIpsProject, IIpsProject childIpsProject) throws Exception {
        initStructure(rootIpsProject, childIpsProject, false);
    }

    private void initStructure(IIpsProject rootIpsProject, IIpsProject childIpsProject, boolean alternatePackageRoots)
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

        when(associations[0].getType()).thenReturn(types[0]);
        when(associations[1].getType()).thenReturn(types[0]);
        when(associations[2].getType()).thenReturn(types[1]);
        when(associations[3].getType()).thenReturn(types[2]);

        when(types[0].findAllNotDerivedAssociations(any(IIpsProject.class)))
                .thenReturn(Arrays.asList(new IProductCmptTypeAssociation[] { associations[0], associations[1] }));
        when(types[1].findAllNotDerivedAssociations(any(IIpsProject.class)))
                .thenReturn(Arrays.asList(new IProductCmptTypeAssociation[] { associations[2] }));
        when(types[2].findAllNotDerivedAssociations(any(IIpsProject.class)))
                .thenReturn(Arrays.asList(new IProductCmptTypeAssociation[] { associations[3] }));

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
            ArrayList<IIpsObjectGeneration> generations = new ArrayList<IIpsObjectGeneration>();
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

        List<IValidationRuleConfig> ruleConfigs = new ArrayList<IValidationRuleConfig>();
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

        structure = new ProductCmptTreeStructure(productCmpts[0], new GregorianCalendar(), mock(IIpsProject.class));

        deepCopyTreeStatus = new DeepCopyTreeStatus();
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
            IProductCmpt target) throws CoreException {
        when(link.findAssociation(any(IIpsProject.class))).thenReturn(association);
        when(link.getProductCmpt()).thenReturn(source);
        when(link.getIpsObject()).thenReturn(source);
        when(link.findTarget(any(IIpsProject.class))).thenReturn(target);
    }

    @Test
    public void testIsEnabled() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_COPY);
        initStructure();
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
    public void testgetCopyOrLink_CopyModus() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_COPY);
        initStructure();

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference instanceof IProductCmptTypeAssociationReference) {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.UNDEFINED));
            } else {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.COPY));
            }
        }
    }

    @Test
    public void testgetCopyOrLink_LinkModus() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_LINK);
        initStructure();
        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference.isRoot()) {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.COPY));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.UNDEFINED));
            } else {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.LINK));
            }
        }
    }

    @Test
    public void testgetCopyOrLink_SmartModusSameIpsProject() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        initStructure();

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference.isRoot()) {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.COPY));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.UNDEFINED));
            } else {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.COPY));
            }
        }
    }

    @Test
    public void testgetCopyOrLink_SmartModusOtherIpsProject() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        IIpsProject rootIpsProject = mock(IIpsProject.class);
        IIpsProject childIpsProject = mock(IIpsProject.class);

        initStructure(rootIpsProject, childIpsProject);

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference.isRoot()) {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.COPY));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.UNDEFINED));
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
                    assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.COPY));
                } else {
                    assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.LINK));
                }
            }
        }
    }

    @Test
    public void testgetCopyOrLink_SmartModusOtherPackageRoot() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        IIpsProject rootIpsProject = mock(IIpsProject.class);

        initStructure(rootIpsProject, rootIpsProject, true);

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            CopyOrLink copyOrLink = deepCopyTreeStatus.getCopyOrLink(reference);
            if (reference.isRoot()) {
                assertTrue(copyOrLink.equals(CopyOrLink.COPY));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertTrue(copyOrLink.equals(CopyOrLink.UNDEFINED));
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
                    assertTrue(number + " should be copied", copyOrLink.equals(CopyOrLink.COPY));
                } else {
                    assertTrue(number + " should be linked", copyOrLink.equals(CopyOrLink.LINK));
                }
            }
        }
    }

    @Test
    public void testgetCopyOrLink_SmartModusOtherProjectOrPackageRoot() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_SMARTMODE);
        IIpsProject rootIpsProject = mock(IIpsProject.class);
        IIpsProject childIpsProject = mock(IIpsProject.class);

        initStructure(rootIpsProject, childIpsProject, true);

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            CopyOrLink copyOrLink = deepCopyTreeStatus.getCopyOrLink(reference);
            if (reference.isRoot()) {
                assertTrue(copyOrLink.equals(CopyOrLink.COPY));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertTrue(copyOrLink.equals(CopyOrLink.UNDEFINED));
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
                    assertTrue(number + " should be copied", copyOrLink.equals(CopyOrLink.COPY));
                } else {
                    assertTrue(number + " should be linked", copyOrLink.equals(CopyOrLink.LINK));
                }
            }
        }
    }
}
