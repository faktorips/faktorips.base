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

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JumpToSourceCodeDynamicMenuContributionTest {

    private JumpToSourceCodeDynamicMenuContribution menuContribution;

    @Mock
    private IServiceLocator mockServiceLocator;

    @Mock
    private IEvaluationContext mockEvaluationContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        IEvaluationService mockEvaluationService = mock(IEvaluationService.class);
        when(mockEvaluationService.getCurrentState()).thenReturn(mockEvaluationContext);
        when(mockServiceLocator.getService(IEvaluationService.class)).thenReturn(mockEvaluationService);
        ICommandService mockCommandService = mock(ICommandService.class);
        when(mockServiceLocator.getService(ICommandService.class)).thenReturn(mockCommandService);

        menuContribution = new JumpToSourceCodeDynamicMenuContribution();
        menuContribution.initialize(mockServiceLocator);
    }

    /*
     * Cannot test more without PowerMock as creating CommandContributionItems would require to mock
     * out Commands but Command is a final class.
     */

    @Test(expected = IllegalArgumentException.class)
    public void testGetContributionItemsNoIpsElementSelected() {
        setCurrentSelection(new Object());

        menuContribution.getContributionItems();
    }

    private void setCurrentSelection(final Object selectedItem) {
        IStructuredSelection mockSelection = mock(IStructuredSelection.class);
        when(mockSelection.iterator()).thenReturn(new SelectionIterator(selectedItem));
        when(mockEvaluationContext.getVariable(ISources.ACTIVE_MENU_SELECTION_NAME)).thenReturn(mockSelection);
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
