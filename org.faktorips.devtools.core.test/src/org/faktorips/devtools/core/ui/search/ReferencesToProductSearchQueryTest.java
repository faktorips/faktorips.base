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

package org.faktorips.devtools.core.ui.search;

import java.util.GregorianCalendar;
import java.util.HashSet;

import org.eclipse.search.ui.NewSearchUI;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;

public class ReferencesToProductSearchQueryTest extends AbstractIpsPluginTest {

    private IIpsProject proj;
    private IIpsPackageFragmentRoot root;
    private ReferencesToProductSearchQuery query;
    private IProductCmpt prodCmptReferenced;
    private IProductCmpt prodCmpt;
    private IProductCmpt prodCmpt2;
    private IProductCmpt prodCmpt3;
    private IProductCmpt prodCmptNoRef;
    
    protected void setUp() throws Exception {
        super.setUp();
        GregorianCalendar calendar= (GregorianCalendar)GregorianCalendar.getInstance();
        proj = newIpsProject("TestProjekt");
        root = (IpsPackageFragmentRoot) proj.getIpsPackageFragmentRoots()[0];

        prodCmptReferenced = newProductCmpt(root, "toBeReferenced");
        prodCmpt = newProductCmpt(root, "TestProductComponent");
        IProductCmptGeneration generation= (IProductCmptGeneration) prodCmpt.newGeneration(calendar);
        IProductCmptRelation relation= generation.newRelation("");
        relation.setTarget(prodCmptReferenced.getQualifiedName());

        prodCmpt2 = newProductCmpt(root, "TestProductComponent2");
        IProductCmptGeneration generation2= (IProductCmptGeneration) prodCmpt2.newGeneration(calendar);
        IProductCmptRelation relation2= generation2.newRelation("");
        relation2.setTarget(prodCmptReferenced.getQualifiedName());

        prodCmpt3 = newProductCmpt(root, "TestProductComponent3");
        IProductCmptGeneration generation3= (IProductCmptGeneration) prodCmpt3.newGeneration(calendar);
        IProductCmptRelation relation3= generation3.newRelation("");
        relation3.setTarget(prodCmptReferenced.getQualifiedName());

        prodCmptNoRef = newProductCmpt(root, "TestProductComponentNoRef");
        
        calendar= (GregorianCalendar)GregorianCalendar.getInstance();
        calendar.add(GregorianCalendar.DATE, 2);
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(calendar);
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.search.ReferenceSearchQuery.run(IProgressMonitor)'
     */
    public void testRun() {
        query= new ReferencesToProductSearchQuery(prodCmptReferenced);
        // run query in same thread as this test
        NewSearchUI.runQueryInForeground(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow(), query);

        ReferenceSearchResult searchResult= (ReferenceSearchResult) query.getSearchResult();
        Object[] results= searchResult.getElements(); 
        assertEquals(3, results.length);

        Object[] res1= (Object[])results[0];
        Object[] res2= (Object[])results[1];
        Object[] res3= (Object[])results[2];
        assertEquals(2, res1.length);
        assertEquals(2, res2.length);
        assertEquals(2, res3.length);
        
        HashSet resultSet= new HashSet();
        resultSet.add(res1[0]);
        resultSet.add(res2[0]);
        resultSet.add(res3[0]);
        
        HashSet expectedSet= new HashSet();
        expectedSet.add(prodCmpt);
        expectedSet.add(prodCmpt2);
        expectedSet.add(prodCmpt3);
        assertEquals(expectedSet, resultSet);
        assertFalse(resultSet.contains(prodCmptNoRef));
    }
    
}
