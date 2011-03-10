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

    /** A menu cleaner that filters out the "additions" menu group. */
    public static final MenuCleaner ADDITIONS_CLEANER = new MenuCleaner(IContextMenuConstants.GROUP_ADDITIONS);

    /** All items matching at least one of the prefixes contained in this set will be filtered out. */
    private final Set<String> filteredPrefixes = new HashSet<String>();

    /** All items belonging to a group contained in this set will be filtered out. */
    private final Set<String> filteredMenuGroups = new HashSet<String>();

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

    private MenuCleaner(String... filteredMenuGroups) {
        for (String menuGroup : filteredMenuGroups) {
            addFilteredMenuGroup(menuGroup);
        }
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

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        filterMenuGroups(manager);
    }

    private void filterMenuGroups(IMenuManager manager) {
        boolean inFilteredGroup = false;
        for (IContributionItem item : manager.getItems()) {
            if (item.getId() == null) {
                continue;
            }
            if (item.isGroupMarker()) {
                inFilteredGroup = isFilteredMenuGroupId(item.getId());
            }
            if (inFilteredGroup) {
                item.setVisible(false);
            }
        }
    }

    private boolean isFilteredMenuGroupId(String id) {
        return filteredMenuGroups.contains(id);
    }

}
