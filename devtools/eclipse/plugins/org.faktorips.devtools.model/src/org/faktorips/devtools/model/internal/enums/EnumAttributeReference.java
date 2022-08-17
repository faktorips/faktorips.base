/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import org.faktorips.devtools.model.IIpsMetaObject;
import org.faktorips.devtools.model.internal.ipsobject.PartReference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EnumAttributeReference extends PartReference {

    /** The XML tag for this IPS object part. */
    static final String XML_TAG = "EnumAttributeReference"; //$NON-NLS-1$

    /**
     * Creates a new <code>EnumAttributeReference</code>.
     * 
     * @param parent The <code>IEnumContent</code> this <code>EnumAttributeReference</code> belongs
     *            to.
     * @param id A unique ID for this <code>EnumAttributeReference</code>.
     */
    public EnumAttributeReference(IIpsMetaObject parent, String id) {
        super(parent, id);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

}
