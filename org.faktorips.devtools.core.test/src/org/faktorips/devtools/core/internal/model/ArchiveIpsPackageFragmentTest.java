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

package org.faktorips.devtools.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
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
    private IPolicyCmptType type;

    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        type = newPolicyCmptType(archiveProject, "mycompany.motor.Policy");
        newPolicyCmptType(archiveProject, "mycompany.motor.Coverage");
        newPolicyCmptType(archiveProject, "mycompany.motor.collision.CollisionCoverage");

        project = newIpsProject();
        archiveFile = project.getProject().getFile("test.ipsar");

        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        path.newArchiveEntry(archiveFile);
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
        IIpsSrcFile file = pack.getIpsSrcFile(type.getIpsSrcFile().getName());
        assertNotNull(file);
        assertEquals(pack, file.getParent());
    }

    public void testFindIpsObjects() throws CoreException {
        List result = new ArrayList();
        pack.findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE, result);
        assertEquals(2, result.size());
        IIpsObject obj = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "mycompany.motor.Policy");
        assertTrue(result.contains(obj));
        obj = project.findIpsObject(IpsObjectType.POLICY_CMPT_TYPE, "mycompany.motor.Coverage");
        assertTrue(result.contains(obj));
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

    public void testGetSortedChildIpsPackageFragmentsBasics() throws Exception  {

        IIpsProject project = createTestArchive();

        ArchiveIpsPackageFragmentRoot root = (ArchiveIpsPackageFragmentRoot) project.getIpsPackageFragmentRoots()[1];
        ArchiveIpsPackageFragment pack = (ArchiveIpsPackageFragment)root.getIpsPackageFragment("products");

        IIpsPackageFragment[] children = pack.getSortedChildIpsPackageFragments();

        assertEquals(4, children.length);
        assertEquals("products.hausrat", children[0].getName());
        assertEquals("products.haftpflicht", children[1].getName());
        assertEquals("products.kranken", children[2].getName());
        assertEquals("products.unfall", children[3].getName());

        pack = (ArchiveIpsPackageFragment)root.getIpsPackageFragment("products.kranken.leistungsarten");
        children = pack.getSortedChildIpsPackageFragments();
        assertEquals(2, children.length);
        assertEquals("products.kranken.leistungsarten.fix", children[0].getName());
        assertEquals("products.kranken.leistungsarten.optional", children[1].getName());

    }

    public void testSetSortDefinition() throws Exception {

        IpsPackageFragmentArbitrarySortDefinition sortDef = new IpsPackageFragmentArbitrarySortDefinition();
        boolean ok = false;

        try {
            this.pack.setSortDefinition(sortDef);
        } catch (CoreException e) {
            ok = true;
        }

        assertTrue(ok);

    }

    /**
     * @throws Exception
     *
     */
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

        List list = new ArrayList();
        list.add("products");

        createPackageOrderFile((IFolder) rootPackage.getCorrespondingResource(), list);
        list.clear();

        list.add("unfall");
        list.add("kranken");
        list.add("folder");
        list.add("haftpflicht");
        list.add("hausrat");

        createPackageOrderFile((IFolder) products.getCorrespondingResource(), list);
        list.clear();

        list.add("optional");
        list.add("fix");

        createPackageOrderFile((IFolder) service.getCorrespondingResource(), list);
        list.clear();


        IIpsProject project = newIpsProject("TestProjekt2");
        archiveFile = project.getProject().getFile("test2.ipsar");

        createArchive(archiveProject, archiveFile);

        IIpsObjectPath path = project.getIpsObjectPath();
        path.newArchiveEntry(archiveFile);
        project.setIpsObjectPath(path);

        return project;
    }
}
