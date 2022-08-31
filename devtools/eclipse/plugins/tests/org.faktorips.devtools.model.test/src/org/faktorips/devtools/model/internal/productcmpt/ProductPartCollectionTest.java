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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ProductPartCollectionTest {

    @Mock
    private ProductCmptLinkCollection productCmptLinkCollection;

    @Mock
    private PropertyValueCollection propertyValueCollection;

    private ProductPartCollection productPartCollection;

    @Before
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
