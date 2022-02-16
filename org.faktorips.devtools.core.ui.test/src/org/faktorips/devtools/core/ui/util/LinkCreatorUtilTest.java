/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureContentProvider;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for LinkCreatorUtil
 * 
 * @author Cornelius Dirmeier
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
    // private ProductCmpt cmptB3;
    private ProductCmpt cmptC2;
    private IProductCmptTypeAssociation associationToB1;
    private IProductCmptTypeAssociation associationToB2;
    private IProductCmptTypeAssociation associationToC;
    private ProductCmpt cmptC3;
    private MyLinkCreator linkCreator;
    private ProductCmptType typeSubB;
    private ProductCmpt cmptSubB1;

    @Override
    @Before
    public void setUp() throws Exception {
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
        // cmptB3 = newProductCmpt(typeB, "testPackY.CmptB3");
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

    @Test
    public void testCreateLink() {
        IProductCmptLink[] links;

        // CmptReference target
        checkDropWithSinglePossibility(structure.getRoot(), 0);

        // AssociationReference target
        IProductCmptTypeAssociationReference[] references = structure
                .getChildProductCmptTypeAssociationReferences(structure.getRoot());
        checkDropWithSinglePossibility(references[2], 0);

        // Link target
        // TODO maybe move to new testclass
        // IProductCmptLink link =
        // ((IProductCmptGeneration)cmptA.getFirstGeneration()).newLink(associationToC);
        // link.setTarget(cmptC3.getQualifiedName());
        // checkDropWithSinglePossibility(link, 1);
        // link.delete();

        // drop single component on CmptReference with multiple possibility to add

        // select first
        linkCreator.setSelection(1 << 0);
        assertTrue(linkCreator.createLinks(getList(cmptB1), structure.getRoot()));
        links = cmptA.getFirstGeneration().getLinks();
        assertEquals(1, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToB1.getName(), links[0].getAssociation());
        links[0].delete();

        // select second
        linkCreator.setSelection(1 << 1);
        assertTrue(linkCreator.createLinks(getList(cmptB1), structure.getRoot()));
        links = cmptA.getFirstGeneration().getLinks();
        assertEquals(1, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(associationToB2.getName(), links[0].getAssociation());
        links[0].delete();

        // select both
        linkCreator.setSelection(1 << 0 | 1 << 1);
        assertTrue(linkCreator.createLinks(getList(cmptB1), structure.getRoot()));
        links = cmptA.getFirstGeneration().getLinks();
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
        links = cmptA.getFirstGeneration().getLinks();
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
        links = cmptA.getFirstGeneration().getLinks();
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
        links = cmptA.getFirstGeneration().getLinks();
        assertEquals(4, links.length);
        assertEquals(cmptB1.getQualifiedName(), links[0].getTarget());
        assertEquals(cmptSubB1.getQualifiedName(), links[1].getTarget());
        assertEquals(cmptB1.getQualifiedName(), links[2].getTarget());
        assertEquals(cmptSubB1.getQualifiedName(), links[3].getTarget());
        assertEquals(associationToB1.getName(), links[0].getAssociation());
        assertEquals(associationToB1.getName(), links[1].getAssociation());
        assertEquals(associationToB2.getName(), links[2].getAssociation());
        assertEquals(associationToB2.getName(), links[3].getAssociation());
        links[0].delete();
        links[1].delete();
        links[2].delete();
        links[3].delete();
    }

    @Test
    public void testCreateLinkWithStaticAssociation() {
        cmptA = spy(cmptA);
        IProductCmptGeneration cmptGeneration = spy(cmptA.getFirstGeneration());
        doReturn(cmptA).when(cmptGeneration).getProductCmpt();
        associationToB1.setChangingOverTime(false);
        linkCreator.createLink(associationToB1, cmptGeneration, cmptB1.getQualifiedName());
        verify(cmptA).newLink("associationToB1");
    }

    @Test
    public void testCreateLinkWithChangingAssociation() {
        IProductCmptGeneration cmptGeneration = spy(cmptA.getFirstGeneration());
        associationToB1.setChangingOverTime(true);
        linkCreator.createLink(associationToB1, cmptGeneration, cmptB1.getQualifiedName());
        verify(cmptGeneration).newLink("associationToB1");
    }

    @Test
    public void testSaveFile() {
        IIpsSrcFile ipsSrcFile = cmptA.getIpsSrcFile();
        assertTrue(ipsSrcFile.isMutable());
        ipsSrcFile.save(false, null);
        assertFalse(ipsSrcFile.isDirty());

        // test with cmpt reference target
        checkSaveFile(ipsSrcFile, structure.getRoot());

        // test with association reference target
        IProductCmptTypeAssociationReference[] references = structure
                .getChildProductCmptTypeAssociationReferences(structure.getRoot());
        checkSaveFile(ipsSrcFile, references[2]);

        // test with link target
        IProductCmptLink link = cmptA.getFirstGeneration().newLink(associationToC);
        link.setTarget(cmptC3.getQualifiedName());
        ipsSrcFile.save(false, null);
        checkSaveFile(ipsSrcFile, structure.getRoot());
    }

    private void checkSaveFile(IIpsSrcFile ipsSrcFile, IProductCmptStructureReference target) {
        linkCreator = new MyLinkCreator(false);
        assertFalse(ipsSrcFile.isDirty());
        assertTrue(linkCreator.createLinks(getList(cmptC1), target));
        assertTrue(ipsSrcFile.isDirty());
        assertTrue(linkCreator.createLinks(getList(cmptC2), target));
        assertTrue(ipsSrcFile.isDirty());
        ipsSrcFile.discardChanges();

        linkCreator = new MyLinkCreator(true);
        assertTrue(linkCreator.createLinks(getList(cmptC1), target));
        assertFalse(ipsSrcFile.isDirty());

        IProductCmptLink[] links = cmptA.getFirstGeneration().getLinks();
        // delete the last one
        links[links.length - 1].delete();
        assertTrue(ipsSrcFile.isDirty());
        assertTrue(linkCreator.createLinks(getList(cmptC1), target));
        assertTrue(ipsSrcFile.isDirty());

        // reset for next test
        links = cmptA.getFirstGeneration().getLinks();
        links[0].delete();
        ipsSrcFile.save(false, null);
    }

    /**
     * check dropping components on the target, target have to be set before calling this method. In
     * case of target is a link there already have to exists at least one link
     * 
     * @param alreadyExistingLinks the number of links that already exists in the product cmpt
     */
    private void checkDropWithSinglePossibility(IProductCmptStructureReference target, int alreadyExistingLinks) {
        // drop single component on CmptReference with no possibility to add
        assertFalse(linkCreator.createLinks(getList(cmptA), target));
        IProductCmptLink[] links = cmptA.getFirstGeneration().getLinks();
        assertEquals(alreadyExistingLinks, links.length);

        // drop single component on CmptReference with one possibility to add
        assertTrue(linkCreator.createLinks(getList(cmptC1), target));
        links = cmptA.getFirstGeneration().getLinks();
        assertEquals(1 + alreadyExistingLinks, links.length);
        assertEquals(cmptC1.getQualifiedName(), links[0 + alreadyExistingLinks].getTarget());
        assertEquals(associationToC.getName(), links[0 + alreadyExistingLinks].getAssociation());
        links[0 + alreadyExistingLinks].delete();

        // drop multiple component on CmptReference with one possibility to add
        assertTrue(linkCreator.createLinks(getList(cmptC1, cmptC2), target));
        links = cmptA.getFirstGeneration().getLinks();
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
        protected Object[] selectAssociation(String droppedCmptName, List<IProductCmptTypeAssociation> possibleAssos) {
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

        public void setSelection(int selection) {
            this.selection = selection;
        }

        public int getSelection() {
            return selection;
        }

    }

}
