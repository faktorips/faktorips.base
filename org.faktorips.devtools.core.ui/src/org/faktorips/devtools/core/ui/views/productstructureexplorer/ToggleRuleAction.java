/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;

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
            } catch (CoreException e) {
                IpsPlugin.log(new IpsStatus("Could not save IpsSrcFile \"" + vRuleConfig.getIpsSrcFile() + "\"", e)); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }
}
