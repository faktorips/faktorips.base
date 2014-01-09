/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums;

import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <tt>IEnumAttributeReference</tt>, see the corresponding interface for more
 * details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumAttributeReference
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.4
 */
public class EnumAttributeReference extends AtomicIpsObjectPart implements IEnumAttributeReference {

    /**
     * Creates a new <tt>IEnumAttributeReference</tt>.
     * 
     * @param parent The <tt>IEnumContent</tt> this <tt>IEnumAttributeReference</tt> belongs to.
     * @param id A unique ID for this <tt>IEnumAttributeReference</tt>.
     */
    public EnumAttributeReference(IEnumContent parent, String id) {
        super(parent, id);
    }

    @Override
    public void setName(String name) {
        ArgumentCheck.notNull(name);

        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
    }

}
