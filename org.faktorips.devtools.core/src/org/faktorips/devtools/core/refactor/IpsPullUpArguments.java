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

package org.faktorips.devtools.core.refactor;

import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * Arguments for Faktor-IPS "Pull Up" refactorings.
 * 
 * @author Alexander Weickmann
 */
public final class IpsPullUpArguments extends RefactoringArguments {

    private final IIpsObjectPartContainer target;

    public IpsPullUpArguments(IIpsObjectPartContainer target) {
        this.target = target;
    }

    public IIpsObjectPartContainer getTarget() {
        return target;
    }

}
