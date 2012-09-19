/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.search.ui.NewSearchUI;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.junit.Before;
import org.junit.Test;

public class ReferencesToTableContentsSearchQueryTest extends AbstractIpsPluginTest {

    private IIpsProject proj;
    private IIpsPackageFragmentRoot root;
    private ReferencesToTableContentsSearchQuery query;
    private ITableContents tableContentsReferenced;
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

        tableContentsReferenced = newTableContents(root, "toBeReferenced");
        prodCmpt = newProductCmpt(root, "TestProductComponent");
        IProductCmptGeneration generation = (IProductCmptGeneration)prodCmpt.newGeneration(calendar);
        ITableContentUsage usage = generation.newTableContentUsage();
        usage.setTableContentName(tableContentsReferenced.getQualifiedName());

        prodCmpt2 = newProductCmpt(root, "TestProductComponent2");
        IProductCmptGeneration generation2 = (IProductCmptGeneration)prodCmpt2.newGeneration(calendar);
        ITableContentUsage usage2 = generation2.newTableContentUsage();
        usage2.setTableContentName(tableContentsReferenced.getQualifiedName());

        prodCmpt3 = newProductCmpt(root, "TestProductComponent3");
        IProductCmptGeneration generation3 = (IProductCmptGeneration)prodCmpt3.newGeneration(calendar);
        ITableContentUsage usage3 = generation3.newTableContentUsage();
        usage3.setTableContentName(tableContentsReferenced.getQualifiedName());

        prodCmptNoRef = newProductCmpt(root, "TestProductComponentNoRef");
    }

    @Test
    public void testRun() {
        query = new ReferencesToTableContentsSearchQuery(tableContentsReferenced);
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

    /**
     * Test for Jira Issue FIPS-771
     */
    @Test
    public void testFindHitInReferencingProject() throws CoreException {

        IIpsProject otherProject = newIpsProject("SubTestProjekt");
        IIpsPackageFragmentRoot rootOtherProject = otherProject.getIpsPackageFragmentRoots()[0];

        IIpsProjectProperties properties = otherProject.getProperties();
        IIpsObjectPath ipsObjectPath = properties.getIpsObjectPath();

        IpsProjectRefEntry ipsProjectRefEntry = new IpsProjectRefEntry((IpsObjectPath)ipsObjectPath, proj);

        IIpsObjectPathEntry[] newEntries = new IIpsObjectPathEntry[] { ipsProjectRefEntry,
                rootOtherProject.getIpsObjectPathEntry() };
        ipsObjectPath.setEntries(newEntries);
        otherProject.setProperties(properties);

        IProductCmpt prodCmptInOtherProject = newProductCmpt(otherProject, "TestProductComponentInReferencingProject");
        IProductCmptGeneration generationInOtherProject = (IProductCmptGeneration)prodCmptInOtherProject
                .newGeneration((GregorianCalendar)Calendar.getInstance());
        ITableContentUsage usageInterProject = generationInOtherProject.newTableContentUsage();
        usageInterProject.setTableContentName(tableContentsReferenced.getQualifiedName());

        query = new ReferencesToTableContentsSearchQuery(tableContentsReferenced);
        // run query in same thread as this test
        NewSearchUI.runQueryInForeground(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow(), query);

        ReferenceSearchResult searchResult = (ReferenceSearchResult)query.getSearchResult();
        Object[] results = searchResult.getMatchingElements();

        List<Object> result = Arrays.asList(results);

        assertEquals(4, result.size());

        List<Object> resultProductCmpts = new ArrayList<Object>();

        for (Object object : result) {
            resultProductCmpts.add(((Object[])object)[0]);
        }
        assertTrue(resultProductCmpts.contains(prodCmptInOtherProject));

    }
}
