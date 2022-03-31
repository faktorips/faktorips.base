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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;

public class IpsPackageFragmentDecoratorTest {

    private final IpsPackageFragmentDecorator ipsPackageFragmentDecorator = new IpsPackageFragmentDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = ipsPackageFragmentDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(IpsPackageFragmentDecorator.IPS_PACKAGE_FRAGMENT_ICON)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        assertThat(ipsPackageFragmentDecorator.getImageDescriptor(null),
                is(ipsPackageFragmentDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_GetChildrenFails() {
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        doThrow(new IpsException(new IpsStatus("CAN'T FIND IT"))).when(packageFragment).getChildren();

        ImageDescriptor imageDescriptor = ipsPackageFragmentDecorator.getImageDescriptor(packageFragment);

        assertThat(imageDescriptor, is(descriptorOf(IpsPackageFragmentDecorator.IPS_PACKAGE_FRAGMENT_EMPTY_ICON)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_NoChildren() {
        ImageDescriptor imageDescriptor = ipsPackageFragmentDecorator
                .getImageDescriptor(mock(IIpsPackageFragment.class));

        assertThat(imageDescriptor, is(descriptorOf(IpsPackageFragmentDecorator.IPS_PACKAGE_FRAGMENT_EMPTY_ICON)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_EmptyChildren() {
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        when(packageFragment.getChildren()).thenReturn(new IIpsElement[0]);

        ImageDescriptor imageDescriptor = ipsPackageFragmentDecorator.getImageDescriptor(packageFragment);

        assertThat(imageDescriptor, is(descriptorOf(IpsPackageFragmentDecorator.IPS_PACKAGE_FRAGMENT_EMPTY_ICON)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor() {
        IIpsPackageFragment packageFragment = mock(IIpsPackageFragment.class);
        when(packageFragment.getChildren()).thenReturn(new IIpsElement[] { mock(IIpsElement.class) });

        ImageDescriptor imageDescriptor = ipsPackageFragmentDecorator.getImageDescriptor(packageFragment);

        assertThat(imageDescriptor, is(descriptorOf(IpsPackageFragmentDecorator.IPS_PACKAGE_FRAGMENT_ICON)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetLabel_Null() {
        assertThat(ipsPackageFragmentDecorator.getLabel(null), is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetLabel_NoName() {
        IIpsPackageFragment ipsPackageFragment = mock(IIpsPackageFragment.class);

        String label = ipsPackageFragmentDecorator.getLabel(ipsPackageFragment);

        assertThat(label, is(Messages.DefaultLabelProvider_labelDefaultPackage));
    }

    @Test
    public void testGetLabel() {
        IIpsPackageFragment ipsPackageFragment = mock(IIpsPackageFragment.class);
        when(ipsPackageFragment.getName()).thenReturn("foo");

        String label = ipsPackageFragmentDecorator.getLabel(ipsPackageFragment);

        assertThat(label, is("foo"));
    }

}
