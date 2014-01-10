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

package org.faktorips.devtools.core.refactor;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
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
        verify(ipsSrcFile).setMemento(modification.getOriginalContent());

        verifyNoMoreInteractions(ipsSrcFile);
    }

    @Test
    public void testUndo_rename() throws Exception {
        IpsSrcFileModification modification = IpsSrcFileModification.createBeforeRename(ipsSrcFile, ipsSrcFile2);
        IpsSrcFileModification modificationSpy = spy(modification);
        doNothing().when(modificationSpy).move(ipsSrcFile2, ipsSrcFile);

        verify(ipsSrcFile).newMemento();

        when(ipsSrcFile.exists()).thenReturn(false);
        when(ipsSrcFile2.exists()).thenReturn(true);
        modificationSpy.undo();

        verify(ipsSrcFile2).discardChanges();
        verify(ipsSrcFile).exists();
        verify(ipsSrcFile).setMemento(modificationSpy.getOriginalContent());

        verifyNoMoreInteractions(ipsSrcFile);
    }

}
