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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.junit.Before;
import org.junit.Test;

public class MenuCleanerTest {

    private MenuCleaner cleaner;

    @Before
    public void setUp() {
        cleaner = new MenuCleaner();
    }

    @Test
    public void testAdditionsCleaner() {
        assertEquals(0, MenuCleaner.ADDITIONS_CLEANER.getFilteredPrefixes().size());
        assertEquals(1, MenuCleaner.ADDITIONS_CLEANER.getFilteredMenuGroups().size());
        assertTrue(MenuCleaner.ADDITIONS_CLEANER.getFilteredMenuGroups()
                .contains(IContextMenuConstants.GROUP_ADDITIONS));
    }

    @Test
    public void testDefaultConstructor() {
        assertTrue(cleaner.getFilteredPrefixes().isEmpty());
        assertTrue(cleaner.getFilteredMenuGroups().isEmpty());
    }

    @Test
    public void testConstructor() {
        Set<String> filteredPrefixes = new HashSet<String>();
        filteredPrefixes.add("foo");
        Set<String> filteredMenuGroups = new HashSet<String>();
        filteredMenuGroups.add("bar");
        MenuCleaner menuCleaner = new MenuCleaner(filteredPrefixes, filteredMenuGroups);

        assertEquals(filteredPrefixes, menuCleaner.getFilteredPrefixes());
        assertEquals(filteredMenuGroups, menuCleaner.getFilteredMenuGroups());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorFilteredPrefixesNull() {
        new MenuCleaner(null, new HashSet<String>());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorFilteredMenuGroupsNull() {
        new MenuCleaner(new HashSet<String>(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorBothFilteredPrefixesAndMenuGroupsNull() {
        new MenuCleaner(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetFilteredPrefixesUnmodifiable() {
        cleaner.getFilteredPrefixes().add("foo");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetFilteredMenuGroupsUnmodifiable() {
        cleaner.getFilteredMenuGroups().add("foo");
    }

    @Test
    public void testAddFilteredPrefix() {
        assertTrue(cleaner.addFilteredPrefix("foo"));
        Set<String> filteredPrefixes = cleaner.getFilteredPrefixes();
        assertEquals(1, filteredPrefixes.size());
        assertTrue(filteredPrefixes.contains("foo"));
    }

    @Test
    public void testAddFilteredPrefixAlreadyFiltered() {
        cleaner.addFilteredMenuGroup("foo");
        assertFalse(cleaner.addFilteredMenuGroup("foo"));
    }

    @Test(expected = NullPointerException.class)
    public void testAddFilteredPrefixNull() {
        cleaner.addFilteredPrefix(null);
    }

    @Test
    public void testAddFilteredMenuGroup() {
        assertTrue(cleaner.addFilteredMenuGroup("foo"));
        Set<String> filteredMenuGroups = cleaner.getFilteredMenuGroups();
        assertEquals(1, filteredMenuGroups.size());
        assertTrue(filteredMenuGroups.contains("foo"));
    }

    @Test
    public void testAddFilteredMenuGroupAlreadyFiltered() {
        cleaner.addFilteredMenuGroup("foo");
        assertFalse(cleaner.addFilteredMenuGroup("foo"));
    }

    @Test(expected = NullPointerException.class)
    public void testAddFilteredMenuGroupNull() {
        cleaner.addFilteredMenuGroup(null);
    }

    /**
     * Tests that the a specific group and all contribution items belonging to the group are made
     * invisible. Every other item must remain visible.
     */
    @Test
    public void testMenuAboutToShowFilterMenuGroup() {
        cleaner.addFilteredMenuGroup("foo");
        cleaner.addFilteredMenuGroup("blub");

        IMenuManager menuManager = new MenuManager();

        IContributionItem beforeFooItem = addMockContributionItem(menuManager, "beforeFoo");

        IContributionItem beginFooGroupMarker = addMockGroupMarker(menuManager, "foo");
        IContributionItem fooItem1 = addMockContributionItem(menuManager, "foo1");
        IContributionItem fooItem2 = addMockContributionItem(menuManager, "foo2");

        IContributionItem barGroupMarker = addMockGroupMarker(menuManager, "bar");
        IContributionItem barItem = addMockContributionItem(menuManager, "barItem");

        IContributionItem blubGroupMarker = addMockGroupMarker(menuManager, "blub");
        IContributionItem blubItem = addMockContributionItem(menuManager, "blubItem");

        cleaner.menuAboutToShow(menuManager);

        verify(beforeFooItem, never()).setVisible(false);

        verify(beginFooGroupMarker).setVisible(false);
        verify(fooItem1).setVisible(false);
        verify(fooItem2).setVisible(false);

        verify(barGroupMarker, never()).setVisible(false);
        verify(barItem, never()).setVisible(false);

        verify(blubGroupMarker).setVisible(false);
        verify(blubItem).setVisible(false);
    }

    private IContributionItem addMockContributionItem(IMenuManager menuManager, String id) {
        IContributionItem mockContributionItem = mock(IContributionItem.class);
        when(mockContributionItem.getId()).thenReturn(id);
        when(mockContributionItem.isSeparator()).thenReturn(false);

        menuManager.add(mockContributionItem);
        return mockContributionItem;
    }

    private IContributionItem addMockGroupMarker(IMenuManager menuManager, String id) {
        IContributionItem mockGroupMarker = mock(IContributionItem.class);
        when(mockGroupMarker.getId()).thenReturn(id);
        when(mockGroupMarker.isGroupMarker()).thenReturn(true);

        menuManager.add(mockGroupMarker);
        return mockGroupMarker;
    }

}
