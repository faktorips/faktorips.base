/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.devtools.model.type.IAssociation;
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
