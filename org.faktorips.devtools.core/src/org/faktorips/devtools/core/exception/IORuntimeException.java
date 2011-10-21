/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.exception;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

/**
 * Similar to the {@link CoreRuntimeException} the {@link IORuntimeException} wraps another
 * exception to throw it as an unchecked exception. In this case an {@link IOException} is wrapped.
 * Where before a {@link CoreException} was thrown with an {@link IOException} as a cause, now throw
 * an {@link IORuntimeException}. This will allow for a distinction between a few important
 * exception types.
 * 
 * @author Stefan Widaier, FaktorZehn AG
 */
public class IORuntimeException extends RuntimeException {
    private static final long serialVersionUID = 5921527192427878708L;

    public IORuntimeException(IOException cause) {
        super(cause);
    }

    public IORuntimeException(String message, IOException cause) {
        super(message, cause);
    }
}
