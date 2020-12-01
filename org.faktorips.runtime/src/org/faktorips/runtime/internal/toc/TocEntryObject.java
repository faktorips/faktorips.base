/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

import org.faktorips.values.ObjectUtil;
import org.w3c.dom.Element;

/**
 * A toc entry that represents a product component, a table, a test case or an enum content
 * identified by the qualified name. Each entry gives access to the class implementing either the
 * product component, the table, the test case or the enum content.
 * 
 * @author Jan Ortmann
 */
public abstract class TocEntryObject extends TocEntry {

    public static final String PROPERTY_ENTRYTYPE = "entryType";
    public static final String PROPERTY_IPS_OBJECT_ID = "ipsObjectId";
    public static final String PROPERTY_IPS_OBJECT_QNAME = "ipsObjectQualifiedName";

    /**
     * The identifier of the ips object (either the qualified name for a table or the runtime id for
     * a product component).
     */
    private String ipsObjectId;

    /** The qualified name of the ips object. */
    private String ipsObjectQualifiedName;

    protected TocEntryObject(String ipsObjectId, String ipsObjectQualifiedName, String xmlResourceName,
            String implementationClassName) {
        super(implementationClassName, xmlResourceName);
        this.ipsObjectId = ipsObjectId;
        this.ipsObjectQualifiedName = ipsObjectQualifiedName;
    }

    /**
     * Returns the id for the object (either the runtime id if the object is a product component or
     * the qualified name if the object is a table
     */
    public String getIpsObjectId() {
        return ipsObjectId;
    }

    /**
     * Returns the qualified name of the object.
     */
    public String getIpsObjectQualifiedName() {
        return ipsObjectQualifiedName;
    }

    @Override
    protected void addToXml(Element entryElement) {
        super.addToXml(entryElement);
        entryElement.setAttribute(PROPERTY_IPS_OBJECT_ID, ipsObjectId);
        entryElement.setAttribute(PROPERTY_IPS_OBJECT_QNAME, ipsObjectQualifiedName);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("TocEntry(").append(getXmlElementTag()).append(':').append(ipsObjectId)
                .append(')').toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((ipsObjectId == null) ? 0 : ipsObjectId.hashCode());
        result = prime * result + ((ipsObjectQualifiedName == null) ? 0 : ipsObjectQualifiedName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof TocEntryObject)) {
            return false;
        }
        TocEntryObject other = (TocEntryObject)obj;
        if (!ObjectUtil.equals(ipsObjectId, other.ipsObjectId)) {
            return false;
        }
        if (!ObjectUtil.equals(ipsObjectQualifiedName, other.ipsObjectQualifiedName)) {
            return false;
        }
        return super.equals(obj);
    }

}
