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

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.model.abstractions.AWorkspaceAbstractionsImplementationProvider;
import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions.AWorkspaceAbstractionsImplementation;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class IpsModelActivator implements BundleActivator, AWorkspaceAbstractionsImplementationProvider {

    public static final String PLUGIN_ID = "org.faktorips.devtools.model"; //$NON-NLS-1$

    /** The shared instance. */
    private static IpsModelActivator activator;

    private Bundle bundle;

    private ServiceTracker<AWorkspaceAbstractionsImplementation, AWorkspaceAbstractionsImplementation> implementationServiceTracker;

    private AWorkspaceAbstractionsImplementation workspaceAbstractionsImplementation;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "This is a standard Eclipse singleton pattern")
    public IpsModelActivator() {
        activator = this;
    }

    public static IpsModelActivator get() {
        return activator;
    }

    public static boolean isStarted() {
        return activator != null;
    }

    public static final ILog getLog() {
        return Platform.getLog(get().bundle);
    }

    /**
     * This method is called upon plug-in activation.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        bundle = context.getBundle();

        implementationServiceTracker = new ServiceTracker<>(context, AWorkspaceAbstractionsImplementation.class, null);
        implementationServiceTracker.open();

        // force loading of class before model is created!
        IpsObjectType.POLICY_CMPT_TYPE.getId();

    }

    /**
     * This method is called when the plug-in is stopped.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        implementationServiceTracker = null;
    }

    /**
     * Returns the number of the installed Faktor-IPS version.
     */
    public static final String getInstalledFaktorIpsVersion() {
        return Platform.getBundle(PLUGIN_ID).getVersion().toString();
    }

    /**
     * Returns the location in the local file system of the plug-in state area for this plug-in. If
     * the plug-in state area did not exist prior to this call, it is created.
     * <p>
     * The plug-in state area is a file directory within the platform's metadata area where a
     * plug-in is free to create files. The content and structure of this area is defined by the
     * plug-in, and the particular plug-in is solely responsible for any files it puts there. It is
     * recommended for plug-in preference settings and other configuration parameters.
     * </p>
     *
     * @throws IllegalStateException when the system is running with no data area (-data @none), or
     *             when a data area has not been set yet.
     * @return a local file system path XXX Investigate the usage of a service factory (see also
     *             platform.getStateLocation)
     */
    public static IPath getStateLocation() throws IllegalStateException {
        return Platform.getStateLocation(get().bundle);
    }

    @Override
    public AWorkspaceAbstractionsImplementation getWorkspaceAbstractionsImplementation() {
        if (workspaceAbstractionsImplementation == null) {
            var tracker = implementationServiceTracker;
            workspaceAbstractionsImplementation = tracker != null ? tracker.getService() : null;
        }
        return workspaceAbstractionsImplementation;
    }

    public void setWorkspaceAbstractionsImplementation(
            AWorkspaceAbstractionsImplementation workspaceAbstractionsImplementation) {
        this.workspaceAbstractionsImplementation = workspaceAbstractionsImplementation;
    }

    @Override
    public boolean canRun() {
        return implementationServiceTracker != null;
    }

    @Override
    public int getPriority() {
        return 1;
    }

}
