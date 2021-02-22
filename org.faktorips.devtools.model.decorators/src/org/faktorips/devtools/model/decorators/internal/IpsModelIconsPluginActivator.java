/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

public class IpsModelIconsPluginActivator implements BundleActivator {

    private static Bundle bundle;

    public static Bundle getBundle() {
        return bundle;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        bundle = context.getBundle();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        bundle = null;
    }

}
