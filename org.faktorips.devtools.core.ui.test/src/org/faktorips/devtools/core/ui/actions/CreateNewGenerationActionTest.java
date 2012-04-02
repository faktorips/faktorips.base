/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CreateNewGenerationActionTest extends AbstractIpsPluginTest {

    @Mock
    private Shell shell;

    @Mock
    private ISelectionProvider selectionProvider;

    private CreateNewGenerationAction action;

    @Override
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        action = new CreateNewGenerationAction(shell, selectionProvider);
    }

    @Test
    public void testComputeEnabledProperty_NotEnabledIfSelectionIsEmpty() {
        assertFalse(action.computeEnabledProperty(new StructuredSelection()));
    }

    @Test
    public void testComputeEnabledProperty_NotEnabledIfElementOtherThanProductCmptOrProductCmptReferenceIncluded() {
        IStructuredSelection selection = new StructuredSelection(Arrays.asList(mock(IProductCmpt.class),
                mock(IProductCmptReference.class), mock(IEnumType.class)));

        assertFalse(action.computeEnabledProperty(selection));
    }

    @Test
    public void testComputeEnabledProperty_EnabledIfOnlyProductCmptOrProductCmptReferenceIncluded() {
        IStructuredSelection selection = new StructuredSelection(Arrays.asList(mock(IProductCmpt.class),
                mock(IProductCmptReference.class)));

        assertTrue(action.computeEnabledProperty(selection));
    }

}
