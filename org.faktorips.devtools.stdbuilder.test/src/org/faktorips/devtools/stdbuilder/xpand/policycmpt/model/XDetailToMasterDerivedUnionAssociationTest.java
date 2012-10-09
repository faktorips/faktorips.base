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
import static org.junit.Assert.assertTrue;
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
    private XPolicyCmptClass xPolicyCmptClass;

    @Mock
    private IPolicyCmptTypeAssociation policyAssoc;

    private XPolicyAssociation associationNode1;

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
        associationNode1 = mock(XPolicyAssociation.class);
        associationNode2 = mock(XPolicyAssociation.class);
        Set<XPolicyAssociation> assocs = new LinkedHashSet<XPolicyAssociation>();
        assocs.add(associationNode1);
        assocs.add(associationNode2);
        doReturn(assocs).when(policyCmptClass).getAssociations();
        return policyCmptClass;
    }

    @Mock
    IIpsProject ipsProject;
    @Mock
    IPolicyCmptType subTypeChild;
    @Mock
    IPolicyCmptType parent;
    @Mock
    IPolicyCmptType subParent;
    @Mock
    IPolicyCmptTypeAssociation derivedUnion;
    @Mock
    IPolicyCmptTypeAssociation inverseDerivedUnion;
    @Mock
    IPolicyCmptTypeAssociation subset1;
    @Mock
    IPolicyCmptTypeAssociation inverseSubset1;
    @Mock
    IPolicyCmptTypeAssociation subset2;
    @Mock
    IPolicyCmptTypeAssociation inverseSubset2;

    @Test
    public void testIsImplementedInSuperclass() throws Exception {
        when(xPolicyCmptClass.getIpsProject()).thenReturn(ipsProject);

        XDetailToMasterDerivedUnionAssociation detailDUAssoc = new XDetailToMasterDerivedUnionAssociation(
                inverseDerivedUnion, modelContext, modelService);
        when(inverseDerivedUnion.getType()).thenReturn(type);
        when(xPolicyCmptClass.getType()).thenReturn(type);
        assertFalse(detailDUAssoc.isImplementedInSuperclass(xPolicyCmptClass));

        when(derivedUnion.isDerivedUnion()).thenReturn(true);

        when(inverseDerivedUnion.getInverseAssociation()).thenReturn("derivedUnion");
        when(inverseDerivedUnion.isCompositionDetailToMaster()).thenReturn(true);

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
                Arrays.asList(new IPolicyCmptTypeAssociation[] { inverseDerivedUnion }));
        when(subParent.getPolicyCmptTypeAssociations()).thenReturn(
                Arrays.asList(new IPolicyCmptTypeAssociation[] { subset2 }));
        when(subTypeChild.getPolicyCmptTypeAssociations()).thenReturn(
                Arrays.asList(new IPolicyCmptTypeAssociation[] { inverseSubset2 }));

        when(xPolicyCmptClass.getType()).thenReturn(subTypeChild);
        assertFalse(detailDUAssoc.isImplementedInSuperclass(xPolicyCmptClass));

        when(parent.getPolicyCmptTypeAssociations()).thenReturn(
                Arrays.asList(new IPolicyCmptTypeAssociation[] { derivedUnion, subset1 }));
        when(type.getPolicyCmptTypeAssociations()).thenReturn(
                Arrays.asList(new IPolicyCmptTypeAssociation[] { inverseDerivedUnion, inverseSubset1 }));
        assertTrue(detailDUAssoc.isImplementedInSuperclass(xPolicyCmptClass));
    }
}
