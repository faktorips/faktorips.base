/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.fl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.fl.IFlIdentifierFilterExtension;
import org.faktorips.devtools.model.fl.IdentifierFilter;
import org.faktorips.devtools.model.fl.IdentifierKind;
import org.faktorips.devtools.model.type.IAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IdentifierFilterTest {

    @Mock
    private IAttribute attribute;

    @Mock
    private IFlIdentifierFilterExtension flIdentifierExtension;

    private IdentifierFilter filter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        List<IFlIdentifierFilterExtension> flIdentifierExtensions = new ArrayList<>();
        flIdentifierExtensions.add(flIdentifierExtension);
        filter = new IdentifierFilter(flIdentifierExtensions);
    }

    @Test
    public void testIsIdentifierAllowed() throws Exception {
        when(flIdentifierExtension.isIdentifierAllowed(attribute, IdentifierKind.ATTRIBUTE)).thenReturn(false);
        when(flIdentifierExtension.isIdentifierAllowed(attribute, IdentifierKind.DEFAULT_IDENTIFIER)).thenReturn(true);
        assertFalse(filter.isIdentifierAllowed(attribute, IdentifierKind.ATTRIBUTE));
        assertTrue(filter.isIdentifierAllowed(attribute, IdentifierKind.DEFAULT_IDENTIFIER));

        when(flIdentifierExtension.isIdentifierAllowed(attribute, IdentifierKind.ATTRIBUTE)).thenReturn(true);
        assertTrue(filter.isIdentifierAllowed(attribute, IdentifierKind.ATTRIBUTE));

    }

}
