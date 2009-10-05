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
import java.util.Iterator;
import java.util.List;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.w3c.dom.Element;

/**
 * Base class that allows to implement ips object subclasses in a simple way. The handling of parts
 * of this object is done using IpsObjectPartCollections.
 * 
 * @see IpsObjectPartCollection
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public abstract class BaseIpsObject extends IpsObject {

    private List<IpsObjectPartCollection> partCollections = new ArrayList<IpsObjectPartCollection>(1);

    protected BaseIpsObject(IIpsSrcFile file) {
        super(file);
    }

    protected void addPartCollection(IpsObjectPartCollection container) {
        partCollections.add(container);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        List<IIpsObjectPart> result = new ArrayList<IIpsObjectPart>();
        for (Iterator<IpsObjectPartCollection> it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = it.next();
            int size = container.size();
            for (int i = 0; i < size; i++) {
                result.add(container.getPart(i));
            }
        }

        return ((IIpsElement[])result.toArray(new IIpsElement[result.size()]));
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        for (Iterator<IpsObjectPartCollection> it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = it.next();
            IIpsObjectPart part = container.newPart(xmlTag, id);
            if (part != null) {
                return part;
            }
        }

        throw new RuntimeException("Could not create part for xml element " + xmlTag.getNodeName()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void addPart(IIpsObjectPart part) {
        for (Iterator<IpsObjectPartCollection> it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = it.next();
            if (container.addPart(part)) {
                return;
            }
        }

        throw new IllegalArgumentException("Could not re-add part " + part); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        for (Iterator<IpsObjectPartCollection> it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = it.next();
            container.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        for (Iterator<IpsObjectPartCollection> it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = it.next();
            if (container.removePart(part)) {
                return;
            }
        }

        return;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        for (Iterator<IpsObjectPartCollection> it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = it.next();
            IIpsObjectPart newPart = container.newPart(partType);
            if (newPart != null) {
                return newPart;
            }
        }

        throw new IllegalArgumentException("Could not create a new part for class " + partType); //$NON-NLS-1$
    }
}
