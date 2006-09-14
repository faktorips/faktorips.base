/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

public class ProductCmptCompareItemCreatorTest extends AbstractIpsPluginTest {

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
    private IProductCmptRelation relation1;
    private IProductCmptRelation relation2;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject proj= (IpsProject)newIpsProject("TestProject");
        root = proj.getIpsPackageFragmentRoots()[0];
        product = newProductCmpt(root, "TestProductCmpt");
        IProductCmpt productReferenced = newProductCmpt(root, "TestProductCmptReferenced");
        
        GregorianCalendar calendar= new GregorianCalendar();
        generation1 = (IProductCmptGeneration) product.newGeneration(calendar);
        calendar= new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        generation2 = (IProductCmptGeneration) product.newGeneration(calendar);
        calendar= new GregorianCalendar();
        calendar.add(Calendar.MONTH, 2);
        generation3 = (IProductCmptGeneration) product.newGeneration(calendar);

        configElement1 = generation1.newConfigElement();
        configElement1.setPcTypeAttribute("configElement1");    // set name to ensure sorting order
        configElement2 = generation1.newConfigElement();
        configElement2.setPcTypeAttribute("configElement2");
        relation1 = generation1.newRelation(productReferenced.getQualifiedName());
        relation2 = generation1.newRelation(productReferenced.getQualifiedName());
        
        srcFile = product.getIpsSrcFile();
        correspondingFile = srcFile.getCorrespondingFile();

        compareItemRoot = (ProductCmptCompareItem) structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.ProductCmptCompareItemCreator.getStructure(Object)'
     */
    public void testGetStructure() {
        assertEquals(srcFile, compareItemRoot.getIpsElement());
        
        Object[] children= compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem= (ProductCmptCompareItem) children[0];
        assertEquals(product, compareItem.getIpsElement());
        
        children= compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemGen2= (ProductCmptCompareItem) children[1];
        ProductCmptCompareItem compareItemGen3= (ProductCmptCompareItem) children[2];
        
        assertEquals(generation1, compareItemGen1.getIpsElement());
        assertEquals(generation2, compareItemGen2.getIpsElement());
        assertEquals(generation3, compareItemGen3.getIpsElement());
        
        children= compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemConfigElement1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemConfigElement2= (ProductCmptCompareItem) children[1];
        ProductCmptCompareItem compareItemRelation1= (ProductCmptCompareItem) children[2];
        ProductCmptCompareItem compareItemRelation2= (ProductCmptCompareItem) children[3];

        assertEquals(configElement1, compareItemConfigElement1.getIpsElement());
        assertEquals(configElement2, compareItemConfigElement2.getIpsElement());
        assertEquals(relation1, compareItemRelation1.getIpsElement());
        assertEquals(relation2, compareItemRelation2.getIpsElement());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.ProductCmptCompareItemCreator.getContents(Object, boolean)'
     */
    public void testGetContents() {
        Object[] children= compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem= (ProductCmptCompareItem) children[0];

        String contentString= structureCreator.getContents(compareItemRoot, false);
        assertEquals(compareItemRoot.getContentString(), contentString);
        contentString= structureCreator.getContents(compareItem, false);
        assertEquals(compareItem.getContentString(), contentString);
        
        contentString= structureCreator.getContents(compareItemRoot, true);
        assertTrue(compareItemRoot.getContentString().equals(contentString));
        contentString= structureCreator.getContents(compareItem, true);
        assertTrue(compareItem.getContentString().equals(contentString));
    }

}
