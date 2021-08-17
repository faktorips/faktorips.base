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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Filters {@link UUID UUIDs} in the form created by {@link UUID#toString()} from the wrapped
 * {@link InputStream}.
 */
public class UUIDFilterStream extends FilterInputStream {

    private LinkedList<Integer> potentialUUID = new LinkedList<>();

    public UUIDFilterStream(InputStream in) {
        super(in);
    }

    // CSOFF: CyclomaticComplexity
    // CSOFF: BooleanExpressionComplexity
    // Yes, those are a lot of if cases. But you wouldn't be able to understand them better if they
    // were spread out over half a dozen methods...
    @Override
    public int read() throws IOException {
        if (!potentialUUID.isEmpty()) {
            return potentialUUID.removeFirst();
        }
        int c = super.read();
        // @formatter:off
        // ' id="d09e8d9a-748a-48e1-a2cd-905bd7124106"'
        while (c != -1
                && potentialUUID.size() <= 42
                && (c == ' ' && potentialUUID.isEmpty()
                || c == 'i' && potentialUUID.size() == 1
                || c == 'd' && potentialUUID.size() == 2
                || c == '=' && potentialUUID.size() == 3
                || c == '"' && potentialUUID.size() == 4
                || (potentialUUID.size() > 4 && potentialUUID.size() < 41)
                || c == '"' && potentialUUID.size() == 41
                        )
                ) {
            // @formatter:on
            potentialUUID.offer(c);
            c = super.read();
        }
        if (potentialUUID.size() == 42) {
            potentialUUID.clear();
        }

        if (!potentialUUID.isEmpty()) {
            potentialUUID.offer(c);
            c = potentialUUID.removeFirst();
        }
        return c;
    }

    // CSON: BooleanExpressionComplexity
    // CSON: CyclomaticComplexity

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /*
     * copied from {@link InputStream#read(byte[], int, int)}, because we don't want to use the
     * wrapped {@link InputStream InputStream's} implementation which we might have to call multiple
     * times and build a new buffer with the contents of the returned buffers without the UUIDs.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte)c;
            }
            // CSOFF: EmptyBlock
        } catch (IOException ee) {
            // InputStream#read(byte[], int, int) ignores it, so how bad can it be...
        }
        // CSON: EmptyBlock
        return i;
    }

}
