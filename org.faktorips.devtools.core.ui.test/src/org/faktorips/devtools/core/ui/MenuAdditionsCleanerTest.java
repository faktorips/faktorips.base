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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.junit.Before;
import org.junit.Test;

public class MenuAdditionsCleanerTest {

    private MenuAdditionsCleaner cleaner;

    @Before
    public void setUp() {
        cleaner = new MenuAdditionsCleaner();
    }

    /**
     * Tests that the additions menu and all contribution items belonging to the additions menu are
     * made invisible. Every other item must remain visible.
     */
    @Test
    public void testMenuAboutToShow() {
        IContributionItem beforeAdditionsItem = mockContributionItem("beforeAdditions");
        IContributionItem beginAdditionsSeparator = mockSeparator(IContextMenuConstants.GROUP_ADDITIONS);
        IContributionItem additionsItem1 = mockContributionItem("additions1");
        IContributionItem additionsItem2 = mockContributionItem("additions2");
        IContributionItem endAdditionsSeparator = mockSeparator(IContextMenuConstants.GROUP_ADDITIONS + "-end");
        IContributionItem afterAdditionsItem = mockContributionItem("afterAdditions");

        IMenuManager menuManager = new MenuManager();
        menuManager.add(beforeAdditionsItem);
        menuManager.add(beginAdditionsSeparator);
        menuManager.add(additionsItem1);
        menuManager.add(additionsItem2);
        menuManager.add(endAdditionsSeparator);
        menuManager.add(afterAdditionsItem);

        cleaner.menuAboutToShow(menuManager);

        verify(beforeAdditionsItem, never()).setVisible(false);
        verify(beginAdditionsSeparator).setVisible(false);
        verify(additionsItem1).setVisible(false);
        verify(additionsItem2).setVisible(false);
        verify(endAdditionsSeparator).setVisible(false);
        verify(afterAdditionsItem, never()).setVisible(false);
    }

    private IContributionItem mockContributionItem(String id) {
        IContributionItem mockContributionItem = mock(IContributionItem.class);
        when(mockContributionItem.getId()).thenReturn(id);
        when(mockContributionItem.isSeparator()).thenReturn(false);
        return mockContributionItem;
    }

    private IContributionItem mockSeparator(String id) {
        IContributionItem mockContributionItem = mock(IContributionItem.class);
        when(mockContributionItem.getId()).thenReturn(id);
        when(mockContributionItem.isSeparator()).thenReturn(true);
        return mockContributionItem;
    }

}
