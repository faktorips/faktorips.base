/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
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

    private List<IpsObjectPartCollection<?>> partCollections = new ArrayList<IpsObjectPartCollection<?>>(1);

    protected BaseIpsObject(IIpsSrcFile file) {
        super(file);
    }

    protected void addPartCollection(IpsObjectPartCollection<?> container) {
        partCollections.add(container);
    }

    @Override
    public IIpsElement[] getChildren() {
        List<IIpsObjectPart> result = new ArrayList<IIpsObjectPart>();
        for (IpsObjectPartCollection<?> container : partCollections) {
            int size = container.size();
            for (int i = 0; i < size; i++) {
                result.add(container.getPart(i));
            }
        }

        return (result.toArray(new IIpsElement[result.size()]));
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            IIpsObjectPart part = container.newPart(xmlTag, id);
            if (part != null) {
                return part;
            }
        }

        throw new RuntimeException("Could not create part for xml element " + xmlTag.getNodeName()); //$NON-NLS-1$
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            if (container.addPart(part)) {
                return;
            }
        }

        throw new IllegalArgumentException("Could not re-add part " + part); //$NON-NLS-1$
    }

    @Override
    protected void reinitPartCollections() {
        for (IpsObjectPartCollection<?> container : partCollections) {
            container.clear();
        }
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            if (container.removePart(part)) {
                return;
            }
        }
    }

    @Override
    public IIpsObjectPart newPart(Class<?> partType) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            IIpsObjectPart newPart = container.newPart(partType);
            if (newPart != null) {
                return newPart;
            }
        }
        throw new IllegalArgumentException("Could not create a new part for class " + partType); //$NON-NLS-1$
    }

}