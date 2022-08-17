/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.actions.Messages;

/**
 * Enable an navigator with a 'link to editor' widget.
 * 
 * @author Markus Blum
 */
public class ToggleLinkingAction extends Action {

    private ModelExplorer explorer;

    /**
     * Constructs a new action.
     */
    public ToggleLinkingAction(ModelExplorer explorer) {

        super(Messages.ToggleLinkingAction_Text);
        setDescription(Messages.ToggleLinkingAction_Description);
        setToolTipText(Messages.ToggleLinkingAction_ToolTipText);

        ImageDescriptor descriptor = IpsUIPlugin.getImageHandling().createImageDescriptor("elcl16/synced.gif"); //$NON-NLS-1$
        setHoverImageDescriptor(descriptor);
        setImageDescriptor(descriptor);

        this.explorer = explorer;
        setChecked(explorer.isLinkingEnabled());
    }

    /**
     * Runs the action.
     */
    @Override
    public void run() {
        explorer.setLinkingEnabled(isChecked());
    }
}
