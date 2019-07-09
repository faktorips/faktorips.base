/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.functional.Function;
import org.faktorips.util.functional.Supplier;
import org.junit.Test;

public class SimpleOpenIpsObjectPartActionTest {

    @Test
    public void testUpdate() throws Exception {
        IWorkbenchAdapter workbenchAdapter = mock(IWorkbenchAdapter.class);
        ImageDescriptor fooImage = mock(ImageDescriptor.class);
        ImageDescriptor barImage = mock(ImageDescriptor.class);
        final IIpsObjectPartContainer foo = mockPart(workbenchAdapter, fooImage);
        final IIpsObjectPartContainer bar = mockPart(workbenchAdapter, barImage);
        SwitchableSupplier<IIpsObjectPartContainer> partSupplier = new SwitchableSupplier<IIpsObjectPartContainer>(foo);
        SimpleOpenIpsObjectPartAction<IIpsObjectPartContainer> action = new SimpleOpenIpsObjectPartAction<IIpsObjectPartContainer>(
                partSupplier, new Function<IIpsObjectPartContainer, String>() {

                    @Override
                    public String apply(IIpsObjectPartContainer t) {
                        return t == foo ? "foo" : "bar";
                    }
                });

        assertThat(action.getText(), is("foo"));
        assertThat(action.getToolTipText(), is("foo"));
        assertThat(action.getImageDescriptor(), is(fooImage));

        partSupplier.set(bar);

        assertThat(action.getText(), is("bar"));
        assertThat(action.getToolTipText(), is("bar"));
        assertThat(action.getImageDescriptor(), is(barImage));
    }

    @Test
    public void testUpdate_Null() throws Exception {
        IWorkbenchAdapter workbenchAdapter = mock(IWorkbenchAdapter.class);
        ImageDescriptor fooImage = mock(ImageDescriptor.class);
        final IIpsObjectPartContainer foo = mockPart(workbenchAdapter, fooImage);
        SwitchableSupplier<IIpsObjectPartContainer> partSupplier = new SwitchableSupplier<IIpsObjectPartContainer>(foo);
        SimpleOpenIpsObjectPartAction<IIpsObjectPartContainer> action = new SimpleOpenIpsObjectPartAction<IIpsObjectPartContainer>(
                partSupplier, new Function<IIpsObjectPartContainer, String>() {

                    @Override
                    public String apply(IIpsObjectPartContainer t) {
                        return t == foo ? "foo" : "bar";
                    }
                });

        assertThat(action.getText(), is("foo"));
        assertThat(action.getToolTipText(), is("foo"));
        assertThat(action.getImageDescriptor(), is(fooImage));

        partSupplier.set(null);

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

    private static final class SwitchableSupplier<T> implements Supplier<T> {

        private T value;

        public SwitchableSupplier(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }

    }

}
