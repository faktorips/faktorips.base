/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.memento.Memento;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DialogMementoHelperTest {

    @Mock
    private Dialog dialog;

    @Mock
    private Memento memento;

    @Mock
    private IIpsSrcFile ipsSrcFile;

    @Mock
    private IIpsObjectPartContainer ipsObjectPartContainer;

    private DialogMementoHelper dialogMementoHelper;

    private AutoCloseable openMocks;

    @Before
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);

        when(ipsObjectPartContainer.getIpsSrcFile()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.isMutable()).thenReturn(true);
        when(ipsObjectPartContainer.newMemento()).thenReturn(memento);

        dialogMementoHelper = new DialogMementoHelper() {
            @Override
            protected Dialog createDialog() {
                return dialog;
            }
        };
    }

    @After
    public void releaseMocks() throws Exception {
        openMocks.close();
    }

    @Test
    public void testOpenDialogWithMemento_RestoreMementoOnCancel() {
        when(dialog.getReturnCode()).thenReturn(Window.CANCEL);

        dialogMementoHelper.openDialogWithMemento(ipsObjectPartContainer);

        InOrder inOrder = inOrder(dialog, ipsObjectPartContainer);
        inOrder.verify(dialog).open();
        inOrder.verify(ipsObjectPartContainer).setState(memento);
    }

    @Test
    public void testOpenDialogWithMemento_MarkAsCleanIfWasCleanBefore() {
        when(dialog.getReturnCode()).thenReturn(Window.CANCEL);
        when(ipsSrcFile.isDirty()).thenReturn(false);

        dialogMementoHelper.openDialogWithMemento(ipsObjectPartContainer);

        verify(ipsSrcFile).markAsClean();
    }

    @Test
    public void testOpenDialogWithMemento_DoNotMarkAsCleanIfWasNotCleanBefore() {
        when(dialog.getReturnCode()).thenReturn(Window.CANCEL);
        when(ipsSrcFile.isDirty()).thenReturn(true);

        dialogMementoHelper.openDialogWithMemento(ipsObjectPartContainer);

        verify(ipsSrcFile, never()).markAsClean();
    }

    @Test
    public void testOpenDialogWithMemento_DoNotRestoreMementoIfSrcFileImmutable() {
        when(dialog.getReturnCode()).thenReturn(Window.CANCEL);
        when(ipsSrcFile.isMutable()).thenReturn(false);

        dialogMementoHelper.openDialogWithMemento(ipsObjectPartContainer);

        verify(ipsObjectPartContainer, never()).setState(memento);
    }

    @Test
    public void testOpenDialogWithMemento_DoNotRestoreMementoOnReturnCodeOK() {
        when(dialog.getReturnCode()).thenReturn(Window.OK);

        dialogMementoHelper.openDialogWithMemento(ipsObjectPartContainer);

        verify(ipsObjectPartContainer, never()).setState(memento);
    }

    @Test
    public void testOpenDialogWithMemento_ReturnDialogReturnCode() {
        when(dialog.getReturnCode()).thenReturn(123456);
        assertEquals(123456, dialogMementoHelper.openDialogWithMemento(ipsObjectPartContainer));
    }

}
