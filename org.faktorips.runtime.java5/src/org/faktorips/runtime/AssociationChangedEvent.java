/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
