/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
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

    private final IIpsObjectPartContainer part;

    private final List<IIpsObjectPart> movedParts;

    private final int type;

    private final Set<PropertyChangeEvent> propertyChangeEvents;

    private ContentChangeEvent(IIpsSrcFile ipsSrcFile, PropertyChangeEvent... propertyChangeEvents) {
        this.ipsSrcFile = ipsSrcFile;
        type = TYPE_WHOLE_CONTENT_CHANGED;
        this.propertyChangeEvents = new LinkedHashSet<>(Arrays.asList(propertyChangeEvents));
        part = null;
        movedParts = null;
    }

    private ContentChangeEvent(IIpsSrcFile ipsSrcFile, Set<PropertyChangeEvent> propertyChangeEvents) {
        this.ipsSrcFile = ipsSrcFile;
        type = TYPE_WHOLE_CONTENT_CHANGED;
        this.propertyChangeEvents = propertyChangeEvents;
        part = null;
        movedParts = null;
    }

    private ContentChangeEvent(IIpsObjectPartContainer part, int eventType) {
        ArgumentCheck.notNull(part);
        this.part = part;
        ipsSrcFile = part.getIpsObject().getIpsSrcFile();
        type = eventType;
        propertyChangeEvents = Collections.emptySet();
        movedParts = null;
    }

    private ContentChangeEvent(IIpsSrcFile file, IIpsObjectPart[] parts) {
        ipsSrcFile = file;
        movedParts = Collections.unmodifiableList(Arrays.asList(parts));
        type = TYPE_PARTS_CHANGED_POSITIONS;
        propertyChangeEvents = Collections.emptySet();
        part = null;
    }

    private ContentChangeEvent(List<? extends IIpsObjectPart> parts) {
        movedParts = Collections.unmodifiableList(parts);
        type = TYPE_PARTS_CHANGED_POSITIONS;
        ipsSrcFile = parts.get(0).getIpsSrcFile();
        part = null;
        propertyChangeEvents = Collections.emptySet();
    }

    private ContentChangeEvent(IIpsObjectPartContainer part, PropertyChangeEvent... propertyChangeEvents) {
        ArgumentCheck.notNull(part);
        this.part = part;
        ipsSrcFile = part.getIpsSrcFile();
        type = TYPE_PROPERTY_CHANGED;
        this.propertyChangeEvents = new LinkedHashSet<>(Arrays.asList(propertyChangeEvents));
        movedParts = null;
    }

    private ContentChangeEvent(IIpsSrcFile srcFile, IIpsObjectPartContainer part, int type,
            Set<PropertyChangeEvent> propertyChangeEvents) {
        this.part = part;
        ipsSrcFile = srcFile;
        this.type = type;
        this.propertyChangeEvents = propertyChangeEvents;
        movedParts = null;
    }

    public static final ContentChangeEvent newPartAddedEvent(IIpsObjectPart part) {
        return new ContentChangeEvent(part, TYPE_PART_ADDED);
    }

    public static final ContentChangeEvent newPartRemovedEvent(IIpsObjectPartContainer part) {
        return new ContentChangeEvent(part, TYPE_PART_REMOVED);
    }

    public static final ContentChangeEvent newPartChangedEvent(IIpsObjectPartContainer part) {
        return new ContentChangeEvent(part, TYPE_PROPERTY_CHANGED);
    }

    public static final ContentChangeEvent newPartChangedEvent(IIpsObjectPartContainer part,
            PropertyChangeEvent... propertyChangeEvents) {
        ArgumentCheck.isTrue(propertyChangeEvents.length > 0);
        return new ContentChangeEvent(part, propertyChangeEvents);
    }

    public static final ContentChangeEvent newPartsChangedPositionsChangedEvent(IIpsSrcFile file,
            IIpsObjectPart[] parts) {
        return new ContentChangeEvent(file, parts);
    }

    public static final ContentChangeEvent newPartsChangedPositionsChangedEvent(List<? extends IIpsObjectPart> parts) {
        return new ContentChangeEvent(parts);
    }

    public static final ContentChangeEvent newWholeContentChangedEvent(IIpsSrcFile file,
            PropertyChangeEvent... propertyChangeEvents) {
        return new ContentChangeEvent(file, propertyChangeEvents);
    }

    public static final ContentChangeEvent mergeChangeEvents(ContentChangeEvent ce1, ContentChangeEvent ce2) {
        if (!Objects.equals(ce1.getIpsSrcFile(), ce2.getIpsSrcFile())) {
            throw new IllegalArgumentException("Can only merge change events from same source file. Was " //$NON-NLS-1$
                    + ce1.getIpsSrcFile() + " and " + ce2.getIpsSrcFile()); //$NON-NLS-1$
        }
        Set<PropertyChangeEvent> propertyChangeEvents = new LinkedHashSet<>(
                ce1.getPropertyChangeEvents());
        propertyChangeEvents.addAll(ce2.getPropertyChangeEvents());
        if (ce1.getEventType() == ce2.getEventType() && Objects.equals(ce1.getPart(), ce2.getPart())) {
            return new ContentChangeEvent(ce1.getIpsSrcFile(), ce1.getPart(), ce1.getEventType(), propertyChangeEvents);
        } else {
            return new ContentChangeEvent(ce1.getIpsSrcFile(), propertyChangeEvents);
        }
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
     * @see #TYPE_WHOLE_CONTENT_CHANGED
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
    public IIpsObjectPartContainer getPart() {
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
        if ((partContainer == part) || isAffectedIpsSrcFile(partContainer) || isChildOf(part, partContainer)) {
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

    private boolean isChildOf(IIpsObjectPartContainer potentialChild, IIpsObjectPartContainer potentialParent) {
        if (potentialChild == null) {
            return false;
        }
        IIpsElement parent = potentialChild.getParent();
        if (parent != null) {
            if (potentialParent.equals(parent)) {
                return true;
            } else {
                if (parent instanceof IIpsObjectPart) {
                    return isChildOf((IIpsObjectPart)parent, potentialParent);
                }
            }
        }
        return false;
    }

    public boolean isPropertyAffected(String propertyName) {
        if (propertyChangeEvents == null || propertyName == null) {
            return false;
        }
        for (PropertyChangeEvent propertyChangeEvent : propertyChangeEvents) {
            if (propertyName.equals(propertyChangeEvent.getPropertyName())) {
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

    /**
     * Returns the first property change event which was the trigger of this event.
     * 
     * @return the first underlying {@link PropertyChangeEvent}
     * 
     * @see #getPropertyChangeEvents()
     */
    public PropertyChangeEvent getFirstPropertyChangeEvent() {
        if (propertyChangeEvents.isEmpty()) {
            return null;
        } else {
            return propertyChangeEvents.iterator().next();
        }
    }

    /**
     * Returns the underlying {@link PropertyChangeEvent events} that have triggered this
     * {@link ContentChangeEvent}
     * 
     * @return A set of underlying {@link PropertyChangeEvent events}
     */
    public Set<PropertyChangeEvent> getPropertyChangeEvents() {
        return Collections.unmodifiableSet(propertyChangeEvents);
    }

}
