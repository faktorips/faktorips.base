/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class StreamUtilTest extends TestCase {

    public final void testCopy() throws Exception {
        helper(1, 1);
        helper(8, 7);
        helper(8, 8);
        helper(8, 9);
        helper(31, 8);
        helper(32, 8);
        helper(33, 8);
    }

    public void helper(int streamSize, int increment) throws IOException {
        byte[] bytes = new byte[streamSize];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 1;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ByteArrayInputStream copiedStream = StreamUtil.copy(bis, increment);
        int value = copiedStream.read();
        int counter = 0;
        while (value != -1) {
            assertEquals("At postion: " + counter, 1, value);
            value = copiedStream.read();
            counter++;
        }
        assertEquals(streamSize, counter);
        
    }
}
