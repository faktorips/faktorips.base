/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
public class ContentChangeEventTest {

    @Mock
    private IIpsObjectPart part;

    @Mock
    private IIpsObjectPart partContainer;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsSrcFile srcFile;

    @Mock
    private IIpsObjectPart unrelatedPart;

    private ContentChangeEvent event;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(part.getParent()).thenReturn(partContainer);
        lenient().when(partContainer.getParent()).thenReturn(ipsObject);

        lenient().when(part.getIpsObject()).thenReturn(ipsObject);
        lenient().when(partContainer.getIpsObject()).thenReturn(ipsObject);

        lenient().when(part.getIpsSrcFile()).thenReturn(srcFile);
        lenient().when(partContainer.getIpsSrcFile()).thenReturn(srcFile);
        lenient().when(ipsObject.getIpsSrcFile()).thenReturn(srcFile);
    }

    @Test
    public void testIsAffectedPartIdentical() throws Exception {
        event = ContentChangeEvent.newPartRemovedEvent(part);

        assertTrue(event.isAffected(part));
    }

    @Test
    public void testIsAffectedUnrelatedPart() throws Exception {
        event = ContentChangeEvent.newPartRemovedEvent(part);

        assertFalse(event.isAffected(unrelatedPart));
    }

    @Test
    public void testIsAffectedParent() throws Exception {
        event = ContentChangeEvent.newPartRemovedEvent(part);

        assertTrue(event.isAffected(partContainer));
    }

    @Test
    public void testIsAffectedIpsObject() throws Exception {
        event = ContentChangeEvent.newPartRemovedEvent(part);

        assertTrue(event.isAffected(ipsObject));
    }

    @Test
    public void testIsAffectedIpsObject_partAdded() throws Exception {
        event = ContentChangeEvent.newPartAddedEvent(part);

        assertTrue(event.isAffected(ipsObject));
    }

    @Test
    public void testIsAffectedIpsObject_partChanged() throws Exception {
        event = ContentChangeEvent.newPartChangedEvent(part);

        assertTrue(event.isAffected(ipsObject));
    }

    @Test
    public void testIsAffectedIpsObject_partMoved() throws Exception {
        event = ContentChangeEvent.newPartsChangedPositionsChangedEvent(srcFile, new IIpsObjectPart[] { part });

        assertTrue(event.isAffected(ipsObject));
    }

    @Test
    public void testIsAffected_wholeContentChanged() throws Exception {
        event = ContentChangeEvent.newWholeContentChangedEvent(srcFile);

        assertTrue(event.isAffected(part));
        assertTrue(event.isAffected(partContainer));
        assertTrue(event.isAffected(ipsObject));
    }

    @Test
    public void testIsAffected_notWholeContentChanged() throws Exception {

        event = ContentChangeEvent.newPartChangedEvent(partContainer);

        assertFalse(event.isAffected(part));
        assertTrue(event.isAffected(partContainer));
        assertTrue(event.isAffected(ipsObject));
    }

}
