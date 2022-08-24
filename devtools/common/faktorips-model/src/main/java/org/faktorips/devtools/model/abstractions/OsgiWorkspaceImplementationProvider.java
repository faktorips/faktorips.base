/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstractions;

import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions.AWorkspaceAbstractionsImplementation;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class OsgiWorkspaceImplementationProvider implements BundleActivator, AWorkspaceAbstractionsImplementationProvider {

    private static OsgiWorkspaceImplementationProvider instance;

    private ServiceTracker<AWorkspaceAbstractionsImplementation, AWorkspaceAbstractionsImplementation> implementationServiceTracker;

    public static OsgiWorkspaceImplementationProvider getInstance() {
        return instance;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        implementationServiceTracker = new ServiceTracker<>(context,
                AWorkspaceAbstractionsImplementation.class, null);
        implementationServiceTracker.open();
        instance = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        implementationServiceTracker = null;
        instance = null;
    }

    @Override
    public AWorkspaceAbstractionsImplementation get() {
        var tracker = implementationServiceTracker;
        return tracker != null ? tracker.getService() : null;
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
