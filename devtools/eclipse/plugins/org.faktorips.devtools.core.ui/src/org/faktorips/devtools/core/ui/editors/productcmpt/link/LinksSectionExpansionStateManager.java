/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.core.ui.IpsWorkspacePreferences;

/**
 * Stores and restores the expansion state of associations in the links section tree viewer, keyed
 * by the qualified name of the ProductCmptType.
 * <p>
 * The set of expanded association names is stored as a comma-separated string. A sentinel boolean
 * key distinguishes "no state saved yet" from "all associations collapsed".
 */
class LinksSectionExpansionStateManager {

    private static final String KEY_PREFIX = "LinksSection_expansionState_"; //$NON-NLS-1$
    private static final String HAS_STATE_SUFFIX = "_hasState"; //$NON-NLS-1$
    private static final String ASSOCIATION_NAMES_SEPARATOR = ","; //$NON-NLS-1$

    private final IpsWorkspacePreferences preferences;

    LinksSectionExpansionStateManager() {
        this(new IpsWorkspacePreferences());
    }

    LinksSectionExpansionStateManager(IpsWorkspacePreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Returns {@code true} if an expansion state has been saved for the given type.
     */
    boolean hasExpansionState(String productCmptTypeQName) {
        return preferences.getBoolean(expansionStateKey(productCmptTypeQName) + HAS_STATE_SUFFIX);
    }

    /**
     * Saves which associations are expanded for the given type.
     */
    void saveExpansionState(String productCmptTypeQName, Set<String> expandedAssociationNames) {
        preferences.putBoolean(expansionStateKey(productCmptTypeQName) + HAS_STATE_SUFFIX, true);
        preferences.putString(expansionStateKey(productCmptTypeQName),
                String.join(ASSOCIATION_NAMES_SEPARATOR, expandedAssociationNames));
    }

    /**
     * Loads the set of expanded association names for the given type. Returns an empty set if no
     * state has been saved.
     */
    Set<String> loadExpansionState(String productCmptTypeQName) {
        if (!hasExpansionState(productCmptTypeQName)) {
            return Collections.emptySet();
        }
        String savedNames = preferences.getString(expansionStateKey(productCmptTypeQName));
        if (savedNames.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(savedNames.split(ASSOCIATION_NAMES_SEPARATOR)));
    }

    private String expansionStateKey(String productCmptTypeQName) {
        return KEY_PREFIX + productCmptTypeQName;
    }
}
