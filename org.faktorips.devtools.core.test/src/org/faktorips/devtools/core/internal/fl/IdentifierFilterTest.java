/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.fl;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.faktorips.devtools.core.fl.IFlIdentifierFilterExtension;
import org.faktorips.devtools.core.fl.IdentifierKind;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class IdentifierFilterTest extends TestCase {

    @Mock
    private IAttribute attribute;

    @Mock
    private IFlIdentifierFilterExtension flIdentifierExtension;

    private IdentifierFilter filter;

    @Override
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        List<IFlIdentifierFilterExtension> flIdentifierExtensions = new ArrayList<IFlIdentifierFilterExtension>();
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
