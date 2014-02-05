/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

/**
 * Custom JAXB marshaling/unmarshaling for product components.
 * <p>
 * When marshaling/unmarshaling a configurable policy component, the link to its product component
 * is preserved in XML by the means of the product component ID.
 */
public class ProductComponentXmlAdapter extends XmlAdapter<String, IProductComponent> {

    private IRuntimeRepository repository;

    public ProductComponentXmlAdapter() {
        // nothing to do
    }

    public ProductComponentXmlAdapter(IRuntimeRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the ID for the specified product component.
     */
    @Override
    public String marshal(IProductComponent v) throws Exception {
        if (v == null) {
            return null;
        }

        return v.getId();
    }

    /**
     * Returns the product component for the specified ID.
     */
    @Override
    public IProductComponent unmarshal(String id) throws Exception {
        if (id == null) {
            return null;
        }

        return repository.getProductComponent(id);
    }

}
