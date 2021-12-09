/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelstructure;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.junit.Test;

public class AssociationComponentNodeTest extends AbstractIpsPluginTest {

    @Test
    public void testNewAssociationComponentNode_NullParentAllowed() throws CoreRuntimeException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        AssociationComponentNode.newAssociationComponentNode(association, null, project);
    }

    @Test(expected = NullPointerException.class)
    public void testNewAssociationComponentNode_NullProjectNotAllowed() throws CoreRuntimeException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        AssociationComponentNode.newAssociationComponentNode(association, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNewAssociationComponentNode_NullAssociationNotAllowed() throws CoreRuntimeException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        AssociationComponentNode.newAssociationComponentNode(null, null, project);
    }

    @Test
    public void testGetValue_IsAssociationTargetValue() throws CoreRuntimeException {
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");

        IAssociation association = vertrag.newAssociation();
        association.setTarget(deckung.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        AssociationComponentNode node = AssociationComponentNode
                .newAssociationComponentNode(association, null, project);

        assertEquals(deckung, node.getValue());
    }
}
