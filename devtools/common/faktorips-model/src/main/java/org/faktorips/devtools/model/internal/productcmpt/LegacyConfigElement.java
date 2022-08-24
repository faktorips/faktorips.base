/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is only used to load the legacy product component XML. It is not used as regular part
 * and is not added to any composite structure!
 * 
 */
public class LegacyConfigElement extends IpsObjectPart {

    public static final String XML_TAG = ValueToXmlHelper.LEGACY_XML_TAG_CONFIG_ELEMENT;

    private final PropertyValueCollection collection;

    public LegacyConfigElement(PropertyValueCollection collection) {
        super();
        this.collection = collection;
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        return new IIpsElement[0];
    }

    @Override
    protected Element createElement(Document doc) {
        return null;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        // do nothing
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        return collection.newPropertyValue(xmlTag.getNodeName(), getNextPartId());
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        return null;
    }

}
