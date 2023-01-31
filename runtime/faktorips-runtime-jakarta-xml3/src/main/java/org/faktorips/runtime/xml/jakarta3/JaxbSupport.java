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
import org.faktorips.runtime.xml.IXmlBindingSupport;

import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.JAXBContext;

/**
 * Jakarta {@link JAXB} version of the {@link IXmlBindingSupport}, to be used with Java EE 9 or
 * newer.
 */
public enum JaxbSupport implements IXmlBindingSupport<JAXBContext> {
    INSTANCE;

    @Override
    public JAXBContext newJAXBContext(IRuntimeRepository repository) {
        return newJAXBContext(null, repository);
    }

    @Override
    public JAXBContext newJAXBContext(JAXBContext ctx, IRuntimeRepository repository) {
        return null;
    }

}
