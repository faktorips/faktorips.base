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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
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
}
