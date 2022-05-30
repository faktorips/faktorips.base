/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

/**
 * A {@link TocEntryObject} representing a enum xml adapter element
 * 
 * @author dirmeier
 */
public class EnumXmlAdapterTocEntry extends TocEntryObject {

    public static final String XML_TAG = "EnumXmlAdapter";

    public EnumXmlAdapterTocEntry(String ipsObjectId, String qualifiedName, String implementationClassName) {
        super(ipsObjectId, qualifiedName, "", implementationClassName);
    }

    @Override
    protected String getXmlElementTag() {
        return XML_TAG;
    }

}
