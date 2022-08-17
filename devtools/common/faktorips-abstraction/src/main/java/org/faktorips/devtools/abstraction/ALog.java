/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

public interface ALog extends AAbstraction {

    /**
     * Logs the status.
     */
    void log(IStatus status);

    /**
     * Logs the given core exception.
     */
    default void log(CoreException e) {
        log(e.getStatus());
    }

    void addLogListener(ALogListener listener);

    void removeLogListener(ALogListener listener);

}
