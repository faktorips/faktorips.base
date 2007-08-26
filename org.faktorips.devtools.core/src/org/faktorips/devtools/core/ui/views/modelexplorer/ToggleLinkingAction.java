/*******************************************************************************
 * Copyright (c) 2007 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
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
            
            ImageDescriptor descriptor = IpsPlugin.getDefault().getImageDescriptor("elcl16/synced.gif"); //$NON-NLS-1$
            this.setHoverImageDescriptor(descriptor);
            this.setImageDescriptor(descriptor);         
    
            this.explorer = explorer;
            setChecked(explorer.isLinkingEnabled());
        }

    
        /**
         * Runs the action.
         */
        public void run() {
            explorer.setLinkingEnabled(isChecked());
        }
}

