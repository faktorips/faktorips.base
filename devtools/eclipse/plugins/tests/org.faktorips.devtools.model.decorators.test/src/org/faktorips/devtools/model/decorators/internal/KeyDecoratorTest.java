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
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKey;
import org.junit.Test;

public class KeyDecoratorTest {

    private final KeyDecorator keyDecorator = new KeyDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = keyDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(KeyDecorator.TABLE_KEY)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = keyDecorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(keyDecorator.getDefaultImageDescriptor()));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor() {
        IKey key = mock(IKey.class);

        ImageDescriptor imageDescriptor = keyDecorator.getImageDescriptor(key);

        assertThat(imageDescriptor, is(descriptorOf(KeyDecorator.TABLE_KEY)));
    }

    @Test
    public void testGetImageDescriptor_Index() {
        IKey index = mock(IIndex.class);

        ImageDescriptor imageDescriptor = keyDecorator.getImageDescriptor(index);

        assertThat(imageDescriptor, is(descriptorOf(KeyDecorator.TABLE_KEY_NON_UNIQUE)));
    }

    @Test
    public void testGetImageDescriptor_UniqueIndex() {
        IIndex uniqueIndex = mock(IIndex.class);
        when(uniqueIndex.isUniqueKey()).thenReturn(true);

        ImageDescriptor imageDescriptor = keyDecorator.getImageDescriptor(uniqueIndex);

        assertThat(imageDescriptor, is(descriptorOf(KeyDecorator.TABLE_KEY)));
    }

    @Test
    public void testGetLabel_null() {
        assertThat(keyDecorator.getLabel(null), is(""));
    }

    @Test
    public void testGetLabel() {
        IKey key = mock(IKey.class);
        when(key.getName()).thenReturn("foo");

        String label = keyDecorator.getLabel(key);

        assertThat(label, is("foo"));
    }

}
