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
