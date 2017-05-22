/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.enums;

import org.faktorips.devtools.core.internal.model.PartReference;
import org.faktorips.devtools.core.model.IIpsMetaObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EnumAttributeReference extends PartReference {

    /** The XML tag for this IPS object part. */
    static final String XML_TAG = "EnumAttributeReference"; //$NON-NLS-1$

    /**
     * Creates a new <tt>EnumAttributeReference</tt>.
     * 
     * @param parent The <tt>IEnumContent</tt> this <tt>EnumAttributeReference</tt> belongs to.
     * @param id A unique ID for this <tt>EnumAttributeReference</tt>.
     */
    public EnumAttributeReference(IIpsMetaObject parent, String id) {
        super(parent, id);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

}
