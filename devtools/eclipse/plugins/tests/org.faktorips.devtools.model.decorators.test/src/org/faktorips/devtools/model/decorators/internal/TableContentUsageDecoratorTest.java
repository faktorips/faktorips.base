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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.junit.Test;

public class TableContentUsageDecoratorTest {

    private final TableContentUsageDecorator tableContentUsageDecorator = new TableContentUsageDecorator();

    @Test
    public void testGetDefaultImageDescriptor() {
        ImageDescriptor defaultImageDescriptor = tableContentUsageDecorator.getDefaultImageDescriptor();

        assertThat(defaultImageDescriptor, is(descriptorOf(TableContentUsageDecorator.TABLE_CONTENTS_USAGE_IMAGE)));
        assertThat(defaultImageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor_Null() {
        ImageDescriptor imageDescriptor = tableContentUsageDecorator.getImageDescriptor(null);

        assertThat(imageDescriptor, is(tableContentUsageDecorator.getDefaultImageDescriptor()));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetImageDescriptor() {
        ITableContentUsage tableContentUsage = mock(ITableContentUsage.class);

        ImageDescriptor imageDescriptor = tableContentUsageDecorator.getImageDescriptor(tableContentUsage);

        assertThat(imageDescriptor, is(tableContentUsageDecorator.getDefaultImageDescriptor()));
        assertThat(imageDescriptor, hasNoOverlay());
    }

    @Test
    public void testGetLabel_null() {
        assertThat(tableContentUsageDecorator.getLabel(null), is(IpsStringUtils.EMPTY));
    }

    @Test
    public void testGetLabel() {
        ITableContentUsage tableContentUsage = mock(ITableContentUsage.class);
        when(tableContentUsage.getCaption(any(Locale.class))).thenReturn("Foo");
        when(tableContentUsage.getTableContentName()).thenReturn("baz.Bar");

        String label = tableContentUsageDecorator.getLabel(tableContentUsage);

        assertThat(label, is("Foo: Bar"));
    }

}
