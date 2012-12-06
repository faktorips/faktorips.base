/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XDetailToMasterDerivedUnionAssociationTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private IPolicyCmptType type;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IPolicyCmptType subTypeChild;

    @Mock
    private IPolicyCmptType parent;

    @Mock
    private IPolicyCmptType subParent;

    @Mock
    private IPolicyCmptTypeAssociation derivedUnion;

    @Mock
    private IPolicyCmptTypeAssociation detailToMasterDerivedUnion;

    @Mock
    private IPolicyCmptTypeAssociation subset1;

    @Mock
    private IPolicyCmptTypeAssociation inverseSubset1;

    @Mock
    private IPolicyCmptTypeAssociation subset2;

    @Mock
    private IPolicyCmptTypeAssociation inverseSubset2;

    @Mock
    private XPolicyCmptClass xPolicyCmptClass;

    @Mock
    private IPolicyCmptTypeAssociation policyAssoc;

    @Mock
    private XPolicyAssociation associationNode1;

    @Mock
    private XPolicyAssociation associationNode2;

    @Test
    public void testgetInverseCompositions() {
        XPolicyCmptClass policyCmptClass = setupPolicyClassWithGetAssociations();
        XDetailToMasterDerivedUnionAssociation detailDUAssoc = new XDetailToMasterDerivedUnionAssociation(policyAssoc,
                modelContext, modelService);

        when(associationNode1.isCompositionDetailToMaster()).thenReturn(false);
        when(associationNode2.isCompositionDetailToMaster()).thenReturn(false);
        when(associationNode2.isSubsetOf(any(XDerivedUnionAssociation.class))).thenReturn(false);
        assertEquals(0, detailDUAssoc.getSubsetAssociations(policyCmptClass).size());

        when(associationNode1.isCompositionDetailToMaster()).thenReturn(false);
        when(associationNode2.isCompositionDetailToMaster()).thenReturn(true);
        when(associationNode2.isSubsetOf(any(XDerivedUnionAssociation.class))).thenReturn(false);
        assertEquals(0, detailDUAssoc.getSubsetAssociations(policyCmptClass).size());

        when(associationNode1.isCompositionDetailToMaster()).thenReturn(false);
        when(associationNode2.isCompositionDetailToMaster()).thenReturn(true);
        when(associationNode2.isSubsetOf(any(XDerivedUnionAssociation.class))).thenReturn(true);
        assertEquals(1, detailDUAssoc.getSubsetAssociations(policyCmptClass).size());
        assertEquals(associationNode2, detailDUAssoc.getSubsetAssociations(policyCmptClass).iterator().next());
    }

    private XPolicyCmptClass setupPolicyClassWithGetAssociations() {
        XPolicyCmptClass policyCmptClass = spy(new XPolicyCmptClass(type, modelContext, modelService));
        Set<XPolicyAssociation> assocs = new LinkedHashSet<XPolicyAssociation>();
        assocs.add(associationNode1);
        assocs.add(associationNode2);
        when(associationNode1.isCompositionDetailToMaster()).thenReturn(true);
        when(associationNode2.isCompositionDetailToMaster()).thenReturn(true);
        doReturn(assocs).when(policyCmptClass).getAssociations();
        XPolicyAssociation inverseAsso1 = mock(XPolicyAssociation.class);
        when(associationNode1.getInverseAssociation()).thenReturn(inverseAsso1);
        XPolicyAssociation inverseAsso2 = mock(XPolicyAssociation.class);
        when(associationNode2.getInverseAssociation()).thenReturn(inverseAsso2);
        return policyCmptClass;
    }

    @Test
    public void testIsImplementedInSuperclass() throws Exception {
        when(xPolicyCmptClass.getIpsProject()).thenReturn(ipsProject);

        XDetailToMasterDerivedUnionAssociation detailDUAssoc = new XDetailToMasterDerivedUnionAssociation(
                detailToMasterDerivedUnion, modelContext, modelService);
        when(detailToMasterDerivedUnion.getType()).thenReturn(type);
        when(xPolicyCmptClass.getType()).thenReturn(type);
        assertFalse(detailDUAssoc.isImplementedInSuperclass(xPolicyCmptClass));

        when(derivedUnion.isDerivedUnion()).thenReturn(true);

        when(detailToMasterDerivedUnion.getInverseAssociation()).thenReturn("derivedUnion");
        when(detailToMasterDerivedUnion.isCompositionDetailToMaster()).thenReturn(true);

        when(subset1.getSubsettedDerivedUnion()).thenReturn("derivedUnion");

        when(inverseSubset1.findInverseAssociation(ipsProject)).thenReturn(subset1);
        when(inverseSubset1.isCompositionDetailToMaster()).thenReturn(true);
        when(inverseSubset2.findInverseAssociation(ipsProject)).thenReturn(subset2);
        when(inverseSubset2.isCompositionDetailToMaster()).thenReturn(true);

        when(subset2.getSubsettedDerivedUnion()).thenReturn("derivedUnion");
        when(subset2.findInverseAssociation(ipsProject)).thenReturn(inverseSubset2);

        when(subTypeChild.findSupertype(ipsProject)).thenReturn(type);
        when(subParent.findSupertype(ipsProject)).thenReturn(parent);

        when(parent.getPolicyCmptTypeAssociations()).thenReturn(
                Arrays.asList(new IPolicyCmptTypeAssociation[] { derivedUnion }));
        when(type.getPolicyCmptTypeAssociations()).thenReturn(
                Arrays.asList(new IPolicyCmptTypeAssociation[] { detailToMasterDerivedUnion }));
        when(subParent.getPolicyCmptTypeAssociations()).thenReturn(
                Arrays.asList(new IPolicyCmptTypeAssociation[] { subset2 }));
        when(subTypeChild.getPolicyCmptTypeAssociations()).thenReturn(
                Arrays.asList(new IPolicyCmptTypeAssociation[] { inverseSubset2 }));

        when(xPolicyCmptClass.getType()).thenReturn(subTypeChild);
        assertFalse(detailDUAssoc.isImplementedInSuperclass(xPolicyCmptClass));

        when(parent.getPolicyCmptTypeAssociations()).thenReturn(
                Arrays.asList(new IPolicyCmptTypeAssociation[] { derivedUnion, subset1 }));
        when(type.getPolicyCmptTypeAssociations()).thenReturn(
                Arrays.asList(new IPolicyCmptTypeAssociation[] { detailToMasterDerivedUnion, inverseSubset1 }));
        assertTrue(detailDUAssoc.isImplementedInSuperclass(xPolicyCmptClass));
    }

    @Test
    public void testGetDetailToMasterSubsetAssociations_findNothing() throws Exception {
        XDetailToMasterDerivedUnionAssociation detailDUAssoc = new XDetailToMasterDerivedUnionAssociation(
                detailToMasterDerivedUnion, modelContext, modelService);
        when(detailToMasterDerivedUnion.getName()).thenReturn("theName");
        XPolicyCmptClass policyCmptClass = setupPolicyClassWithGetAssociations();

        Set<XPolicyAssociation> detailToMasterSubsetAssociations = detailDUAssoc
                .getDetailToMasterSubsetAssociations(policyCmptClass);

        assertTrue(detailToMasterSubsetAssociations.isEmpty());
    }

    @Test
    public void testGetDetailToMasterSubsetAssociations_sharedAssociation() throws Exception {
        XDetailToMasterDerivedUnionAssociation detailDUAssoc = new XDetailToMasterDerivedUnionAssociation(
                detailToMasterDerivedUnion, modelContext, modelService);
        XPolicyCmptClass policyCmptClass = setupPolicyClassWithGetAssociations();
        when(associationNode1.isSharedAssociation()).thenReturn(true);
        when(associationNode1.getName()).thenReturn("theName");
        when(detailToMasterDerivedUnion.getName()).thenReturn("theName");

        Set<XPolicyAssociation> detailToMasterSubsetAssociations = detailDUAssoc
                .getDetailToMasterSubsetAssociations(policyCmptClass);

        assertEquals(1, detailToMasterSubsetAssociations.size());
        assertThat(detailToMasterSubsetAssociations, hasItem(associationNode1));
    }

    @Test
    public void testGetDetailToMasterSubsetAssociations_sameName() throws Exception {
        XDetailToMasterDerivedUnionAssociation detailDUAssoc = new XDetailToMasterDerivedUnionAssociation(
                detailToMasterDerivedUnion, modelContext, modelService);
        XPolicyCmptClass policyCmptClass = setupPolicyClassWithGetAssociations();
        when(associationNode1.getName()).thenReturn("theName");
        when(associationNode1.getInverseAssociation().isDerived()).thenReturn(true);
        when(detailToMasterDerivedUnion.getName()).thenReturn("theName");
        when(associationNode2.getName()).thenReturn("theName");

        Set<XPolicyAssociation> detailToMasterSubsetAssociations = detailDUAssoc
                .getDetailToMasterSubsetAssociations(policyCmptClass);

        assertEquals(1, detailToMasterSubsetAssociations.size());
        assertThat(detailToMasterSubsetAssociations, hasItem(associationNode2));
    }

    @Test
    public void testGetDetailToMasterSubsetAssociations_normalSubset() throws Exception {
        XPolicyCmptClass policyCmptClass = setupPolicyClassWithGetAssociations();
        when(associationNode1.getName()).thenReturn("theName");
        when(associationNode1.getInverseAssociation().isDerived()).thenReturn(false);
        when(associationNode2.getName()).thenReturn("theName");
        when(associationNode2.getInverseAssociation().isDerived()).thenReturn(true);
        when(detailToMasterDerivedUnion.findInverseAssociation(any(IIpsProject.class))).thenReturn(derivedUnion);
        XDerivedUnionAssociation xDerivedUnion = mock(XDerivedUnionAssociation.class);
        when(modelService.getModelNode(derivedUnion, XDerivedUnionAssociation.class, modelContext)).thenReturn(
                xDerivedUnion);
        XDetailToMasterDerivedUnionAssociation detailDUAssoc = new XDetailToMasterDerivedUnionAssociation(
                detailToMasterDerivedUnion, modelContext, modelService);
        when(detailToMasterDerivedUnion.getName()).thenReturn("theName");
        XPolicyAssociation masterToDetail1 = associationNode1.getInverseAssociation();
        when(masterToDetail1.isRecursiveSubsetOf(xDerivedUnion)).thenReturn(true);
        XPolicyAssociation masterToDetail2 = associationNode2.getInverseAssociation();
        when(masterToDetail2.isRecursiveSubsetOf(xDerivedUnion)).thenReturn(true);

        Set<XPolicyAssociation> detailToMasterSubsetAssociations = detailDUAssoc
                .getDetailToMasterSubsetAssociations(policyCmptClass);

        assertEquals(1, detailToMasterSubsetAssociations.size());
        assertThat(detailToMasterSubsetAssociations, hasItem(associationNode1));
    }

}
