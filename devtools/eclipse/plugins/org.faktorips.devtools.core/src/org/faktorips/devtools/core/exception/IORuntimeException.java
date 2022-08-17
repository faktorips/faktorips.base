/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.exception;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.abstraction.exception.IpsException;

/**
 * Similar to the {@link IpsException} the {@link IORuntimeException} wraps another exception to
 * throw it as an unchecked exception. In this case an {@link IOException} is wrapped. Where before
 * a {@link CoreException} was thrown with an {@link IOException} as a cause, now throw an
 * {@link IORuntimeException}. This will allow for a distinction between a few important exception
 * types.
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
