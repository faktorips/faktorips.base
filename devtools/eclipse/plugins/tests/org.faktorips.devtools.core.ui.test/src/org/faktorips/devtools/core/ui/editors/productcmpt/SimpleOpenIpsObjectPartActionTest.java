/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.junit.Test;

public class SimpleOpenIpsObjectPartActionTest {

    @Test
    public void testUpdate() throws Exception {
        IWorkbenchAdapter workbenchAdapter = mock(IWorkbenchAdapter.class);
        ImageDescriptor fooImage = mock(ImageDescriptor.class);
        ImageDescriptor barImage = mock(ImageDescriptor.class);
        final IIpsObjectPartContainer foo = mockPart(workbenchAdapter, fooImage);
        final IIpsObjectPartContainer bar = mockPart(workbenchAdapter, barImage);
        Supplier<IIpsObjectPartContainer> partSupplier = () -> foo;
        SimpleOpenIpsObjectPartAction<IIpsObjectPartContainer> action = new SimpleOpenIpsObjectPartAction<>(
                partSupplier, container -> container == foo ? "foo" : "bar");

        assertThat(action.getText(), is("foo"));
        assertThat(action.getToolTipText(), is("foo"));
        assertThat(action.getImageDescriptor(), is(fooImage));

        action.setPartSupplier(() -> bar);

        assertThat(action.getText(), is("bar"));
        assertThat(action.getToolTipText(), is("bar"));
        assertThat(action.getImageDescriptor(), is(barImage));
    }

    @Test
    public void testUpdate_Null() throws Exception {
        IWorkbenchAdapter workbenchAdapter = mock(IWorkbenchAdapter.class);
        ImageDescriptor fooImage = mock(ImageDescriptor.class);
        final IIpsObjectPartContainer foo = mockPart(workbenchAdapter, fooImage);
        Supplier<IIpsObjectPartContainer> partSupplier = () -> foo;
        SimpleOpenIpsObjectPartAction<IIpsObjectPartContainer> action = new SimpleOpenIpsObjectPartAction<>(
                partSupplier, container -> container == foo ? "foo" : "bar");

        assertThat(action.getText(), is("foo"));
        assertThat(action.getToolTipText(), is("foo"));
        assertThat(action.getImageDescriptor(), is(fooImage));

        action.setPartSupplier(() -> null);

        assertThat(action.getText(), is(""));
        assertThat(action.getToolTipText(), is(""));
        assertThat(action.getImageDescriptor(), is(nullValue()));
    }

    private IIpsObjectPartContainer mockPart(IWorkbenchAdapter workbenchAdapter, ImageDescriptor image) {
        final IIpsObjectPartContainer foo = mock(IIpsObjectPartContainer.class);
        IIpsObject fooObject = mock(IIpsObject.class);
        when(foo.getIpsObject()).thenReturn(fooObject);
        when(workbenchAdapter.getImageDescriptor(fooObject)).thenReturn(image);
        when(fooObject.getAdapter(IWorkbenchAdapter.class)).thenReturn(workbenchAdapter);
        return foo;
    }
}
