/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelstructure;

import java.util.Objects;

import org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.ToChildAssociationType;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.util.ArgumentCheck;

class PathElement {

    private IType component;
    private ToChildAssociationType associationType;

    /**
     * Represents a path element in a tree created by the {@link ModelStructureContentProvider}.
     * 
     * @param component the contained {@link IType} element
     * @param associationType the {@link ToChildAssociationType} to the next path element
     */
    public PathElement(IType component, ToChildAssociationType associationType) {
        ArgumentCheck.notNull(component);
        ArgumentCheck.notNull(associationType);

        this.component = component;
        this.associationType = associationType;
    }

    public ModelStructureContentProvider.ToChildAssociationType getAssociationType() {
        return associationType;
    }

    public IType getComponent() {
        return component;
    }

    @Override
    public int hashCode() {
        return Objects.hash(associationType, component);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        PathElement other = (PathElement)obj;
        if (associationType != other.associationType) {
            return false;
        }
        return Objects.equals(component, other.component);
    }

}
