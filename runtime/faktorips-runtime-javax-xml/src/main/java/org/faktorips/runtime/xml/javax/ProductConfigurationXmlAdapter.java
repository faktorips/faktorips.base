/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.xml.javax;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductConfiguration;
import org.faktorips.runtime.xml.IIpsXmlAdapter;
import org.faktorips.runtime.xml.IpsProductConfigurationXmlAdapter;

/**
 * Custom JAXB marshalling/unmarshalling for {@link ProductConfiguration} instances.
 * <p>
 * When marshalling/unmarshalling a configurable policy component (and thus a
 * {@link ProductConfiguration}), the respective product component is preserved in XML by the means
 * of the product component ID.
 */
public class ProductConfigurationXmlAdapter extends XmlAdapter<String, ProductConfiguration>
        implements IIpsXmlAdapter<String, ProductConfiguration> {

    private final IpsProductConfigurationXmlAdapter ipsProductConfigurationXmlAdapter;

    public ProductConfigurationXmlAdapter(IRuntimeRepository repository) {
        ipsProductConfigurationXmlAdapter = new IpsProductConfigurationXmlAdapter(repository);
    }

    @SuppressWarnings("unused")
    private ProductConfigurationXmlAdapter() {
        // JAXB/MOXy needs to instantiate this adapter but never uses this instance.
        ipsProductConfigurationXmlAdapter = null;
    }

    /**
     * Returns the ID for the configuring product component or <code>null</code> if it could not be
     * determined. Cannot return an ID if either the given {@link ProductConfiguration} is
     * <code>null</code> or if it does not contain a {@link IProductComponent} instance.
     */
    @Override
    public String marshal(ProductConfiguration config) throws Exception {
        return ipsProductConfigurationXmlAdapter.marshal(config);
    }

    /**
     * Returns the product component for the specified ID.
     */
    @Override
    public ProductConfiguration unmarshal(String id) throws Exception {
        return ipsProductConfigurationXmlAdapter.unmarshal(id);
    }

}
