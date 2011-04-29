/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
