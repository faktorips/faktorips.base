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

package org.faktorips.devtools.core.ui.search.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.junit.Test;

public class ModelSearchPresentationModelTest {

    @Test
    public void testSearchClazzes() {
        ModelSearchPresentationModel presentationModel = new ModelSearchPresentationModel();

        assertTrue(presentationModel.getSearchedClazzes().contains(IAssociation.class));

        presentationModel.setSearchAssociations(false);
        assertFalse(presentationModel.getSearchedClazzes().contains(IAssociation.class));

        presentationModel.setSearchAssociations(true);
        assertTrue(presentationModel.getSearchedClazzes().contains(IAssociation.class));
    }

    @Test
    public void testDialogSettings() {
        ModelSearchPresentationModel presentationModelBasis = new ModelSearchPresentationModel();

        String searchTerm = "search";
        String typeName = "type";

        presentationModelBasis.setSearchTerm(searchTerm);
        presentationModelBasis.setSrcFilePattern(typeName);

        presentationModelBasis.setSearchAssociations(false);
        presentationModelBasis.setSearchAttributes(false);

        IDialogSettings settings = new DialogSettings("section");

        presentationModelBasis.store(settings);

        ModelSearchPresentationModel presentationModelTest = new ModelSearchPresentationModel();

        presentationModelTest.read(settings);

        assertEquals(searchTerm, presentationModelTest.getSearchTerm());
        assertEquals(typeName, presentationModelTest.getSrcFilePattern());

        assertFalse(presentationModelTest.isSearchAttributes());
        assertFalse(presentationModelTest.isSearchAssociations());
        assertTrue(presentationModelTest.isSearchMethods());
    }

}
