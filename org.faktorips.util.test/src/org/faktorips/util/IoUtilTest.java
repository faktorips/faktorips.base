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
