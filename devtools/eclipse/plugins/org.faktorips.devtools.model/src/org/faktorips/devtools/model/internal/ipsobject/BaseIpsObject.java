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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.w3c.dom.Element;

/**
 * Base class that allows to implement IPS object subclasses in a simple way. The handling of parts
 * of this object is done using IpsObjectPartCollections.
 * 
 * @see IpsObjectPartCollection
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public abstract class BaseIpsObject extends IpsObject {

    private final List<IpsObjectPartCollection<?>> partCollections = new ArrayList<>(1);

    protected BaseIpsObject(IIpsSrcFile file) {
        super(file);
    }

    protected final void addPartCollection(IpsObjectPartCollection<?> container) {
        partCollections.add(container);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        List<IIpsElement> children = new ArrayList<>();
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
            IIpsObjectPart part = container.newPart(partType);
            if (part != null) {
                return part;
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
