/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.propertybuilder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class MessagesPropertiesTest {

    @Test
    public void testPut() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());

        validationMessages.put("abc", "message");

        assertThat(validationMessages.getMessage("abc"), is("message"));
    }

    @Test
    public void shouldCallPropertiesRemove() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());

        validationMessages.put("abc", "message");
        validationMessages.remove("abc");

        assertThat(validationMessages.getMessage("abc"), is(nullValue()));
    }

    @Test
    public void shouldBeModifiedAfterSuccessfullPut() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());
        assertThat(validationMessages.isModified(), is(false));

        validationMessages.put("key", "message");
        assertThat(validationMessages.isModified(), is(true));

        validationMessages.store(mock(OutputStream.class));
        validationMessages.put("key", "message");
        assertThat(validationMessages.isModified(), is(false));
    }

    @Test
    public void shouldBeModifiedAfterSuccessfullRemove() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());
        assertThat(validationMessages.isModified(), is(false));

        validationMessages.remove("key");
        assertThat(validationMessages.isModified(), is(false));

        validationMessages.put("key", "message");

        validationMessages.store(mock(OutputStream.class));

        validationMessages.remove("key");
        assertThat(validationMessages.isModified(), is(true));
    }

    @Test
    public void testStoreNoTimestamp() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());
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
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());

        InputStream inputStream = mock(InputStream.class);
        validationMessages.load(inputStream);

        verify(inputStream).read(any(byte[].class), any(int.class), any(int.class));
        verify(inputStream).close();
    }

    @Test
    public void shouldLoadAndStoreUTF8EncodedStrings() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());

        InputStream inputStream = new ByteArrayInputStream(
                "name_ä=A name with umlaut".getBytes(StandardCharsets.UTF_8));
        validationMessages.load(inputStream);

        assertThat(validationMessages.getMessage("name_ä"), is("A name with umlaut"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        validationMessages.store(outputStream);

        String string = outputStream.toString(StandardCharsets.UTF_8);
        assertThat(string, is("name_ä=A name with umlaut" + System.lineSeparator()));
    }

    @Test
    public void shouldNotBeModifiedAfterLoad() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());
        assertThat(validationMessages.isModified(), is(false));

        validationMessages.put("abc", "text");

        InputStream inputStream = new ByteArrayInputStream(
                "name_ä=A name with umlaut".getBytes(StandardCharsets.UTF_8));
        validationMessages.load(inputStream);

        assertThat(validationMessages.getMessage("abc"), is(nullValue()));
        assertThat(validationMessages.getMessage("name_ä"), is("A name with umlaut"));
        assertThat(validationMessages.isModified(), is(false));
    }

    @Test
    public void shouldNotBeModifiedAfterStore() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());
        assertThat(validationMessages.isModified(), is(false));

        String messageText = "text";
        validationMessages.put("abc", messageText);

        OutputStream outputStream = mock(OutputStream.class);
        validationMessages.store(outputStream);

        assertThat(validationMessages.getMessage("abc"), is(messageText));
        assertThat(validationMessages.isModified(), is(false));
    }

    @Test
    public void shouldGetSizeFromProperties() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());

        validationMessages.put("abc", "123");
        validationMessages.put("abc", "312");
        validationMessages.put("xyz", "123");

        assertThat(validationMessages.size(), is(2));
    }

    @Test
    public void shouldSortPropertyKeys() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());

        validationMessages.put("aaa1", "123");
        validationMessages.put("aaa0", "123");
        validationMessages.put("abc", "312");
        validationMessages.put("bcd", "312");
        validationMessages.put("cde", "312");
        validationMessages.put("def", "312");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        validationMessages.store(outputStream);

        String propertyText = outputStream.toString(StandardCharsets.UTF_8);

        assertThat(propertyText, is("aaa0=123" + System.lineSeparator()
                + "aaa1=123" + System.lineSeparator()
                + "abc=312" + System.lineSeparator()
                + "bcd=312" + System.lineSeparator()
                + "cde=312" + System.lineSeparator()
                + "def=312" + System.lineSeparator()));
    }

    @Test
    public void shouldLoadUmlautePropertyKeysAndValues() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());

        String umlaute = "Ä=Ä" + System.lineSeparator()
                + "Ö=Ö" + System.lineSeparator()
                + "Ü=Ü" + System.lineSeparator()
                + "ä=ä" + System.lineSeparator()
                + "ö=ö" + System.lineSeparator()
                + "ü=ü" + System.lineSeparator()
                + "ß=ß" + System.lineSeparator();
        InputStream inputStream = new ByteArrayInputStream(
                umlaute.getBytes(StandardCharsets.UTF_8));

        validationMessages.load(inputStream);

        assertThat(validationMessages.getMessage("Ä"), is("Ä"));
        assertThat(validationMessages.getMessage("Ö"), is("Ö"));
        assertThat(validationMessages.getMessage("Ü"), is("Ü"));
        assertThat(validationMessages.getMessage("ä"), is("ä"));
        assertThat(validationMessages.getMessage("ö"), is("ö"));
        assertThat(validationMessages.getMessage("ü"), is("ü"));
        assertThat(validationMessages.getMessage("ß"), is("ß"));
    }

    @Test
    public void shouldSaveUmlautePropertyKeysAndValues() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());

        validationMessages.put("ä", "ä");
        validationMessages.put("ö", "ö");
        validationMessages.put("ü", "ü");
        validationMessages.put("ß", "ß");
        validationMessages.put("Ä", "Ä");
        validationMessages.put("Ö", "Ö");
        validationMessages.put("Ü", "Ü");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        validationMessages.store(outputStream);

        String propertyText = outputStream.toString(StandardCharsets.UTF_8);

        assertThat(propertyText, is("Ä=Ä" + System.lineSeparator()
                + "Ö=Ö" + System.lineSeparator()
                + "Ü=Ü" + System.lineSeparator()
                + "ß=ß" + System.lineSeparator()
                + "ä=ä" + System.lineSeparator()
                + "ö=ö" + System.lineSeparator()
                + "ü=ü" + System.lineSeparator()));
    }

    @Test
    public void shouldLoadEmptyStream() throws Exception {
        MessagesProperties validationMessages = new MessagesProperties(System.lineSeparator());

        validationMessages.load(new ByteArrayInputStream(new byte[0]));

        assertThat(validationMessages.size(), is(0));
    }
}
