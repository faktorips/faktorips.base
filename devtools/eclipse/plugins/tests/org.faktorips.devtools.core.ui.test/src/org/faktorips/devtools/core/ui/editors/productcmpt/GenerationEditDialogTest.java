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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.ui.editors.productcmpt.GenerationEditDialog.GenerationEditDialogPMO;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class GenerationEditDialogTest {

    @Mock
    private IProductCmptGeneration generation;

    private GregorianCalendar calendar = new GregorianCalendar();
    private GregorianCalendar calendar2 = new GregorianCalendar(2000, 1, 1);

    private GenerationEditDialogPMO pmo;

    @Before
    public void setUp() {
        when(generation.getValidFrom()).thenReturn(calendar);
        pmo = new GenerationEditDialogPMO(generation);
    }

    @Test
    public void testPMO_isOkButtonEnabled_init() {
        when(generation.getValidFrom()).thenReturn(null);
        pmo = new GenerationEditDialogPMO(generation);
        assertFalse(pmo.isOkButtonEnabled());

        when(generation.getValidFrom()).thenReturn(calendar);
        pmo = new GenerationEditDialogPMO(generation);
        assertTrue(pmo.isOkButtonEnabled());
    }

    @Test
    public void testPMO_isOkButtonEnabled() {
        pmo.setValidFrom(calendar);
        assertTrue(pmo.isOkButtonEnabled());

        pmo.setValidFrom(null);
        assertFalse(pmo.isOkButtonEnabled());

        pmo.setValidFrom(calendar2);
        assertTrue(pmo.isOkButtonEnabled());
    }

    @Test
    public void testPMO_getValidFrom() {
        assertEquals(calendar, pmo.getValidFrom());
    }

    @Test
    public void testPMO_setValidFrom() {
        pmo.setValidFrom(calendar2);
        verify(generation).setValidFrom(calendar2);

        pmo.setValidFrom(calendar);
        verify(generation).setValidFrom(calendar);
    }

    @Test
    public void testPMO_setValidFromNull() {
        pmo.setValidFrom(calendar2);
        verify(generation).setValidFrom(calendar2);

        pmo.setValidFrom(null);
        verify(generation, never()).setValidFrom(null);
    }
}
