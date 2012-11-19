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

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.mockito.Mockito.verify;

import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
