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
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.search.ui.NewSearchUI;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;

public class ReferencesToPolicySearchQueryTest extends AbstractIpsPluginTest {

    private IIpsProject proj;
    private IIpsPackageFragmentRoot root;
    private PolicyCmptType pcTypeReferenced;
    private PolicyCmptType pcType;
    private IPolicyCmptType pcType2;
    private IPolicyCmptType pcType3;
    private IPolicyCmptType pcTypeNoRef;
    private ReferencesToPolicySearchQuery query;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        proj = newIpsProject("TestProjekt");
        root = proj.getIpsPackageFragmentRoots()[0];
        pcTypeReferenced = newPolicyCmptType(root, "TestPCTypeReferenced");

        pcType = newPolicyCmptType(root, "TestPCType");
        pcType.newPolicyCmptTypeAttribute();
        IPolicyCmptTypeAssociation relation = pcType.newPolicyCmptTypeAssociation();
        relation.setTarget(pcTypeReferenced.getQualifiedName());

        pcType2 = newPolicyCmptType(root, "TestPCType2");
        pcType2.newPolicyCmptTypeAttribute();
        pcType2.newPolicyCmptTypeAttribute();
        IPolicyCmptTypeAssociation relation2 = pcType2.newPolicyCmptTypeAssociation();
        relation2.setTarget(pcTypeReferenced.getQualifiedName());

        pcType3 = newPolicyCmptType(root, "TestPCType3");
        pcType3.newPolicyCmptTypeAttribute();
        pcType3.newPolicyCmptTypeAttribute();
        pcType3.newPolicyCmptTypeAttribute();
        IPolicyCmptTypeAssociation relation3 = pcType3.newPolicyCmptTypeAssociation();
        relation3.setTarget(pcTypeReferenced.getQualifiedName());

        pcTypeNoRef = newPolicyCmptType(root, "TestPCTypeNoRef");
    }

    @Test
    public void testRun() {
        query = new ReferencesToPolicySearchQuery(pcTypeReferenced);
        // run query in same thread as this test
        NewSearchUI.runQueryInForeground(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow(), query);

        ReferenceSearchResult searchResult = (ReferenceSearchResult)query.getSearchResult();
        Object[] results = searchResult.getMatchingElements();
        assertEquals(3, results.length);

        Object[] res1 = (Object[])results[0];
        Object[] res2 = (Object[])results[1];
        Object[] res3 = (Object[])results[2];
        assertEquals(1, res1.length);
        assertEquals(1, res2.length);
        assertEquals(1, res3.length);

        HashSet<Object> resultSet = new HashSet<Object>();
        resultSet.add(res1[0]);
        resultSet.add(res2[0]);
        resultSet.add(res3[0]);

        HashSet<Object> expectedSet = new HashSet<Object>();
        expectedSet.add(pcType);
        expectedSet.add(pcType2);
        expectedSet.add(pcType3);
        assertEquals(expectedSet, resultSet);
        assertFalse(resultSet.contains(pcTypeNoRef));
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

        IPolicyCmptType pcTypeInOtherProject = newPolicyCmptType(otherProject, "TestTypeInOtherProject");
        IPolicyCmptTypeAssociation associationInterProject = pcTypeInOtherProject.newPolicyCmptTypeAssociation();
        associationInterProject.setTarget(pcTypeReferenced.getQualifiedName());

        query = new ReferencesToPolicySearchQuery(pcTypeReferenced);
        // run query in same thread as this test
        NewSearchUI.runQueryInForeground(IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow(), query);

        ReferenceSearchResult searchResult = (ReferenceSearchResult)query.getSearchResult();
        Object[] results = searchResult.getMatchingElements();

        List<Object> result = Arrays.asList(results);

        assertEquals(4, result.size());

        List<Object> resultPolicyCmptTypes = new ArrayList<Object>();

        for (Object object : result) {
            resultPolicyCmptTypes.add(((Object[])object)[0]);
        }
        assertTrue(resultPolicyCmptTypes.contains(pcTypeInOtherProject));
    }

}
