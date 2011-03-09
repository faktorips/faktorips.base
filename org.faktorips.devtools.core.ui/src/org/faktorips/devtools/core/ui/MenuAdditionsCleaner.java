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

package org.faktorips.devtools.core.ui;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.search.ui.IContextMenuConstants;

/**
 * This class removed the additions section in popup menus. This is necessary for menus that should
 * be registered in the registerContextMenu but should not show the additions provided by other
 * plugins like Team and Validate actions.
 * 
 * This menu listener have to be added to the menu manager after the menu manager is registered.
 * 
 * @author stoll
 */
public class MenuAdditionsCleaner implements IMenuListener {

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        // remove all additions provided by the eclipse platform
        boolean isInAdditions = false;
        for (IContributionItem item : manager.getItems()) {
            if (item == null || item.getId() == null) {
                continue;
            }
            if (item.getId().equals(IContextMenuConstants.GROUP_ADDITIONS)) {
                isInAdditions = true;
                item.setVisible(false);
            }
            if (isInAdditions) {
                item.setVisible(false);
                if (item.isSeparator() && !item.getId().equals(IContextMenuConstants.GROUP_ADDITIONS)) {
                    isInAdditions = false;
                }
            }
        }
    }

}
