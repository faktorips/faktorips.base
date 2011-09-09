/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.scope;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

public class IpsSearchSelectionScopeTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testIpsArchiveSelected() throws CoreException {
        IStructuredSelection selection = mock(IStructuredSelection.class);

        IIpsSrcFile scrFile = mock(IpsSrcFile.class);

        IResource resource = mock(IResource.class);
        IIpsArchive archive = mock(IIpsArchive.class);

        IIpsPackageFragmentRoot root = mock(IIpsPackageFragmentRoot.class);
        when(archive.getRoot()).thenReturn(root);

        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        when(packageFragment.getIpsSrcFiles()).thenReturn(new IIpsSrcFile[] { scrFile });

        when(root.getIpsPackageFragments()).thenReturn(new IIpsPackageFragment[] { packageFragment });

        IProject project = mock(IProject.class);

        when(resource.getProject()).thenReturn(project);
        when(resource.isAccessible()).thenReturn(true);
        when(resource.getAdapter(IIpsElement.class)).thenReturn(scrFile);
        IPath location = mock(IPath.class);
        when(resource.getLocation()).thenReturn(location);
        when(archive.getLocation()).thenReturn(location);

        IIpsProject ipsProject = mock(IIpsProject.class);
        IIpsPackageFragmentRoot emptyPackageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        IIpsArchive wrongArchive = mock(IIpsArchive.class);
        IPath otherLocation = mock(IPath.class);
        when(wrongArchive.getLocation()).thenReturn(otherLocation);
        when(emptyPackageFragmentRoot.isBasedOnIpsArchive()).thenReturn(true);
        when(emptyPackageFragmentRoot.getIpsArchive()).thenReturn(wrongArchive);

        IIpsPackageFragmentRoot archiveRepresentingPackageFragmentRoot = mock(IIpsPackageFragmentRoot.class);
        when(archiveRepresentingPackageFragmentRoot.isBasedOnIpsArchive()).thenReturn(true);
        when(archiveRepresentingPackageFragmentRoot.getIpsArchive()).thenReturn(archive);

        when(ipsProject.getIpsPackageFragmentRoots()).thenReturn(
                new IIpsPackageFragmentRoot[] { emptyPackageFragmentRoot, archiveRepresentingPackageFragmentRoot });

        when(project.getAdapter(IIpsElement.class)).thenReturn(ipsProject);

        when(selection.toList()).thenReturn(Collections.singletonList(resource));

        IpsSearchSelectionScope scope = new IpsSearchSelectionScope(selection);

        assertEquals(1, scope.getSelectedIpsSrcFiles().size());
        assertTrue(scope.getSelectedIpsSrcFiles().contains(scrFile));
    }
}
