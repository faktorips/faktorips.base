/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.application;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.AbstractSplashHandler;

/**
 * The default {@code EclipseSplashHandler} resizes the splash shell composite based on
 * {@code Shell.getSize()}, which returns physical pixels on HiDPI/Retina displays. This causes the
 * splash screen to shift and the progress overlay to render incorrectly after the workspace chooser
 * dialog is shown.
 */
public class IpsModellerSplashHandler extends AbstractSplashHandler {

    @Override
    public void init(Shell splash) {
        super.init(splash);
    }

    @Override
    public org.eclipse.core.runtime.IProgressMonitor getBundleProgressMonitor() {
        return new NullProgressMonitor();
    }

}
