/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Extension of status that sets the correct plug-in id and provides some convenience constructors.
 * 
 * @author Jan Ortmann
 */
public class IpsStatus extends Status {

    /**
     * Creates a new error status based on the given {@code Throwable}.
     */
    public IpsStatus(Throwable throwable) {
        super(IStatus.ERROR, IpsPlugin.PLUGIN_ID, 0,
                throwable.getMessage() != null ? throwable.getMessage() : "", throwable); //$NON-NLS-1$
        // use the throwable's message as the error dialog makes no use of the throwable
    }

    /**
     * Creates a new error status based on the given message.
     */
    public IpsStatus(String msg) {
        super(IStatus.ERROR, IpsPlugin.PLUGIN_ID, 0, msg, null);
    }

    /**
     * Creates a new status with the provided severity and message.
     */
    public IpsStatus(int severity, String msg) {
        super(severity, IpsPlugin.PLUGIN_ID, 0, msg, null);
    }

    /**
     * Creates a new error status based on the given message and {@code Throwable}.
     */
    public IpsStatus(String msg, Throwable t) {
        super(IStatus.ERROR, IpsPlugin.PLUGIN_ID, 0, msg, t);
    }

    /**
     * Creates a new status.
     */
    public IpsStatus(int severity, String message, Throwable exception) {
        super(severity, IpsPlugin.PLUGIN_ID, 0, message, exception);
    }

    /**
     * Creates a new status.
     */
    public IpsStatus(int severity, int code, String message, Throwable exception) {
        super(severity, IpsPlugin.PLUGIN_ID, code, message, exception);
    }

}
