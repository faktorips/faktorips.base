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

package org.faktorips.runtime;

import java.beans.PropertyChangeEvent;

/**
 * Extension of {@link PropertyChangeEvent} for explicitly modeling properties as associations and
 * firing events when association targets are added or removed from the association.
 * 
 * @author Daniel Hohenberger
 */
public class AssociationChangedEvent extends PropertyChangeEvent {

    /**
     * Generated <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 7545458044844395021L;

    private final Object addedAssociationTarget;
    private final Object removedAssociationTarget;

    /**
     * @param source
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    public AssociationChangedEvent(Object source, String associationName, Object removedAssociationTarget,
            Object addedAssociationTarget) {
        super(source, associationName, null, null);
        this.addedAssociationTarget = addedAssociationTarget;
        this.removedAssociationTarget = removedAssociationTarget;
    }

    /**
     * Returns the added AssociationTarget.
     */
    public Object getAddedAssociationTarget() {
        return addedAssociationTarget;
    }

    /**
     * Returns the removed AssociationTarget.
     */
    public Object getRemovedAssociationTarget() {
        return removedAssociationTarget;
    }

    /**
     * Returns the association's name.
     */
    public String getAssociationName() {
        return getPropertyName();
    }

}
