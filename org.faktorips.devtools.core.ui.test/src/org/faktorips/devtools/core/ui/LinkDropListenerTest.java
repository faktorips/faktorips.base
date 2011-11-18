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

package org.faktorips.devtools.core.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.util.LinkCreatorUtil;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureContentProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing LinkDropListener
 * 
 * Note: LinkCreatorUtilTest is very similar
 * 
 * @author Cornelius Dirmeier
 */
public class LinkDropListenerTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private ProductCmptType typeA;
    private ProductCmptType typeB;
    private ProductCmptType typeC;
    private ProductCmpt cmptA;
    private ProductCmpt cmptB1;
    private ProductCmpt cmptB2;
    private ProductCmpt cmptC1;
    private TestDropListener dropListener;
    private ProductStructureContentProvider contentProvider;
    private IProductCmptTreeStructure structure;
    private ProductCmpt cmptC2;
    private IProductCmptTypeAssociation associationToB1;
    private IProductCmptTypeAssociation associationToB2;
    private IProductCmptTypeAssociation associationToC;
    private FileTransfer fileTransfer;

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
        cmptB1 = newProductCmpt(typeB, "testPackX.CmptB1");
        cmptB2 = newProductCmpt(typeB, "CmptB2");
        cmptC1 = newProductCmpt(typeC, "CmptC1");
        cmptC2 = newProductCmpt(typeC, "testPackZ.CmptC2");

        contentProvider = new ProductStructureContentProvider(true);
        structure = cmptA.getStructure(new GregorianCalendar(), ipsProject);

        TreeViewer treeViewer = new TreeViewer(new Shell(Display.getDefault()));
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setInput(structure);

        dropListener = new TestDropListener(treeViewer);

        fileTransfer = FileTransfer.getInstance();
    }

    @Test
    public void testValidateDropEditableState() {
        IProductCmptReference target = structure.getRoot();
        TransferData transfer = getTransfer(cmptB1);

        // check if source file editable state is recognized
        assertTrue(dropListener.validateDropSingle(target, DND.DROP_LINK, transfer));
        IpsPlugin.getDefault().getIpsPreferences().setWorkingMode(IpsPreferences.WORKING_MODE_BROWSE);
        assertFalse(dropListener.validateDropSingle(target, DND.DROP_LINK, transfer));
        IpsPlugin.getDefault().getIpsPreferences().setWorkingMode(IpsPreferences.WORKING_MODE_EDIT);
        assertTrue(dropListener.validateDropSingle(target, DND.DROP_LINK, transfer));
    }

    @Test
    public void testValidateDropForReferenceTargets() {
        int operation = DND.DROP_LINK;
        TransferData transfer = getTransfer(cmptB1);

        // check (reference) targets
        assertTrue(dropListener.validateDropSingle(structure.getRoot(), operation, transfer));
        IProductCmptTypeAssociationReference[] references = structure
                .getChildProductCmptTypeAssociationReferences(structure.getRoot());
        assertTrue(dropListener.validateDropSingle(references[0], operation, transfer));
        assertTrue(dropListener.validateDropSingle(references[1], operation, transfer));
        assertFalse(dropListener.validateDropSingle(references[2], operation, transfer));

        TransferData multiTransfer = getTransfer(cmptB1, cmptB2);
        assertFalse(dropListener.validateDropSingle(references[2], operation, multiTransfer));
        assertTrue(dropListener.validateDropSingle(references[1], operation, multiTransfer));
        assertTrue(dropListener.validateDropSingle(references[0], operation, multiTransfer));
    }

    @Test
    public void testValidateDropTransfer() {
        IProductCmptReference target = structure.getRoot();
        int operation = DND.DROP_LINK;

        // check transfer
        assertTrue(dropListener.validateDropSingle(target, operation, getTransfer(cmptB1)));
        assertTrue(dropListener.validateDropSingle(target, operation, getTransfer(cmptB2)));
        assertTrue(dropListener.validateDropSingle(target, operation, getTransfer(cmptC1)));
        assertFalse(dropListener.validateDropSingle(target, operation, getTransfer(cmptA)));
        assertTrue(dropListener.validateDropSingle(target, operation, getTransfer(cmptB1, cmptB2)));
        assertTrue(dropListener.validateDropSingle(target, operation, getTransfer(cmptB1, cmptC1)));
        assertFalse(dropListener.validateDropSingle(target, operation, getTransfer(cmptB1, cmptA)));
    }

    @Test
    public void testPerformDropObject() {
        IProductCmptLink[] links;

        // CmptReference target
        dropListener.setTarget(structure.getRoot());
        checkDropWithSinglePossibility(0);

        // AssociationReference target
        IProductCmptTypeAssociationReference[] references = structure
                .getChildProductCmptTypeAssociationReferences(structure.getRoot());
        dropListener.setTarget(references[2]);
        checkDropWithSinglePossibility(0);

        // drop single component on CmptReference with multiple possibility to add
        dropListener.setTarget(structure.getRoot());

        // select first
        dropListener.setSelection(1 << 0);
        assertTrue(dropListener.performDropSingle(getFilenames(cmptB1)));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(1, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToB1.getName(), links[0].getAssociation());
        links[0].delete();

        // select second
        dropListener.setSelection(1 << 1);
        assertTrue(dropListener.performDropSingle(getFilenames(cmptB1)));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(1, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToB2.getName(), links[0].getAssociation());
        links[0].delete();

        // select both
        dropListener.setSelection(1 << 0 | 1 << 1);
        assertTrue(dropListener.performDropSingle(getFilenames(cmptB1)));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(2, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(cmptB1.getQualifiedName(), links[1].getTarget());
        assertEquals(associationToB1.getName(), links[0].getAssociation());
        assertEquals(associationToB2.getName(), links[1].getAssociation());
        links[0].delete();
        links[1].delete();

        // drop multiple component on CmptReference with multiple possibility to add

        // select first
        dropListener.setSelection(1 << 0);
        assertTrue(dropListener.performDropSingle(getFilenames(cmptB1, cmptB2)));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(2, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(cmptB2.getQualifiedName(), links[1].getTarget());
        assertEquals(associationToB1.getName(), links[0].getAssociation());
        assertEquals(associationToB1.getName(), links[1].getAssociation());
        links[0].delete();
        links[1].delete();

        // select second
        dropListener.setSelection(1 << 1);
        assertTrue(dropListener.performDropSingle(getFilenames(cmptB1, cmptB2)));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertTrue(links.length == 2);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(cmptB2.getQualifiedName(), links[1].getTarget());
        assertEquals(associationToB2.getName(), links[0].getAssociation());
        assertEquals(associationToB2.getName(), links[1].getAssociation());
        links[0].delete();
        links[1].delete();

        // select both
        dropListener.setSelection(1 << 0 | 1 << 1);
        assertTrue(dropListener.performDropSingle(getFilenames(cmptB1, cmptB2)));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(4, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(cmptB1.getQualifiedName(), links[1].getTarget());
        assertEquals(cmptB2.getQualifiedName(), links[2].getTarget());
        assertEquals(cmptB2.getQualifiedName(), links[3].getTarget());
        assertEquals(associationToB1.getName(), links[0].getAssociation());
        assertEquals(associationToB2.getName(), links[1].getAssociation());
        assertEquals(associationToB1.getName(), links[2].getAssociation());
        assertEquals(associationToB2.getName(), links[3].getAssociation());
        links[0].delete();
        links[1].delete();
        links[2].delete();
        links[3].delete();
    }

    @Test
    public void testSaveFile() throws CoreException {
        IIpsSrcFile ipsSrcFile = cmptA.getIpsSrcFile();
        assertTrue(ipsSrcFile.isMutable());
        ipsSrcFile.save(false, null);
        assertFalse(ipsSrcFile.isDirty());

        // test with cmpt reference target
        dropListener.setTarget(structure.getRoot());
        checkSaveFile(ipsSrcFile);

        // test with association reference target
        IProductCmptTypeAssociationReference[] references = structure
                .getChildProductCmptTypeAssociationReferences(structure.getRoot());
        dropListener.setTarget(references[2]);
        checkSaveFile(ipsSrcFile);
    }

    private void checkSaveFile(IIpsSrcFile ipsSrcFile) throws CoreException {
        dropListener.setAutoSave(false);
        assertFalse(ipsSrcFile.isDirty());
        assertTrue(dropListener.performDropSingle(getFilenames(cmptC1)));
        assertTrue(ipsSrcFile.isDirty());
        assertTrue(dropListener.performDropSingle(getFilenames(cmptC2)));
        assertTrue(ipsSrcFile.isDirty());
        ipsSrcFile.discardChanges();

        dropListener.setAutoSave(true);
        assertTrue(dropListener.performDropSingle(getFilenames(cmptC1)));
        assertFalse(ipsSrcFile.isDirty());

        IProductCmptLink[] links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        // delete the last one
        links[links.length - 1].delete();
        assertTrue(ipsSrcFile.isDirty());
        assertTrue(dropListener.performDropSingle(getFilenames(cmptC1)));
        assertTrue(ipsSrcFile.isDirty());

        // reset for next test
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        links[0].delete();
        ipsSrcFile.save(false, null);
    }

    /**
     * check dropping components on the target, target have to be set before calling this method. In
     * case of target is a link there already have to exists at least one link
     * 
     * @param alreadyExistingLinks The number of links that already exists in the product cmpt
     */
    private void checkDropWithSinglePossibility(int alreadyExistingLinks) {
        // drop single component on CmptReference with no possibility to add
        assertFalse(dropListener.performDropSingle(getFilenames(cmptA)));
        IProductCmptLink[] links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(alreadyExistingLinks, links.length);

        // drop single component on CmptReference with one possibility to add
        assertTrue(dropListener.performDropSingle(getFilenames(cmptC1)));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(1 + alreadyExistingLinks, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0 + alreadyExistingLinks].getTarget());
        assertEquals(associationToC.getName(), links[0 + alreadyExistingLinks].getAssociation());
        links[0 + alreadyExistingLinks].delete();

        // drop multiple component on CmptReference with one possibility to add
        assertTrue(dropListener.performDropSingle(getFilenames(cmptC1, cmptC2)));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(2 + alreadyExistingLinks, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0 + alreadyExistingLinks].getTarget());
        assertEquals(cmptC2.getQualifiedName(), links[1 + alreadyExistingLinks].getTarget());
        assertEquals(associationToC.getName(), links[0 + alreadyExistingLinks].getAssociation());
        assertEquals(associationToC.getName(), links[1 + alreadyExistingLinks].getAssociation());
        links[0 + alreadyExistingLinks].delete();
        links[1 + alreadyExistingLinks].delete();
    }

    private TransferData getTransfer(ProductCmpt... cmpts) {
        String[] filenames = getFilenames(cmpts);

        TransferData transferData = fileTransfer.getSupportedTypes()[0];

        FileTransfer transfer = mock(FileTransfer.class);
        when(transfer.nativeToJava(transferData)).thenReturn(filenames);
        when(transfer.isSupportedType(transferData)).thenReturn(true);
        SingletonMockHelper.setSingletonInstance(FileTransfer.class, transfer);

        return transferData;
    }

    private String[] getFilenames(ProductCmpt... cmpts) {
        String[] filenames = new String[cmpts.length];
        for (int i = 0; i < cmpts.length; i++) {
            filenames[i] = cmpts[i].getIpsSrcFile().getCorrespondingFile().getLocation().toOSString();
        }
        return filenames;
    }

    /**
     * Override the LinkDropListener to inject the current target without constructing drop events
     * and to select possible associations without clicking dialogs
     * 
     * @author Cornelius Dirmeier
     */
    private static class TestDropListener extends LinkDropListener {

        private int selection;

        private Object target;

        public TestDropListener(Viewer viewer) {
            super(viewer);
            super.setLinkCreator(new MyLinkCreator(true));
        }

        public void setAutoSave(boolean b) {
            setLinkCreator(new MyLinkCreator(b));
        }

        @Override
        protected Object getCurrentTarget() {
            return target;
        }

        @Override
        protected int getCurrentOperation() {
            return DND.DROP_LINK;
        }

        /**
         * @param target The target to set.
         */
        public void setTarget(Object target) {
            this.target = target;
        }

        /**
         * @param selection The selection to set.
         */
        public void setSelection(int selection) {
            this.selection = selection;
        }

        public class MyLinkCreator extends LinkCreatorUtil {

            public MyLinkCreator(boolean autoSave) {
                super(autoSave);
            }

            @Override
            protected Object[] selectAssociation(String droppedCmptName, List<IAssociation> possibleAssos) {
                IAssociation[] result = new IAssociation[Integer.bitCount(selection)];
                int j = 0;
                for (int i = 0; i < possibleAssos.size(); i++) {
                    int col = 1 << i;
                    if ((col & selection) == col) {
                        result[j] = possibleAssos.get(i);
                        j++;
                    }
                }
                return result;
            }

        }

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        SingletonMockHelper.setSingletonInstance(FileTransfer.class, fileTransfer);
    }

}
