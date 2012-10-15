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

package org.faktorips.devtools.core.refactor;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsSrcFileModificationTest {

    @Mock
    private IIpsSrcFile ipsSrcFile;

    @Mock
    private IIpsSrcFile ipsSrcFile2;

    @Test
    public void testUndo_modifyNotExisting() throws Exception {
        IpsSrcFileModification modification = IpsSrcFileModification.createBeforeModification(ipsSrcFile);
        verify(ipsSrcFile).newMemento();

        modification.undo();

        verify(ipsSrcFile).exists();
        verifyNoMoreInteractions(ipsSrcFile);
    }

    @Test
    public void testUndo_modifyExisting() throws Exception {
        when(ipsSrcFile.exists()).thenReturn(true);
        IpsSrcFileModification modification = IpsSrcFileModification.createBeforeModification(ipsSrcFile);
        verify(ipsSrcFile).newMemento();

        modification.undo();

        verify(ipsSrcFile).exists();
        verify(ipsSrcFile).discardChanges();
        verify(ipsSrcFile).setMemento(modification.getOriginalContent());

        verifyNoMoreInteractions(ipsSrcFile);
    }

    @Test
    @Ignore
    // TODO using static refactoring util, cannot test currently
    public void testUndo_rename() throws Exception {
        IpsSrcFileModification modification = IpsSrcFileModification.createBeforeRename(ipsSrcFile, ipsSrcFile2);
        verify(ipsSrcFile).newMemento();

        when(ipsSrcFile.exists()).thenReturn(false);
        when(ipsSrcFile2.exists()).thenReturn(true);
        modification.undo();

        verify(ipsSrcFile).exists();
        verify(ipsSrcFile).discardChanges();
        verify(ipsSrcFile).setMemento(modification.getOriginalContent());

        verifyNoMoreInteractions(ipsSrcFile);
    }

}
