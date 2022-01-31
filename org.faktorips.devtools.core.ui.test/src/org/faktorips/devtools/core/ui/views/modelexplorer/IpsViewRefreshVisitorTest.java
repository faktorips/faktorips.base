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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.mapping.PathMapping;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IpsViewRefreshVisitorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot packRoot;
    private List<IResourceChangeListener> listeners = new ArrayList<>();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        packRoot = ipsProject.getIpsPackageFragmentRoots()[0];
    }

    @Override
    @After
    public void tearDown() throws Exception {
        // avoid NPEs by removing our listeners before calling super#tearDown
        for (var iterator = listeners.iterator(); iterator.hasNext();) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(iterator.next());
            iterator.remove();
        }
        super.tearDown();
    }

    @Test
    public void test_FlatLayout() throws Exception {
        // test case 1: new package "model.base" and new PolicyCmptType
        IpsViewRefreshVisitor visitor = newVisitor(LayoutStyle.FLAT);
        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "model.base.Policy");
        IIpsPackageFragment basePack = policyType.getIpsPackageFragment();
        IIpsPackageFragment modelPack = basePack.getParentIpsPackageFragment();
        Set<Object> elementsToRefresh = visitor.getElementsToRefresh();
        Set<Object> elementsToUpdate = visitor.getElementsToUpdate();

        // project must be updated, as for example team label decoration might have changed
        assertThat(elementsToRefresh, doesNotHaveItems(ipsProject.getIpsModel(), ipsProject));
        assertThat(elementsToUpdate, hasItems(ipsProject.getIpsModel(), ipsProject));

        // root must be refreshed as a direct child (pack model) was added.
        assertThat(elementsToRefresh, hasItem(packRoot));
        assertThat(elementsToUpdate, doesNotHaveItem(packRoot));

        // all other children needn't be refreshed or updated as the root is refreshed!
        assertThat(elementsToRefresh, doesNotHaveItems(modelPack, basePack, policyType.getIpsSrcFile()));
        assertThat(elementsToUpdate, doesNotHaveItems(modelPack, basePack, policyType.getIpsSrcFile()));

        // test case 2: new PolicyCmptType but no new package
        visitor = newVisitor(LayoutStyle.FLAT);
        IPolicyCmptType coverageType = newPolicyCmptType(ipsProject, "model.base.Coverage");
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        // all IPS elements above the package containing the added type needn't be refresh, but
        // must be updated
        assertThat(elementsToRefresh, doesNotHaveItems(ipsProject.getIpsModel(), ipsProject, packRoot, modelPack));
        // model package is not a parent in the flat layout style, but gets updated anyways
        assertThat(elementsToUpdate, hasItems(ipsProject.getIpsModel(), ipsProject, packRoot, modelPack));

        // The package to which a new child was added must be refreshed
        assertThat(elementsToRefresh, hasItem(basePack));
        assertThat(elementsToUpdate, doesNotHaveItems(basePack));

        // No need to refresh the added child as the parent is refreshed.
        assertThat(elementsToRefresh, doesNotHaveItems(coverageType.getIpsSrcFile()));

        visitor = newVisitor(LayoutStyle.FLAT);
        IPolicyCmptType personType = newPolicyCmptType(ipsProject, "model.Person");
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        // test case 3: all IPS elements above the package containing the added type needn't be
        // refreshed, but must be updated
        assertThat(elementsToRefresh,
                doesNotHaveItems(ipsProject.getIpsModel(), ipsProject, packRoot, personType.getIpsSrcFile()));
        assertThat(elementsToRefresh, hasItem(modelPack));

        assertThat(elementsToUpdate, hasItems(ipsProject.getIpsModel(), ipsProject, packRoot));
        assertThat(elementsToUpdate, doesNotHaveItem(modelPack));

        // test case 4: change an IpsSrcFile
        visitor = newVisitor(LayoutStyle.FLAT);
        policyType.getIpsSrcFile().getCorrespondingFile().touch(null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        // => all parents must be updated but not refreshed
        // model pack is not a parent in the flat layout style but gets updated anyways
        assertThat(elementsToRefresh, doesNotHaveItems(ipsProject.getIpsModel(), ipsProject, packRoot, basePack));
        assertThat(elementsToUpdate, hasItems(ipsProject.getIpsModel(), ipsProject, packRoot, basePack));

        // the IpsSrcFile itself must be refreshed
        assertThat(elementsToRefresh, hasItems(policyType.getIpsSrcFile()));
        assertThat(elementsToUpdate, doesNotHaveItems(policyType.getIpsSrcFile()));

        // test case 4: new "normal" file inside package
        visitor = newVisitor(LayoutStyle.FLAT);
        IFolder folder = basePack.getCorrespondingResource().unwrap();
        IFile readme = folder.getFile("readme.txt");
        readme.create(new ByteArrayInputStream("hello".getBytes()), true, null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertThat(elementsToRefresh, hasItem(basePack));
        assertThat(elementsToRefresh, doesNotHaveItem(readme));
        assertThat(elementsToUpdate, doesNotHaveItems(basePack, readme));

        // test case 5: change a "normal" file inside a package
        visitor = newVisitor(LayoutStyle.FLAT);
        readme.touch(null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertThat(elementsToRefresh, doesNotHaveItems(basePack, readme));
        assertThat(elementsToUpdate, hasItems(basePack, readme));

        // test case 5: "normal" folder in IpsProject
        visitor = newVisitor(LayoutStyle.FLAT);
        folder = ipsProject.getProject().getFolder("docs").unwrap();
        folder.create(true, true, null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();
        assertThat(elementsToRefresh, hasItem(ipsProject));
        assertThat(elementsToUpdate, doesNotHaveItem(folder));

        // test case 6: add a new "normal" file to a "normal" folder
        visitor = newVisitor(LayoutStyle.FLAT);
        IFile doc1 = folder.getFile("doc1.txt");
        doc1.create(new ByteArrayInputStream("hello".getBytes()), true, null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertThat(elementsToRefresh, doesNotHaveItem(ipsProject));
        assertThat(elementsToRefresh, hasItem(folder));

        assertThat(elementsToUpdate, hasItem(ipsProject));
        assertThat(elementsToUpdate, doesNotHaveItem(folder));

        // test case 7: change "normal" file in "normal" folder
        visitor = newVisitor(LayoutStyle.FLAT);
        doc1.touch(null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertThat(elementsToRefresh, doesNotHaveItems(folder, doc1));
        assertThat(elementsToUpdate, hasItems(folder, doc1));
    }

    @Test
    public void test_HierarchicalLayout() throws Exception {
        // test case 1: new package "model.base" and new PolicyCmptType
        IpsViewRefreshVisitor visitor = newVisitor(LayoutStyle.HIERACHICAL);
        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "model.base.Policy");
        IIpsPackageFragment basePack = policyType.getIpsPackageFragment();
        IIpsPackageFragment modelPack = basePack.getParentIpsPackageFragment();
        Set<Object> elementsToRefresh = visitor.getElementsToRefresh();
        Set<Object> elementsToUpdate = visitor.getElementsToUpdate();

        // project must be updated, as for example team label decoration might have changed
        assertThat(elementsToRefresh, doesNotHaveItems(ipsProject.getIpsModel(), ipsProject));
        assertThat(elementsToUpdate, hasItems(ipsProject.getIpsModel(), ipsProject));

        // root needs to be refreshed as a direct child (pack model) was added
        assertThat(elementsToRefresh, hasItem(packRoot));
        assertThat(elementsToUpdate, doesNotHaveItem(packRoot));

        // all other children needn't be refreshed or updated as the root is refreshed!
        assertThat(elementsToRefresh, doesNotHaveItems(modelPack, basePack, policyType.getIpsSrcFile()));
        assertThat(elementsToUpdate, doesNotHaveItems(modelPack, basePack, policyType.getIpsSrcFile()));

        // test case 2: new PolicyCmptType but no new package
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        IPolicyCmptType coverageType = newPolicyCmptType(ipsProject, "model.base.Coverage");
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        // all IPS elements above the package containing the added type needn't be refreshed, but
        // must be updated (including modelPack, see below)
        assertThat(elementsToRefresh, doesNotHaveItems(ipsProject.getIpsModel(), ipsProject, packRoot, modelPack));

        // model package is the parent in the hierarchical layout style!
        assertThat(elementsToUpdate, hasItems(ipsProject.getIpsModel(), ipsProject, packRoot, modelPack));

        // The package to which a new child was added must be refreshed
        assertThat(elementsToRefresh, hasItem(basePack));
        assertThat(elementsToUpdate, doesNotHaveItem(basePack));

        // No need to refresh the added child as the parent is refreshed
        assertThat(elementsToRefresh, doesNotHaveItem(coverageType.getIpsSrcFile()));

        // test case 3: all IPS elements above the package containing the added type needn't be
        // refreshed, but must be updated
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        IPolicyCmptType personType = newPolicyCmptType(ipsProject, "model.Person");
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertThat(elementsToRefresh,
                doesNotHaveItems(ipsProject.getIpsModel(), ipsProject, packRoot, personType.getIpsSrcFile()));
        assertThat(elementsToRefresh, hasItem(modelPack));

        assertThat(elementsToUpdate, hasItems(ipsProject.getIpsModel(), ipsProject, packRoot));
        assertThat(elementsToUpdate, doesNotHaveItem(modelPack));

        // test case 4: change an IpsSrcFile
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        policyType.getIpsSrcFile().getCorrespondingFile().touch(null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        // => all parents must be updated but not refreshed
        assertThat(elementsToRefresh,
                doesNotHaveItems(ipsProject.getIpsModel(), ipsProject, packRoot, modelPack, basePack));
        assertThat(elementsToUpdate, hasItems(ipsProject.getIpsModel(), ipsProject, packRoot, modelPack, basePack));

        // the IpsSrcFile itself must be refreshed
        assertThat(elementsToRefresh, hasItem(policyType.getIpsSrcFile()));
        assertThat(elementsToUpdate, doesNotHaveItem(policyType.getIpsSrcFile()));

        // test case 4: new "normal" file inside package
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        IFolder folder = basePack.getCorrespondingResource().unwrap();
        IFile readme = folder.getFile("readme.txt");
        readme.create(new ByteArrayInputStream("hello".getBytes()), true, null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertThat(elementsToRefresh, hasItem(basePack));
        assertThat(elementsToRefresh, doesNotHaveItem(readme));
        assertThat(elementsToUpdate, doesNotHaveItems(basePack, readme));

        // test case 5: change a "normal" file inside a package
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        readme.touch(null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertThat(elementsToRefresh, doesNotHaveItems(basePack, readme));
        assertThat(elementsToUpdate, hasItems(basePack, readme));

        // test case 5: "normal" folder in IpsProject
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        folder = ipsProject.getProject().getFolder("docs").unwrap();
        folder.create(true, true, null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertThat(elementsToRefresh, hasItem(ipsProject));
        assertThat(elementsToUpdate, doesNotHaveItem(folder));

        // test case 6: add a new "normal" file to a "normal" folder
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        IFile doc1 = folder.getFile("doc1.txt");
        doc1.create(new ByteArrayInputStream("hello".getBytes()), true, null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertThat(elementsToRefresh, doesNotHaveItem(ipsProject));
        assertThat(elementsToRefresh, hasItem(folder));

        assertThat(elementsToUpdate, hasItem(ipsProject));
        assertThat(elementsToUpdate, doesNotHaveItem(folder));

        // test case 7: change "normal" file in "normal" folder
        visitor = newVisitor(LayoutStyle.HIERACHICAL);
        doc1.touch(null);
        elementsToRefresh = visitor.getElementsToRefresh();
        elementsToUpdate = visitor.getElementsToUpdate();

        assertThat(elementsToRefresh, doesNotHaveItems(folder, doc1));
        assertThat(elementsToUpdate, hasItems(folder, doc1));
    }

    // Test for FIPS-70
    @Test
    public void testUpdateIfManifestIsChanged() throws CoreRuntimeException {

        ModelExplorerConfiguration config = new ModelExplorerConfiguration(
                ipsProject.getIpsModel().getIpsObjectTypes());
        ModelContentProvider contentProvider = new ModelContentProvider(config, LayoutStyle.HIERACHICAL);
        IpsViewRefreshVisitor visitor = new IpsViewRefreshVisitor(contentProvider);

        IResourceDelta delta = mock(IResourceDelta.class);
        IResource manifestResource = mock(IFile.class);

        when(manifestResource.getProject()).thenReturn(ipsProject.getProject().unwrap());
        when(manifestResource.getFullPath()).thenReturn(new Path(ipsProject.getName() + "/META-INF/MANIFEST.MF"));
        when(manifestResource.getProjectRelativePath()).thenReturn(new Path("META-INF/MANIFEST.MF"));

        when(delta.getResource()).thenReturn(manifestResource);

        visitor.visit(delta);

        assertThat(visitor.getElementsToRefresh(), doesNotHaveItem(ipsProject.getIpsModel()));
        assertThat(visitor.getElementsToUpdate(), doesNotHaveItem(ipsProject.getIpsModel()));

        IIpsObjectPath ipsObjectPath = ipsProject.getIpsObjectPath();
        ipsObjectPath.setUsingManifest(true);
        ipsProject.setIpsObjectPath(ipsObjectPath);

        visitor = new IpsViewRefreshVisitor(contentProvider);
        visitor.visit(delta);

        assertThat(visitor.getElementsToRefresh(), hasItem(ipsProject.getIpsModel()));
        assertThat(visitor.getElementsToUpdate(), doesNotHaveItem(ipsProject.getIpsModel()));
    }

    @Test
    public void testVisit_ipsAndJavaResource() throws Exception {
        IpsViewRefreshVisitor visitor = newVisitor(LayoutStyle.HIERACHICAL);
        addIpsRootAsSourceEntry();
        newPolicyCmptType(ipsProject, "model.base.Policy");

        assertThat(visitor.getElementsToRefresh(), hasItem(packRoot));
    }

    private IpsViewRefreshVisitor newVisitor(LayoutStyle style) {

        ModelExplorerConfiguration config = new ModelExplorerConfiguration(
                ipsProject.getIpsModel().getIpsObjectTypes());
        ModelContentProvider contentProvider = new ModelContentProvider(config, style);
        IpsViewRefreshVisitor visitor = new IpsViewRefreshVisitor(contentProvider);

        IResourceChangeListener listner = event -> {
            try {
                event.getDelta().accept(visitor);
            } catch (Exception e) {
                fail(e.getLocalizedMessage());
            }
        };

        ResourcesPlugin.getWorkspace().addResourceChangeListener(listner);
        listeners.add(listner);

        return visitor;
    }

    private void addIpsRootAsSourceEntry() throws Exception {
        IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
        IClasspathEntry[] entries = javaProject.getRawClasspath();

        IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
        System.arraycopy(entries, 0, newEntries, 0, entries.length);
        IPath srcPath = PathMapping.toEclipsePath(packRoot.getEnclosingResource().getWorkspaceRelativePath());
        IClasspathEntry srcEntry = JavaCore.newSourceEntry(srcPath, null);

        newEntries[entries.length] = JavaCore.newSourceEntry(srcEntry.getPath());
        javaProject.setRawClasspath(newEntries, null);
    }

    private static <T> Matcher<Iterable<? super T>> doesNotHaveItem(T expected) {
        return doesNotHaveItems(expected);
    }

    @SafeVarargs
    private static <T> Matcher<Iterable<? super T>> doesNotHaveItems(T... expected) {
        return new BaseMatcher<>() {

            @Override
            public boolean matches(Object actual) {
                return !hasItems(expected).matches(actual);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Collection should not contain ").appendValue(expected);
            }
        };
    }
}
