/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modelstructure;

import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.ToChildAssociationType;
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((associationType == null) ? 0 : associationType.hashCode());
        result = prime * result + ((component == null) ? 0 : component.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PathElement other = (PathElement)obj;
        if (associationType != other.associationType) {
            return false;
        }
        if (component == null) {
            if (other.component != null) {
                return false;
            }
        } else if (!component.equals(other.component)) {
            return false;
        }
        return true;
    }

}
