/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.exception;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * The {@link CoreRuntimeException} is intended to replace the {@link CoreException}. Checked
 * Exceptions should be removed from method signatures.
 * <p>
 * During the migration phase, to avoid CoreExceptions in client code, catch all
 * {@link CoreException CoreExceptions} thrown by framework/FIPS-Devtools code, create a
 * {@link CoreRuntimeException} and throw it instead. Use the
 * {@link CoreRuntimeException#CoreRuntimeException(CoreException)} constructor.
 * <p>
 * As the {@link CoreRuntimeException} is an (unchecked) {@link RuntimeException} it is not declared
 * by method signatures explicitly, which is exactly what we want to achieve. If one of your methods
 * throws a {@link CoreRuntimeException}, document that it does and in which cases this happens.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class CoreRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -2861599541541109752L;
    private final IStatus status;

    /**
     * Constructor that should be used when a {@link CoreException} needs to be wrapped into an
     * unchecked exception.
     */
    public CoreRuntimeException(CoreException cause) {
        super(cause.getMessage(), cause);
        status = cause.getStatus();
    }

    /**
     * Constructor that wraps a status in a {@link CoreRuntimeException} by creating a new
     * {@link CoreException} internally.
     */
    public CoreRuntimeException(IStatus status) {
        super(new CoreException(status));
        this.status = status;
    }

    public CoreRuntimeException(String message, CoreException cause) {
        super(message, cause);
        status = cause.getStatus();
    }

    /**
     * Constructor that should be used when replacing the creation of {@link CoreException
     * CoreExceptions}. Instead of the {@link CoreException} create a {@link CoreRuntimeException}
     * with the same error message.
     * <p>
     * For example this constructor could be used when operations on the model cannot continue due
     * to inconsistencies. E.g. when a component or type not found by qualified name and find()
     * returned <code>null</code>.
     */
    public CoreRuntimeException(String message) {
        super(message);
        status = null;
    }

    public CoreRuntimeException(String message, CoreRuntimeException cause) {
        super(message, cause);
        status = cause.getStatus();
    }

    public IStatus getStatus() {
        return status;
    }
}
