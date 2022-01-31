/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;

/**
 * Action for activating/de-activating validation rules in the ProductStructureExplorer.
 * 
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class ToggleRuleAction extends Action {
    private IValidationRuleConfig vRuleConfig;

    public ToggleRuleAction(IValidationRuleConfig config) {
        super();
        if (config.isActive()) {
            setText(Messages.ToggleRuleAction_Label_deactivate);
            setToolTipText(Messages.ToggleRuleAction_TooltipActivate);
        } else {
            setText(Messages.ToggleRuleAction_Label_activate);
            setToolTipText(Messages.ToggleRuleAction_TooltipDeactivate);
        }

        vRuleConfig = config;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Toggles the activation state of the given {@link IValidationRuleConfig} and saves the
     * corresponding source file if it is not dirty. Thus dirty editors will not be saved but
     * informed via change-event.
     */
    @Override
    public void run() {
        boolean srcFileDirty = vRuleConfig.getIpsSrcFile().isDirty();
        vRuleConfig.setActive(!vRuleConfig.isActive());
        if (!srcFileDirty) {
            try {
                vRuleConfig.getIpsSrcFile().save(false, new NullProgressMonitor());
            } catch (CoreRuntimeException e) {
                IpsPlugin.log(new IpsStatus("Could not save IpsSrcFile \"" + vRuleConfig.getIpsSrcFile() + "\"", e)); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }
}
