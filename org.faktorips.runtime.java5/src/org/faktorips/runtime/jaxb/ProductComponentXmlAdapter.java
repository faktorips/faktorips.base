/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
