/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class StreamUtilTest {

    @Test
    public void testCopy() throws Exception {
        byte[] bytes = new byte[8];
        Arrays.fill(bytes, (byte)1);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ByteArrayInputStream copiedStream = StreamUtil.copy(bis);
        int value = copiedStream.read();
        int counter = 0;
        while (value != -1) {
            assertEquals("At postion: " + counter, 1, value);
            value = copiedStream.read();
            counter++;
        }
        assertEquals(8, counter);

        bytes = new byte[100];
        Arrays.fill(bytes, (byte)1);
        bis = new ByteArrayInputStream(bytes);
        copiedStream = StreamUtil.copy(bis);
        value = copiedStream.read();
        counter = 0;
        while (value != -1) {
            assertEquals("At postion: " + counter, 1, value);
            value = copiedStream.read();
            counter++;
        }
        assertEquals(100, counter);

        bytes = new byte[] { 1 };
        bis = new ByteArrayInputStream(bytes);
        copiedStream = StreamUtil.copy(bis);
        value = copiedStream.read();
        counter = 0;
        while (value != -1) {
            assertEquals("At postion: " + counter, 1, value);
            value = copiedStream.read();
            counter++;
        }
        assertEquals(1, counter);
    }

    @Test
    public void testCopyNull() throws Exception {
        assertNotNull(StreamUtil.copy(null));
    }

    @Test
    public void testStreamIsOpenAfterOriginalIsClosed() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("Foo".getBytes());
        ByteArrayInputStream copy = StreamUtil.copy(in);
        in.close();
        assertEquals('F', copy.read());
        assertEquals('o', copy.read());
        assertEquals('o', copy.read());
        assertEquals(-1, copy.read());
    }

}
