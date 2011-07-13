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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.junit.Test;

public class MessagesPropertiesTest {

    @Test
    public void shouldCallPropertiesPut() throws Exception {
        Properties properties = mock(Properties.class);

        MessagesProperties validationMessages = new MessagesProperties(properties);

        validationMessages.put("abc", "message");

        verify(properties).put("abc", "message");
    }

    @Test
    public void shouldCallPropertiesRemove() throws Exception {
        Properties properties = mock(Properties.class);

        MessagesProperties validationMessages = new MessagesProperties(properties);
        validationMessages.remove("abc");

        verify(properties).remove("abc");
    }

    @Test
    public void shouldGetPropertiesElement() throws Exception {
        Properties properties = mock(Properties.class);
        String key = "abc";
        when(properties.getProperty(key)).thenReturn("xyz");

        MessagesProperties validationMessages = new MessagesProperties(properties);
        assertEquals("xyz", validationMessages.getMessage(key));
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
        Properties properties = mock(Properties.class);
        MessagesProperties validationMessages = new MessagesProperties(properties);

        InputStream inputStream = mock(InputStream.class);
        validationMessages.load(inputStream);

        verify(properties).load(inputStream);
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
        Properties properties = mock(Properties.class);
        MessagesProperties validationMessages = new MessagesProperties(properties);

        OutputStream outputStream = mock(OutputStream.class);
        String comments = "comments";
        validationMessages.store(outputStream, comments);

        verify(properties).store(outputStream, comments);
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
        Properties properties = mock(Properties.class);
        MessagesProperties validationMessages = new MessagesProperties(properties);

        when(properties.size()).thenReturn(123456);

        assertEquals(123456, validationMessages.size());
    }

}
