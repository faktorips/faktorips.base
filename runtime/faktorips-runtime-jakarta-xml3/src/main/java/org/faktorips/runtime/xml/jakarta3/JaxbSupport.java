/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.xml.jakarta3;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.xml.XmlBindingSupportHelper;
import org.faktorips.runtime.xml.IIpsXmlAdapter;
import org.faktorips.runtime.xml.IXmlBindingSupport;

import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Jakarta {@link JAXB} version of the {@link IXmlBindingSupport}, to be used with Java EE 9 or
 * newer.
 */
public enum JaxbSupport implements IXmlBindingSupport<JAXBContext> {
    INSTANCE;

    private static final XmlBindingSupportHelper<JAXBContext> HELPER = new XmlBindingSupportHelper<>(
            XmlRootElement.class, t -> {
                try {
                    return JAXBContext.newInstance(t);
                } catch (JAXBException e) {
                    throw new RuntimeException(e);
                }
            }, IpsJAXBContext::new);

    @Override
    public JAXBContext newJAXBContext(IRuntimeRepository repository) {
        return HELPER.newJAXBContext(repository);
    }

    @Override
    public JAXBContext newJAXBContext(JAXBContext ctx, IRuntimeRepository repository) {
        return HELPER.newJAXBContext(ctx, repository);
    }

    @SuppressWarnings("unchecked")
    public static <ValueType, BoundType> XmlAdapter<ValueType, BoundType> wrap(
            IIpsXmlAdapter<ValueType, BoundType> xmlAdapter) {
        if (xmlAdapter instanceof XmlAdapter) {
            return (XmlAdapter<ValueType, BoundType>)xmlAdapter;
        }
        return new XmlAdapter<>() {

            @Override
            public BoundType unmarshal(ValueType v) throws Exception {
                return xmlAdapter.unmarshal(v);
            }

            @Override
            public ValueType marshal(BoundType v) throws Exception {
                return xmlAdapter.marshal(v);
            }
        };
    }
}
