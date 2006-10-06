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

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

public class ProductCmptCompareItemComparatorTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new ProductCmptCompareItemCreator();
    
    private IProductCmptGeneration generation1;
    
    private ProductCmptCompareItem compareItemRoot;
    
    private IConfigElement configElement1;
    private IConfigElement configElement2;
    private IProductCmptRelation relation1;
    private IProductCmptRelation relation2;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject proj= (IpsProject)newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = proj.getIpsPackageFragmentRoots()[0];
        IProductCmpt product = newProductCmpt(root, "TestProductCmpt");
        IProductCmpt productReferenced = newProductCmpt(root, "TestProductCmptReferenced");
        IProductCmpt productReferenced2 = newProductCmpt(root, "TestProductCmptReferenced2");
        
        GregorianCalendar calendar= new GregorianCalendar();
        generation1 = (IProductCmptGeneration) product.newGeneration(calendar);
        calendar= new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        product.newGeneration(calendar);
        calendar= new GregorianCalendar();
        calendar.add(Calendar.MONTH, 2);
        product.newGeneration(calendar);

        configElement1 = generation1.newConfigElement();
        configElement1.setPcTypeAttribute("configElement1");    // set name to ensure sorting order
        configElement2 = generation1.newConfigElement();
        configElement2.setPcTypeAttribute("configElement2");
        relation1 = generation1.newRelation(productReferenced.getQualifiedName());
        relation2 = generation1.newRelation(productReferenced2.getQualifiedName());
        
        IFile correspondingFile = product.getIpsSrcFile().getCorrespondingFile();

        compareItemRoot = (ProductCmptCompareItem) structureCreator.getStructure(new ResourceNode(correspondingFile));
    }
    
    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.ProductCmptCompareItemComparator.compare(Object, Object)'
     */
    public void testCompare() {
        ProductCmptCompareItemComparator comparator= new ProductCmptCompareItemComparator();

        Object[] children= compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem= (ProductCmptCompareItem) children[0];
        
        // generations
        children= compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemGen2= (ProductCmptCompareItem) children[1];
        ProductCmptCompareItem compareItemGen3= (ProductCmptCompareItem) children[2];
        
        // sort generations by number (gen1.getGenerationNumber()-gen2.getGenerationNumber())
        assertEquals(-1, comparator.compare(compareItemGen1, compareItemGen2));
        assertEquals(-1, comparator.compare(compareItemGen2, compareItemGen3));
        assertEquals(-2, comparator.compare(compareItemGen1, compareItemGen3));
        assertEquals(2, comparator.compare(compareItemGen3, compareItemGen1));
        assertEquals(0, comparator.compare(compareItemGen1, compareItemGen1));

        // relations and configElements
        children= compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemConfigElement1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemConfigElement2= (ProductCmptCompareItem) children[1];
        ProductCmptCompareItem compareItemRelation1= (ProductCmptCompareItem) children[2];
        ProductCmptCompareItem compareItemRelation2= (ProductCmptCompareItem) children[3];
        
        // sort configElements lexicographically
        assertEquals(configElement1.getPcTypeAttribute().compareTo(configElement2.getPcTypeAttribute()),
                comparator.compare(compareItemConfigElement1, compareItemConfigElement2));
        assertEquals(configElement2.getPcTypeAttribute().compareTo(configElement1.getPcTypeAttribute()),
                comparator.compare(compareItemConfigElement2, compareItemConfigElement1));
        
        // sort relations lexicographically
        assertEquals(relation1.getName().compareTo(relation2.getName()),
                comparator.compare(compareItemRelation1, compareItemRelation2));
        assertEquals(relation2.getName().compareTo(relation1.getName()),
                comparator.compare(compareItemRelation2, compareItemRelation1));
        
    }

}
