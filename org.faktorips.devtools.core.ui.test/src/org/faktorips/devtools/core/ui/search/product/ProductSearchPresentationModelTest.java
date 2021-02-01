/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.beans.PropertyChangeListener;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.product.conditions.types.EqualitySearchOperatorType;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ProductAttributeConditionType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;

public class ProductSearchPresentationModelTest extends AbstractIpsPluginTest {

    private ProductSearchPresentationModel model;
    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IPolicyCmptType policyCmptType;
    private String FILE_PATTERN = "srcFilePattern";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mock(PropertyChangeListener.class);

        model = new ProductSearchPresentationModel();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Vertrag", "VertragsArt");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);

        model.setProductCmptType(productCmptType);
    }

    @Test
    public void testConditionsWithSearchableElements() {
        assertTrue(model.getAvailableConditionTypes().isEmpty());

        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType
                .newPolicyCmptTypeAttribute("VertragsAttribut");
        policyCmptTypeAttribute.setValueSetConfiguredByProduct(true);

        assertEquals(1, model.getAvailableConditionTypes().size());

        productCmptType.newProductCmptTypeAttribute("ProduktAttribut");

        assertEquals(2, model.getAvailableConditionTypes().size());

        productCmptType.newAssociation();

        assertEquals(3, model.getAvailableConditionTypes().size());
    }

    @Test
    public void testIsConditionTypeAvailable() {
        assertFalse(model.isConditionTypeAvailable());
        productCmptType.newProductCmptTypeAttribute("ProduktAttribut");
        assertTrue(model.isConditionTypeAvailable());
    }

    @Test
    public void testIsConditionDefined() {
        assertFalse(model.isConditionDefined());
        model.createProductSearchConditionPresentationModel();
        assertTrue(model.isConditionDefined());
    }

    @Test
    public void testIsProductCmptTypeChosen() {
        assertTrue(model.isProductCmptTypeChosen());
        assertFalse(new ProductSearchPresentationModel().isProductCmptTypeChosen());
    }

    @Test
    public void testIsValid() {
        assertFalse(new ProductSearchPresentationModel().isValid());

        assertTrue(model.isValid());

        model.createProductSearchConditionPresentationModel();

        assertFalse(model.isValid());

        ProductSearchConditionPresentationModel conditionPresentationModel = model
                .getProductSearchConditionPresentationModels().get(0);

        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("attribute");
        attribute.setDatatype("Integer");

        conditionPresentationModel.setCondition(new ProductAttributeConditionType());
        conditionPresentationModel.setSearchedElement(attribute);
        conditionPresentationModel.setOperatorType(EqualitySearchOperatorType.EQUALITY);

        assertTrue(model.isValid());
    }

    @Test
    public void testDialogSettings() {
        model.setSrcFilePattern(FILE_PATTERN);
        model.setProductCmptType(productCmptType);
        model.setIpsProjectName(ipsProject.getName());

        IDialogSettings settings = new DialogSettings("section");
        model.store(settings);
        ProductSearchPresentationModel newModel = new ProductSearchPresentationModel();
        newModel.read(settings);

        assertEquals(FILE_PATTERN, newModel.getSrcFilePattern());
        assertEquals(ipsProject.getName(), newModel.getIpsProjectName());
        assertEquals(productCmptType, newModel.getProductCmptType());
    }
}
