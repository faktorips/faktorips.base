/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class ProductCmptCompareItemTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new ProductCmptCompareItemCreator();
    private IProductCmptGeneration generation1;
    private IProductCmptGeneration generation2;
    private IProductCmptGeneration generation3;
    private IIpsSrcFile srcFile;
    private IFile correspondingFile;

    private ProductCmptCompareItem compareItemRoot;
    private IProductCmpt product;
    private IIpsPackageFragmentRoot root;
    private IConfigElement configElement1;
    private IConfigElement configElement2;
    private IAttributeValue configElement3;
    private IProductCmptLink relation1;
    private IProductCmptLink relation2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject proj = newIpsProject("TestProject");
        root = proj.getIpsPackageFragmentRoots()[0];
        product = newProductCmpt(root, "TestProductCmpt");
        IProductCmpt productReferenced = newProductCmpt(root, "TestProductCmptReferenced");

        GregorianCalendar calendar = new GregorianCalendar();
        generation1 = (IProductCmptGeneration)product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        generation2 = (IProductCmptGeneration)product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 2);
        generation3 = (IProductCmptGeneration)product.newGeneration(calendar);

        configElement1 = generation1.newConfigElement();
        configElement1.setPolicyCmptTypeAttribute("configElement1"); // set name to ensure sorting
        // order
        configElement2 = generation1.newConfigElement();
        configElement2.setPolicyCmptTypeAttribute("configElement2");
        configElement2.setValueSetType(ValueSetType.ENUM);
        configElement3 = generation1.newAttributeValue();
        configElement3.setAttribute("configElement3");
        configElement3.setValue("TestWert");
        relation1 = generation1.newLink(productReferenced.getQualifiedName());
        relation2 = generation1.newLink(productReferenced.getQualifiedName());

        srcFile = product.getIpsSrcFile();
        correspondingFile = srcFile.getCorrespondingFile();

        compareItemRoot = (ProductCmptCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    public void testGetChildren() {
        Object[] children = compareItemRoot.getChildren();
        // Srcfile contains ProductComponent
        assertEquals(1, children.length);

        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        children = compareItem.getChildren();
        // productcomponent contains 3 generations
        assertEquals(3, children.length);

        ProductCmptCompareItem compareItemGen = (ProductCmptCompareItem)children[0];
        children = compareItemGen.getChildren();
        // Generation has 3 ConfigElements and 2 Relations
        assertEquals(5, children.length);
    }

    public void testGetImage() {
        assertEquals(IpsUIPlugin.getImageHandling().getImage(srcFile), compareItemRoot.getImage());

        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        assertEquals(IpsUIPlugin.getImageHandling().getImage(product), compareItem.getImage());

        children = compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[0];
        assertEquals(IpsUIPlugin.getImageHandling().getImage(generation1), compareItemGen1.getImage());
        ProductCmptCompareItem compareItemGen2 = (ProductCmptCompareItem)children[1];
        assertEquals(IpsUIPlugin.getImageHandling().getImage(generation2), compareItemGen2.getImage());
        ProductCmptCompareItem compareItemGen3 = (ProductCmptCompareItem)children[2];
        assertEquals(IpsUIPlugin.getImageHandling().getImage(generation3), compareItemGen3.getImage());
    }

    public void testGetType() {
        assertEquals("ipsproduct", compareItemRoot.getType());
    }

    public void testGetParent() {
        assertNull(compareItemRoot.getParent());

        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        assertNotNull(compareItem.getParent());
        assertEquals(compareItemRoot, compareItem.getParent());

        children = compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemGen2 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemGen3 = (ProductCmptCompareItem)children[2];

        assertNotNull(compareItemGen1.getParent());
        assertEquals(compareItem, compareItemGen1.getParent());
        assertNotNull(compareItemGen2.getParent());
        assertEquals(compareItem, compareItemGen2.getParent());
        assertNotNull(compareItemGen3.getParent());
        assertEquals(compareItem, compareItemGen3.getParent());

        // CompareItemComparator sorts Configelements above Relations
        children = compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemConfigElement1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemConfigElement2 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemConfigElement3 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemRelation1 = (ProductCmptCompareItem)children[3];
        ProductCmptCompareItem compareItemRelation2 = (ProductCmptCompareItem)children[4];

        assertNotNull(compareItemConfigElement1.getParent());
        assertEquals(compareItemGen1, compareItemConfigElement1.getParent());
        assertNotNull(compareItemConfigElement2.getParent());
        assertEquals(compareItemGen1, compareItemConfigElement2.getParent());
        assertNotNull(compareItemConfigElement3.getParent());
        assertEquals(compareItemGen1, compareItemConfigElement3.getParent());
        assertNotNull(compareItemRelation1.getParent());
        assertEquals(compareItemGen1, compareItemRelation1.getParent());
        assertNotNull(compareItemRelation2.getParent());
        assertEquals(compareItemGen1, compareItemRelation2.getParent());
    }

    public void testGetIpsElement() {
        assertEquals(srcFile, compareItemRoot.getIpsElement());

        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        assertEquals(product, compareItem.getIpsElement());

        children = compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemGen2 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemGen3 = (ProductCmptCompareItem)children[2];

        assertEquals(generation1, compareItemGen1.getIpsElement());
        assertEquals(generation2, compareItemGen2.getIpsElement());
        assertEquals(generation3, compareItemGen3.getIpsElement());

        children = compareItemGen1.getChildren();
        /*
         * Die Kinder jedes CompareItems werden sortiert. Dabei werden ProduktAttribute
         * (compareItem3) vorVertragsAttribute (compareItem1 und -2) gestellt.
         */
        ProductCmptCompareItem compareItemConfigElement3 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemConfigElement1 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemConfigElement2 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemRelation1 = (ProductCmptCompareItem)children[3];
        ProductCmptCompareItem compareItemRelation2 = (ProductCmptCompareItem)children[4];

        assertEquals(configElement1, compareItemConfigElement1.getIpsElement());
        assertEquals(configElement2, compareItemConfigElement2.getIpsElement());
        assertEquals(configElement3, compareItemConfigElement3.getIpsElement());
        assertEquals(relation1, compareItemRelation1.getIpsElement());
        assertEquals(relation2, compareItemRelation2.getIpsElement());
    }

    public void testHashCode() {
        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        children = compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[0];
        children = compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemConfigElement1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemRelation1 = (ProductCmptCompareItem)children[2];

        assertEquals(compareItemRoot.getContentStringWithoutWhiteSpace().hashCode(), compareItemRoot.hashCode());
        assertEquals(compareItemGen1.getContentStringWithoutWhiteSpace().hashCode(), compareItemGen1.hashCode());
        assertEquals(compareItemConfigElement1.getContentStringWithoutWhiteSpace().hashCode(),
                compareItemConfigElement1.hashCode());
        assertEquals(compareItemRelation1.getContentStringWithoutWhiteSpace().hashCode(), compareItemRelation1
                .hashCode());
    }
}
