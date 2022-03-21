/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.abstraction.exception;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * The {@link IpsException} is intended to replace Eclipse's {@link CoreException}. Checked
 * Exceptions should be removed from method signatures.
 * <p>
 * During the migration phase, to avoid CoreExceptions in client code, catch all
 * {@link CoreException CoreExceptions} thrown by framework/FIPS-Devtools code, create a
 * {@link IpsException} and throw it instead. Use the
 * {@link IpsException#IpsException(CoreException)} constructor.
 * <p>
 * As the {@link IpsException} is an (unchecked) {@link RuntimeException} it is not declared by
 * method signatures explicitly, which is exactly what we want to achieve. If one of your methods
 * throws a {@link IpsException}, document that it does and in which cases this happens.
 * 
 * @since 22.6, replacing org.faktorips.devtools.model.exception.CoreRuntimeException
 */
public class IpsException extends RuntimeException {

    private static final String ORG_FAKTORIPS_DEVTOOLS_MODEL = "org.faktorips.devtools.model"; //$NON-NLS-1$
    private static final long serialVersionUID = -2861599541541109752L;
    private final IStatus status;

    /**
     * Constructor that should be used when a {@link CoreException} needs to be wrapped into an
     * unchecked exception.
     */
    public IpsException(CoreException cause) {
        super(cause.getMessage(), cause);
        status = cause.getStatus();
    }

    /**
     * Constructor that wraps a status in a {@link IpsException} by creating a new
     * {@link CoreException} internally.
     */
    public IpsException(IStatus status) {
        super(new CoreException(status));
        this.status = status;
    }

    public IpsException(String message, CoreException cause) {
        super(message, cause);
        status = cause.getStatus();
    }

    public IpsException(String message, Throwable cause) {
        this(new Status(IStatus.ERROR, ORG_FAKTORIPS_DEVTOOLS_MODEL, message, cause));
    }

    /**
     * Constructor that should be used when replacing the creation of {@link CoreException
     * CoreExceptions}. Instead of the {@link CoreException} create an {@link IpsException} with the
     * same error message.
     * <p>
     * For example this constructor could be used when operations on the model cannot continue due
     * to inconsistencies. E.g. when a component or type not found by qualified name and find()
     * returned {@code null}.
     */
    public IpsException(String message) {
        super(message);
        status = new Status(IStatus.ERROR, ORG_FAKTORIPS_DEVTOOLS_MODEL, message);
    }

    public IpsException(String message, IpsException cause) {
        super(message, cause);
        status = cause.getStatus();
    }

    public IStatus getStatus() {
        return status;
    }
}
