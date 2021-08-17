/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class IpsModelDecoratorsPluginActivator implements BundleActivator {

    public static final String PLUGIN_ID = "org.faktorips.devtools.model.decorators"; //$NON-NLS-1$

    private static Bundle bundle;

    public static Bundle getBundle() {
        return bundle;
    }

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    @Override
    public void start(BundleContext context) throws Exception {
        bundle = context.getBundle();
    }

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    @Override
    public void stop(BundleContext context) throws Exception {
        bundle = null;
        ((ImageHandling)IpsDecorators.getImageHandling()).dispose();
    }

}
