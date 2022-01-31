/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.scope;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class IpsSearchSelectionScopeTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testIpsArchiveSelected() throws CoreRuntimeException {
        IStructuredSelection selection = mock(IStructuredSelection.class);

        IIpsSrcFile scrFile = mock(IpsSrcFile.class);

        IResource resource = mock(IResource.class);
        IIpsArchive archive = mock(IIpsArchive.class);

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        // when(archive.getRoot()).thenReturn(root);

        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        when(packageFragment.getIpsSrcFiles()).thenReturn(new IIpsSrcFile[] { scrFile });

        when(root.getIpsPackageFragments()).thenReturn(new IIpsPackageFragment[] { packageFragment });

        IProject project = mock(IProject.class);

        when(resource.getProject()).thenReturn(project);
        when(resource.isAccessible()).thenReturn(true);
        when(resource.getAdapter(IIpsElement.class)).thenReturn(scrFile);
        IPath location = mock(IPath.class);
        Path archiveLocation = mock(Path.class);
        when(resource.getLocation()).thenReturn(location);
        when(archive.getLocation()).thenReturn(archiveLocation);

        IIpsProject ipsProject = mock(IIpsProject.class);
        IIpsPackageFragmentRoot emptyPackageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        IIpsArchive wrongArchive = mock(IIpsArchive.class);
        Path otherLocation = mock(Path.class);
        when(wrongArchive.getLocation()).thenReturn(otherLocation);
        when(emptyPackageFragmentRoot.isBasedOnIpsArchive()).thenReturn(true);
        when(emptyPackageFragmentRoot.getIpsStorage()).thenReturn(wrongArchive);

        IIpsPackageFragmentRoot archiveRepresentingPackageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        when(archiveRepresentingPackageFragmentRoot.isBasedOnIpsArchive()).thenReturn(true);
        when(archiveRepresentingPackageFragmentRoot.getIpsStorage()).thenReturn(archive);

        when(ipsProject.getIpsPackageFragmentRoots()).thenReturn(
                new IIpsPackageFragmentRoot[] { emptyPackageFragmentRoot, archiveRepresentingPackageFragmentRoot });

        when(project.getAdapter(IIpsElement.class)).thenReturn(ipsProject);

        when(selection.toList()).thenReturn(Collections.singletonList(resource));

        IpsSearchSelectionScope scope = new IpsSearchSelectionScope(selection);

        assertEquals(1, scope.getSelectedIpsSrcFiles().size());
        assertTrue(scope.getSelectedIpsSrcFiles().contains(scrFile));
    }
}
