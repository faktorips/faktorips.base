/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
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

    public static final int TYPE_WHOLE_CONTENT_CHANGED = 1;

    public static final int TYPE_PROPERTY_CHANGED = 2;

    public static final int TYPE_PART_ADDED = 4;

    public static final int TYPE_PART_REMOVED = 8;

    public static final int TYPE_PARTS_CHANGED_POSITIONS = 16;

    private final IIpsSrcFile ipsSrcFile;

    private final IIpsObjectPart part;

    private final List<IIpsObjectPart> movedParts;

    private final int type;

    private final PropertyChangeEvent propertyChangeEvent;

    private ContentChangeEvent(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
        type = TYPE_WHOLE_CONTENT_CHANGED;
        propertyChangeEvent = new PropertyChangeEvent(ipsSrcFile, null, null, null);
        part = null;
        movedParts = null;
    }

    private ContentChangeEvent(IIpsObjectPart part, int eventType) {
        ArgumentCheck.notNull(part);
        this.part = part;
        ipsSrcFile = part.getIpsObject().getIpsSrcFile();
        type = eventType;
        propertyChangeEvent = new PropertyChangeEvent(part, null, null, null);
        movedParts = null;
    }

    private ContentChangeEvent(IIpsSrcFile file, IIpsObjectPart[] parts) {
        ipsSrcFile = file;
        movedParts = Collections.unmodifiableList(Arrays.asList(parts));
        type = TYPE_PARTS_CHANGED_POSITIONS;
        propertyChangeEvent = new PropertyChangeEvent(file, null, null, null);
        part = null;
    }

    private ContentChangeEvent(List<? extends IIpsObjectPart> parts) {
        movedParts = Collections.unmodifiableList(parts);
        type = TYPE_PARTS_CHANGED_POSITIONS;
        ipsSrcFile = parts.get(0).getIpsSrcFile();
        part = null;
        propertyChangeEvent = new PropertyChangeEvent(ipsSrcFile, null, null, null);
    }

    private ContentChangeEvent(IIpsObjectPart part, PropertyChangeEvent propertyChangeEvent) {
        ArgumentCheck.notNull(part);
        this.part = part;
        ipsSrcFile = part.getIpsSrcFile();
        type = TYPE_PROPERTY_CHANGED;
        this.propertyChangeEvent = propertyChangeEvent;
        movedParts = null;
    }

    public static final ContentChangeEvent newPartAddedEvent(IIpsObjectPart part) {
        return new ContentChangeEvent(part, TYPE_PART_ADDED);
    }

    public static final ContentChangeEvent newPartRemovedEvent(IIpsObjectPart part) {
        return new ContentChangeEvent(part, TYPE_PART_REMOVED);
    }

    public static final ContentChangeEvent newPartChangedEvent(IIpsObjectPart part) {
        return new ContentChangeEvent(part, TYPE_PROPERTY_CHANGED);
    }

    public static final ContentChangeEvent newPartChangedEvent(IIpsObjectPart part,
            PropertyChangeEvent propertyChangeEvent) {
        ArgumentCheck.notNull(propertyChangeEvent);
        return new ContentChangeEvent(part, propertyChangeEvent);
    }

    public static final ContentChangeEvent newPartsChangedPositionsChangedEvent(IIpsSrcFile file, IIpsObjectPart[] parts) {
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
    public static final ContentChangeEvent newPartsChangedPositionsChangedEvent(IIpsSrcFile file,
            List<? extends IIpsObjectPart> parts) {

        return newPartsChangedPositionsChangedEvent(parts);
    }

    public static final ContentChangeEvent newPartsChangedPositionsChangedEvent(List<? extends IIpsObjectPart> parts) {
        return new ContentChangeEvent(parts);
    }

    public static final ContentChangeEvent newWholeContentChangedEvent(IIpsSrcFile file) {
        return new ContentChangeEvent(file);
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

    /**
     * Checks whether the given partContainer is affected by this change event.
     * <p>
     * Returns <code>true</code> in following cases:
     * <ul>
     * <li>The given partContainer is the same as the part changed by this event</li>
     * <li>The {@link IIpsSrcFile} of the given partContainer matches the changed
     * {@link IIpsSrcFile} of this change event and it is of type
     * {@link #TYPE_WHOLE_CONTENT_CHANGED}</li>
     * <li>The part changed by this event is a child of the given partContainer</li>
     * <li>The given partContainer is affected by a move operation because it is moved directly or
     * it is the parent of a moved part</li>
     * </ul>
     * 
     */
    public boolean isAffected(IIpsObjectPartContainer partContainer) {
        if (partContainer == null) {
            return false;
        }
        if (partContainer == part) {
            return true;
        }
        if (isAffectedIpsSrcFile(partContainer)) {
            return true;
        }
        if (isChildOf(part, partContainer)) {
            return true;
        }
        if (movedParts == null) {
            return false;
        }
        for (IIpsObjectPart movedPart : movedParts) {
            if (movedPart == partContainer || isChildOf(movedPart, partContainer)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAffectedIpsSrcFile(IIpsObjectPartContainer partContainer) {
        return partContainer.getIpsSrcFile() != null && partContainer.getIpsSrcFile().equals(ipsSrcFile)
                && (type == TYPE_WHOLE_CONTENT_CHANGED);
    }

    private boolean isChildOf(IIpsObjectPart potentialChild, IIpsObjectPartContainer potentialParent) {
        if (potentialChild == null) {
            return false;
        }
        IIpsElement parent = potentialChild.getParent();
        if (parent == null) {
            return false;
        } else if (potentialParent.equals(parent)) {
            return true;
        } else {
            if (parent instanceof IIpsObjectPart) {
                return isChildOf((IIpsObjectPart)parent, potentialParent);
            }
        }
        return false;
    }

    public boolean isPropertyAffected(String propertyName) {
        if (propertyChangeEvent == null || propertyName == null) {
            return false;
        }
        return propertyName.equals(propertyChangeEvent.getPropertyName());
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
        for (IIpsObjectPart movedPart : movedParts) {
            if (type.isAssignableFrom(movedPart.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ContentChangeEvent for " + ipsSrcFile; //$NON-NLS-1$
    }

    public PropertyChangeEvent getPropertyChangeEvent() {
        return propertyChangeEvent;
    }

}
