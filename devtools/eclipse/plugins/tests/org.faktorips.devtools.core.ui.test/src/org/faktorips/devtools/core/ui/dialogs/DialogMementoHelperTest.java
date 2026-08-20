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

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.memento.Memento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class DialogMementoHelperTest {

    @Mock
    private Dialog dialog;

    @Mock
    private Memento memento;

    @Mock
    private IIpsSrcFile ipsSrcFile;

    @Mock
    private IIpsObjectPartContainer ipsObjectPartContainer;

    private MockitoSession mockito;

    private DialogMementoHelper dialogMementoHelper;

    @BeforeEach
    public void setUp() {
        mockito = createMocks(this);

        when(ipsObjectPartContainer.getIpsSrcFile()).thenReturn(ipsSrcFile);
        lenient().when(ipsSrcFile.isMutable()).thenReturn(true);
        when(ipsObjectPartContainer.newMemento()).thenReturn(memento);

        dialogMementoHelper = new DialogMementoHelper() {
            @Override
            protected Dialog createDialog() {
                return dialog;
            }
        };
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        mockito.finishMocking();
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
