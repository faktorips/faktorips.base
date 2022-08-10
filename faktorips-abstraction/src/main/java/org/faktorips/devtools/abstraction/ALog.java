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
    public void log(IStatus status);

    /**
     * Logs the given core exception.
     */
    public default void log(CoreException e) {
        log(e.getStatus());
    }

    public void addLogListener(ALogListener listener);

    public void removeLogListener(ALogListener listener);

}