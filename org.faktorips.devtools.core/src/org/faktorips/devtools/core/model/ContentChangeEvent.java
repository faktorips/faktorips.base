/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.util.ArgumentCheck;

/**
 * An event that signals the change of an ips source file's content. 
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

    public final static ContentChangeEvent newWholeContentChangedEvent(IIpsSrcFile file) {
        return new ContentChangeEvent(file);
    }
    
    private IIpsSrcFile ipsSrcFile;
    
    private IIpsObjectPart part;
    
    private IIpsObjectPart[] movedParts = null;
    
    private int type = TYPE_PROPERTY_CHANGED;
    
    private ContentChangeEvent(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
        this.type = TYPE_WHOLE_CONTENT_CHANGED;
    }
    
    private ContentChangeEvent(IIpsObjectPart part, int eventType) {
        ArgumentCheck.notNull(part);
        this.part = part;
        this.ipsSrcFile = part.getIpsObject().getIpsSrcFile();
        this.type = eventType;
    }
    
    private ContentChangeEvent(IIpsSrcFile file, IIpsObjectPart[] parts) {
        this.ipsSrcFile = file;
        this.movedParts = parts;
        this.type = TYPE_PARTS_CHANGED_POSITIONS;
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
     * Returns the part that was either changed, added, or removed. 
     * Returns <code>null</code> if this information is not available.
     */
    public IIpsObjectPart getPart() {
        return part;
    }
    
    public IIpsObjectPart[] getMovedParts() {
        if (movedParts==null) {
            return new IIpsObjectPart[0];
        }
        return movedParts;
    }
    
    public boolean isAffected(IIpsObjectPart part) {
        if (part==null) {
            return false;
        }
        if (part==this.part) {
            return true;
        }
        if (movedParts==null) {
            return false;
        }
        for (int i = 0; i < movedParts.length; i++) {
            if (movedParts[i] == part) {
                return true;
            }
        }
        return false;
    }
    
    public boolean containsAffectedObjects(Class type) {
        ArgumentCheck.notNull(type);
        if (part!=null) {
            if (type.isAssignableFrom(part.getClass())) {
                return true;
            }
        }
        if (movedParts==null) {
            return false;
        }
        for (int i = 0; i < movedParts.length; i++) {
            if (type.isAssignableFrom(movedParts[i].getClass())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "ContentChangeEvent for " + ipsSrcFile; //$NON-NLS-1$
    }
    
    
}
