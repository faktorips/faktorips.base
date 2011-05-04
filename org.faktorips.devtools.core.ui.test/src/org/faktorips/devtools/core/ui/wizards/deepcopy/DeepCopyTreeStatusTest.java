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
import java.util.Set;

import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.wizards.deepcopy.LinkStatus.CopyOrLink;
import org.junit.Before;
import org.junit.Test;

public class DeepCopyTreeStatusTest {

    private DeepCopyTreeStatus deepCopyTreeStatus;
    private IProductCmptType[] types;
    private IProductCmptTypeAssociation[] associations;
    private IProductCmpt[] productCmpts;
    private IProductCmptLink[] links;
    private IProductCmptGeneration[] productCmptsGenerations;
    private ProductCmptTreeStructure structure;

    @Before
    public void setUp() throws Exception {
        types = new IProductCmptType[6];
        for (int i = 0; i < types.length; i++) {
            types[i] = mock(IProductCmptType.class);
            when(types[i].getName()).thenReturn("type" + i);
        }

        associations = new IProductCmptTypeAssociation[4];
        for (int i = 0; i < associations.length; i++) {
            associations[i] = mock(IProductCmptTypeAssociation.class);
            when(associations[i].getName()).thenReturn("association" + i);
        }

        when(associations[0].getType()).thenReturn(types[0]);
        when(associations[1].getType()).thenReturn(types[0]);
        when(associations[2].getType()).thenReturn(types[1]);
        when(associations[3].getType()).thenReturn(types[2]);

        when(types[0].findAllNotDerivedAssociations()).thenReturn(
                new ArrayList<IAssociation>(Arrays.asList(new IAssociation[] { associations[0], associations[1] })));
        when(types[1].findAllNotDerivedAssociations()).thenReturn(
                new ArrayList<IAssociation>(Arrays.asList(new IAssociation[] { associations[2] })));
        when(types[2].findAllNotDerivedAssociations()).thenReturn(
                new ArrayList<IAssociation>(Arrays.asList(new IAssociation[] { associations[3] })));

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
            when(productCmpts[i].findGenerationEffectiveOn(any(GregorianCalendar.class))).thenReturn(
                    productCmptsGenerations[i]);
            when(productCmpts[i].findProductCmptType(any(IIpsProject.class))).thenReturn(types[i]);
            when(productCmptsGenerations[i].getTableContentUsages()).thenReturn(new ITableContentUsage[0]);

            links[i] = mock(IProductCmptLink.class);
        }

        when(links[0].findAssociation(any(IIpsProject.class))).thenReturn(associations[0]);
        when(links[0].getProductCmpt()).thenReturn(productCmpts[0]);
        when(links[0].getParent()).thenReturn(productCmptsGenerations[0]);
        when(links[0].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[1]);

        when(links[1].findAssociation(any(IIpsProject.class))).thenReturn(associations[0]);
        when(links[1].getProductCmpt()).thenReturn(productCmpts[0]);
        when(links[1].getParent()).thenReturn(productCmptsGenerations[0]);
        when(links[1].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[2]);

        when(links[2].findAssociation(any(IIpsProject.class))).thenReturn(associations[1]);
        when(links[2].getProductCmpt()).thenReturn(productCmpts[0]);
        when(links[2].getParent()).thenReturn(productCmptsGenerations[0]);
        when(links[2].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[3]);

        when(links[3].findAssociation(any(IIpsProject.class))).thenReturn(associations[2]);
        when(links[3].getProductCmpt()).thenReturn(productCmpts[1]);
        when(links[3].getParent()).thenReturn(productCmptsGenerations[1]);
        when(links[3].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[4]);

        when(links[4].findAssociation(any(IIpsProject.class))).thenReturn(associations[2]);
        when(links[4].getProductCmpt()).thenReturn(productCmpts[1]);
        when(links[4].getParent()).thenReturn(productCmptsGenerations[1]);
        when(links[4].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[2]);

        when(links[5].findAssociation(any(IIpsProject.class))).thenReturn(associations[3]);
        when(links[5].getProductCmpt()).thenReturn(productCmpts[2]);
        when(links[5].getParent()).thenReturn(productCmptsGenerations[2]);
        when(links[5].findTarget(any(IIpsProject.class))).thenReturn(productCmpts[5]);

        when(productCmptsGenerations[0].getLinks()).thenReturn(new IProductCmptLink[] { links[0], links[1], links[2] });
        when(productCmptsGenerations[1].getLinks()).thenReturn(new IProductCmptLink[] { links[3], links[4] });
        when(productCmptsGenerations[2].getLinks()).thenReturn(new IProductCmptLink[] { links[5] });
        when(productCmptsGenerations[3].getLinks()).thenReturn(new IProductCmptLink[] {});
        when(productCmptsGenerations[4].getLinks()).thenReturn(new IProductCmptLink[] {});
        when(productCmptsGenerations[5].getLinks()).thenReturn(new IProductCmptLink[] {});

        IValidationRuleConfig[] ruleConfigs = new IValidationRuleConfig[2];
        ruleConfigs[0] = mock(IValidationRuleConfig.class);
        ruleConfigs[1] = mock(IValidationRuleConfig.class);
        when(ruleConfigs[0].getName()).thenReturn("Rule1");
        when(ruleConfigs[1].getName()).thenReturn("RuleTwo");

        when(productCmptsGenerations[0].getValidationRuleConfigs()).thenReturn(ruleConfigs);
        when(productCmptsGenerations[1].getValidationRuleConfigs()).thenReturn(new IValidationRuleConfig[] {});
        when(productCmptsGenerations[2].getValidationRuleConfigs()).thenReturn(new IValidationRuleConfig[] {});
        when(productCmptsGenerations[3].getValidationRuleConfigs()).thenReturn(new IValidationRuleConfig[] {});
        when(productCmptsGenerations[4].getValidationRuleConfigs()).thenReturn(new IValidationRuleConfig[] {});
        when(productCmptsGenerations[5].getValidationRuleConfigs()).thenReturn(new IValidationRuleConfig[] {});

        structure = new ProductCmptTreeStructure(productCmpts[0], new GregorianCalendar(), mock(IIpsProject.class));

        deepCopyTreeStatus = new DeepCopyTreeStatus();
        deepCopyTreeStatus.initialize(structure);
    }

    @Test
    public void testIsEnabled() throws Exception {
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
}
