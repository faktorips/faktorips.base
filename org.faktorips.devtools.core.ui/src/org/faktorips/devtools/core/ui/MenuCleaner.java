/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.ui.IWorkbenchActionConstants;
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
 * It's also possible to set a matching group. If a matching group is set, filtering rules are only
 * applied within this matching group. This for example enables clients to filter out only specific
 * entries from the "additions" menu group.
 * <p>
 * <strong>Important:</strong> The listener has to be added to the menu manager AFTER the menu
 * manager has been registered with the platform.
 * 
 * @author stoll
 * @author Alexander Weickmann
 */
public final class MenuCleaner implements IMenuListener {

    public static final String WHITE_LIST_IPS_PREFIX = "org.faktorips"; //$NON-NLS-1$

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
     * ID of the menu group to which filter is applied or null if filtering shall be applied to the
     * menu as a whole.
     */
    private String matchingGroup;

    /**
     * Creates a menu cleaner that filters out the some default menu groups like "additions" and
     * "group.reorganize".
     * <p>
     * <strong>Important:</strong> This only works correctly if the "additions" menu group really
     * exists in the given {@link IMenuManager}. If
     * <ul>
     * <li>{@link IMenuManager#getRemoveAllWhenShown()} returns false and the menu group does not
     * exist, it will be automatically added by this operation
     * <li>{@link IMenuManager#getRemoveAllWhenShown()} returns true, clients have to care that the
     * "additions" menu group is always present
     * </ul>
     * <p>
     * This method must be called AFTER the menu manager was registered with the platform.
     * 
     * @param menuManager The {@link IMenuManager} from which to remove the "additions" menu group
     */
    public static MenuCleaner addDefaultCleaner(IMenuManager menuManager) {
        if (!menuManager.getRemoveAllWhenShown()) {
            boolean foundAdditionsGroup = false;
            for (IContributionItem item : menuManager.getItems()) {
                if (IWorkbenchActionConstants.MB_ADDITIONS.equals(item.getId())) {
                    foundAdditionsGroup = true;
                    break;
                }
            }
            if (!foundAdditionsGroup) {
                menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            }
        }

        MenuCleaner additionsCleaner = new MenuCleaner();
        additionsCleaner.addFilteredMenuGroup(IWorkbenchActionConstants.MB_ADDITIONS);
        additionsCleaner.addFilteredMenuGroup(IContextMenuConstants.GROUP_ADDITIONS);
        additionsCleaner.addFilteredMenuGroup(IContextMenuConstants.GROUP_REORGANIZE);
        menuManager.addMenuListener(additionsCleaner);

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
     * Creates a new menu cleaner that filters the set of {@link #filteredPrefixes} and the set of
     * {@link #filteredMenuGroups}. The cleaner could only filter out those groups that does exists
     * in the menu. That means if you want to filter out all additions that are contributed by
     * eclipse you have to add a separator for additions first. .
     * 
     * @param filteredPrefixes Set of prefixes that should be filtered out (will be copied
     *            defensively)
     * @param filteredMenuGroups Set of menu group IDs that should be filtered out (will be copied
     *            defensively)
     * 
     * @throws NullPointerException If any parameter is null
     */
    public MenuCleaner(Set<String> filteredPrefixes, Set<String> filteredMenuGroups) {
        this();
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

    /**
     * Configures this menu cleaner to only filter within the provided matching group.
     * 
     * @param matchingGroup The group within to apply filtering or null to apply filtering to the
     *            menu as a whole
     */
    public void setMatchingGroup(String matchingGroup) {
        this.matchingGroup = matchingGroup;
    }

    /**
     * Returns the group to which filtering is applied or null if filtering is applied to the whole
     * menu.
     */
    public String getMatchingGroup() {
        return matchingGroup;
    }

    /**
     * Clears the set of filtered menu groups.
     */
    public void clearFilteredMenuGroups() {
        filteredMenuGroups.clear();
    }

    /**
     * Clears the set of filtered prefixes.
     */
    public void clearFilteredPrefixes() {
        filteredPrefixes.clear();
    }

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        filterItems(manager);
    }

    private void filterItems(IMenuManager menuManager) {
        boolean inMatchingGroup = matchingGroup == null;
        boolean inFilteredGroup = false;
        for (IContributionItem item : menuManager.getItems()) {
            if (item.getId() == null) {
                continue;
            }
            if (item.isGroupMarker()) {
                inMatchingGroup = isMatchingGroupId(item.getId());
                inFilteredGroup = isFilteredMenuGroupId(item.getId());
            }
            if (!inMatchingGroup && matchingGroup != null) {
                continue;
            }
            filterItem(item, inFilteredGroup);
        }
    }

    private void filterItem(IContributionItem item, boolean inFilteredGroup) {
        if (isBlackListMode()) {
            if (inFilteredGroup || isFilteredPrefixId(item.getId())) {
                item.setVisible(false);
            }
        } else {
            if (!isMatchingGroupId(item.getId()) && !inFilteredGroup && !isFilteredPrefixId(item.getId())) {
                item.setVisible(false);
            }
        }
    }

    private boolean isMatchingGroupId(String id) {
        return id.equals(matchingGroup);
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

}
