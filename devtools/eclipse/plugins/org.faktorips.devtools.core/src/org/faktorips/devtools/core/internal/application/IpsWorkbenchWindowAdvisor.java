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

import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * Advisor for workbench windows for FaktorIPS
 * 
 * @author Thorsten Guenther
 */
class IpsWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    /**
     * @param configurer The configurer to use.
     */
    public IpsWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    @Override
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new IpsActionBarAdvisor(configurer);
    }

    @Override
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

        // show the progress indicator, which are hidden by default
        configurer.setShowPerspectiveBar(false);
        configurer.setShowProgressIndicator(true);
        configurer.setTitle(Messages.IpsWorkbenchAdvisor_title);
    }

}
