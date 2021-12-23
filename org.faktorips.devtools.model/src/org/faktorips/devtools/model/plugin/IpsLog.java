/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.IIpsModelExtensions;

public class IpsLog {

    private static boolean suppressLoggingDuringTestExecution;

    private IpsLog() {
        // no instances
    }

    public static ALog get() {
        return Abstractions.getLog();
    }

    /**
     * Logs the status.
     */
    public static final void log(IStatus status) {
        if (IpsModelActivator.isStarted() && !suppressLoggingDuringTestExecution) {
            get().log(status);
        }
    }

    /**
     * Logs the given core exception.
     */
    public static final void log(CoreException e) {
        log(e.getStatus());
    }

    /**
     * Logs the exception.
     */
    public static final void log(Throwable t) {
        log(new IpsStatus(t));
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public static final void logAndShowErrorDialog(final IStatus status) {
        get().log(status);
        IIpsModelExtensions.get().getWorkspaceInteractions().showErrorDialog(status);
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public static final void logAndShowErrorDialog(Exception e) {
        logAndShowErrorDialog(new IpsStatus(e));
    }

    /**
     * Logs the status and shows the status in a standard error dialog.
     */
    public static final void logAndShowErrorDialog(CoreException e) {
        logAndShowErrorDialog(e.getStatus());
    }

    /**
     * <strong>FOR INTERNAL TEST USE ONLY.</strong>
     * 
     * @param suppress <code>true</code> if logging should be disabled during test execution. The
     *            default behavior is not to suppress logging. However, in some test cases we use
     *            test data with an invalid state which results in exceptions in the error log which
     *            is the correct behavior. However if looking at the error log after all tests have
     *            been run, these "correct" exceptions make it difficult, to see the unexpected
     *            exceptions. You see the exception, and think something has gone wrong. In this
     *            test cases it is appropriate to turn off logging. In the setup of the
     *            <code>AbstractIpsModelPluginTest</code> logging is explicitly turned on, so there
     *            is no need to reset this flag, after your test method.
     * 
     * @see #log(CoreException)
     * @see #log(IStatus)
     */
    public static void setSuppressLoggingDuringTest(boolean suppress) {
        suppressLoggingDuringTestExecution = suppress;
    }

    public static boolean isSuppressLoggingDuringTestExecution() {
        return suppressLoggingDuringTestExecution;
    }

}
