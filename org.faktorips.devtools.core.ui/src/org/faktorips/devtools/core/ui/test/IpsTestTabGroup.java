/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.test;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;

/**
 * Launch configuration tab group for ips tests.
 * 
 * @author Joerg Ortmann
 */
public class IpsTestTabGroup extends AbstractLaunchConfigurationTabGroup {
    /**
     * {@inheritDoc}
     */
    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        ILaunchConfigurationTab[] tabs = null;
        tabs = new ILaunchConfigurationTab[] { new TestSelectionTab(), new JavaArgumentsTab(), new EnvironmentTab(),
                new CommonTab() };
        setTabs(tabs);
    }
}
