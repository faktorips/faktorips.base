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

package org.faktorips.devtools.stdbuilder.ui.dynamicmenus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JumpToSourceCodeDynamicMenuContributionTest {

    private JumpToSourceCodeDynamicMenuContribution menuContribution;

    @Mock
    private IServiceLocator mockServiceLocator;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IEvaluationService mockEvaluationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockServiceLocator.getService(IEvaluationService.class)).thenReturn(mockEvaluationService);

        menuContribution = new JumpToSourceCodeDynamicMenuContribution();
        menuContribution.initialize(mockServiceLocator);
    }

    /*
     * Cannot test more without PowerMock as creating CommandContributionItems would require to mock
     * out commands but Command is a final class.
     */

    @Test(expected = IllegalArgumentException.class)
    public void testGetContributionItemsNoIpsElementSelected() {
        setCurrentSelection(new Object());

        menuContribution.getContributionItems();
    }

    private void setCurrentSelection(final Object selectedItem) {
        IStructuredSelection mockSelection = mock(IStructuredSelection.class);
        when(mockSelection.iterator()).thenReturn(new SelectionIterator(selectedItem));
        when(mockEvaluationService.getCurrentState().getVariable(ISources.ACTIVE_MENU_SELECTION_NAME)).thenReturn(
                mockSelection);
    }

    private static class SelectionIterator implements Iterator<Object> {

        private Object[] selectedItems;

        private int position;

        private SelectionIterator(Object... selectedItems) {
            this.selectedItems = selectedItems;
        }

        @Override
        public boolean hasNext() {
            return position < selectedItems.length;
        }

        @Override
        public Object next() {
            return selectedItems[position++];
        }

        @Override
        public void remove() {

        }

    }

}
