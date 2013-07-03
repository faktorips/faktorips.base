/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.fl;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.faktorips.devtools.core.fl.IFlIdentifierFilterExtension;
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
        when(flIdentifierExtension.isIdentifierAllowed(attribute)).thenReturn(false);
        assertFalse(filter.isIdentifierAllowed(attribute));

        when(flIdentifierExtension.isIdentifierAllowed(attribute)).thenReturn(true);
        assertTrue(filter.isIdentifierAllowed(attribute));

    }

}
