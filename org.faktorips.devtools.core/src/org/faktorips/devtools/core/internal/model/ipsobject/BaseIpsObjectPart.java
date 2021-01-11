/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.w3c.dom.Element;

/**
 * Base class that allows to implement subclasses for an IPS object part that contains other parts
 * in a simple way. The handling of the contained parts is done using IpsObjectPartCollections.
 * 
 * @see IpsObjectPartCollection
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public abstract class BaseIpsObjectPart extends IpsObjectPart {

    private List<String> tagsToIgnore = new ArrayList<String>(0);

    private List<IpsObjectPartCollection<?>> partCollections = new ArrayList<IpsObjectPartCollection<?>>(1);

    public BaseIpsObjectPart(IIpsObjectPartContainer parent, String id) {
        super(parent, id);
    }

    protected void addPartCollection(IpsObjectPartCollection<?> container) {
        partCollections.add(container);
    }

    protected void addTagToIgnore(String xmlTagName) {
        tagsToIgnore.add(xmlTagName);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        List<IIpsElement> children = new ArrayList<IIpsElement>();
        for (IpsObjectPartCollection<?> container : partCollections) {
            int size = container.size();
            for (int i = 0; i < size; i++) {
                children.add(container.getPart(i));
            }
        }
        return children.toArray(new IIpsElement[children.size()]);
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        if (tagsToIgnore.contains(xmlTag.getNodeName())) {
            return null;
        }
        for (IpsObjectPartCollection<?> container : partCollections) {
            IIpsObjectPart part = container.newPart(xmlTag, id);
            if (part != null) {
                return part;
            }
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            IIpsObjectPart newPart = container.newPart(partType);
            if (newPart != null) {
                return newPart;
            }
        }
        return null;
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            if (container.addPart(part)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            if (container.removePart(part)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        for (IpsObjectPartCollection<?> container : partCollections) {
            container.clear();
        }
    }

}
