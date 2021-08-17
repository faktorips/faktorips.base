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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.junit.Test;

public class IpsSrcFileDecoratorTest {

    private final IpsSrcFileDecorator ipsSrcFileDecorator = new IpsSrcFileDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = ipsSrcFileDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(IpsSrcFileDecorator.IPS_SRC_FILE_IMAGE)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() throws Exception {
        assertThat(ipsSrcFileDecorator.getImageDescriptor(null),
                is(ipsSrcFileDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_SrcFileDoesNotExist() throws Exception {
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.exists()).thenReturn(false);

        ImageDescriptor descriptor = ipsSrcFileDecorator.getImageDescriptor(srcFile);

        assertThat(descriptor, is(ipsSrcFileDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_SrcFileIsNotParsable() throws Exception {
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.exists()).thenReturn(true);
        when(srcFile.isContentParsable()).thenReturn(false);

        ImageDescriptor descriptor = ipsSrcFileDecorator.getImageDescriptor(srcFile);

        assertThat(descriptor, is(ipsSrcFileDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_IsContentParsableFails() throws Exception {
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.exists()).thenReturn(true);
        doThrow(new CoreException(new IpsStatus("BROKEN"))).when(srcFile).isContentParsable();

        ImageDescriptor descriptor = ipsSrcFileDecorator.getImageDescriptor(srcFile);

        assertThat(descriptor, is(ipsSrcFileDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_IpsObjectTypeHasNoImplementingClass() throws Exception {
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.exists()).thenReturn(true);
        when(srcFile.isContentParsable()).thenReturn(true);
        IpsObjectType brokenIpsObjectType = mock(IpsObjectType.class);
        when(srcFile.getIpsObjectType()).thenReturn(brokenIpsObjectType);

        ImageDescriptor descriptor = ipsSrcFileDecorator.getImageDescriptor(srcFile);

        assertThat(descriptor, is(ipsSrcFileDecorator.getDefaultImageDescriptor()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testGetImageDescriptor_IpsObjectTypeHasImplementingClassWithNoDecorator() throws Exception {
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.exists()).thenReturn(true);
        when(srcFile.isContentParsable()).thenReturn(true);
        IpsObjectType brokenIpsObjectType = mock(IpsObjectType.class);
        when(brokenIpsObjectType.getImplementingClass()).thenReturn((Class)UndecoratedIpsObject.class);
        when(srcFile.getIpsObjectType()).thenReturn(brokenIpsObjectType);

        ImageDescriptor descriptor = ipsSrcFileDecorator.getImageDescriptor(srcFile);

        assertThat(descriptor, is(ipsSrcFileDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor_DecoratorForIpsObjectType() throws Exception {
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.exists()).thenReturn(true);
        when(srcFile.isContentParsable()).thenReturn(true);
        when(srcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT_TYPE);

        ImageDescriptor descriptor = ipsSrcFileDecorator.getImageDescriptor(srcFile);

        assertThat(descriptor, is(descriptorOf(IpsDecorators.PRODUCT_CMPT_TYPE_IMAGE)));
    }

    private static class UndecoratedIpsObject extends BaseIpsObject {

        protected UndecoratedIpsObject(IIpsSrcFile file) {
            super(file);
        }

        static final IpsObjectType IPS_OBJECT_TYPE = mock(IpsObjectType.class);

        @Override
        public IpsObjectType getIpsObjectType() {
            return IPS_OBJECT_TYPE;
        }

    }

}
