/***************************************************************************************************
 * Copyright (c) 2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.values.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Can be used to map interfaces.
 * 
 * @see "https://jaxb.dev.java.net/guide/Mapping_interfaces.html"
 */
public class AnyTypeAdapter extends XmlAdapter<Object, Object> {

    @Override
    public Object unmarshal(Object v) {
        return v;
    }

    @Override
    public Object marshal(Object v) {
        return v;
    }

}
