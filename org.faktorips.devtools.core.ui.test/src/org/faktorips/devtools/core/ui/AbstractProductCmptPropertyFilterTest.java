/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import static org.mockito.Mockito.verify;

import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.junit.Before;
import org.junit.Test;

public class AbstractProductCmptPropertyFilterTest {

    private TestFilter filter;

    @Before
    public void setUp() {
        filter = new TestFilter();
    }

    @Test
    public void testNotifyController() {
        IPropertyVisibleController controller = mock(IPropertyVisibleController.class);
        filter.setPropertyVisibleController(controller);

        filter.notifyController();

        verify(controller).updateUI();
    }

    private static class TestFilter extends AbstractProductCmptPropertyFilter {

        @Override
        public boolean isFiltered(IProductCmptProperty property) {
            return false;
        }

    }

}
