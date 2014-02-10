/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.Test;

public class StreamUtilTest {
	
    @Test
    public void testCopy() throws Exception {
        byte[] bytes = new byte[8];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 1;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ByteArrayInputStream copiedStream = StreamUtil.copy(bis, 100);
        int value = copiedStream.read();
        int counter = 0;
        while (value != -1) {
            assertEquals("At postion: " + counter, 1, value);
            value = copiedStream.read();
            counter++;
        }
        assertEquals(8, counter);

        bytes = new byte[100];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 1;
        }
        bis = new ByteArrayInputStream(bytes);
        copiedStream = StreamUtil.copy(bis, 9);
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
        copiedStream = StreamUtil.copy(bis, 1);
        value = copiedStream.read();
        counter = 0;
        while (value != -1) {
            assertEquals("At postion: " + counter, 1, value);
            value = copiedStream.read();
            counter++;
        }
        assertEquals(1, counter);
    }

}
