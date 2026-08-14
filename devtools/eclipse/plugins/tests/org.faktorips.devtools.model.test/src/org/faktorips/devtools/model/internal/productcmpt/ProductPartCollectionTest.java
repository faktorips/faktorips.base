/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.mockito.Mockito.verify;

import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.IPropertyValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductPartCollectionTest {

    @Mock
    private ProductCmptLinkCollection productCmptLinkCollection;

    @Mock
    private PropertyValueCollection propertyValueCollection;

    private ProductPartCollection productPartCollection;

    @BeforeEach
    public void createProductPartCollection() throws Exception {
        productPartCollection = new ProductPartCollection(propertyValueCollection, productCmptLinkCollection);
    }

    @Test
    public void testGetProductParts_propertyValue() throws Exception {
        productPartCollection.getProductParts(IPropertyValue.class);

        verify(propertyValueCollection).getPropertyValues(IPropertyValue.class);
    }

    @Test
    public void testGetProductParts_propertyValueSubclass() throws Exception {
        productPartCollection.getProductParts(IAttributeValue.class);

        verify(propertyValueCollection).getPropertyValues(IAttributeValue.class);
    }

    @Test
    public void testGetProductParts_link() throws Exception {
        productPartCollection.getProductParts(IProductCmptLink.class);

        verify(productCmptLinkCollection).getLinks();
    }

}
