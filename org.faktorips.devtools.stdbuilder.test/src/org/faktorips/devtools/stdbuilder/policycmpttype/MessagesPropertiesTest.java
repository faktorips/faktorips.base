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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

public class MessagesPropertiesTest {

    @Test
    public void testPut() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();

        validationMessages.put("abc", "message");

        assertEquals("message", validationMessages.getMessage("abc"));
    }

    @Test
    public void shouldCallPropertiesRemove() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();

        validationMessages.put("abc", "message");
        validationMessages.remove("abc");

        assertNull(validationMessages.getMessage("abc"));
    }

    @Test
    public void shouldBeModifiedAfterSuccessfullPut() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();
        assertFalse(validationMessages.isModified());

        validationMessages.put("key", "message");
        assertTrue(validationMessages.isModified());

        validationMessages.store(mock(OutputStream.class), "");
        validationMessages.put("key", "message");
        assertFalse(validationMessages.isModified());

    }

    @Test
    public void shouldBeModifiedAfterSuccessfullRemove() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();
        assertFalse(validationMessages.isModified());

        validationMessages.remove("key");
        assertFalse(validationMessages.isModified());

        validationMessages.put("key", "message");

        validationMessages.store(mock(OutputStream.class), "");

        validationMessages.remove("key");
        assertTrue(validationMessages.isModified());
    }

    @Test
    public void shouldCallPropertyLoad() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();

        InputStream inputStream = mock(InputStream.class);
        validationMessages.load(inputStream);

        verify(inputStream).read(any(byte[].class));
        verify(inputStream).close();
    }

    @Test
    public void shouldNotBeModifiedAfterLoad() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();
        assertFalse(validationMessages.isModified());

        validationMessages.put("abc", "text");

        InputStream inputStream = mock(InputStream.class);
        validationMessages.load(inputStream);

        assertNull(validationMessages.getMessage("abc"));
        assertFalse(validationMessages.isModified());
    }

    @Test
    public void shouldCallPropertyStore() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();

        OutputStream outputStream = mock(OutputStream.class);
        String comments = "comments";
        validationMessages.store(outputStream, comments);

        verify(outputStream).write(any(byte[].class), anyInt(), anyInt());
        verify(outputStream).close();
    }

    @Test
    public void shouldNotBeModifiedAfterStore() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();
        assertFalse(validationMessages.isModified());

        String messageText = "text";
        validationMessages.put("abc", messageText);

        OutputStream outputStream = mock(OutputStream.class);
        validationMessages.store(outputStream, "comment");

        assertEquals(messageText, validationMessages.getMessage("abc"));
        assertFalse(validationMessages.isModified());
    }

    @Test
    public void shouldGetSizeFromProperties() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();

        validationMessages.put("abc", "123");
        validationMessages.put("abc", "312");
        validationMessages.put("xyz", "123");

        assertEquals(2, validationMessages.size());
    }

    @Test
    public void shouldSortPropertyKeys() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();

        validationMessages.put("aaa1", "123");
        validationMessages.put("aaa0", "123");
        validationMessages.put("abc", "312");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        validationMessages.store(outputStream, "");

        String propertyText = outputStream.toString();
        int pos0 = propertyText.indexOf("aaa0");
        int pos1 = propertyText.indexOf("aaa1");

        assertTrue(pos0 < pos1);
    }

}
