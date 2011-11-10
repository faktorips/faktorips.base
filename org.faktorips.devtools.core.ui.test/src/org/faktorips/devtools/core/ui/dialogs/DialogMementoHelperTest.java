/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.dialogs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.memento.Memento;
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

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

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
