/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * @author Jan Ortmann
 */
public class ArchiveIpsPackageFragmentTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IFile archiveFile;
    private ArchiveIpsPackageFragmentRoot root;
    private ArchiveIpsPackageFragment pack;
    private IPolicyCmptType policy;
    private IPolicyCmptType coverage;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        policy = newPolicyCmptTypeWithoutProductCmptType(archiveProject, "mycompany.motor.Policy");
        coverage = newPolicyCmptTypeWithoutProductCmptType(archiveProject, "mycompany.motor.Coverage");
        newPolicyCmptTypeWithoutProductCmptType(archiveProject, "mycompany.motor.collision.CollisionCoverage");

        project = newIpsProject();
        archiveFile = project.getProject().getFile("test.ipsar");

        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        path.newArchiveEntry(archiveFile.getProjectRelativePath());
        project.setIpsObjectPath(path);
        root = (ArchiveIpsPackageFragmentRoot)project.getIpsPackageFragmentRoots()[1];
        pack = (ArchiveIpsPackageFragment)root.getIpsPackageFragment("mycompany.motor");
    }

    public void testGetChildren() throws CoreException {
        IIpsElement[] children = pack.getChildren();
        assertEquals(2, children.length);
        assertTrue((children[0] instanceof IIpsSrcFile));
        assertTrue((children[1] instanceof IIpsSrcFile));
    }

    public void testGetIpsSrcFile() {
        IIpsSrcFile file = pack.getIpsSrcFile(policy.getIpsSrcFile().getName());
        assertNotNull(file);
        assertEquals(pack, file.getParent());
    }

    public void testFindIpsObjectsByIpsObjectType() throws CoreException {
        List<IIpsObject> result = new ArrayList<IIpsObject>();
        pack.findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE, result);
        assertEquals(2, result.size());
        IIpsObject obj = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "mycompany.motor.Policy");
        assertTrue(result.contains(obj));
        obj = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "mycompany.motor.Coverage");
        assertTrue(result.contains(obj));
    }

    public void testFindIpsObjects() throws CoreException {
        List<IIpsObject> result = new ArrayList<IIpsObject>();
        pack.findIpsObjects(result);
        assertEquals(2, result.size());
        List<QualifiedNameType> qnts = new ArrayList<QualifiedNameType>();
        for (IIpsObject ipsObject : result) {
            qnts.add(ipsObject.getQualifiedNameType());
        }

        assertTrue(qnts.contains(policy.getQualifiedNameType()));
        assertTrue(qnts.contains(coverage.getQualifiedNameType()));
    }

    public void testGetChildIpsPackageFragments() throws CoreException {
        IIpsPackageFragment[] packs = pack.getChildIpsPackageFragments();
        assertEquals(1, packs.length);
        assertEquals("mycompany.motor.collision", packs[0].getName());
    }

    public void testGetNonIpsResources() throws CoreException {
        assertEquals(0, pack.getNonIpsResources().length);
    }

    public void testGetCorrespondingResource() {
        assertNull(pack.getCorrespondingResource());
    }

    public void testGetEnclosingResource() {
        assertEquals(archiveFile, pack.getEnclosingResource());
    }

    public void testGetParent() {
        assertEquals(root, pack.getParent());
    }

    public void testExists() {
        assertTrue(pack.exists());
        assertFalse(root.getIpsPackageFragment("unknownPack").exists());
    }

    public void testGetSortedChildIpsPackageFragmentsBasics() throws Exception {

        IIpsProject project = createTestArchive();

        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot)project.getIpsPackageFragmentRoots()[1];
        ArchiveIpsPackageFragment pack = (ArchiveIpsPackageFragment)root.getIpsPackageFragment("products");

        IIpsPackageFragment[] children = pack.getSortedChildIpsPackageFragments();

        assertEquals(4, children.length);
        // assertEquals("products.hausrat", children[0].getName());
        // assertEquals("products.haftpflicht", children[1].getName());
        // assertEquals("products.kranken", children[2].getName());
        // assertEquals("products.unfall", children[3].getName());

        pack = (ArchiveIpsPackageFragment)root.getIpsPackageFragment("products.kranken.leistungsarten");
        children = pack.getSortedChildIpsPackageFragments();
        assertEquals(2, children.length);
        // assertEquals("products.kranken.leistungsarten.fix", children[0].getName());
        // assertEquals("products.kranken.leistungsarten.optional", children[1].getName());

    }

    public void testSetSortDefinition() throws Exception {

        IpsPackageFragmentArbitrarySortDefinition sortDef = new IpsPackageFragmentArbitrarySortDefinition();
        try {
            this.pack.setSortDefinition(sortDef);
            fail();
        } catch (Exception e) {
        }
    }

    private IIpsProject createTestArchive() throws Exception {
        IIpsProject archiveProject = newIpsProject("ArchiveProject2");
        newPolicyCmptType(archiveProject, "products.hausrat.file1");
        newPolicyCmptType(archiveProject, "products.kranken.file1");
        newPolicyCmptType(archiveProject, "products.kranken.leistungsarten.fix.file1");
        newPolicyCmptType(archiveProject, "products.kranken.leistungsarten.optional.file1");
        newPolicyCmptType(archiveProject, "products.kranken.leistungsarten.file1");
        newPolicyCmptType(archiveProject, "products.kranken.vertragsarten.file1");
        newPolicyCmptType(archiveProject, "products.kranken.gruppenarten.file1");
        newPolicyCmptType(archiveProject, "products.unfall.file1");
        newPolicyCmptType(archiveProject, "products.haftpflicht.file1");

        IIpsPackageFragmentRoot rootPackage = archiveProject.findIpsPackageFragmentRoot("productdef");
        IIpsPackageFragment products = rootPackage.getIpsPackageFragment("products");
        IIpsPackageFragment service = rootPackage.getIpsPackageFragment("products.kranken.leistungsarten");

        List<String> list = new ArrayList<String>();
        list.add("products");

        createPackageOrderFile((IFolder)rootPackage.getCorrespondingResource(), list);
        list.clear();

        list.add("unfall");
        list.add("kranken");
        list.add("folder");
        list.add("haftpflicht");
        list.add("hausrat");

        createPackageOrderFile((IFolder)products.getCorrespondingResource(), list);
        list.clear();

        list.add("optional");
        list.add("fix");

        createPackageOrderFile((IFolder)service.getCorrespondingResource(), list);
        list.clear();

        IIpsProject project = newIpsProject("TestProjekt2");
        archiveFile = project.getProject().getFile("test2.ipsar");

        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        path.newArchiveEntry(archiveFile.getLocation());
        project.setIpsObjectPath(path);

        return project;
    }

    public void testHasChildIpsPackageFragments() throws CoreException {
        IIpsPackageFragment empty = root.getIpsPackageFragment("mycompany.motor.Coverage");
        assertFalse(empty.hasChildIpsPackageFragments());

        assertTrue(pack.hasChildIpsPackageFragments());
        assertTrue(root.getDefaultIpsPackageFragment().hasChildIpsPackageFragments());
    }
}
