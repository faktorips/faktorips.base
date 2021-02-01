/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

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
