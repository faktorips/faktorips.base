/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    public AssociationChangedEvent(Object source, String associationName, Object removedAssociationTarget,
            Object addedAssociationTarget) {
        super(source, associationName, removedAssociationTarget, addedAssociationTarget);
    }

    public Object getAddedAssociationTarget() {
        return getNewValue();
    }

    public Object getRemovedAssociationTarget() {
        return getOldValue();
    }

    public String getAssociationName() {
        return getPropertyName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[propertyName=").append(getPropertyName());
        sb.append("; added=").append(getAddedAssociationTarget());
        sb.append("; removed=").append(getRemovedAssociationTarget());
        sb.append("; propagationId=").append(getPropagationId());
        sb.append("; source=").append(getSource());
        return sb.append("]").toString();
    }

}
