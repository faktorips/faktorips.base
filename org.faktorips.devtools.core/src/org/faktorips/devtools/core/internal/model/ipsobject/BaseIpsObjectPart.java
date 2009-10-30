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
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.w3c.dom.Element;

/**
 * Base class that allows to implement subclasses for an ips object part that contains other parts
 * in a simple way. The handling of the contained parts is done using IpsObjectPartCollections.
 * 
 * @see IpsObjectPartCollection
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public abstract class BaseIpsObjectPart extends IpsObjectPart {

    private List tagsToIgnore = new ArrayList(0);
    private List partCollections = new ArrayList(1);

    /**
     * @param parent
     * @param id
     */
    public BaseIpsObjectPart(IIpsObject parent, int id) {
        super(parent, id);
    }

    /**
     * @param parent
     * @param id
     */
    public BaseIpsObjectPart(IIpsObjectPart parent, int id) {
        super(parent, id);
    }

    protected void addPartCollection(IpsObjectPartCollection container) {
        partCollections.add(container);
    }

    protected void addTagToIgnore(String xmlTagName) {
        tagsToIgnore.add(xmlTagName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsElement[] getChildren() {
        List result = new ArrayList();
        for (Iterator it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = (IpsObjectPartCollection)it.next();
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
    @Override
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        for (Iterator it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = (IpsObjectPartCollection)it.next();
            IIpsObjectPart part = container.newPart(xmlTag, id);
            if (part != null) {
                return part;
            }
        }
        if (tagsToIgnore.contains(xmlTag.getNodeName())) {
            return null;
        }
        throw new RuntimeException("Could not create part for xml element " + xmlTag.getNodeName()); //$NON-NLS-1$
    }

    /**
     * Renamed to {@link #addPart(IIpsObjectPart)}
     * 
     * @param part
     */
    @Deprecated
    protected void reAddPart(IIpsObjectPart part) {
        addPart(part);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPart(IIpsObjectPart part) {
        for (Iterator it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = (IpsObjectPartCollection)it.next();
            if (container.addPart(part)) {
                return;
            }
        }
        throw new IllegalArgumentException("Could not re-add part " + part); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reinitPartCollections() {
        for (Iterator it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = (IpsObjectPartCollection)it.next();
            container.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removePart(IIpsObjectPart part) {
        for (Iterator it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = (IpsObjectPartCollection)it.next();
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
        for (Iterator it = partCollections.iterator(); it.hasNext();) {
            IpsObjectPartCollection container = (IpsObjectPartCollection)it.next();
            IIpsObjectPart newPart = container.newPart(partType);
            if (newPart != null) {
                return newPart;
            }
        }
        throw new IllegalArgumentException("Could not create a new part for class " + partType); //$NON-NLS-1$
    }

}
