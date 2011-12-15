/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;

public class LinkSectionDropListenerTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private ProductCmptType typeA;
    private ProductCmptType typeB;
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
    private ProductCmptEditor productCmptEditor;

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

        cmptA = newProductCmpt(typeA, "CmptA");
        cmptAGeneration = (IProductCmptGeneration)cmptA.getFirstGeneration();
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

        productCmptEditor = mock(ProductCmptEditor.class);
        dropListener = spy(new TestDropListener(productCmptEditor, treeViewer, cmptAGeneration));
    }

    @Test
    public void testValidateDropNewLink() {
        when(productCmptEditor.isDataChangeable()).thenReturn(true);

        int operation = DND.DROP_LINK;

        // check (link) targets
        IProductCmptLink link = ((IProductCmptGeneration)cmptA.getFirstGeneration()).newLink(associationToB1);
        link.setTarget(cmptB1.getQualifiedName());
        assertFalse(dropListener.validateDrop(link, operation, getTransfer(cmptB1)));
        assertFalse(dropListener.validateDrop(link, operation, getTransfer(cmptB1, cmptB2)));
        assertTrue(dropListener.validateDrop(link, operation, getTransfer(cmptB2)));
        assertTrue(dropListener.validateDrop(link, operation, getTransfer(cmptB2, cmptB3)));
        assertFalse(dropListener.validateDrop(link, operation, getTransfer(cmptC1)));
        assertFalse(dropListener.validateDrop(link, operation, getTransfer(cmptC1, cmptB1)));
        assertFalse(dropListener.validateDrop(link, operation, getTransfer(cmptC1, cmptB2)));
        link.delete();
    }

    @Test
    public void testValidateDrop_editorEditable() throws Exception {
        when(productCmptEditor.isDataChangeable()).thenReturn(true);

        int operation = DND.DROP_LINK;

        IProductCmptLink link = ((IProductCmptGeneration)cmptA.getFirstGeneration()).newLink(associationToB1);
        link.setTarget(cmptB1.getQualifiedName());
        assertTrue(dropListener.validateDrop(link, operation, getTransfer(cmptB2)));

        when(productCmptEditor.isDataChangeable()).thenReturn(false);
        assertFalse(dropListener.validateDrop(link, operation, getTransfer(cmptB2)));
    }

    @Test
    public void testValidateDropMoveLink() {
        when(productCmptEditor.isDataChangeable()).thenReturn(true);

        IProductCmptLink b1Link1 = cmptAGeneration.newLink(associationToB1);
        b1Link1.setTarget(cmptB1.getQualifiedName());
        IProductCmptLink b1Link2 = cmptAGeneration.newLink(associationToB1);
        b1Link2.setTarget(cmptB2.getQualifiedName());

        IProductCmptLink b2Link2 = cmptAGeneration.newLink(associationToB2);
        b2Link2.setTarget(cmptB2.getQualifiedName());
        IProductCmptLink b2Link3 = cmptAGeneration.newLink(associationToB2);
        b2Link3.setTarget(cmptB3.getQualifiedName());

        IProductCmptLink cLink1 = cmptAGeneration.newLink(associationToC);
        cLink1.setTarget(cmptC1.getQualifiedName());
        IProductCmptLink cLink2 = cmptAGeneration.newLink(associationToC);
        cLink2.setTarget(cmptC2.getQualifiedName());
        IProductCmptLink cLink3 = cmptAGeneration.newLink(associationToC);
        cLink3.setTarget(cmptC3.getQualifiedName());

        // transfertype will be ignored --> just any transferType
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

    private void checkValidateMove(IProductCmptLink b1Link1,
            IProductCmptLink b1Link2,
            IProductCmptLink b2Link3,
            IProductCmptLink cLink1,
            IProductCmptLink cLink2,
            int bitmask) {
        assertEquals((bitmask & 1 << 0) == 1 << 0,
                dropListener.validateDrop(b1Link1, DND.DROP_MOVE, getTransfer(cmptA)));
        assertEquals((bitmask & 1 << 1) == 1 << 1,
                dropListener.validateDrop(b1Link2, DND.DROP_MOVE, getTransfer(cmptA)));
        assertEquals((bitmask & 1 << 2) == 1 << 2,
                dropListener.validateDrop(b2Link3, DND.DROP_MOVE, getTransfer(cmptA)));
        assertEquals((bitmask & 1 << 3) == 1 << 3, dropListener.validateDrop(cLink1, DND.DROP_MOVE, getTransfer(cmptA)));
        assertEquals((bitmask & 1 << 4) == 1 << 4, dropListener.validateDrop(cLink2, DND.DROP_MOVE, getTransfer(cmptA)));
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

        dropListener.setTarget(associationToC.getName());
        checkCreateNewLink(0, 2);
    }

    private void checkCreateNewLink(int linksBeforeTarget, int linksAfterTarget) {
        // drop single component on target with no possibility to add
        assertFalse(dropListener.performDrop(getFilenames(cmptA)));
        IProductCmptLink[] links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(linksBeforeTarget + linksAfterTarget, links.length);

        // drop single component on target with one possibility to add
        assertTrue(dropListener.performDrop(getFilenames(cmptC1)));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(1 + linksBeforeTarget + linksAfterTarget, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0 + linksBeforeTarget].getTarget());
        assertEquals(associationToC.getName(), links[0 + linksBeforeTarget].getAssociation());
        links[0 + linksBeforeTarget].delete();

        // drop multiple component on target with one possibility to add
        assertTrue(dropListener.performDrop(getFilenames(cmptC1, cmptC2)));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
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

        // test move to association (String target)
        dropListener.setTarget(associationToB1.getName());
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

        dropListener.setTarget(associationToB1.getName());
        checkMove(b1Link1, b1Link2, b2Link3, cLink1, cLink2, cLink3);
        assertEquals(associationToB1.getName(), b1Link1.getAssociation());
        assertEquals(associationToB1.getName(), b1Link2.getAssociation());

        dropListener.setTarget(cLink3);
        assertFalse(dropListener.performDrop(null));

        dropListener.setTarget(associationToC.getName());
        assertFalse(dropListener.performDrop(null));
    }

    private void checkMove(IProductCmptLink... expected) {
        assertTrue(dropListener.performDrop(null));
        IProductCmptLink[] links = cmptAGeneration.getLinks();
        assertEquals(links.length, expected.length);
        for (int i = 0; i < links.length; i++) {
            assertEquals(expected[i], links[i]);
        }
    }

    private TransferData getTransfer(ProductCmpt... cmpts) {
        String[] filenames = getFilenames(cmpts);
        TransferData transfer = FileTransfer.getInstance().getSupportedTypes()[0];
        FileTransfer.getInstance().javaToNative(filenames, transfer);
        return transfer;
    }

    private String[] getFilenames(ProductCmpt... cmpts) {
        String[] filenames = new String[cmpts.length];
        for (int i = 0; i < cmpts.length; i++) {
            filenames[i] = cmpts[i].getIpsSrcFile().getCorrespondingFile().getLocation().toOSString();
        }
        return filenames;
    }

    public static class TestDropListener extends LinkSectionDropListener {

        private Object target;
        private int operation;
        private int location;

        public TestDropListener(ProductCmptEditor editor, Viewer viewer, IProductCmptGeneration generation) {
            super(editor, viewer, generation);
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
