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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.faktorips.util.ArgumentCheck;

/**
 * A menu listener that can be used to filter specific items from an {@link IMenuManager}.
 * <p>
 * This class provides two different filtering methods:
 * <ol>
 * <li>Filter out all items whose id match a set of configurable prefixes
 * <li>Filter out all items belonging to certain menu groups (e.g. "additions")
 * </ol>
 * <p>
 * <strong>Important:</strong> The listener has to be added to the menu manager AFTER the menu
 * manager has been registered with the platform.
 * 
 * @author stoll
 * @author Alexander Weickmann
 */
public class MenuCleaner implements IMenuListener {

    private static final String MENU_ID_GENERATE = "generateMenuId"; //$NON-NLS-1$

    private static final String MENU_ID_SOURCE = "sourceMenuId"; //$NON-NLS-1$

    /** All items matching at least one of the prefixes contained in this set will be filtered out. */
    private final Set<String> filteredPrefixes = new HashSet<String>();

    /** All items belonging to a group contained in this set will be filtered out. */
    private final Set<String> filteredMenuGroups = new HashSet<String>();

    /**
     * In white list mode every item that is not in the set of filtered prefixes or filtered menu
     * group IDs will be filtered out.
     */
    private boolean whiteListMode;

    /**
     * Creates a menu cleaner that filters out the "additions" menu group as well as the "Source"
     * and / or "Generate" menu.
     */
    public static MenuCleaner createAdditionsCleaner() {
        MenuCleaner additionsCleaner = new MenuCleaner();
        additionsCleaner.addFilteredMenuGroup(IContextMenuConstants.GROUP_ADDITIONS);
        additionsCleaner.addFilteredPrefix(MENU_ID_GENERATE);
        additionsCleaner.addFilteredPrefix(MENU_ID_SOURCE);
        return additionsCleaner;
    }

    /**
     * Creates an empty menu cleaner meaning that no prefixes or menu groups are filtered out as
     * long as none are added via {@link #addFilteredPrefix(String)} or
     * {@link #addFilteredMenuGroup(String)}.
     */
    public MenuCleaner() {
        // Nothing to do
    }

    /**
     * @param filteredPrefixes Set of prefixes that should be filtered out (will be copied
     *            defensively)
     * @param filteredMenuGroups Set of menu group IDs that should be filtered out (will be copied
     *            defensively)
     * 
     * @throws NullPointerException If any parameter is null
     */
    public MenuCleaner(Set<String> filteredPrefixes, Set<String> filteredMenuGroups) {
        this.filteredPrefixes.addAll(filteredPrefixes);
        this.filteredMenuGroups.addAll(filteredMenuGroups);
    }

    /**
     * Returns an unmodifiable view on the set of filtered out prefixes.
     */
    public Set<String> getFilteredPrefixes() {
        return Collections.unmodifiableSet(filteredPrefixes);
    }

    /**
     * Returns an unmodifiable view on the set of filtered out menu group IDs.
     */
    public Set<String> getFilteredMenuGroups() {
        return Collections.unmodifiableSet(filteredMenuGroups);
    }

    /**
     * Adds the given prefix to the set of filtered out prefixes.
     * <p>
     * Returns true if the prefix was not already filtered out.
     * 
     * @param prefix The prefix to filter out from now on
     * 
     * @throws NullPointerException If the parameter is null
     */
    public boolean addFilteredPrefix(String prefix) {
        ArgumentCheck.notNull(prefix);
        return filteredPrefixes.add(prefix);
    }

    /**
     * Adds the given menu group ID to the set of filtered out menu group IDs.
     * <p>
     * Returns true if the menu group ID was not already filtered out.
     * 
     * @param menuGroup The menu group ID to filter out from now on
     * 
     * @throws NullPointerException If the parameter is null
     */
    public boolean addFilteredMenuGroup(String menuGroup) {
        ArgumentCheck.notNull(menuGroup);
        return filteredMenuGroups.add(menuGroup);
    }

    /**
     * Allows to set the menu cleaner into white list mode.
     * <p>
     * In white list mode every item that is not in the set of filtered prefixes or filtered menu
     * group IDs will be filtered out.
     * 
     * @param whiteListMode Flag indicating whether white list mode should be active
     */
    public void setWhiteListMode(boolean whiteListMode) {
        this.whiteListMode = whiteListMode;
    }

    /**
     * Returns whether this menu cleaner is currently in white list mode.
     */
    public boolean isWhiteListMode() {
        return whiteListMode;
    }

    /**
     * Returns whether this menu cleaner is currently in black list mode.
     */
    public boolean isBlackListMode() {
        return !whiteListMode;
    }

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        filterItems(manager);
    }

    private void filterItems(IMenuManager menuManager) {
        boolean inFilteredGroup = false;
        for (IContributionItem item : menuManager.getItems()) {
            String id = item.getId();
            if (id == null) {
                continue;
            }
            if (item.isGroupMarker()) {
                inFilteredGroup = isFilteredMenuGroupId(id);
            }
            if (isBlackListMode()) {
                if (inFilteredGroup || isFilteredPrefixId(id)) {
                    filterItem(item);
                }
            } else {
                if (!inFilteredGroup && !isFilteredPrefixId(id)) {
                    filterItem(item);
                }
            }
        }
    }

    private boolean isFilteredMenuGroupId(String id) {
        return filteredMenuGroups.contains(id);
    }

    private boolean isFilteredPrefixId(String id) {
        for (String prefix : filteredPrefixes) {
            if (id.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void filterItem(IContributionItem item) {
        item.setVisible(false);
    }

}
