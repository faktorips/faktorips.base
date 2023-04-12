/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.xml;

import org.w3c.dom.Element;

/**
 * Interface for runtime objects implementing a method to write their properties to XML.
 */
// there is no toXml-Method in this interface, as that is historically already part of the IXmlPersistenceSupport interface,
// which unfortunately extends IClRepositoryObject which is incompatible with how e.g. tables are initialized.
public interface IToXmlSupport {

    /**
     * Subclasses override this method to write their properties into the given XML element.
     * <p>
     * The standard implementation throws an {@link UnsupportedOperationException} if the support
     * for toXml ("Generate toXml Support") is not activated in the FIPS standard builder. Generated
     * classes override but do <em>NOT</em> call super.
     *
     * @param element the XML element to write the properties to
     */
    void writePropertiesToXml(Element element);

    /**
     * This method may be used to write a check in a default toXml implementation.
     *
     * @param runtimeObject an object that might be an instance of {@link IToXmlSupport}
     * @throws UnsupportedOperationException if the given {@code runtimeObject} is no instance of
     *             {@link IToXmlSupport}.
     */
    static void check(Object runtimeObject) {
        if (!(runtimeObject instanceof IToXmlSupport)) {
            throw new UnsupportedOperationException(
                    "The method toXml is currently not supported, as the required writePropertiesToXml-methods were not generated. "
                            + "To activate toXml() please check your Faktor-IPS Builder settings and make sure \"Generate toXml Support\" is set to true.");
        }
    }
}
