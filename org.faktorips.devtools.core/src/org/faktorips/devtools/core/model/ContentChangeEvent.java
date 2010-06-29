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

package org.faktorips.devtools.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.ArgumentCheck;

/**
 * An event that signals the change of an IPS source file's content.
 */
public class ContentChangeEvent {

    public final static int TYPE_WHOLE_CONTENT_CHANGED = 1;

    public final static int TYPE_PROPERTY_CHANGED = 2;

    public final static int TYPE_PART_ADDED = 4;

    public final static int TYPE_PART_REMOVED = 8;

    public final static int TYPE_PARTS_CHANGED_POSITIONS = 16;

    public final static ContentChangeEvent newPartAddedEvent(IIpsObjectPart part) {
        return new ContentChangeEvent(part, TYPE_PART_ADDED);
    }

    public final static ContentChangeEvent newPartRemovedEvent(IIpsObjectPart part) {
        return new ContentChangeEvent(part, TYPE_PART_REMOVED);
    }

    public final static ContentChangeEvent newPartChangedEvent(IIpsObjectPart part) {
        return new ContentChangeEvent(part, TYPE_PROPERTY_CHANGED);
    }

    public final static ContentChangeEvent newPartsChangedPositionsChangedEvent(IIpsSrcFile file, IIpsObjectPart[] parts) {
        return new ContentChangeEvent(file, parts);
    }

    /**
     * @deprecated Use {@link #newPartsChangedPositionsChangedEvent(List)} instead as the IPS source
     *             file is not relevant in this case.
     */
    @Deprecated
    // Deprecated since 3.0
    @SuppressWarnings("unused")
    // OK to suppress because the method is deprecated
    public final static ContentChangeEvent newPartsChangedPositionsChangedEvent(IIpsSrcFile file,
            List<? extends IIpsObjectPart> parts) {

        return newPartsChangedPositionsChangedEvent(parts);
    }

    public final static ContentChangeEvent newPartsChangedPositionsChangedEvent(List<? extends IIpsObjectPart> parts) {
        return new ContentChangeEvent(parts);
    }

    public final static ContentChangeEvent newWholeContentChangedEvent(IIpsSrcFile file) {
        return new ContentChangeEvent(file);
    }

    private IIpsSrcFile ipsSrcFile;

    private IIpsObjectPart part;

    private List<IIpsObjectPart> movedParts = null;

    private int type = TYPE_PROPERTY_CHANGED;

    private ContentChangeEvent(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
        type = TYPE_WHOLE_CONTENT_CHANGED;
    }

    private ContentChangeEvent(IIpsObjectPart part, int eventType) {
        ArgumentCheck.notNull(part);
        this.part = part;
        ipsSrcFile = part.getIpsObject().getIpsSrcFile();
        type = eventType;
    }

    private ContentChangeEvent(IIpsSrcFile file, IIpsObjectPart[] parts) {
        ipsSrcFile = file;
        movedParts = new ArrayList<IIpsObjectPart>();
        for (IIpsObjectPart part2 : parts) {
            movedParts.add(part2);
        }
        type = TYPE_PARTS_CHANGED_POSITIONS;
    }

    private ContentChangeEvent(List<? extends IIpsObjectPart> parts) {
        movedParts = Collections.unmodifiableList(parts);
    }

    /**
     * Returns the source file which contents has changed.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

    /**
     * Returns event type.
     * 
     * @see #TYPE_PART_ADDED
     * @see #TYPE_PART_REMOVED
     * @see #TYPE_PARTS_CHANGED_POSITIONS
     * @see #TYPE_PROPERTY_CHANGED
     */
    public int getEventType() {
        return type;
    }

    /**
     * Returns the part that was either changed, added, or removed. Returns <code>null</code> if
     * this information is not available.
     */
    public IIpsObjectPart getPart() {
        return part;
    }

    public IIpsObjectPart[] getMovedParts() {
        if (movedParts == null) {
            return new IIpsObjectPart[0];
        }
        return movedParts.toArray(new IIpsObjectPart[movedParts.size()]);
    }

    public boolean isAffected(IIpsObjectPartContainer partContainer) {
        if (partContainer == null) {
            return false;
        }
        if (partContainer == part) {
            return true;
        }
        if (partContainer.getIpsSrcFile() != null && partContainer.getIpsSrcFile().equals(ipsSrcFile)) {
            return true;
        }
        if (type == TYPE_WHOLE_CONTENT_CHANGED) {
            return true;
        }
        if (movedParts == null) {
            return false;
        }
        for (IIpsObjectPart part : movedParts) {
            if (part == partContainer) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAffectedObjects(Class<?> type) {
        ArgumentCheck.notNull(type);
        if (part != null) {
            if (type.isAssignableFrom(part.getClass())) {
                return true;
            }
        }
        if (movedParts == null) {
            return false;
        }
        for (IIpsObjectPart part : movedParts) {
            if (type.isAssignableFrom(part.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ContentChangeEvent for " + ipsSrcFile; //$NON-NLS-1$
    }

}
