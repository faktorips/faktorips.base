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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.faktorips.devtools.core.ui.IpsWorkspacePreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LinksSectionExpansionStateManagerTest {

    private static final String TYPE_A = "type.productComponent1";
    private static final String TYPE_B = "type.productComponent2";

    private LinksSectionExpansionStateManager manager;
    private IpsWorkspacePreferences preferences;

    @BeforeEach
    public void setUp() {
        IPreferencesService preferencesService = mock(IPreferencesService.class);
        preferences = new IpsWorkspacePreferences(preferencesService);
        manager = new LinksSectionExpansionStateManager(preferences);
    }

    @Test
    public void testHasExpansionState_returnsFalse_whenNothingSaved() {
        assertFalse(manager.hasExpansionState(TYPE_A));
    }

    @Test
    public void testLoadExpansionState_returnsEmptySet_whenNothingSaved() {
        assertTrue(manager.loadExpansionState(TYPE_A).isEmpty());
    }

    @Test
    public void testHasExpansionState_returnsTrue_whenStateSaved() {
        Map<String, String> store = new HashMap<>();
        LinksSectionExpansionStateManager localManager = new LinksSectionExpansionStateManager(
                inMemoryPreferences(store));
        Set<String> expanded = Set.of("association1", "association2");
        localManager.saveExpansionState(TYPE_A, expanded);
        assertTrue(localManager.hasExpansionState(TYPE_A));
    }

    @Test
    public void testLoadExpansionState_returnSet_whenStateSaved() {
        Map<String, String> store = new HashMap<>();
        LinksSectionExpansionStateManager realManager = new LinksSectionExpansionStateManager(
                inMemoryPreferences(store));

        Set<String> expanded = Set.of("coverage1", "coverage2");
        realManager.saveExpansionState(TYPE_A, expanded);

        assertTrue(realManager.hasExpansionState(TYPE_A));
        assertEquals(expanded, realManager.loadExpansionState(TYPE_A));
    }

    @Test
    public void testHasExpansionState_returnsTrue_whenAllAssociationsCollapsed() {
        Map<String, String> store = new HashMap<>();
        LinksSectionExpansionStateManager localManager = new LinksSectionExpansionStateManager(
                inMemoryPreferences(store));
        localManager.saveExpansionState(TYPE_A, Set.of());
        assertTrue(localManager.hasExpansionState(TYPE_A));
        assertTrue(localManager.loadExpansionState(TYPE_A).isEmpty());
    }

    @Test
    public void testStatesAreIndependentPerType() {
        Map<String, String> store = new HashMap<>();
        LinksSectionExpansionStateManager localManager = new LinksSectionExpansionStateManager(
                inMemoryPreferences(store));
        localManager.saveExpansionState(TYPE_A, Set.of("assocA"));
        localManager.saveExpansionState(TYPE_B, Set.of("assocB", "assocC"));
        assertTrue(localManager.loadExpansionState(TYPE_A).contains("assocA"));
        assertFalse(localManager.loadExpansionState(TYPE_A).contains("assocB"));
        assertFalse(localManager.loadExpansionState(TYPE_B).contains("assocA"));
        assertTrue(localManager.loadExpansionState(TYPE_B).containsAll(Set.of("assocB", "assocC")));
    }

    private IpsWorkspacePreferences inMemoryPreferences(Map<String, String> store) {
        return new IpsWorkspacePreferences(mock(IPreferencesService.class)) {
            @Override
            public boolean getBoolean(String id) {
                return Boolean.parseBoolean(store.getOrDefault(id, "false"));
            }

            @Override
            public boolean getBoolean(String id, boolean def) {
                return Boolean.parseBoolean(store.getOrDefault(id, String.valueOf(def)));
            }

            @Override
            public void putBoolean(String id, boolean v) {
                store.put(id, String.valueOf(v));
            }

            @Override
            public String getString(String id) {
                return store.getOrDefault(id, "");
            }

            @Override
            public void putString(String id, String v) {
                store.put(id, v);
            }
        };
    }
}
