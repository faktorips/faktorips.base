/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.abstraction.ALogListener;

/**
 * An implementation of the {@link ILog} interface for testing purposes. Not all of the methods are
 * implemented but can be implemented as needed. Those methods that are not implemented throw a
 * RuntimeException.
 * 
 * @author Peter Erzberger
 */
public class TestLogger implements ALog {

    private List<IStatus> logEntries = new ArrayList<>();

    /**
     * Returns the log entries added to this logger.
     */
    public List<IStatus> getLogEntries() {
        return logEntries;
    }

    /**
     * Resets the log entry list.
     */
    public void reset() {
        logEntries.clear();
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public void addLogListener(ALogListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(IStatus status) {
        logEntries.add(status);
    }

    /**
     * Throws RuntimeException
     */
    @Override
    public void removeLogListener(ALogListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap() {
        throw new UnsupportedOperationException();
    }

}
