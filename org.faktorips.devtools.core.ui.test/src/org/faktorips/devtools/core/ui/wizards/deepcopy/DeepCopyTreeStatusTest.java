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

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
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
    private IProductCmptGeneration[] productCmptsGenerations;
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

        when(types[0].findAllNotDerivedAssociations(any(IIpsProject.class))).thenReturn(
                Arrays.asList(new IProductCmptTypeAssociation[] { associations[0], associations[1] }));
        when(types[1].findAllNotDerivedAssociations(any(IIpsProject.class))).thenReturn(
                Arrays.asList(new IProductCmptTypeAssociation[] { associations[2] }));
        when(types[2].findAllNotDerivedAssociations(any(IIpsProject.class))).thenReturn(
                Arrays.asList(new IProductCmptTypeAssociation[] { associations[3] }));

        productCmpts = new IProductCmpt[6];
        links = new IProductCmptLink[6];
        productCmptsGenerations = new IProductCmptGeneration[10];
        for (int i = 0; i < productCmpts.length; i++) {
            productCmpts[i] = mock(IProductCmpt.class);
            productCmpts[i].setProductCmptType("type" + i);
            productCmptsGenerations[i] = mock(IProductCmptGeneration.class);
            when(productCmptsGenerations[i].getParent()).thenReturn(productCmpts[i]);
            ArrayList<IIpsObjectGeneration> generations = new ArrayList<IIpsObjectGeneration>();
            generations.add(productCmptsGenerations[i]);
            when(productCmpts[i].getGenerations()).thenReturn(generations);
            when(productCmpts[i].getGenerationEffectiveOn(any(GregorianCalendar.class))).thenReturn(
                    productCmptsGenerations[i]);
            when(productCmpts[i].findProductCmptType(any(IIpsProject.class))).thenReturn(types[i]);
            IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
            when(productCmpts[i].getIpsSrcFile()).thenReturn(srcFile);
            when(srcFile.getIpsProject()).thenReturn(childIpsProject);
            when(productCmpts[i].getIpsProject()).thenReturn(rootIpsProject);
            when(productCmptsGenerations[i].getTableContentUsages()).thenReturn(new ITableContentUsage[0]);
            links[i] = mock(IProductCmptLink.class);
        }

        when(links[0].findAssociation(any(IIpsProject.class))).thenReturn(associations[0]);
        when(links[0].getProductCmpt()).thenReturn(productCmpts[0]);
        when(links[0].getIpsObject()).thenReturn(productCmpts[0]);
        when(links[0].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[1]);

        when(links[1].findAssociation(any(IIpsProject.class))).thenReturn(associations[0]);
        when(links[1].getProductCmpt()).thenReturn(productCmpts[0]);
        when(links[1].getIpsObject()).thenReturn(productCmpts[0]);
        when(links[1].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[2]);

        when(links[2].findAssociation(any(IIpsProject.class))).thenReturn(associations[1]);
        when(links[2].getProductCmpt()).thenReturn(productCmpts[0]);
        when(links[2].getIpsObject()).thenReturn(productCmpts[0]);
        when(links[2].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[3]);

        when(links[3].findAssociation(any(IIpsProject.class))).thenReturn(associations[2]);
        when(links[3].getProductCmpt()).thenReturn(productCmpts[1]);
        when(links[3].getIpsObject()).thenReturn(productCmpts[0]);
        when(links[3].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[4]);

        when(links[4].findAssociation(any(IIpsProject.class))).thenReturn(associations[2]);
        when(links[4].getProductCmpt()).thenReturn(productCmpts[1]);
        when(links[4].getIpsObject()).thenReturn(productCmpts[1]);
        when(links[4].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[2]);

        when(links[5].findAssociation(any(IIpsProject.class))).thenReturn(associations[3]);
        when(links[5].getProductCmpt()).thenReturn(productCmpts[2]);
        when(links[5].getIpsObject()).thenReturn(productCmpts[2]);
        when(links[5].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[5]);

        when(productCmptsGenerations[0].getLinksIncludingProductCmpt()).thenReturn(
                Arrays.asList(new IProductCmptLink[] { links[0], links[1], links[2] }));
        when(productCmptsGenerations[1].getLinksIncludingProductCmpt()).thenReturn(
                Arrays.asList(new IProductCmptLink[] { links[3], links[4] }));
        when(productCmptsGenerations[2].getLinksIncludingProductCmpt()).thenReturn(
                Arrays.asList(new IProductCmptLink[] { links[5] }));
        when(productCmptsGenerations[3].getLinksIncludingProductCmpt()).thenReturn(
                Arrays.asList(new IProductCmptLink[] {}));
        when(productCmptsGenerations[4].getLinksIncludingProductCmpt()).thenReturn(
                Arrays.asList(new IProductCmptLink[] {}));
        when(productCmptsGenerations[5].getLinksIncludingProductCmpt()).thenReturn(
                Arrays.asList(new IProductCmptLink[] {}));

        List<IValidationRuleConfig> ruleConfigs = new ArrayList<IValidationRuleConfig>();
        ruleConfigs.add(mock(IValidationRuleConfig.class));
        ruleConfigs.add(mock(IValidationRuleConfig.class));
        when(ruleConfigs.get(0).getName()).thenReturn("Rule1");
        when(ruleConfigs.get(1).getName()).thenReturn("RuleTwo");

        when(productCmptsGenerations[0].getValidationRuleConfigs()).thenReturn(ruleConfigs);
        when(productCmptsGenerations[1].getValidationRuleConfigs()).thenReturn(new ArrayList<IValidationRuleConfig>());
        when(productCmptsGenerations[2].getValidationRuleConfigs()).thenReturn(new ArrayList<IValidationRuleConfig>());
        when(productCmptsGenerations[3].getValidationRuleConfigs()).thenReturn(new ArrayList<IValidationRuleConfig>());
        when(productCmptsGenerations[4].getValidationRuleConfigs()).thenReturn(new ArrayList<IValidationRuleConfig>());
        when(productCmptsGenerations[5].getValidationRuleConfigs()).thenReturn(new ArrayList<IValidationRuleConfig>());

        structure = new ProductCmptTreeStructure(productCmpts[0], new GregorianCalendar(), mock(IIpsProject.class));

        deepCopyTreeStatus = new DeepCopyTreeStatus();
        deepCopyTreeStatus.initialize(structure);
    }

    @Test
    public void testIsEnabled() throws Exception {
        ipsPreferences.setCopyWizardMode(IpsPreferences.COPY_WIZARD_MODE_COPY);
        initStructure();
        Set<IProductCmptStructureReference> allCopyEnabledElements = deepCopyTreeStatus.getAllEnabledElements(
                CopyOrLink.COPY, structure, true);
        // 6 product components + 2 are referenced twice (productCmpts[2] and productCmpts[5])
        assertEquals(8, allCopyEnabledElements.size());
        Set<IProductCmptStructureReference> allLinkEnabledElements = deepCopyTreeStatus.getAllEnabledElements(
                CopyOrLink.LINK, structure, true);
        assertEquals(0, allLinkEnabledElements.size());

        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            assertTrue(deepCopyTreeStatus.isEnabled(reference));
        }

        IProductCmptStructureReference r1 = structure.getRoot().getChildren()[0].getChildren()[1];
        deepCopyTreeStatus.setCopOrLink(r1, CopyOrLink.LINK);
        allCopyEnabledElements = deepCopyTreeStatus.getAllEnabledElements(CopyOrLink.COPY, structure, true);
        assertEquals(6, allCopyEnabledElements.size());
        allLinkEnabledElements = deepCopyTreeStatus.getAllEnabledElements(CopyOrLink.LINK, structure, true);
        assertEquals(1, allLinkEnabledElements.size());
        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference.getParent() == r1
                    || (reference.getParent() != null && reference.getParent().getParent() == r1)) {
                assertFalse(deepCopyTreeStatus.isEnabled(reference));
            } else {
                assertTrue(deepCopyTreeStatus.isEnabled(reference));
            }
        }

        deepCopyTreeStatus.setChecked(r1, false);
        allCopyEnabledElements = deepCopyTreeStatus.getAllEnabledElements(CopyOrLink.COPY, structure, true);
        assertEquals(6, allCopyEnabledElements.size());
        allLinkEnabledElements = deepCopyTreeStatus.getAllEnabledElements(CopyOrLink.LINK, structure, true);
        assertEquals(0, allLinkEnabledElements.size());
        for (IProductCmptStructureReference reference : structure.toSet(false)) {
            if (reference == r1 || reference.getParent() == r1
                    || (reference.getParent() != null && reference.getParent().getParent() == r1)) {
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
            if (reference.getParent() == null) {
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
            if (reference.getParent() == null) {
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
            if (reference.getParent() == null) {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.COPY));
            } else if (reference instanceof IProductCmptTypeAssociationReference) {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.UNDEFINED));
            } else {
                assertTrue(deepCopyTreeStatus.getCopyOrLink(reference).equals(CopyOrLink.LINK));
            }
        }
    }
}
