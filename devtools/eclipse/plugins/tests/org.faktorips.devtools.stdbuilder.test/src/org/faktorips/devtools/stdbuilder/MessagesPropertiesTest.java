/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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

        validationMessages.store(mock(OutputStream.class));
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

        validationMessages.store(mock(OutputStream.class));

        validationMessages.remove("key");
        assertTrue(validationMessages.isModified());
    }

    @Test
    public void testStoreNoTimestamp() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();
        validationMessages.remove("key");
        validationMessages.put("key", "message");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        validationMessages.store(out);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
        String firstLine = reader.readLine();
        assertThat(firstLine, is("key=message"));
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
    public void shouldNotBeModifiedAfterStore() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties();
        assertFalse(validationMessages.isModified());

        String messageText = "text";
        validationMessages.put("abc", messageText);

        OutputStream outputStream = mock(OutputStream.class);
        validationMessages.store(outputStream);

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
        validationMessages.put("bcd", "312");
        validationMessages.put("cde", "312");
        validationMessages.put("def", "312");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        validationMessages.store(outputStream);

        String propertyText = outputStream.toString();

        assertThat(propertyText, is("aaa0=123" + System.lineSeparator()
                + "aaa1=123" + System.lineSeparator()
                + "abc=312" + System.lineSeparator()
                + "bcd=312" + System.lineSeparator()
                + "cde=312" + System.lineSeparator()
                + "def=312" + System.lineSeparator()));
    }

}
