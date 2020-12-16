/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductConfiguration;

/**
 * Custom JAXB marshaling/unmarshaling for {@link ProductConfiguration} instances.
 * <p>
 * When marshaling/unmarshaling a configurable policy component (and thus a
 * {@link ProductConfiguration}), the respective product component is preserved in XML by the means
 * of the product component ID.
 */
public class ProductConfigurationXmlAdapter extends XmlAdapter<String, ProductConfiguration> {

    private final IRuntimeRepository repository;

    public ProductConfigurationXmlAdapter(IRuntimeRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the ID for the configuring product component or <code>null</code> if it could not be
     * determined. Cannot return an ID if either the given {@link ProductConfiguration} is
     * <code>null</code> or if it does not contain a {@link IProductComponent} instance.
     */
    @Override
    public String marshal(ProductConfiguration config) throws Exception {
        if (config == null || config.getProductComponent() == null) {
            return null;
        }
        return config.getProductComponent().getId();
    }

    /**
     * Returns the product component for the specified ID.
     */
    @Override
    public ProductConfiguration unmarshal(String id) throws Exception {
        IProductComponent productComponent = getProductComponentFor(id);
        return new ProductConfiguration(productComponent);
    }

    private IProductComponent getProductComponentFor(String id) {
        IProductComponent productComponent;
        if (id == null) {
            return null;
        } else {
            productComponent = repository.getProductComponent(id);
        }
        return productComponent;
    }

}
