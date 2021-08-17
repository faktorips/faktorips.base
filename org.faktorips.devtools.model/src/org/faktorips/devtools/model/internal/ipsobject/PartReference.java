/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import org.faktorips.devtools.model.IIpsMetaObject;
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

public abstract class PartReference extends AtomicIpsObjectPart implements IPartReference {

    public PartReference(IIpsMetaObject parent, String id) {
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
    protected void initPropertiesFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.removeAttribute(IpsObjectPart.PROPERTY_ID);
        element.setAttribute(PROPERTY_NAME, name);
    }
}
