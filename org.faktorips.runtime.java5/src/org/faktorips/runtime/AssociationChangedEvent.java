/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
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

    private static final long serialVersionUID = 7545458044844395021L;

    private final Object addedAssociationTarget;
    private final Object removedAssociationTarget;

    public AssociationChangedEvent(Object source, String associationName, Object removedAssociationTarget,
            Object addedAssociationTarget) {

        super(source, associationName, null, null);
        this.addedAssociationTarget = addedAssociationTarget;
        this.removedAssociationTarget = removedAssociationTarget;
    }

    public Object getAddedAssociationTarget() {
        return addedAssociationTarget;
    }

    public Object getRemovedAssociationTarget() {
        return removedAssociationTarget;
    }

    public String getAssociationName() {
        return getPropertyName();
    }

}
