/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.internal;

import org.faktorips.devtools.abstraction.AImplementationProvider;
import org.faktorips.devtools.abstraction.Abstractions.AImplementation;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class OsgiImplementationProvider implements BundleActivator, AImplementationProvider {

    private static OsgiImplementationProvider instance;

    private ServiceTracker<AImplementation, AImplementation> implementationServiceTracker;

    public static OsgiImplementationProvider getInstance() {
        return instance;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        implementationServiceTracker = new ServiceTracker<>(context,
                AImplementation.class, null);
        implementationServiceTracker.open();
        instance = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        implementationServiceTracker = null;
        instance = null;
    }

    @Override
    public AImplementation get() {
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
