/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;

public class LinkSectionDropListenerTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private ProductCmptType typeA;
    private ProductCmptType typeB;
    private ProductCmptType typeB2;
    private ProductCmptType typeC;
    private IProductCmptTypeAssociation associationToB1;
    private IProductCmptTypeAssociation associationToB2;
    private IProductCmptTypeAssociation associationToC;
    private ProductCmpt cmptA;
    private ProductCmpt cmptB1;
    private ProductCmpt cmptB2;
    private ProductCmpt cmptB3;
    private ProductCmpt cmptC1;
    private ProductCmpt cmptC2;
    private ProductCmpt cmptC3;
    private TestDropListener dropListener;
    private IProductCmptGeneration cmptAGeneration;
    private ProductCmpt cmptC4;
    private TreeViewer treeViewer;
    private LinksSection linkSection;
    private IProductCmptTypeAssociation staticAssociationToB;
    private IProductCmptTypeAssociation staticAssociationToC;
    private IProductCmptLink b1Link1;
    private IProductCmptLink b1Link2;
    private IProductCmptLink b2Link2;
    private IProductCmptLink b2Link3;
    private IProductCmptLink cLink1;
    private IProductCmptLink cLink2;
    private IProductCmptLink cLink3;
    private IProductCmptLink staticLinkToB1;
    private IProductCmptLink staticLinkToB2;
    private IProductCmptLink staticLinkToC1;
    private IPolicyCmptTypeAssociation policyAssociationToC;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        typeA = newProductCmptType(ipsProject, "testPack.TypA");
        typeB = newProductCmptType(ipsProject, "testPackB.TypB");
        typeC = newProductCmptType(ipsProject, "TypC");

        associationToB1 = typeA.newProductCmptTypeAssociation();
        associationToB1.setTargetRoleSingular("associationToB1");
        associationToB1.setTarget(typeB.getQualifiedName());

        associationToB2 = typeA.newProductCmptTypeAssociation();
        associationToB2.setTargetRoleSingular("associationToB2");
        associationToB2.setTarget(typeB.getQualifiedName());

        associationToC = typeA.newProductCmptTypeAssociation();
        associationToC.setTargetRoleSingular("associationToC");
        associationToC.setTarget(typeC.getQualifiedName());

        staticAssociationToB = typeA.newProductCmptTypeAssociation();
        staticAssociationToB.setTargetRoleSingular("staticAssociationToB");
        staticAssociationToB.setTarget(typeB.getQualifiedName());
        staticAssociationToB.setChangingOverTime(false);
        staticAssociationToC = typeA.newProductCmptTypeAssociation();
        staticAssociationToC.setTargetRoleSingular("staticAssociationToC");
        staticAssociationToC.setTarget(typeC.getQualifiedName());
        staticAssociationToC.setChangingOverTime(false);

        cmptA = newProductCmpt(typeA, "CmptA");
        cmptAGeneration = cmptA.getFirstGeneration();
        cmptB1 = newProductCmpt(typeB, "testPackX.CmptB1");
        cmptB2 = newProductCmpt(typeB, "CmptB2");
        cmptB3 = newProductCmpt(typeB, "testPackY.CmptB3");
        cmptC1 = newProductCmpt(typeC, "CmptC1");
        cmptC2 = newProductCmpt(typeC, "testPackZ.CmptC2");
        cmptC3 = newProductCmpt(typeC, "CmptC3");
        cmptC4 = newProductCmpt(typeC, "CmptC4");

        LinksContentProvider contentProvider = new LinksContentProvider();
        treeViewer = new TreeViewer(new Shell(Display.getDefault()));
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setInput(cmptAGeneration);

        linkSection = mock(LinksSection.class);
        when(linkSection.getViewer()).thenReturn(treeViewer);
        dropListener = spy(new TestDropListener(linkSection, cmptAGeneration));
    }

    @Test
    public void testValidateDropNewLink() {
        when(linkSection.isDataChangeable()).thenReturn(true);

        int operation = DND.DROP_LINK;

        // check (link) targets
        IProductCmptLink link = cmptAGeneration.newLink(associationToB1);
        link.setTarget(cmptB1.getQualifiedName());
        assertFalse(dropListener.validateDrop(link, operation, mockElementTransfer(cmptB1)));
        assertFalse(dropListener.validateDrop(link, operation, mockElementTransfer(cmptB1, cmptB2)));
        assertTrue(dropListener.validateDrop(link, operation, mockElementTransfer(cmptB2)));
        assertTrue(dropListener.validateDrop(link, operation, mockElementTransfer(cmptB2, cmptB3)));
        assertFalse(dropListener.validateDrop(link, operation, mockElementTransfer(cmptC1)));
        assertFalse(dropListener.validateDrop(link, operation, mockElementTransfer(cmptC1, cmptB1)));
        assertFalse(dropListener.validateDrop(link, operation, mockElementTransfer(cmptC1, cmptB2)));
        link.delete();
    }

    @Test
    public void testValidateDrop_editorEditable() throws Exception {
        when(linkSection.isDataChangeable()).thenReturn(true);

        int operation = DND.DROP_LINK;

        IProductCmptLink link = cmptA.getFirstGeneration().newLink(associationToB1);
        link.setTarget(cmptB1.getQualifiedName());
        assertTrue(dropListener.validateDrop(link, operation, mockElementTransfer(cmptB2)));

        when(linkSection.isDataChangeable()).thenReturn(false);
        assertFalse(dropListener.validateDrop(link, operation, mockElementTransfer(cmptB2)));
    }

    @Test
    public void testValidateDropMoveLink() {
        setUpLinksAndSetEditorChangeable();

        dropListener.setToMove(b1Link1);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 1 << 0 | 1 << 1 | 1 << 2 | 0 << 3 | 0 << 4);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 1 << 0 | 1 << 1 | 1 << 2 | 0 << 3 | 0 << 4);

        dropListener.setToMove(b1Link2);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 1 << 0 | 1 << 1 | 0 << 2 | 0 << 3 | 0 << 4);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 1 << 0 | 1 << 1 | 0 << 2 | 0 << 3 | 0 << 4);

        dropListener.setToMove(b1Link1, b1Link2);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 1 << 0 | 1 << 1 | 0 << 2 | 0 << 3 | 0 << 4);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 1 << 0 | 1 << 1 | 0 << 2 | 0 << 3 | 0 << 4);

        dropListener.setToMove(b1Link1, b2Link3);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 1 << 0 | 1 << 1 | 1 << 2 | 0 << 3 | 0 << 4);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 1 << 0 | 1 << 1 | 1 << 2 | 0 << 3 | 0 << 4);

        dropListener.setToMove(b1Link1, cLink2);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 0 << 0 | 0 << 1 | 0 << 2 | 0 << 3 | 0 << 4);
    }

    @Test
    public void testMoveChangingLinkToStaticAssociation() {
        setUpLinksAndSetEditorChangeable();

        dropListener.setToMove(b1Link1);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        assertFalse(dropListener.validateDrop(staticLinkToB2, DND.DROP_MOVE, mockElementTransfer(cmptA)));
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        assertFalse(dropListener.validateDrop(staticLinkToB2, DND.DROP_MOVE, mockElementTransfer(cmptA)));
    }

    @Test
    public void testMoveStaticLinkToMatchingStaticAssociation() {
        setUpLinksAndSetEditorChangeable();

        dropListener.setToMove(staticLinkToB1);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        assertTrue(dropListener.validateDrop(staticLinkToB2, DND.DROP_MOVE, mockElementTransfer(cmptA)));
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        assertTrue(dropListener.validateDrop(staticLinkToB2, DND.DROP_MOVE, mockElementTransfer(cmptA)));
    }

    @Test
    public void testMoveStaticLinkToMisMatchingStaticAssociation() {
        setUpLinksAndSetEditorChangeable();

        dropListener.setToMove(staticLinkToB1);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        assertFalse(dropListener.validateDrop(staticLinkToC1, DND.DROP_MOVE, mockElementTransfer(cmptA)));
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        assertFalse(dropListener.validateDrop(staticLinkToC1, DND.DROP_MOVE, mockElementTransfer(cmptA)));
    }

    @Test
    public void testMoveStaticLinkToChangingAssociation() {
        setUpLinksAndSetEditorChangeable();

        dropListener.setToMove(staticLinkToB2);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 0 << 0 | 0 << 1 | 0 << 2 | 0 << 3 | 0 << 4);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkValidateMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, 0 << 0 | 0 << 1 | 0 << 2 | 0 << 3 | 0 << 4);
    }

    private void setUpLinksAndSetEditorChangeable() {
        when(linkSection.isDataChangeable()).thenReturn(true);

        b1Link1 = cmptAGeneration.newLink(associationToB1);
        b1Link1.setTarget(cmptB1.getQualifiedName());
        b1Link2 = cmptAGeneration.newLink(associationToB1);
        b1Link2.setTarget(cmptB2.getQualifiedName());

        b2Link2 = cmptAGeneration.newLink(associationToB2);
        b2Link2.setTarget(cmptB2.getQualifiedName());
        b2Link3 = cmptAGeneration.newLink(associationToB2);
        b2Link3.setTarget(cmptB3.getQualifiedName());

        cLink1 = cmptAGeneration.newLink(associationToC);
        cLink1.setTarget(cmptC1.getQualifiedName());
        cLink2 = cmptAGeneration.newLink(associationToC);
        cLink2.setTarget(cmptC2.getQualifiedName());
        cLink3 = cmptAGeneration.newLink(associationToC);
        cLink3.setTarget(cmptC3.getQualifiedName());

        staticLinkToB1 = cmptA.newLink(staticAssociationToB);
        staticLinkToB1.setTarget(cmptB1.getQualifiedName());
        staticLinkToB2 = cmptA.newLink(staticAssociationToB);
        staticLinkToB2.setTarget(cmptB2.getQualifiedName());
        staticLinkToC1 = cmptA.newLink(staticAssociationToC);
        staticLinkToC1.setTarget(cmptC1.getQualifiedName());
    }

    private void checkValidateMove(IProductCmptLink b1Link1,
            IProductCmptLink b1Link2,
            IProductCmptLink b2Link3,
            IProductCmptLink cLink1,
            IProductCmptLink cLink2,
            int bitmask) {
        // transfertype will be ignored anyway. Ignore "getTransfer(cmptA)".
        assertEquals((bitmask & 1 << 0) == 1 << 0,
                dropListener.validateDrop(b1Link1, DND.DROP_MOVE, mockElementTransfer(cmptA)));
        assertEquals((bitmask & 1 << 1) == 1 << 1,
                dropListener.validateDrop(b1Link2, DND.DROP_MOVE, mockElementTransfer(cmptA)));
        assertEquals((bitmask & 1 << 2) == 1 << 2,
                dropListener.validateDrop(b2Link3, DND.DROP_MOVE, mockElementTransfer(cmptA)));
        assertEquals((bitmask & 1 << 3) == 1 << 3,
                dropListener.validateDrop(cLink1, DND.DROP_MOVE, mockElementTransfer(cmptA)));
        assertEquals((bitmask & 1 << 4) == 1 << 4,
                dropListener.validateDrop(cLink2, DND.DROP_MOVE, mockElementTransfer(cmptA)));
    }

    @Test
    public void testPerformDropNewLink() {
        IProductCmptLink link = cmptAGeneration.newLink(associationToC);
        link.setTarget(cmptC3.getQualifiedName());

        dropListener.setTarget(link);
        dropListener.setOperation(DND.DROP_LINK);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkCreateNewLink(1, 0);

        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkCreateNewLink(0, 1);

        IProductCmptLink link2 = cmptAGeneration.newLink(associationToC);
        link2.setTarget(cmptC4.getQualifiedName());

        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkCreateNewLink(0, 2);

        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkCreateNewLink(1, 1);

        dropListener.setTarget(link2);

        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkCreateNewLink(1, 1);

        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkCreateNewLink(2, 0);

        dropListener.setTarget(new AssociationViewItem(cmptAGeneration, associationToC));
        checkCreateNewLink(0, 2);
    }

    private void checkCreateNewLink(int linksBeforeTarget, int linksAfterTarget) {
        // drop single component on target with no possibility to add
        assertFalse(dropListener.performDrop(getFilenames(cmptA)));
        IProductCmptLink[] links = cmptA.getFirstGeneration().getLinks();
        assertEquals(linksBeforeTarget + linksAfterTarget, links.length);

        // drop single component on target with one possibility to add
        assertTrue(dropListener.performDrop(getFilenames(cmptC1)));
        links = cmptA.getFirstGeneration().getLinks();
        assertEquals(1 + linksBeforeTarget + linksAfterTarget, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0 + linksBeforeTarget].getTarget());
        assertEquals(associationToC.getName(), links[0 + linksBeforeTarget].getAssociation());
        links[0 + linksBeforeTarget].delete();

        // drop multiple component on target with one possibility to add
        assertTrue(dropListener.performDrop(getFilenames(cmptC1, cmptC2)));
        links = cmptA.getFirstGeneration().getLinks();
        assertEquals(2 + linksBeforeTarget + linksAfterTarget, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0 + linksBeforeTarget].getTarget());
        assertEquals(cmptC2.getQualifiedName(), links[1 + linksBeforeTarget].getTarget());
        assertEquals(associationToC.getName(), links[0 + linksBeforeTarget].getAssociation());
        assertEquals(associationToC.getName(), links[1 + linksBeforeTarget].getAssociation());
        links[0 + linksBeforeTarget].delete();
        links[1 + linksBeforeTarget].delete();
    }

    @Test
    public void testPerformDropMoveLink() {
        IProductCmptLink b1Link1 = cmptAGeneration.newLink(associationToB1);
        b1Link1.setTarget(cmptB1.getQualifiedName());
        IProductCmptLink b1Link2 = cmptAGeneration.newLink(associationToB1);
        b1Link2.setTarget(cmptB2.getQualifiedName());

        IProductCmptLink b2Link3 = cmptAGeneration.newLink(associationToB2);
        b2Link3.setTarget(cmptB3.getQualifiedName());

        IProductCmptLink cLink1 = cmptAGeneration.newLink(associationToC);
        cLink1.setTarget(cmptC1.getQualifiedName());
        IProductCmptLink cLink2 = cmptAGeneration.newLink(associationToC);
        cLink2.setTarget(cmptC2.getQualifiedName());
        IProductCmptLink cLink3 = cmptAGeneration.newLink(associationToC);
        cLink3.setTarget(cmptC3.getQualifiedName());

        dropListener.setOperation(DND.DROP_MOVE);

        dropListener.setToMove(b1Link1);

        dropListener.setTarget(b1Link1);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, cLink3);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, cLink3);

        dropListener.setTarget(b1Link2);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, cLink3);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkMove(b1Link2, b1Link1, b2Link3, cLink1, cLink2, cLink3);

        dropListener.setTarget(b2Link3);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkMove(b1Link2, b1Link1, b2Link3, cLink1, cLink2, cLink3);
        assertEquals(associationToB2.getName(), b1Link1.getAssociation());
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkMove(b1Link2, b2Link3, b1Link1, cLink1, cLink2, cLink3);
        assertEquals(associationToB2.getName(), b1Link1.getAssociation());

        // test move to association (view item as target)
        dropListener.setTarget(new AssociationViewItem(cmptAGeneration, associationToB1));
        checkMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, cLink3);

        // multi move
        dropListener.setToMove(b1Link1, b1Link2);

        dropListener.setTarget(b1Link1);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkMove(b1Link2, b1Link1, b2Link3, cLink1, cLink2, cLink3);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, cLink3);

        assertEquals(associationToB1.getName(), b1Link1.getAssociation());
        assertEquals(associationToB1.getName(), b1Link2.getAssociation());

        dropListener.setTarget(b1Link2);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, cLink3);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkMove(b1Link2, b1Link1, b2Link3, cLink1, cLink2, cLink3);

        dropListener.setTarget(b2Link3);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_BEFORE);
        checkMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, cLink3);
        assertEquals(associationToB2.getName(), b1Link1.getAssociation());
        assertEquals(associationToB2.getName(), b1Link2.getAssociation());

        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);
        checkMove(b2Link3, b1Link1, b1Link2, cLink1, cLink2, cLink3);
        assertEquals(associationToB2.getName(), b1Link1.getAssociation());
        assertEquals(associationToB2.getName(), b1Link2.getAssociation());

        dropListener.setTarget(new AssociationViewItem(cmptAGeneration, associationToB1));
        checkMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, cLink3);
        assertEquals(associationToB1.getName(), b1Link1.getAssociation());
        assertEquals(associationToB1.getName(), b1Link2.getAssociation());

        dropListener.setTarget(cLink3);
        assertFalse(dropListener.performDrop(null));

        dropListener.setTarget(new AssociationViewItem(cmptAGeneration, associationToC));
        assertFalse(dropListener.performDrop(null));
    }

    @Test
    public void testDropLinkWithPolicyAssociationCardinalitySingleLink() {

        setUpPolicyAssociation();
        AssociationViewItem associationViewItem = new AssociationViewItem(cmptAGeneration, associationToC);
        dropListener.setTarget(associationViewItem);
        dropListener.setOperation(DND.DROP_LINK);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);

        assertTrue(dropListener.performDrop(getFilenames(cmptC1)));
        IProductCmptLink[] links = cmptA.getFirstGeneration().getLinks();
        assertEquals(1, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToC.getName(), links[0].getAssociation());
        assertEquals(1, links[0].getMinCardinality());
        assertEquals(1, links[0].getDefaultCardinality());
        assertEquals(Cardinality.CARDINALITY_MANY, links[0].getMaxCardinality());
    }

    @Test
    public void testDropLinkWithPolicyAssociationCardinalitySingleLinkTo1() {

        setUpPolicyAssociation();
        policyAssociationToC.setMaxCardinality(1);
        treeViewer.setInput(cmptAGeneration);

        AssociationViewItem associationViewItem = new AssociationViewItem(cmptAGeneration, associationToC);
        dropListener.setTarget(associationViewItem);
        dropListener.setOperation(DND.DROP_LINK);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);

        assertTrue(dropListener.performDrop(getFilenames(cmptC1)));
        IProductCmptLink[] links = cmptA.getFirstGeneration().getLinks();
        assertEquals(1, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToC.getName(), links[0].getAssociation());
        assertEquals(1, links[0].getMinCardinality());
        assertEquals(1, links[0].getDefaultCardinality());
        assertEquals(1, links[0].getMaxCardinality());
    }

    @Test
    public void testDropLinkWithPolicyAssociationCardinalityMultipleLinks() {

        setUpPolicyAssociation();

        AssociationViewItem associationViewItem = new AssociationViewItem(cmptAGeneration, associationToC);
        dropListener.setTarget(associationViewItem);
        dropListener.setOperation(DND.DROP_LINK);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);

        assertTrue(dropListener.performDrop(getFilenames(cmptC1, cmptC2)));
        IProductCmptLink[] links = cmptA.getFirstGeneration().getLinks();

        assertEquals(2, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToC.getName(), links[0].getAssociation());
        assertEquals(cmptC2.getQualifiedName(), links[1].getTarget());
        assertEquals(associationToC.getName(), links[1].getAssociation());

        // Link1
        assertEquals(0, links[0].getMinCardinality());
        assertEquals(0, links[0].getDefaultCardinality());
        assertEquals(1, links[0].getMaxCardinality());

        // Link2
        assertEquals(0, links[1].getMinCardinality());
        assertEquals(0, links[1].getDefaultCardinality());
        assertEquals(1, links[1].getMaxCardinality());

    }

    @Test
    public void testDropLinkWithPolicyAssociationCardinalityMultipleLinksOnTo1() {

        setUpPolicyAssociation();
        policyAssociationToC.setMaxCardinality(1);
        treeViewer.setInput(cmptAGeneration);

        AssociationViewItem associationViewItem = new AssociationViewItem(cmptAGeneration, associationToC);
        dropListener.setTarget(associationViewItem);
        dropListener.setOperation(DND.DROP_LINK);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);

        assertTrue(dropListener.performDrop(getFilenames(cmptC1, cmptC2)));
        IProductCmptLink[] links = cmptA.getFirstGeneration().getLinks();

        assertEquals(2, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToC.getName(), links[0].getAssociation());
        assertEquals(cmptC2.getQualifiedName(), links[1].getTarget());
        assertEquals(associationToC.getName(), links[1].getAssociation());

        // Link1
        assertEquals(0, links[0].getMinCardinality());
        assertEquals(0, links[0].getDefaultCardinality());
        assertEquals(1, links[0].getMaxCardinality());

        // Link2
        assertEquals(0, links[1].getMinCardinality());
        assertEquals(0, links[1].getDefaultCardinality());
        assertEquals(1, links[1].getMaxCardinality());

    }

    @Test
    public void testDropLinkWithPolicyAssociationCardinalityExistingLink() {

        setUpPolicyAssociation();

        var c1Link = cmptAGeneration.newLink(associationToC);
        c1Link.setTarget(cmptC1.getQualifiedName());
        IProductCmptLink[] links = cmptA.getFirstGeneration().getLinks(associationToC.getName());

        assertEquals(1, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToC.getName(), links[0].getAssociation());

        assertEquals(0, links[0].getMinCardinality());
        assertEquals(0, links[0].getDefaultCardinality());
        assertEquals(1, links[0].getMaxCardinality());

        AssociationViewItem associationViewItem = new AssociationViewItem(cmptAGeneration, associationToC);
        dropListener.setTarget(associationViewItem);
        dropListener.setOperation(DND.DROP_LINK);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);

        assertTrue(dropListener.performDrop(getFilenames(cmptC2)));
        links = cmptA.getFirstGeneration().getLinks();

        assertEquals(2, links.length);
        assertEquals(cmptC2.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToC.getName(), links[0].getAssociation());
        assertEquals(cmptC1.getQualifiedName(), links[1].getTarget());
        assertEquals(associationToC.getName(), links[1].getAssociation());

        // Link1
        assertEquals(0, links[0].getMinCardinality());
        assertEquals(0, links[0].getDefaultCardinality());
        assertEquals(1, links[0].getMaxCardinality());

        // Link2
        assertEquals(0, links[1].getMinCardinality());
        assertEquals(0, links[1].getDefaultCardinality());
        assertEquals(1, links[1].getMaxCardinality());

    }

    @Test
    public void testDropLinkWithPolicyAssociationCardinalityMultipleAssociationsToSameTargetClass() {

        setUpPolicyAssociation();

        b1Link1 = cmptAGeneration.newLink(associationToB1);
        b1Link1.setTarget(cmptB1.getQualifiedName());
        IProductCmptLink[] links = cmptA.getFirstGeneration().getLinks(associationToB1.getName());

        assertEquals(1, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToB1.getName(), links[0].getAssociation());

        assertEquals(0, links[0].getMinCardinality());
        assertEquals(0, links[0].getDefaultCardinality());
        assertEquals(1, links[0].getMaxCardinality());

        AssociationViewItem associationViewItem = new AssociationViewItem(cmptAGeneration, associationToB2);
        dropListener.setTarget(associationViewItem);
        dropListener.setOperation(DND.DROP_LINK);
        dropListener.setLocation(ViewerDropAdapter.LOCATION_AFTER);

        assertTrue(dropListener.performDrop(getFilenames(cmptB2)));
        links = cmptA.getFirstGeneration().getLinks(associationToB2.getName());

        assertEquals(1, links.length);
        assertEquals(cmptB2.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToB2.getName(), links[0].getAssociation());

        assertEquals(1, links[0].getMinCardinality());
        assertEquals(1, links[0].getDefaultCardinality());
        assertEquals(Cardinality.CARDINALITY_MANY, links[0].getMaxCardinality());
    }

    private void checkMove(IProductCmptLink... expected) {
        assertTrue(dropListener.performDrop(null));
        IProductCmptLink[] links = cmptAGeneration.getLinks();
        assertEquals(links.length, expected.length);
        for (int i = 0; i < links.length; i++) {
            assertEquals(expected[i], links[i]);
        }
    }

    private TransferData mockElementTransfer(IProductCmpt... cmpts) {
        when(dropListener.getTransferElements(any(TransferData.class))).thenReturn(Arrays.asList(cmpts));
        return mock(TransferData.class);
    }

    private String[] getFilenames(ProductCmpt... cmpts) {
        String[] filenames = new String[cmpts.length];
        for (int i = 0; i < cmpts.length; i++) {
            filenames[i] = cmpts[i].getIpsSrcFile().getCorrespondingFile().getLocation().toString();
        }
        return filenames;
    }

    private void setUpPolicyAssociation() {
        PolicyCmptType policyA = newPolicyCmptType(ipsProject, "testPack.PolicyA");
        policyA.setProductCmptType(typeA.getQualifiedName());
        typeA.setPolicyCmptType(policyA.getQualifiedName());

        PolicyCmptType policyB = newPolicyCmptType(ipsProject, "PolicyB");
        policyB.setProductCmptType(typeB.getQualifiedName());
        typeB.setPolicyCmptType(policyB.getQualifiedName());

        PolicyCmptType policyC = newPolicyCmptType(ipsProject, "PolicyC");
        policyC.setProductCmptType(typeC.getQualifiedName());
        typeC.setPolicyCmptType(policyC.getQualifiedName());

        policyAssociationToC = policyA.newPolicyCmptTypeAssociation();
        policyAssociationToC.setTargetRoleSingular("associationToC");
        policyAssociationToC.setTarget(policyC.getQualifiedName());
        policyAssociationToC.setMinCardinality(1);
        policyAssociationToC.setMaxCardinality(Cardinality.CARDINALITY_MANY);

        associationToC.setMatchingAssociationName(policyAssociationToC.getName());
        associationToC.setMatchingAssociationSource(policyA.getQualifiedName());

        IPolicyCmptTypeAssociation policyAssociationToB1 = policyA.newPolicyCmptTypeAssociation();
        policyAssociationToB1.setTargetRoleSingular("associationToB1");
        policyAssociationToB1.setTarget(policyB.getQualifiedName());
        policyAssociationToB1.setMinCardinality(1);
        policyAssociationToB1.setMaxCardinality(Cardinality.CARDINALITY_MANY);

        associationToB1.setMatchingAssociationName(policyAssociationToB1.getName());
        associationToB1.setMatchingAssociationSource(policyA.getQualifiedName());

        IPolicyCmptTypeAssociation policyAssociationToB2 = policyA.newPolicyCmptTypeAssociation();
        policyAssociationToB2.setTargetRoleSingular("associationToB2");
        policyAssociationToB2.setTarget(policyB.getQualifiedName());
        policyAssociationToB2.setMinCardinality(1);
        policyAssociationToB2.setMaxCardinality(Cardinality.CARDINALITY_MANY);

        associationToB2.setMatchingAssociationName(policyAssociationToB2.getName());
        associationToB2.setMatchingAssociationSource(policyA.getQualifiedName());

        treeViewer.setInput(cmptAGeneration);
    }

    public static class TestDropListener extends LinkSectionDropListener {

        private Object target;
        private int operation;
        private int location;

        public TestDropListener(LinksSection section, IProductCmptGeneration generation) {
            super(section, generation);
        }

        public void setTarget(Object target) {
            this.target = target;

        }

        @Override
        protected Object getCurrentTarget() {
            return target;
        }

        @Override
        protected int getCurrentOperation() {
            return operation;
        }

        public void setOperation(int operation) {
            this.operation = operation;
        }

        @Override
        protected int getCurrentLocation() {
            return location;
        }

        public void setLocation(int location) {
            this.location = location;
        }

        public void setToMove(IProductCmptLink... links) {
            setToMove(Arrays.asList(links));
        }

        @Override
        public void setToMove(List<IProductCmptLink> selectedLinks) {
            super.setToMove(selectedLinks);
        }

    }

}
