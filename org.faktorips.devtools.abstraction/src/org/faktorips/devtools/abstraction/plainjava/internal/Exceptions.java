/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

public class Exceptions {

    private Exceptions() {
        // util
    }

    public static CoreRuntimeException asCoreRuntimeException(String message, Exception cause) {
        return new CoreRuntimeException(new Status(IStatus.ERROR, Abstractions.getImplementationId(), message, cause));
    }
}
