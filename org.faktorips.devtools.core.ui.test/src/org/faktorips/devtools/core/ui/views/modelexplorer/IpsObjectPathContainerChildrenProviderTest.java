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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.model.internal.ipsproject.LibraryIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.model.ipsproject.bundle.IIpsBundleEntry;
import org.junit.Before;
import org.junit.Test;

public class IpsObjectPathContainerChildrenProviderTest {

    private IpsObjectPathContainerChildrenProvider childrenProvider;
    private IIpsProject referencedProject;
    private IIpsProject referencedProject2;
    private IIpsObjectPathContainer container;
    private LibraryIpsPackageFragmentRoot jarBundleFragmentRoot;

    @Before
    public void setUp() {
        childrenProvider = new IpsObjectPathContainerChildrenProvider();

        IIpsProjectRefEntry projectEntry = mock(IIpsProjectRefEntry.class);
        referencedProject = mock(IIpsProject.class);
        when(projectEntry.getReferencedIpsProject()).thenReturn(referencedProject);

        IIpsProjectRefEntry projectEntry2 = mock(IIpsProjectRefEntry.class);
        referencedProject2 = mock(IIpsProject.class);
        when(projectEntry2.getReferencedIpsProject()).thenReturn(referencedProject2);

        IIpsBundleEntry jarBundleEntry = mock(IIpsBundleEntry.class);
        jarBundleFragmentRoot = mock(LibraryIpsPackageFragmentRoot.class);
        when(jarBundleEntry.getIpsPackageFragmentRoot()).thenReturn(jarBundleFragmentRoot);

        List<IIpsObjectPathEntry> values = Arrays.asList(projectEntry, jarBundleEntry, projectEntry2);

        container = mock(IIpsObjectPathContainer.class);
        when(container.resolveEntries()).thenReturn(values);

    }

    @Test
    public void testGetChildren() {

        Object[] children = childrenProvider.getChildren(container);

        assertEquals(3, children.length);
        assertEquals(referencedProject, ((ReferencedIpsProjectViewItem)children[0]).getIpsProject());
        assertEquals(jarBundleFragmentRoot, children[1]);
        assertEquals(referencedProject2, ((ReferencedIpsProjectViewItem)children[2]).getIpsProject());
    }

}
