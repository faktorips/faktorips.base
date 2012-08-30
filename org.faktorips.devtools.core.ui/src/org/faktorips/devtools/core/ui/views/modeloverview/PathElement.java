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

package org.faktorips.devtools.core.ui.views.modeloverview;

import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.views.modeloverview.ModelOverviewContentProvider.ToChildAssociationType;
import org.faktorips.util.ArgumentCheck;

class PathElement {

    private IType component;
    private ToChildAssociationType associationType;

    /**
     * Represents a path element in a tree created by the {@link ModelOverviewContentProvider}.
     * 
     * @param component the containd {@link IType} element
     * @param associationType the {@link ToChildAssociationType} to the next path element
     */
    public PathElement(IType component, ToChildAssociationType associationType) {
        ArgumentCheck.notNull(component);
        ArgumentCheck.notNull(associationType);

        this.component = component;
        this.associationType = associationType;
    }

    public ModelOverviewContentProvider.ToChildAssociationType getAssociationType() {
        return associationType;
    }

    public IType getComponent() {
        return component;
    }

}
