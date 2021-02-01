/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.helper.IoHandler;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class TestUtil {

    public IoHandler createMockIoHandler() {
        IoHandler ioHandler = mock(IoHandler.class);
        try {
            when(ioHandler.readFile(anyString())).thenReturn(new byte[0]);
        } catch (IOException e) {
            // should not happen
            throw new RuntimeException(e);
        }
        return ioHandler;
    }

    public DocumentationContext createMockDocumentationContext() {
        DocumentationContext context = mock(DocumentationContext.class);

        IIpsProject ipsProject = mock(IIpsProject.class);
        when(context.getIpsProject()).thenReturn(ipsProject);

        when(ipsProject.getName()).thenReturn("Project Name");

        return context;
    }
}
