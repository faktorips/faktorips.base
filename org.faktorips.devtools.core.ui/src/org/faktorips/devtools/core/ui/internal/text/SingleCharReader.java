/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal.text;

import java.io.IOException;
import java.io.Reader;

/**
 * NOTE: This class is a copy of the corresponding internal Eclipse class. It is copied as the
 * class' package has changed from Eclipse version 3.2 to 3.3.
 */
public abstract class SingleCharReader extends Reader {

    @Override
    public abstract int read() throws IOException;

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        int end = off + len;
        for (int i = off; i < end; i++) {
            int ch = read();
            if (ch == -1) {
                if (i == off) {
                    return -1;
                }
                return i - off;
            }
            cbuf[i] = (char)ch;
        }
        return len;
    }

    @Override
    public boolean ready() throws IOException {
        return true;
    }

    /**
     * Returns the readable content as string.
     * 
     * @return the readable content as string
     * @exception IOException in case reading fails
     */
    public String getString() throws IOException {
        StringBuffer buf = new StringBuffer();
        int ch;
        while ((ch = read()) != -1) {
            buf.append((char)ch);
        }
        return buf.toString();
    }
}
