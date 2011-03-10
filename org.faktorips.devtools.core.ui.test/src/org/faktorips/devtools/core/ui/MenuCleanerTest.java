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
    public void testDefaultConstructor() {
        assertTrue(cleaner.getFilteredPrefixes().isEmpty());
        assertTrue(cleaner.getFilteredMenuGroups().isEmpty());
        assertFalse(cleaner.isWhiteListMode());
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
        assertFalse(cleaner.isWhiteListMode());
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
     * Tests that a specific menu group and all contribution items belonging to this group are made
     * invisible. Every other item must remain visible.
     */
    @Test
    public void testMenuAboutToShowFilterMenuGroup() {
        cleaner.addFilteredMenuGroup("foo");

        IMenuManager menuManager = new MenuManager();

        IContributionItem beforeFooItem = addContributionItem(menuManager, "beforeFoo");
        IContributionItem beginFooGroupMarker = addGroupMarker(menuManager, "foo");
        IContributionItem fooItem1 = addContributionItem(menuManager, "foo1");
        IContributionItem fooItem2 = addContributionItem(menuManager, "foo2");
        IContributionItem barGroupMarker = addGroupMarker(menuManager, "bar");
        IContributionItem barItem = addContributionItem(menuManager, "barItem");

        cleaner.menuAboutToShow(menuManager);

        verify(beforeFooItem, never()).setVisible(false);
        verify(beginFooGroupMarker).setVisible(false);
        verify(fooItem1).setVisible(false);
        verify(fooItem2).setVisible(false);
        verify(barGroupMarker, never()).setVisible(false);
        verify(barItem, never()).setVisible(false);
    }

    /**
     * Tests that null IDs are ignored when filtering menu groups.
     */
    @Test
    public void testMenuAboutToShowFilterMenuGroupWithNullIds() {
        cleaner.addFilteredMenuGroup("foo");

        IMenuManager menuManager = new MenuManager();
        IContributionItem fooGroupMarker = addGroupMarker(menuManager, "foo");
        addContributionItem(menuManager, null);
        addGroupMarker(menuManager, null);

        cleaner.menuAboutToShow(menuManager);

        verify(fooGroupMarker).setVisible(false);
    }

    @Test
    public void testMenuAboutToShowFilterMenuGroupWhiteListMode() {
        cleaner.addFilteredMenuGroup("foo");
        cleaner.setWhiteListMode(true);

        IMenuManager menuManager = new MenuManager();
        IContributionItem fooGroup = addGroupMarker(menuManager, "foo");
        IContributionItem fooItem = addContributionItem(menuManager, "fooItem");
        IContributionItem barGroup = addGroupMarker(menuManager, "bar");
        IContributionItem barItem = addContributionItem(menuManager, "barItem");

        cleaner.menuAboutToShow(menuManager);

        verify(fooGroup, never()).setVisible(false);
        verify(fooItem, never()).setVisible(false);
        verify(barGroup).setVisible(false);
        verify(barItem).setVisible(false);
    }

    /**
     * Tests that all contribution items matching a specific prefix are filtered out (made
     * invisible) while all other items remain visible.
     */
    @Test
    public void testMenuAboutToShowFilterPrefixes() {
        cleaner.addFilteredPrefix("foo.bar");

        IMenuManager menuManager = new MenuManager();
        IContributionItem notMatchedItem1 = addContributionItem(menuManager, "foo.notMatchedItem1");
        IContributionItem matchedItem1 = addContributionItem(menuManager, "foo.bar.matchedItem1");
        IContributionItem notMatchedItem2 = addContributionItem(menuManager, "bar.notMatchedItem2");
        IContributionItem matchedItem2 = addContributionItem(menuManager, "foo.bar.matchedItem2");
        IContributionItem notMatchedItem3 = addContributionItem(menuManager, "notMatchedItem3");
        IContributionItem matchedItem3 = addContributionItem(menuManager, "foo.bar.f10.matchedItem3");

        cleaner.menuAboutToShow(menuManager);

        verify(notMatchedItem1, never()).setVisible(false);
        verify(notMatchedItem2, never()).setVisible(false);
        verify(notMatchedItem3, never()).setVisible(false);

        verify(matchedItem1).setVisible(false);
        verify(matchedItem2).setVisible(false);
        verify(matchedItem3).setVisible(false);
    }

    /**
     * Tests that null IDs are ignored when filtering prefixes.
     */
    @Test
    public void testMenuAboutToShowFilterPrefixesNullIds() {
        cleaner.addFilteredPrefix("foo.bar");

        IMenuManager menuManager = new MenuManager();
        addContributionItem(menuManager, null);
        IContributionItem matchedItem = addContributionItem(menuManager, "foo.bar.matchedItem");
        addContributionItem(menuManager, null);
        IContributionItem notMachtedItem = addContributionItem(menuManager, "bar.foo.notMatchedItem");

        cleaner.menuAboutToShow(menuManager);

        verify(matchedItem).setVisible(false);
        verify(notMachtedItem, never()).setVisible(false);
    }

    @Test
    public void testMenuAboutToShowFilterPrefixesWhiteListMode() {
        cleaner.addFilteredPrefix("foo");
        cleaner.setWhiteListMode(true);

        IMenuManager menuManager = new MenuManager();
        IContributionItem matchedItem = addContributionItem(menuManager, "foo.item");
        IContributionItem notMatchedItem = addContributionItem(menuManager, "bar.item");

        cleaner.menuAboutToShow(menuManager);

        verify(matchedItem, never()).setVisible(false);
        verify(notMatchedItem).setVisible(false);
    }

    /**
     * Tests that filtering prefixes and filtering menu groups works in unison.
     */
    @Test
    public void testMenuAboutToShowFilterBothPrefixesAndMenuGroups() {
        cleaner.addFilteredMenuGroup("foo");
        cleaner.addFilteredPrefix("bar");

        IMenuManager menuManager = new MenuManager();
        IContributionItem fooGroup = addGroupMarker(menuManager, "foo");
        IContributionItem barItem1 = addContributionItem(menuManager, "bar.item1");
        IContributionItem fooItem1 = addContributionItem(menuManager, "foo.item1");
        IContributionItem barGroup = addGroupMarker(menuManager, "barGroup");
        IContributionItem barItem2 = addContributionItem(menuManager, "bar.item2");
        IContributionItem fooItem2 = addContributionItem(menuManager, "foo.item2");

        cleaner.menuAboutToShow(menuManager);

        verify(fooGroup).setVisible(false);
        verify(barItem1).setVisible(false);
        verify(fooItem1).setVisible(false);
        verify(barGroup).setVisible(false);
        verify(barItem2).setVisible(false);
        verify(fooItem2, never()).setVisible(false);
    }

    private IContributionItem addContributionItem(IMenuManager menuManager, String id) {
        IContributionItem mockContributionItem = mock(IContributionItem.class);
        when(mockContributionItem.getId()).thenReturn(id);
        when(mockContributionItem.isSeparator()).thenReturn(false);

        menuManager.add(mockContributionItem);
        return mockContributionItem;
    }

    private IContributionItem addGroupMarker(IMenuManager menuManager, String id) {
        IContributionItem mockGroupMarker = mock(IContributionItem.class);
        when(mockGroupMarker.getId()).thenReturn(id);
        when(mockGroupMarker.isGroupMarker()).thenReturn(true);

        menuManager.add(mockGroupMarker);
        return mockGroupMarker;
    }

}
