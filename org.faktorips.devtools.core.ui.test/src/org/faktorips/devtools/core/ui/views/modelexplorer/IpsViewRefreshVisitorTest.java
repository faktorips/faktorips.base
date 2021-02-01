/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.junit.Before;
import org.junit.Test;

public class IpsViewRefreshVisitorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot packRoot;
    private IResourceChangeEvent event;
    private IResourceChangeListener resourceChangeListener;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        resourceChangeListener = new IResourceChangeListener() {

            @Override
            public void resourceChanged(IResourceChangeEvent event) {
                IpsViewRefreshVisitorTest.this.event = event;
            }
        };
        ipsProject = newIpsProject();
        packRoot = ipsProject.getIpsPackageFragmentRoots()[0];
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
    }

    @Override
    protected void tearDownExtension() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
    }

    @Test
    public void test_FlatLayout() throws Exception {
        // test case 1: new package "model.base" and new PolicyCmptType
        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "model.base.Policy");
        IIpsPackageFragment basePack = policyType.getIpsPackageFragment();
        IIpsPackageFragment modelPack = basePack.getParentIpsPackageFragment();
        IpsViewRefreshVisitor visitor = newVisitor(LayoutStyle.FLAT);
        Set<Object> elementsToRefresh = visitor.getElementsToRefresh();
        Set<Object> elementsToUpdate = visitor.getElementsToUpdate();

        assertFalse(elementsToRefresh.contains(ipsProject.getIpsModel()));
        assertTrue(elementsToUpdate.contains(ipsProject.getIpsModel()));

        // project must be updated, as for example team label decoration might have changed
        assertFalse(elementsToRefresh.contains(ipsProject));
        assertTrue(elementsToUpdate.contains(ipsProject));

        // root must be refreshed as a direct child (pack model) was added.
        assertTrue(elementsToRefresh.contains(packRoot));
        assertFalse(elementsToUpdate.contains(packRoot));

        // all other children needn't be refreshed or updated as the root is refreshed!
        assertFalse(elementsToRefresh.contains(modelPack));
        assertFalse(elementsToUpdate.contains(modelPack));
        assertFalse(elementsToRefresh.contains(basePack));
        assertFalse(elementsToUpdate.contains(basePack));
        assertFalse(elementsToRefresh.contains(policyType.getIpsSrcFile()));
        assertFalse(elementsToUpdate.contains(policyType.getIpsSrcFile()));

        // test case 2: new PolicyCmptType but no new package
        IPolicyCmptType coverageType = newPolicyCmptType(ipsProject, "model.base.Coverage");
        visitor = newVisitor(LayoutStyle.FLAT);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        // all ips elements above the package containing the added type needn't be refresh, but
        // must be updated
        assertFalse(elementsToRefresh.contains(ipsProject.getIpsModel()));
        assertTrue(elementsToUpdate.contains(ipsProject.getIpsModel()));
        assertFalse(elementsToRefresh.contains(ipsProject));
        assertTrue(elementsToUpdate.contains(ipsProject));
        assertFalse(elementsToRefresh.contains(packRoot));
        assertTrue(elementsToUpdate.contains(packRoot));
        assertFalse(elementsToRefresh.contains(modelPack));
        assertTrue(elementsToUpdate.contains(modelPack)); // model package is not a parent in the
        // flay layout style, but gets updated anyways

        // The package to that a new child was added must be refresh.
        assertTrue(elementsToRefresh.contains(basePack));
        assertFalse(elementsToUpdate.contains(basePack));

        // No need to refreh the added child as the parent is refreshed.
        assertFalse(elementsToRefresh.contains(coverageType.getIpsSrcFile()));

        IPolicyCmptType personType = newPolicyCmptType(ipsProject, "model.Person");
        visitor = newVisitor(LayoutStyle.FLAT);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        // test case 3: all ips elements above the package containg the added type needn't be
        // refresh, but must be updated
        assertFalse(elementsToRefresh.contains(ipsProject.getIpsModel()));
        assertTrue(elementsToUpdate.contains(ipsProject.getIpsModel()));
        assertFalse(elementsToRefresh.contains(ipsProject));
        assertTrue(elementsToUpdate.contains(ipsProject));
        assertFalse(elementsToRefresh.contains(packRoot));
        assertTrue(elementsToUpdate.contains(packRoot));

        assertTrue(elementsToRefresh.contains(modelPack));
        assertFalse(elementsToUpdate.contains(modelPack));
        assertFalse(elementsToRefresh.contains(personType.getIpsSrcFile()));

        // test case 4: change an IpsSrcFile
        policyType.getIpsSrcFile().getCorrespondingFile().touch(null);
        visitor = newVisitor(LayoutStyle.FLAT);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        // => all parents must be updated but not refresh
        assertFalse(elementsToRefresh.contains(ipsProject.getIpsModel()));
        assertTrue(elementsToUpdate.contains(ipsProject.getIpsModel()));
        assertFalse(elementsToRefresh.contains(ipsProject));
        assertTrue(elementsToUpdate.contains(ipsProject));
        assertFalse(elementsToRefresh.contains(packRoot));
        assertTrue(elementsToUpdate.contains(packRoot));
        // model pack is not a parent in the flat layout style! but gets updated anyways
        assertFalse(elementsToRefresh.contains(basePack));
        assertTrue(elementsToUpdate.contains(basePack));
        // the IpsSrcFile itself must be refreshed
        assertTrue(elementsToRefresh.contains(policyType.getIpsSrcFile()));
        assertFalse(elementsToUpdate.contains(policyType.getIpsSrcFile()));

        // test case 4: new "normal" file inside package
        IFolder folder = (IFolder)basePack.getCorrespondingResource();
        IFile readme = folder.getFile("readme.txt");
        readme.create(new ByteArrayInputStream("hello".getBytes()), true, null);
        visitor = newVisitor(LayoutStyle.FLAT);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertTrue(visitor.getElementsToRefresh().contains(basePack));
        assertFalse(visitor.getElementsToUpdate().contains(basePack));
        assertFalse(visitor.getElementsToRefresh().contains(readme));
        assertFalse(visitor.getElementsToUpdate().contains(readme));

        // test case 5: change a "normal" file inside a package
        readme.touch(null);
        visitor = newVisitor(LayoutStyle.FLAT);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertFalse(visitor.getElementsToRefresh().contains(basePack));
        assertTrue(visitor.getElementsToUpdate().contains(basePack));
        assertFalse(visitor.getElementsToRefresh().contains(readme));
        assertTrue(visitor.getElementsToUpdate().contains(readme));

        // test case 5: "normal" folder in IpsProject
        folder = ipsProject.getProject().getFolder("docs");
        folder.create(true, true, null);
        visitor = newVisitor(LayoutStyle.FLAT);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertTrue(visitor.getElementsToRefresh().contains(ipsProject));
        assertFalse(visitor.getElementsToUpdate().contains(folder));

        // test case 6: add a new "normal" file to a "normal" folder
        IFile doc1 = folder.getFile("doc1.txt");
        doc1.create(new ByteArrayInputStream("hello".getBytes()), true, null);
        visitor = newVisitor(LayoutStyle.FLAT);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertFalse(visitor.getElementsToRefresh().contains(ipsProject));
        assertTrue(visitor.getElementsToUpdate().contains(ipsProject));
        assertTrue(visitor.getElementsToRefresh().contains(folder));
        assertFalse(visitor.getElementsToUpdate().contains(folder));

        // test case 7: change "normal" file in "normal" folder
        doc1.touch(null);
        visitor = newVisitor(LayoutStyle.FLAT);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertFalse(visitor.getElementsToRefresh().contains(folder));
        assertTrue(visitor.getElementsToUpdate().contains(folder));
        assertFalse(visitor.getElementsToRefresh().contains(doc1));
        assertTrue(visitor.getElementsToUpdate().contains(doc1));
    }

    @Test
    public void test_HierarchicalLayout() throws Exception {
        // test case 1: new package "model.base" and new PolicyCmptType
        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "model.base.Policy");
        IIpsPackageFragment basePack = policyType.getIpsPackageFragment();
        IIpsPackageFragment modelPack = basePack.getParentIpsPackageFragment();
        IpsViewRefreshVisitor visitor = newVisitor(LayoutStyle.HIERACHICAL);
        Set<Object> elementsToRefresh = visitor.getElementsToRefresh();
        Set<Object> elementsToUpdate = visitor.getElementsToUpdate();

        assertFalse(elementsToRefresh.contains(ipsProject.getIpsModel()));
        assertTrue(elementsToUpdate.contains(ipsProject.getIpsModel()));

        // project must be updated, as for example team label decoration might have changed
        assertFalse(elementsToRefresh.contains(ipsProject));
        assertTrue(elementsToUpdate.contains(ipsProject));

        // root needs to be refreshed as a direct child (pack model) was added.
        assertTrue(elementsToRefresh.contains(packRoot));
        assertFalse(elementsToUpdate.contains(packRoot));

        // all other children needn't be refreshed or updated as the root is refreshed!
        assertFalse(elementsToRefresh.contains(modelPack));
        assertFalse(elementsToUpdate.contains(modelPack));
        assertFalse(elementsToRefresh.contains(basePack));
        assertFalse(elementsToUpdate.contains(basePack));
        assertFalse(elementsToRefresh.contains(policyType.getIpsSrcFile()));
        assertFalse(elementsToUpdate.contains(policyType.getIpsSrcFile()));

        // test case 2: new PolicyCmptType but no new package
        IPolicyCmptType coverageType = newPolicyCmptType(ipsProject, "model.base.Coverage");
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        // all ips elements above the package containg the added type needn't be refresh, but
        // must be updated (including modelPack, see below)
        assertFalse(elementsToRefresh.contains(ipsProject.getIpsModel()));
        assertTrue(elementsToUpdate.contains(ipsProject.getIpsModel()));
        assertFalse(elementsToRefresh.contains(ipsProject));
        assertTrue(elementsToUpdate.contains(ipsProject));
        assertFalse(elementsToRefresh.contains(packRoot));
        assertTrue(elementsToUpdate.contains(packRoot));
        assertFalse(elementsToRefresh.contains(modelPack));
        assertTrue(elementsToUpdate.contains(modelPack)); // model package is the parent in the
        // hierarchical layout style!

        // The package to that a new child was added must be refresh.
        assertTrue(elementsToRefresh.contains(basePack));
        assertFalse(elementsToUpdate.contains(basePack));

        // No need to refresh the added child as the parent is refreshed.
        assertFalse(elementsToRefresh.contains(coverageType.getIpsSrcFile()));

        // test case 3: all ips elements above the package containing the added type needn't be
        // refresh, but
        // must be updated
        IPolicyCmptType personType = newPolicyCmptType(ipsProject, "model.Person");
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertFalse(elementsToRefresh.contains(ipsProject.getIpsModel()));
        assertTrue(elementsToUpdate.contains(ipsProject.getIpsModel()));
        assertFalse(elementsToRefresh.contains(ipsProject));
        assertTrue(elementsToUpdate.contains(ipsProject));
        assertFalse(elementsToRefresh.contains(packRoot));
        assertTrue(elementsToUpdate.contains(packRoot));

        assertTrue(elementsToRefresh.contains(modelPack));
        assertFalse(elementsToUpdate.contains(modelPack));
        assertFalse(elementsToRefresh.contains(personType.getIpsSrcFile()));

        // test case 4: change an IpsSrcFile
        policyType.getIpsSrcFile().getCorrespondingFile().touch(null);
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        // => all parents must be updated but not refresh
        assertFalse(elementsToRefresh.contains(ipsProject.getIpsModel()));
        assertTrue(elementsToUpdate.contains(ipsProject.getIpsModel()));
        assertFalse(elementsToRefresh.contains(ipsProject));
        assertTrue(elementsToUpdate.contains(ipsProject));
        assertFalse(elementsToRefresh.contains(packRoot));
        assertTrue(elementsToUpdate.contains(packRoot));
        assertFalse(elementsToRefresh.contains(modelPack));
        assertTrue(elementsToUpdate.contains(modelPack));
        assertFalse(elementsToRefresh.contains(basePack));
        assertTrue(elementsToUpdate.contains(basePack));
        // the IpsSrcFile itself must be refreshed
        assertTrue(elementsToRefresh.contains(policyType.getIpsSrcFile()));
        assertFalse(elementsToUpdate.contains(policyType.getIpsSrcFile()));

        // test case 4: new "normal" file inside package
        IFolder folder = (IFolder)basePack.getCorrespondingResource();
        IFile readme = folder.getFile("readme.txt");
        readme.create(new ByteArrayInputStream("hello".getBytes()), true, null);
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertTrue(visitor.getElementsToRefresh().contains(basePack));
        assertFalse(visitor.getElementsToUpdate().contains(basePack));
        assertFalse(visitor.getElementsToRefresh().contains(readme));
        assertFalse(visitor.getElementsToUpdate().contains(readme));

        // test case 5: change a "normal" file inside a package
        readme.touch(null);
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertFalse(visitor.getElementsToRefresh().contains(basePack));
        assertTrue(visitor.getElementsToUpdate().contains(basePack));
        assertFalse(visitor.getElementsToRefresh().contains(readme));
        assertTrue(visitor.getElementsToUpdate().contains(readme));

        // test case 5: "normal" folder in IpsProject
        folder = ipsProject.getProject().getFolder("docs");
        folder.create(true, true, null);
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertTrue(visitor.getElementsToRefresh().contains(ipsProject));
        assertFalse(visitor.getElementsToUpdate().contains(folder));

        // test case 6: add a new "normal" file to a "normal" folder
        IFile doc1 = folder.getFile("doc1.txt");
        doc1.create(new ByteArrayInputStream("hello".getBytes()), true, null);
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertFalse(visitor.getElementsToRefresh().contains(ipsProject));
        assertTrue(visitor.getElementsToUpdate().contains(ipsProject));
        assertTrue(visitor.getElementsToRefresh().contains(folder));
        assertFalse(visitor.getElementsToUpdate().contains(folder));

        // test case 7: change "normal" file in "normal" folder
        doc1.touch(null);
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertFalse(visitor.getElementsToRefresh().contains(folder));
        assertTrue(visitor.getElementsToUpdate().contains(folder));
        assertFalse(visitor.getElementsToRefresh().contains(doc1));
        assertTrue(visitor.getElementsToUpdate().contains(doc1));
    }

    // Test for FIPS-70
    @Test
    public void testUpdateIfManifestIsChanged() throws CoreException {

        ModelExplorerConfiguration config = new ModelExplorerConfiguration(ipsProject.getIpsModel().getIpsObjectTypes());
        ModelContentProvider contentProvider = new ModelContentProvider(config, LayoutStyle.HIERACHICAL);
        IpsViewRefreshVisitor visitor = new IpsViewRefreshVisitor(contentProvider);

        IResourceDelta delta = mock(IResourceDelta.class);
        IResource manifestResource = mock(IResource.class);

        when(manifestResource.getProject()).thenReturn(ipsProject.getProject());
        when(manifestResource.getFullPath()).thenReturn(new Path(ipsProject.getName() + "/META-INF/MANIFEST.MF"));
        when(manifestResource.getProjectRelativePath()).thenReturn(new Path("META-INF/MANIFEST.MF"));

        when(delta.getResource()).thenReturn(manifestResource);

        visitor.visit(delta);

        assertFalse(visitor.getElementsToRefresh().contains(ipsProject.getIpsModel()));
        assertFalse(visitor.getElementsToUpdate().contains(ipsProject.getIpsModel()));

        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.setUsingManifest(true);
        ipsProject.setIpsObjectPath(ipsObjectPath);

        visitor = new IpsViewRefreshVisitor(contentProvider);
        visitor.visit(delta);

        assertTrue(visitor.getElementsToRefresh().contains(ipsProject.getIpsModel()));
        assertFalse(visitor.getElementsToUpdate().contains(ipsProject.getIpsModel()));
    }

    private IpsViewRefreshVisitor newVisitor(LayoutStyle style) throws CoreException, InterruptedException {
        ModelExplorerConfiguration config = new ModelExplorerConfiguration(ipsProject.getIpsModel().getIpsObjectTypes());
        ModelContentProvider contentProvider = new ModelContentProvider(config, style);
        IpsViewRefreshVisitor visitor = new IpsViewRefreshVisitor(contentProvider);
        IResourceDelta delta = getTriggeredEvent().getDelta();
        delta.accept(visitor);
        return visitor;
    }

    private IResourceChangeEvent getTriggeredEvent() throws InterruptedException {
        while (event == null) {
            Thread.sleep(500);
            Thread.yield();
        }
        IResourceChangeEvent recentEvent = event;
        event = null;
        return recentEvent;
    }

    @Test
    public void testVisit_ipsAndJavaResource() throws Exception {
        addIpsRootAsSourceEntry();
        newPolicyCmptType(ipsProject, "model.base.Policy");

        IpsViewRefreshVisitor visitor = newVisitor(LayoutStyle.HIERACHICAL);
        Set<Object> elementsToRefresh = visitor.getElementsToRefresh();

        assertTrue(elementsToRefresh.contains(packRoot));
    }

    private void addIpsRootAsSourceEntry() throws Exception {
        IJavaProject javaProject = ipsProject.getJavaProject();
        IClasspathEntry[] entries = javaProject.getRawClasspath();

        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);

        IPath srcPath = packRoot.getEnclosingResource().getFullPath();
        IClasspathEntry srcEntry = JavaCore.newSourceEntry(srcPath, null);

        newEntries[entries.length] = JavaCore.newSourceEntry(srcEntry.getPath());
        javaProject.setRawClasspath(newEntries, null);
    }

}
