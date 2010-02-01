/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.util;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureContentProvider;

/**
 * Testing LinkDropListener
 * 
 * Also testing LinkCreatorUtil
 * 
 * @author Cornelius Dirmeier
 * 
 */
public class LinkCreatorUtilTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private ProductCmptType typeA;
    private ProductCmptType typeB;
    private ProductCmptType typeC;
    private ProductCmpt cmptA;
    private ProductCmpt cmptB1;
    private ProductCmpt cmptB2;
    private ProductCmpt cmptC1;
    private ProductStructureContentProvider contentProvider;
    private IProductCmptTreeStructure structure;
    private ProductCmpt cmptB3;
    private ProductCmpt cmptC2;
    private IProductCmptTypeAssociation associationToB1;
    private IProductCmptTypeAssociation associationToB2;
    private IProductCmptTypeAssociation associationToC;
    private ProductCmpt cmptC3;
    private MyLinkCreator linkCreator;
    private ProductCmptType typeSubB;
    private ProductCmpt cmptSubB1;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        typeA = newProductCmptType(ipsProject, "testPack.TypA");
        typeB = newProductCmptType(ipsProject, "testPackB.TypB");
        typeC = newProductCmptType(ipsProject, "TypC");
        typeSubB = newProductCmptType(ipsProject, "TypSubB");
        typeSubB.setSupertype(typeB.getQualifiedName());

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
        cmptB3 = newProductCmpt(typeB, "testPackY.CmptB3");
        cmptC1 = newProductCmpt(typeC, "CmptC1");
        cmptC2 = newProductCmpt(typeC, "testPackZ.CmptC2");
        cmptC3 = newProductCmpt(typeC, "CmptC3");
        cmptSubB1 = newProductCmpt(typeSubB, "CmptSubB1");

        contentProvider = new ProductStructureContentProvider(true);
        structure = cmptA.getStructure(new GregorianCalendar(), ipsProject);

        TreeViewer treeViewer = new TreeViewer(new Shell(Display.getDefault()));
        treeViewer.setContentProvider(contentProvider);
        treeViewer.setInput(structure);

        linkCreator = new MyLinkCreator(true);
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.ui.LinkDropListener#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)}
     * .
     * 
     * @throws CoreException
     */
    public void testValidateCreateLink() throws CoreException {
        IProductCmptReference target = structure.getRoot();
        List<IProductCmpt> singleCmpt = getList(cmptB1);
        List<IProductCmpt> multiCmpts = getList(cmptB1, cmptB2);

        // check transfer
        assertTrue(linkCreator.canCreateLinks(target, getList(cmptB1)));
        assertTrue(linkCreator.canCreateLinks(target, getList(cmptB2)));
        assertTrue(linkCreator.canCreateLinks(target, getList(cmptC1)));
        assertFalse(linkCreator.canCreateLinks(target, getList(cmptA)));
        assertTrue(linkCreator.canCreateLinks(target, getList(cmptB1, cmptB2)));
        assertTrue(linkCreator.canCreateLinks(target, getList(cmptB1, cmptC1)));
        assertFalse(linkCreator.canCreateLinks(target, getList(cmptB1, cmptA)));

        assertTrue(linkCreator.canCreateLinks(target, getList(cmptSubB1)));

        // check (reference) targets
        assertTrue(linkCreator.canCreateLinks(structure.getRoot(), singleCmpt));
        IProductCmptTypeAssociationReference[] references = structure.getChildProductCmptTypeAssociationReferences(structure
                .getRoot());
        assertTrue(linkCreator.canCreateLinks(references[0], singleCmpt));
        assertTrue(linkCreator.canCreateLinks(references[0], multiCmpts));
        assertTrue(linkCreator.canCreateLinks(references[1], singleCmpt));
        assertTrue(linkCreator.canCreateLinks(references[1], multiCmpts));
        assertFalse(linkCreator.canCreateLinks(references[2], singleCmpt));
        assertFalse(linkCreator.canCreateLinks(references[2], multiCmpts));

        // check (link) targets
        IProductCmptLink link = ((IProductCmptGeneration)cmptA.getFirstGeneration()).newLink(associationToB1);
        link.setTarget(cmptB1.getQualifiedName());
        assertFalse(linkCreator.canCreateLinks(link, getList(cmptB1)));
        assertFalse(linkCreator.canCreateLinks(link, getList(cmptB1, cmptB2)));
        assertTrue(linkCreator.canCreateLinks(link, getList(cmptB2)));
        assertTrue(linkCreator.canCreateLinks(link, getList(cmptB2, cmptB3)));
        assertFalse(linkCreator.canCreateLinks(link, getList(cmptC1)));
        assertFalse(linkCreator.canCreateLinks(link, getList(cmptC1, cmptB1)));
        assertFalse(linkCreator.canCreateLinks(link, getList(cmptC1, cmptB2)));
        link.delete();
    }

    /**
     * Test method for
     * {@link org.faktorips.devtools.core.ui.LinkDropListener#performDrop(java.lang.Object)}.
     */
    public void testCreateLink() {
        IProductCmptLink[] links;

        // CmptReference target
        checkDropWithSinglePossibility(structure.getRoot(), 0);

        // AssociationReference target
        IProductCmptTypeAssociationReference[] references = structure.getChildProductCmptTypeAssociationReferences(structure
                .getRoot());
        checkDropWithSinglePossibility(references[2], 0);

        // Link target
        IProductCmptLink link = ((IProductCmptGeneration)cmptA.getFirstGeneration()).newLink(associationToC);
        link.setTarget(cmptC3.getQualifiedName());
        checkDropWithSinglePossibility(link, 1);
        link.delete();

        // drop single component on CmptReference with multiple possibility to add

        // select first
        linkCreator.setSelection(1 << 0);
        assertTrue(linkCreator.createLinks(getList(cmptB1), structure.getRoot()));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(1, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToB1.getName(), links[0].getAssociation());
        links[0].delete();

        // select second
        linkCreator.setSelection(1 << 1);
        assertTrue(linkCreator.createLinks(getList(cmptB1), structure.getRoot()));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(1, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToB2.getName(), links[0].getAssociation());
        links[0].delete();

        // select both
        linkCreator.setSelection(1 << 0 | 1 << 1);
        assertTrue(linkCreator.createLinks(getList(cmptB1), structure.getRoot()));
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
        linkCreator.setSelection(1 << 0);
        assertTrue(linkCreator.createLinks(getList(cmptB1, cmptB2), structure.getRoot()));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(2, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(cmptB2.getQualifiedName(), links[1].getTarget());
        assertEquals(associationToB1.getName(), links[0].getAssociation());
        assertEquals(associationToB1.getName(), links[1].getAssociation());
        links[0].delete();
        links[1].delete();

        // select second
        linkCreator.setSelection(1 << 1);
        assertTrue(linkCreator.createLinks(getList(cmptB1, cmptB2), structure.getRoot()));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertTrue(links.length == 2);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(cmptB2.getQualifiedName(), links[1].getTarget());
        assertEquals(associationToB2.getName(), links[0].getAssociation());
        assertEquals(associationToB2.getName(), links[1].getAssociation());
        links[0].delete();
        links[1].delete();

        // select both
        linkCreator.setSelection(1 << 0 | 1 << 1);
        assertTrue(linkCreator.createLinks(getList(cmptB1, cmptSubB1), structure.getRoot()));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(4, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(cmptB1.getQualifiedName(), links[1].getTarget());
        assertEquals(cmptSubB1.getQualifiedName(), links[2].getTarget());
        assertEquals(cmptSubB1.getQualifiedName(), links[3].getTarget());
        assertEquals(associationToB1.getName(), links[0].getAssociation());
        assertEquals(associationToB2.getName(), links[1].getAssociation());
        assertEquals(associationToB1.getName(), links[2].getAssociation());
        assertEquals(associationToB2.getName(), links[3].getAssociation());
        links[0].delete();
        links[1].delete();
        links[2].delete();
        links[3].delete();
    }

    public void testSaveFile() throws CoreException {
        IIpsSrcFile ipsSrcFile = cmptA.getIpsSrcFile();
        assertTrue(ipsSrcFile.isMutable());
        ipsSrcFile.save(false, null);
        assertFalse(ipsSrcFile.isDirty());

        // test with cmpt reference target
        checkSaveFile(ipsSrcFile, structure.getRoot());

        // test with association reference target
        IProductCmptTypeAssociationReference[] references = structure.getChildProductCmptTypeAssociationReferences(structure
                .getRoot());
        checkSaveFile(ipsSrcFile, references[2]);

        // test with link target
        IProductCmptLink link = ((IProductCmptGeneration)cmptA.getFirstGeneration()).newLink(associationToC);
        link.setTarget(cmptC3.getQualifiedName());
        ipsSrcFile.save(false, null);
        checkSaveFile(ipsSrcFile, structure.getRoot());
    }

    private void checkSaveFile(IIpsSrcFile ipsSrcFile, Object target) throws CoreException {
        linkCreator.setAutoSave(false);
        assertFalse(ipsSrcFile.isDirty());
        assertTrue(linkCreator.createLinks(getList(cmptC1), target));
        assertTrue(ipsSrcFile.isDirty());
        assertTrue(linkCreator.createLinks(getList(cmptC2), target));
        assertTrue(ipsSrcFile.isDirty());
        ipsSrcFile.discardChanges();

        linkCreator.setAutoSave(true);
        assertTrue(linkCreator.createLinks(getList(cmptC1), target));
        assertFalse(ipsSrcFile.isDirty());

        IProductCmptLink[] links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        // delete the last one
        links[links.length - 1].delete();
        assertTrue(ipsSrcFile.isDirty());
        assertTrue(linkCreator.createLinks(getList(cmptC1), target));
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
     * @param the number of links that already exists in the product cmpt
     * 
     */
    private void checkDropWithSinglePossibility(Object target, int alreadyExistingLinks) {
        // drop single component on CmptReference with no possibility to add
        assertFalse(linkCreator.createLinks(getList(cmptA), target));
        IProductCmptLink[] links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(alreadyExistingLinks, links.length);

        // drop single component on CmptReference with one possibility to add
        assertTrue(linkCreator.createLinks(getList(cmptC1), target));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(1 + alreadyExistingLinks, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0 + alreadyExistingLinks].getTarget());
        assertEquals(associationToC.getName(), links[0 + alreadyExistingLinks].getAssociation());
        links[0 + alreadyExistingLinks].delete();

        // drop multiple component on CmptReference with one possibility to add
        assertTrue(linkCreator.createLinks(getList(cmptC1, cmptC2), target));
        links = ((IProductCmptGeneration)cmptA.getFirstGeneration()).getLinks();
        assertEquals(2 + alreadyExistingLinks, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0 + alreadyExistingLinks].getTarget());
        assertEquals(cmptC2.getQualifiedName(), links[1 + alreadyExistingLinks].getTarget());
        assertEquals(associationToC.getName(), links[0 + alreadyExistingLinks].getAssociation());
        assertEquals(associationToC.getName(), links[1 + alreadyExistingLinks].getAssociation());
        links[0 + alreadyExistingLinks].delete();
        links[1 + alreadyExistingLinks].delete();
    }

    private List<IProductCmpt> getList(IProductCmpt... cmpts) {
        return Arrays.asList(cmpts);
    }

    /**
     * override the original class to select an association without calling the dialog. Dialog
     * clicking could not be tested very well.
     * 
     * @author dirmeier
     */
    public class MyLinkCreator extends LinkCreatorUtil {

        private int selection;

        public MyLinkCreator(boolean autoSave) {
            super(autoSave);
        }

        @Override
        protected Object[] selectAssociation(String droppedCmptName, List<IAssociation> possibleAssos) {
            IAssociation[] result = new IAssociation[Integer.bitCount(getSelection())];
            int j = 0;
            for (int i = 0; i < possibleAssos.size(); i++) {
                int col = 1 << i;
                if ((col & getSelection()) == col) {
                    result[j] = possibleAssos.get(i);
                    j++;
                }
            }
            return result;
        }

        /**
         * @param selection The selection to set.
         */
        public void setSelection(int selection) {
            this.selection = selection;
        }

        /**
         * @return Returns the selection.
         */
        public int getSelection() {
            return selection;
        }

    }

}
