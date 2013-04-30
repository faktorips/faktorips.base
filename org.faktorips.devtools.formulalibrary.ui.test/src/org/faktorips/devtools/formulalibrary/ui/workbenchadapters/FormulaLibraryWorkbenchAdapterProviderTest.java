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

package org.faktorips.devtools.formulalibrary.ui.workbenchadapters;

import junit.framework.TestCase;

import org.faktorips.devtools.formulalibrary.internal.model.FormulaFunction;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaLibrary;
import org.junit.Test;

public class FormulaLibraryWorkbenchAdapterProviderTest extends TestCase {

    @Test
    public void testRegisterAdapter() {
        FormulaLibraryWorkbenchAdapterProvider adapter = new FormulaLibraryWorkbenchAdapterProvider();
        assertEquals(2, adapter.getAdapterMap().size());
        assertNotNull(adapter.getAdapterMap().get(FormulaLibrary.class));
        assertNotNull(adapter.getAdapterMap().get(FormulaFunction.class));
    }
}
