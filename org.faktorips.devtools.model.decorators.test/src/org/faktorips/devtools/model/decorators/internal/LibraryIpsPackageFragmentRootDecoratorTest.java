/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.descriptorOf;
import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasNoOverlay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.ipsproject.ILibraryIpsPackageFragmentRoot;
import org.junit.Test;

public class LibraryIpsPackageFragmentRootDecoratorTest {

    private final LibraryIpsPackageFragmentRootDecorator libraryIpsPackageFragmentRootDecorator = new LibraryIpsPackageFragmentRootDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = libraryIpsPackageFragmentRootDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(LibraryIpsPackageFragmentRootDecorator.IPS_ARCHIVE_IMAGE)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = libraryIpsPackageFragmentRootDecorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(libraryIpsPackageFragmentRootDecorator.getDefaultImageDescriptor()));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor() {
        ILibraryIpsPackageFragmentRoot libraryIpsPackageFragmentRoot = mock(ILibraryIpsPackageFragmentRoot.class);

        ImageDescriptor imageDescriptor = libraryIpsPackageFragmentRootDecorator
                .getImageDescriptor(libraryIpsPackageFragmentRoot);

        assertThat(imageDescriptor, is(descriptorOf(LibraryIpsPackageFragmentRootDecorator.IPS_FOLDER_IMAGE)));
    }

    @Test
    public void testGetImageDescriptor_Archive() {
        ILibraryIpsPackageFragmentRoot libraryIpsPackageFragmentRoot = mock(ILibraryIpsPackageFragmentRoot.class);
        when(libraryIpsPackageFragmentRoot.isContainedInArchive()).thenReturn(true);

        ImageDescriptor imageDescriptor = libraryIpsPackageFragmentRootDecorator
                .getImageDescriptor(libraryIpsPackageFragmentRoot);

        assertThat(imageDescriptor, is(descriptorOf(LibraryIpsPackageFragmentRootDecorator.IPS_ARCHIVE_IMAGE)));
    }

    @Test
    public void testGetLabel_null() {
        assertThat(libraryIpsPackageFragmentRootDecorator.getLabel(null), is(""));
    }

    @Test
    public void testGetLabel() {
        ILibraryIpsPackageFragmentRoot libraryIpsPackageFragmentRoot = mock(ILibraryIpsPackageFragmentRoot.class);
        when(libraryIpsPackageFragmentRoot.getName()).thenReturn("Groot");

        String label = libraryIpsPackageFragmentRootDecorator.getLabel(libraryIpsPackageFragmentRoot);

        assertThat(label, is("Groot"));
    }

}
