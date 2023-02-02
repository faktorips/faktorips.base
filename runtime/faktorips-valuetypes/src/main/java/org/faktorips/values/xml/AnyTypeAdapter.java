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
 * @deprecated for removal since 23.6. Use a custom adapter instead.
 */
@Deprecated
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
