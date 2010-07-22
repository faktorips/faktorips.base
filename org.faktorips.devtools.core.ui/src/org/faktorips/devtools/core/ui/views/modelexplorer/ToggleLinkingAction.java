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
