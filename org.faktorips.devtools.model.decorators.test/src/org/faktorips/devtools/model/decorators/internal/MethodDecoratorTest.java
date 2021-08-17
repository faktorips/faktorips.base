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
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.type.IMethod;
import org.junit.Test;

public class MethodDecoratorTest {

    private final MethodDecorator methodDecorator = new MethodDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = methodDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(MethodDecorator.METHOD_IMAGE_NAME)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = methodDecorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(descriptorOf(MethodDecorator.METHOD_IMAGE_NAME)));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor() {
        IMethod overwritingMethod = mock(IMethod.class);

        ImageDescriptor imageDescriptor = methodDecorator.getImageDescriptor(overwritingMethod);

        assertThat(imageDescriptor, hasBaseImage(MethodDecorator.METHOD_IMAGE_NAME));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Abstract() {
        IMethod overwritingMethod = mock(IMethod.class);
        when(overwritingMethod.isAbstract()).thenReturn(true);

        ImageDescriptor imageDescriptor = methodDecorator.getImageDescriptor(overwritingMethod);

        assertThat(imageDescriptor, hasBaseImage(MethodDecorator.METHOD_IMAGE_NAME));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.ABSTRACT, IDecoration.TOP_RIGHT));
    }

    @Test
    public void testGetImageDescriptor_Overwrite() throws CoreException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IMethod overwrittenMethod = mock(IMethod.class);
        IMethod overwritingMethod = mock(IMethod.class);
        when(overwritingMethod.getIpsProject()).thenReturn(ipsProject);
        when(overwritingMethod.findOverriddenMethod(ipsProject)).thenReturn(overwrittenMethod);

        ImageDescriptor imageDescriptor = methodDecorator.getImageDescriptor(overwritingMethod);

        assertThat(imageDescriptor, hasBaseImage(MethodDecorator.METHOD_IMAGE_NAME));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.OVERRIDE, IDecoration.BOTTOM_RIGHT));
    }

    @Test
    public void testGetImageDescriptor_FindOverwrittenFails() throws CoreException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IMethod overwritingMethod = mock(IMethod.class);
        when(overwritingMethod.getIpsProject()).thenReturn(ipsProject);
        doThrow(new CoreException(new IpsStatus("CAN'T FIND IT"))).when(overwritingMethod)
                .findOverriddenMethod(ipsProject);

        ImageDescriptor imageDescriptor = methodDecorator.getImageDescriptor(overwritingMethod);

        assertThat(imageDescriptor, hasBaseImage(MethodDecorator.METHOD_IMAGE_NAME));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_AbstractOverwrite() throws CoreException {
        IIpsProject ipsProject = mock(IIpsProject.class);
        IMethod overwrittenMethod = mock(IMethod.class);
        IMethod overwritingMethod = mock(IMethod.class);
        when(overwritingMethod.getIpsProject()).thenReturn(ipsProject);
        when(overwritingMethod.findOverriddenMethod(ipsProject)).thenReturn(overwrittenMethod);
        when(overwritingMethod.isAbstract()).thenReturn(true);

        ImageDescriptor imageDescriptor = methodDecorator.getImageDescriptor(overwritingMethod);

        assertThat(imageDescriptor, hasBaseImage(MethodDecorator.METHOD_IMAGE_NAME));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.OVERRIDE, IDecoration.BOTTOM_RIGHT));
        assertThat(imageDescriptor, hasOverlay(OverlayIcons.ABSTRACT, IDecoration.TOP_RIGHT));
    }

    @Test
    public void testGetLabel_null() {
        assertThat(methodDecorator.getLabel(null), is(""));
    }

    @Test
    public void testGetLabel() {
        IMethod method = mock(IMethod.class);
        when(method.getName()).thenReturn("foo");
        when(method.getDatatype()).thenReturn("Bar");
        IParameter p1 = mock(IParameter.class);
        when(p1.getDatatype()).thenReturn("P1");
        IParameter p2 = mock(IParameter.class);
        when(p2.getDatatype()).thenReturn("P2");
        when(method.getParameters()).thenReturn(new IParameter[] { p1, p2 });

        String label = methodDecorator.getLabel(method);

        assertThat(label, is("foo(P1, P2) : Bar"));
    }

    @Test
    public void testGetLabel_NoParams() {
        IMethod method = mock(IMethod.class);
        when(method.getName()).thenReturn("foo");
        when(method.getDatatype()).thenReturn("Bar");
        when(method.getParameters()).thenReturn(new IParameter[0]);

        String label = methodDecorator.getLabel(method);

        assertThat(label, is("foo() : Bar"));
    }

    @Test
    public void testGetLabel_UsesUnqualifiedNames() {
        IMethod method = mock(IMethod.class);
        when(method.getName()).thenReturn("foo");
        when(method.getDatatype()).thenReturn("foo.bar.baz.Bar");
        IParameter p1 = mock(IParameter.class);
        when(p1.getDatatype()).thenReturn("a.b.P1");
        IParameter p2 = mock(IParameter.class);
        when(p2.getDatatype()).thenReturn("c.d.e.P2");
        when(method.getParameters()).thenReturn(new IParameter[] { p1, p2 });

        String label = methodDecorator.getLabel(method);

        assertThat(label, is("foo(P1, P2) : Bar"));
    }

}
