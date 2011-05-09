/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.helper.IoHandler;

public class TestUtil {

    public IoHandler createMockIoHandler() {
        IoHandler ioHandler = mock(IoHandler.class);
        try {
            when(ioHandler.readFile(anyString(), anyString())).thenReturn(new byte[0]);
        } catch (IOException e) {
            // should not happen
            throw new RuntimeException(e);
        }
        return ioHandler;
    }

    public DocumentationContext createMockDocumentationContext() {
        return mock(DocumentationContext.class);
    }
}
