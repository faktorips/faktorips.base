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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Ignore;
import org.junit.Test;

public class UUIDFilterStreamTest {

    @Test
    public void testRead() throws Exception {
        byte[] input = "<Description id=\"a26d4eb0-21b1-4b4a-920c-157d49d5ffd1\" locale=\"en\"/>\n".getBytes("UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        UUIDFilterStream uuidFilterStream = new UUIDFilterStream(bis);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while (-1 != (b = uuidFilterStream.read())) {
            bos.write(b);
        }

        assertThat(bos.toString("UTF-8"), is(equalTo("<Description locale=\"en\"/>\n")));
        uuidFilterStream.close();
    }

    @Test
    public void testReadByteArray() throws Exception {
        byte[] input = "<Description id=\"a8d15b0b-517f-4203-8618-ef39b0466893\" locale=\"fr\"/>\n".getBytes("UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        UUIDFilterStream uuidFilterStream = new UUIDFilterStream(bis);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[10];
        int l;
        while (-1 != (l = uuidFilterStream.read(b))) {
            bos.write(b, 0, l);
        }

        assertThat(bos.toString("UTF-8"), is(equalTo("<Description locale=\"fr\"/>\n")));
        uuidFilterStream.close();
    }

    @Test
    public void testReadByteArrayIntInt() throws Exception {
        byte[] input = "<Generation id=\"c92ad3f4-f4ec-4425-bf45-f6bca0728fec\" validFrom=\"2012-03-29\"/>\n"
                .getBytes("UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        UUIDFilterStream uuidFilterStream = new UUIDFilterStream(bis);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[10];
        int l;
        while (-1 != (l = uuidFilterStream.read(b, 2, 5))) {
            bos.write(b, 2, l);
        }

        assertThat(bos.toString("UTF-8"), is(equalTo("<Generation validFrom=\"2012-03-29\"/>\n")));
        uuidFilterStream.close();
    }

    @Test
    public void testRead_filterEvenIllegalIdOfCorrectLength() throws Exception {
        // Warum? Weil wir nicht 37 Character auf [a-f0-9] etc. prüfen, nur weil jemand manuell
        // Schindluder treiben könnte.
        byte[] input = "<Description id=\"Keine UUID, die Form passt aber ;-P \" locale=\"en\"/>\n".getBytes("UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        UUIDFilterStream uuidFilterStream = new UUIDFilterStream(bis);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while (-1 != (b = uuidFilterStream.read())) {
            bos.write(b);
        }

        assertThat(bos.toString("UTF-8"), is(equalTo("<Description locale=\"en\"/>\n")));
        uuidFilterStream.close();
    }

    @Test
    public void testRead_ignoreRuntimeId() throws Exception {
        byte[] input = "<ProductCmpt productCmptType=\"ElementarProdukt\" runtimeId=\"c92ad3f4-f4ec-4425-bf45-f6bca0728fec\" template=\"\" validFrom=\"2012-03-29\" xml:space=\"preserve\">\n"
                .getBytes("UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        UUIDFilterStream uuidFilterStream = new UUIDFilterStream(bis);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while (-1 != (b = uuidFilterStream.read())) {
            bos.write(b);
        }

        assertThat(
                bos.toString("UTF-8"),
                is(equalTo(
                        "<ProductCmpt productCmptType=\"ElementarProdukt\" runtimeId=\"c92ad3f4-f4ec-4425-bf45-f6bca0728fec\" template=\"\" validFrom=\"2012-03-29\" xml:space=\"preserve\">\n")));
        uuidFilterStream.close();
    }

    @Test
    public void testRead_ignoreShort40CharId() throws Exception {
        byte[] input = "<Description id=\"1234567890123456789012345678901234567890\" locale=\"en\"/>\n"
                .getBytes("UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        UUIDFilterStream uuidFilterStream = new UUIDFilterStream(bis);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while (-1 != (b = uuidFilterStream.read())) {
            bos.write(b);
        }

        assertThat(bos.toString("UTF-8"),
                is(equalTo("<Description id=\"1234567890123456789012345678901234567890\" locale=\"en\"/>\n")));
        uuidFilterStream.close();
    }

    @Test
    public void testRead_ignoreLong50CharId() throws Exception {
        byte[] input = "<Description id=\"12345678901234567890123456789012345678901234567890\" locale=\"en\"/>\n"
                .getBytes("UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        UUIDFilterStream uuidFilterStream = new UUIDFilterStream(bis);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while (-1 != (b = uuidFilterStream.read())) {
            bos.write(b);
        }

        assertThat(bos.toString("UTF-8"),
                is(equalTo(
                        "<Description id=\"12345678901234567890123456789012345678901234567890\" locale=\"en\"/>\n")));
        uuidFilterStream.close();
    }

    // ignored because checking if we actually are within an XML tag's values would be too much
    // effort
    @Ignore
    @Test
    public void testRead_ignoreNotAttribute() throws Exception {
        byte[] input = "<Value> id=\"d09e8d9a-748a-48e1-a2cd-905bd7124106\"</Value>\n".getBytes("UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        UUIDFilterStream uuidFilterStream = new UUIDFilterStream(bis);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while (-1 != (b = uuidFilterStream.read())) {
            bos.write(b);
        }

        assertThat(bos.toString("UTF-8"), is(equalTo("<Value> id=\"d09e8d9a-748a-48e1-a2cd-905bd7124106\"</Value>\n")));
        uuidFilterStream.close();
    }

}
