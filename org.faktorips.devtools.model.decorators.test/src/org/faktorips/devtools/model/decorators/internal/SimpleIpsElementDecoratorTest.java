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
import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasBaseImage;
import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasNoOverlay;
import static org.faktorips.devtools.model.decorators.internal.ImageDescriptorMatchers.hasOverlay;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.internal.IpsModel;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFileContent;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.type.IType;
import org.junit.Test;

public class SimpleIpsElementDecoratorTest {

    private static final String TEST_GIF = "TestImage.gif";
    private final SimpleIpsElementDecorator decorator = new SimpleIpsElementDecorator(TEST_GIF);

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = decorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(TEST_GIF)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(descriptorOf(TEST_GIF)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor() {
        IType type = mock(IType.class);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(type);

        assertThat(imageDescriptor, is(descriptorOf(TEST_GIF)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Deprecated() {
        IType deprecatedType = mock(IType.class);
        when(deprecatedType.isDeprecated()).thenReturn(true);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(deprecatedType);

        assertThat(imageDescriptor, hasBaseImage(TEST_GIF));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.DEPRECATED, IDecoration.BOTTOM_LEFT));
    }

    @Test
    public void testGetImageDescriptor_DeprecatedSrcFile_NotCached() {
        IType deprecatedType = mock(IType.class);
        when(deprecatedType.isDeprecated()).thenReturn(true);
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.getIpsObject()).thenReturn(deprecatedType);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(srcFile);

        assertThat(imageDescriptor, is(descriptorOf(TEST_GIF)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetImageDescriptor_DeprecatedSrcFile_Cached() {
        PolicyCmptType deprecatedType = mock(PolicyCmptType.class);
        when(deprecatedType.isDeprecated()).thenReturn(true);
        IIpsSrcFile srcFile = mock(IIpsSrcFile.class);
        when(srcFile.getIpsObject()).thenReturn(deprecatedType);
        when(srcFile.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);
        IpsSrcFileContent content = mock(IpsSrcFileContent.class);
        doReturn(deprecatedType).when(content).getIpsObject();
        IpsModel.get().cache(srcFile, content);

        ImageDescriptor imageDescriptor = decorator.getImageDescriptor(srcFile);

        assertThat(imageDescriptor, hasBaseImage(TEST_GIF));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.DEPRECATED, IDecoration.BOTTOM_LEFT));
    }

}
