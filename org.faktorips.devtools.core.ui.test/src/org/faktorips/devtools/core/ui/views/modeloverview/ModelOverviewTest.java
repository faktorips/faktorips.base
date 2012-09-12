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

package org.faktorips.devtools.core.ui.views.modeloverview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreePath;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.views.modeloverview.ModelOverviewContentProvider.ToChildAssociationType;
import org.junit.Test;

public class ModelOverviewTest extends AbstractIpsPluginTest {

    @Test
    public void testComputePaths_BuildCorrectTreePath() throws CoreException {
        // setup
        // project
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");
        PolicyCmptType hausratGrunddeckung = newPolicyCmptTypeWithoutProductCmptType(project, "HausratGrunddeckung");

        hausratGrunddeckung.setSupertype(deckung.getQualifiedName());

        IAssociation associationVertrag2Deckung = vertrag.newAssociation();
        associationVertrag2Deckung.setTarget(deckung.getQualifiedName());
        associationVertrag2Deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // create path
        List<PathElement> rawPath = new ArrayList<PathElement>();
        rawPath.add(new PathElement(vertrag, ToChildAssociationType.ASSOCIATION));
        rawPath.add(new PathElement(deckung, ToChildAssociationType.SUPERTYPE));
        rawPath.add(new PathElement(hausratGrunddeckung, ToChildAssociationType.SELF));

        ModelOverview view = new ModelOverview();
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        TreePath treePath = view.computePath(rawPath, provider);

        // tests
        assertEquals(5, treePath.getSegmentCount());

        Object vertragNode = treePath.getSegment(0);
        assertTrue(vertragNode instanceof ComponentNode);
        assertEquals(vertrag, ((ComponentNode)vertragNode).getValue());

        Object vertragAssociations = treePath.getSegment(1);
        assertTrue(vertragAssociations instanceof CompositeNode);

        Object deckungNode = treePath.getSegment(2);
        assertTrue(deckungNode instanceof ComponentNode);
        assertEquals(deckung, ((ComponentNode)deckungNode).getValue());

        Object deckungSubtype = treePath.getSegment(3);
        assertTrue(deckungSubtype instanceof SubtypeNode);

        Object hausratGrunddeckungNode = treePath.getSegment(4);
        assertTrue(hausratGrunddeckungNode instanceof ComponentNode);
        assertEquals(hausratGrunddeckung, ((ComponentNode)hausratGrunddeckungNode).getValue());
    }

}
