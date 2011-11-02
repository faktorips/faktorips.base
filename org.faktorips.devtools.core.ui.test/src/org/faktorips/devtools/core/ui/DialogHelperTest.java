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

package org.faktorips.devtools.core.ui;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jface.window.Window;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.util.memento.Memento;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DialogHelperTest {

    @Mock
    private EditDialog editDialog;

    @Mock
    private Memento memento;

    @Mock
    private IIpsSrcFile ipsSrcFile;

    @Mock
    private IIpsObjectPart editedPart;

    private DialogHelper dialogHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(editedPart.getIpsSrcFile()).thenReturn(ipsSrcFile);
        when(ipsSrcFile.isMutable()).thenReturn(true);
        when(editedPart.newMemento()).thenReturn(memento);

        dialogHelper = new DialogHelper();
    }

    @Test
    public void testOpenEditDialogWithMemento_RestoreMementoOnCancel() {
        when(editDialog.getReturnCode()).thenReturn(Window.CANCEL);

        dialogHelper.openEditDialogWithMemento(editDialog, editedPart);

        InOrder inOrder = inOrder(editDialog, editedPart);
        inOrder.verify(editDialog).open();
        inOrder.verify(editedPart).setState(memento);
    }

    @Test
    public void testOpenEditDialogWithMemento_MarkAsCleanIfWasCleanBefore() {
        when(editDialog.getReturnCode()).thenReturn(Window.CANCEL);
        when(ipsSrcFile.isDirty()).thenReturn(false);

        dialogHelper.openEditDialogWithMemento(editDialog, editedPart);

        verify(ipsSrcFile).markAsClean();
    }

    @Test
    public void testOpenEditDialogWithMemento_DoNotMarkAsCleanIfWasNotCleanBefore() {
        when(editDialog.getReturnCode()).thenReturn(Window.CANCEL);
        when(ipsSrcFile.isDirty()).thenReturn(true);

        dialogHelper.openEditDialogWithMemento(editDialog, editedPart);

        verify(ipsSrcFile, never()).markAsClean();
    }

    @Test
    public void testOpenEditDialogWithMemento_DoNotRestoreMementoIfSrcFileImmutable() {
        when(editDialog.getReturnCode()).thenReturn(Window.CANCEL);
        when(ipsSrcFile.isMutable()).thenReturn(false);

        dialogHelper.openEditDialogWithMemento(editDialog, editedPart);

        verify(editedPart, never()).setState(memento);
    }

    @Test
    public void testOpenEditDialogWithMemento_DoNotRestoreMementoOnReturnCodeOK() {
        when(editDialog.getReturnCode()).thenReturn(Window.OK);

        dialogHelper.openEditDialogWithMemento(editDialog, editedPart);

        verify(editedPart, never()).setState(memento);
    }

}
