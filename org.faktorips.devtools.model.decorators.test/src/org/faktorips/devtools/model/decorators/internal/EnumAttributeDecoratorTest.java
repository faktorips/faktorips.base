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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;

public class EnumAttributeDecoratorTest {

    private final EnumAttributeDecorator enumAttributeDecorator = new EnumAttributeDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = enumAttributeDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(EnumAttributeDecorator.ENUM_ATTRIBUTE_ICON)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        assertThat(enumAttributeDecorator.getImageDescriptor(null),
                is(enumAttributeDecorator.getDefaultImageDescriptor()));
    }

    @Test
    public void testGetImageDescriptor() {
        IEnumAttribute enumAttribute = mock(IEnumAttribute.class);

        ImageDescriptor imageDescriptor = enumAttributeDecorator.getImageDescriptor(enumAttribute);

        assertThat(imageDescriptor, hasBaseImage(EnumAttributeDecorator.ENUM_ATTRIBUTE_ICON));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_FindIsUniqueFails() throws CoreRuntimeException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IEnumAttribute enumAttribute = mock(IEnumAttribute.class);
        when(enumAttribute.getIpsProject()).thenReturn(ipsProject);
        doThrow(new CoreException(new IpsStatus("CAN'T FIND IT"))).when(enumAttribute)
                .findIsUnique(ipsProject);

        ImageDescriptor imageDescriptor = enumAttributeDecorator.getImageDescriptor(enumAttribute);

        assertThat(imageDescriptor, hasBaseImage(EnumAttributeDecorator.ENUM_ATTRIBUTE_ICON));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Unique() throws CoreRuntimeException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IEnumAttribute enumAttribute = mock(IEnumAttribute.class);
        when(enumAttribute.getIpsProject()).thenReturn(ipsProject);
        when(enumAttribute.findIsUnique(ipsProject)).thenReturn(true);

        ImageDescriptor imageDescriptor = enumAttributeDecorator.getImageDescriptor(enumAttribute);

        assertThat(imageDescriptor, hasBaseImage(EnumAttributeDecorator.ENUM_ATTRIBUTE_ICON));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.KEY, IDecoration.TOP_LEFT));
    }

    @Test
    public void testGetImageDescriptor_Override() {
        IEnumAttribute enumAttribute = mock(IEnumAttribute.class);
        when(enumAttribute.isInherited()).thenReturn(true);

        ImageDescriptor imageDescriptor = enumAttributeDecorator.getImageDescriptor(enumAttribute);

        assertThat(imageDescriptor, hasBaseImage(EnumAttributeDecorator.ENUM_ATTRIBUTE_ICON));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.OVERRIDE, IDecoration.TOP_RIGHT));
    }

    @Test
    public void testGetImageDescriptor_OverrideUnique() throws CoreRuntimeException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IEnumAttribute enumAttribute = mock(IEnumAttribute.class);
        when(enumAttribute.getIpsProject()).thenReturn(ipsProject);
        when(enumAttribute.findIsUnique(ipsProject)).thenReturn(true);
        when(enumAttribute.isInherited()).thenReturn(true);

        ImageDescriptor imageDescriptor = enumAttributeDecorator.getImageDescriptor(enumAttribute);

        assertThat(imageDescriptor, hasBaseImage(EnumAttributeDecorator.ENUM_ATTRIBUTE_ICON));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.KEY, IDecoration.TOP_LEFT));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.OVERRIDE, IDecoration.TOP_RIGHT));
    }

    @Test
    public void testGetLabel_Null() {
        assertThat(enumAttributeDecorator.getLabel(null), is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetLabel_NoDatatype() {
        IEnumAttribute enumAttribute = mock(IEnumAttribute.class);
        when(enumAttribute.getName()).thenReturn("Foo");

        String label = enumAttributeDecorator.getLabel(enumAttribute);

        assertThat(label, is("Foo"));
    }

    @Test
    public void testGetLabel_FindDatatypeFails() throws CoreRuntimeException {
        IIpsProject ipsProject = mock(IIpsProject.class);

        IEnumAttribute enumAttribute = mock(IEnumAttribute.class);
        when(enumAttribute.getIpsProject()).thenReturn(ipsProject);
        when(enumAttribute.getName()).thenReturn("Foo");
        doThrow(new CoreException(new IpsStatus("CAN'T FIND IT"))).when(enumAttribute).findDatatype(ipsProject);

        String label = enumAttributeDecorator.getLabel(enumAttribute);

        assertThat(label, is("Foo"));
    }

    @Test
    public void testGetLabel() throws CoreRuntimeException {
        IIpsProject ipsProject = mock(IIpsProject.class);

        ValueDatatype datatype = mock(ValueDatatype.class);
        when(datatype.getName()).thenReturn("Bar");

        IEnumAttribute enumAttribute = mock(IEnumAttribute.class);
        when(enumAttribute.getIpsProject()).thenReturn(ipsProject);
        when(enumAttribute.getName()).thenReturn("Foo");
        when(enumAttribute.findDatatype(ipsProject)).thenReturn(datatype);

        String label = enumAttributeDecorator.getLabel(enumAttribute);

        assertThat(label, is("Foo : Bar"));
    }

}
