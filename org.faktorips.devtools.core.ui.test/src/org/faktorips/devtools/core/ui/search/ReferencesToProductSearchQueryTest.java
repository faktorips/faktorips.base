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

package org.faktorips.devtools.core.ui.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

import org.eclipse.search.ui.NewSearchUI;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.junit.Before;
import org.junit.Test;

public class ReferencesToProductSearchQueryTest extends AbstractIpsPluginTest {

    private IIpsProject proj;
    private IIpsPackageFragmentRoot root;
    private ReferencesToProductSearchQuery query;
    private IProductCmpt prodCmptReferenced;
    private IProductCmpt prodCmpt;
    private IProductCmpt prodCmpt2;
    private IProductCmpt prodCmpt3;
    private IProductCmpt prodCmptNoRef;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        GregorianCalendar calendar = (GregorianCalendar)Calendar.getInstance();
        proj = newIpsProject("TestProjekt");
        root = proj.getIpsPackageFragmentRoots()[0];

        prodCmptReferenced = newProductCmpt(root, "toBeReferenced");
        prodCmpt = newProductCmpt(root, "TestProductComponent");
        IProductCmptGeneration generation = (IProductCmptGeneration)prodCmpt.newGeneration(calendar);
        IProductCmptLink relation = generation.newLink("");
        relation.setTarget(prodCmptReferenced.getQualifiedName());

        prodCmpt2 = newProductCmpt(root, "TestProductComponent2");
        IProductCmptGeneration generation2 = (IProductCmptGeneration)prodCmpt2.newGeneration(calendar);
        IProductCmptLink relation2 = generation2.newLink("");
        relation2.setTarget(prodCmptReferenced.getQualifiedName());

        prodCmpt3 = newProductCmpt(root, "TestProductComponent3");
        IProductCmptGeneration generation3 = (IProductCmptGeneration)prodCmpt3.newGeneration(calendar);
        IProductCmptLink relation3 = generation3.newLink("");
        relation3.setTarget(prodCmptReferenced.getQualifiedName());

        prodCmptNoRef = newProductCmpt(root, "TestProductComponentNoRef");

        calendar = (GregorianCalendar)Calendar.getInstance();
        calendar.add(Calendar.DATE, 2);
        IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(calendar);
    }

    @Test
    public void testRun() {
        query = new ReferencesToProductSearchQuery(prodCmptReferenced);
        // run query in same thread as this test
        NewSearchUI.runQueryInForeground(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow(), query);

        ReferenceSearchResult searchResult = (ReferenceSearchResult)query.getSearchResult();
        Object[] results = searchResult.getMatchingElements();
        assertEquals(3, results.length);

        Object[] res1 = (Object[])results[0];
        Object[] res2 = (Object[])results[1];
        Object[] res3 = (Object[])results[2];
        assertEquals(2, res1.length);
        assertEquals(2, res2.length);
        assertEquals(2, res3.length);

        HashSet<Object> resultSet = new HashSet<Object>();
        resultSet.add(res1[0]);
        resultSet.add(res2[0]);
        resultSet.add(res3[0]);

        HashSet<Object> expectedSet = new HashSet<Object>();
        expectedSet.add(prodCmpt);
        expectedSet.add(prodCmpt2);
        expectedSet.add(prodCmpt3);
        assertEquals(expectedSet, resultSet);
        assertFalse(resultSet.contains(prodCmptNoRef));
    }

}
