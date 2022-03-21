/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.versionmanager;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
import org.faktorips.runtime.MessageList;

/**
 * Abstract class for operations migrating the content of an IPS project created with an older
 * version of Faktor-IPS to match the needs of the current installed version of Faktor-IPS.
 * <p>
 * This class is a container for implementations of <code>AbstractMigrationOperation</code>s.
 * 
 * @author Thorsten Guenther
 */
public interface IIpsFeatureMigrationOperation {

    /**
     * Returns the IPS project the operation migrates.
     */
    public abstract IIpsProject getIpsProject();

    /**
     * Returns the description of all the steps done by this operation - to be displayed to the
     * user.
     */
    public abstract String getDescription();

    /**
     * Returns <code>true</code> if no changes will be done by this operation, <code>false</code>
     * otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * Returns a list of messages describing any problems occurred during migration. If this list is
     * empty, migration was either not executed or executed successfully. If this list contains a
     * message with severity error, it was not.
     */
    public abstract MessageList getMessageList();

    /**
     * Performs the steps that are to be treated as a single logical workspace change.
     * <p>
     * Subclasses must implement this method.
     * </p>
     *
     * @param monitor the progress monitor to use to display progress and field user requests to
     *            cancel
     * @exception IpsException if the operation fails due to a CoreException
     * @exception InvocationTargetException if the operation fails due to an exception other than
     *                CoreException
     * @exception InterruptedException if the operation detects a request to cancel, using
     *                <code>IProgressMonitor.isCanceled()</code>, it should exit by throwing
     *                <code>InterruptedException</code>. It is also possible to throw
     *                <code>OperationCanceledException</code>, which gets mapped to
     *                <code>InterruptedException</code> by the <code>run</code> method.
     */
    // CSOFF: ThrowsCount
    abstract void execute(IProgressMonitor monitor)
            throws IpsException, InvocationTargetException,
            InterruptedException;
    // CSON: ThrowsCount

    /**
     * Runs this operation. Progress should be reported to the given progress monitor. This method
     * is usually invoked by an <code>IRunnableContext</code>'s <code>run</code> method, which
     * supplies the progress monitor. A request to cancel the operation should be honored and
     * acknowledged by throwing <code>InterruptedException</code>.
     *
     * @param monitor the progress monitor to use to display progress and receive requests for
     *            cancellation
     * @exception InvocationTargetException if the run method must propagate a checked exception, it
     *                should wrap it inside an <code>InvocationTargetException</code>; runtime
     *                exceptions are automatically wrapped in an
     *                <code>InvocationTargetException</code> by the calling context
     * @exception InterruptedException if the operation detects a request to cancel, using
     *                <code>IProgressMonitor.isCanceled()</code>, it should exit by throwing
     *                <code>InterruptedException</code>
     */
    public abstract void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException;

    /**
     * Returns the {@link IpsMigrationOption configuration options} for all included migration
     * operations or an empty collection if no operation requires configuration.
     */
    public default Collection<IpsMigrationOption<?>> getOptions() {
        return Collections.emptyList();
    }

}
