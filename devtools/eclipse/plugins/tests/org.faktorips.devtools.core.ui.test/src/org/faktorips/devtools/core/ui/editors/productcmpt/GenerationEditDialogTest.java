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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.GregorianCalendar;

import org.faktorips.devtools.core.ui.editors.productcmpt.GenerationEditDialog.GenerationEditDialogPMO;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.util.memento.Memento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class GenerationEditDialogTest {


    @Mock
    private IProductCmptGeneration generation;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsSrcFile ipsSrcFile;

    @Mock
    private Memento memento;

    private MockitoSession mockito;

    private GregorianCalendar calendar = new GregorianCalendar();
    private GregorianCalendar calendar2 = new GregorianCalendar(2000, 1, 1);

    private GenerationEditDialogPMO pmo;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);
        when(generation.getValidFrom()).thenReturn(calendar);
        pmo = new GenerationEditDialogPMO(generation);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testRestoreStateOnAbortion_newGeneration() {
        stubIpsObject();
        GenerationEditDialog dialog = new GenerationEditDialog(generation, null, true);

        assertFalse(dialog.restoreStateOnAbortion());
    }

    @Test
    public void testRestoreStateOnAbortion_editGeneration() {
        stubIpsObject();
        GenerationEditDialog dialog = new GenerationEditDialog(generation, null, false);

        assertTrue(dialog.restoreStateOnAbortion());
    }

    @Test
    public void testAbortion_editGeneration_restoresState() {
        stubIpsObject();
        AbortableGenerationEditDialog dialog = new AbortableGenerationEditDialog(generation, false);

        dialog.abort();

        verify(ipsObject).setState(memento);
    }

    @Test
    public void testAbortion_editGeneration_marksSrcFileAsCleanIfNotDirtyBefore() {
        stubIpsObject();
        AbortableGenerationEditDialog dialog = new AbortableGenerationEditDialog(generation, false);

        dialog.abort();

        verify(ipsSrcFile).markAsClean();
    }

    @Test
    public void testAbortion_editGeneration_keepsSrcFileDirtyIfDirtyBefore() {
        stubIpsObject();
        when(ipsSrcFile.isDirty()).thenReturn(true);
        AbortableGenerationEditDialog dialog = new AbortableGenerationEditDialog(generation, false);

        dialog.abort();

        verify(ipsObject).setState(memento);
        verify(ipsSrcFile, never()).markAsClean();
    }

    @Test
    public void testAbortion_newGeneration_doesNotRestoreState() {
        stubIpsObject();
        AbortableGenerationEditDialog dialog = new AbortableGenerationEditDialog(generation, true);

        dialog.abort();

        verify(ipsObject, never()).setState(any());
        verify(ipsSrcFile, never()).markAsClean();
    }

    private void stubIpsObject() {
        when(generation.getIpsObject()).thenReturn(ipsObject);
        when(ipsObject.newMemento()).thenReturn(memento);
        when(ipsObject.getIpsSrcFile()).thenReturn(ipsSrcFile);
    }

    private static class AbortableGenerationEditDialog extends GenerationEditDialog {

        AbortableGenerationEditDialog(IProductCmptGeneration generation, boolean newGenerationDialog) {
            super(generation, null, newGenerationDialog);
        }

        void abort() {
            handleShellCloseEvent();
        }
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
