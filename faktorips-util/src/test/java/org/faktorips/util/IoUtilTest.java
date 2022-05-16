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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.Closeable;
import java.io.IOException;

import org.junit.Test;

public class IoUtilTest {

    @Test
    public void close() throws IOException {
        Closeable closeable = mock(Closeable.class);
        IoUtil.close(closeable);
        verify(closeable).close();
    }

    @Test
    public void closeNullPointer() {
        IoUtil.close(null);
    }

}
