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

package org.faktorips.devtools.stdbuilder.xpand.model.filter;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractAssociationFilter;

public class AssociationWithoutDUAndInverseDUFilter extends AbstractAssociationFilter {

    /**
     * To be used with policy associations only. Returns <code>true</code> for all associations
     * except derived unions and inverse associations of derived unions.
     */
    @Override
    public boolean isValidAssociation(IAssociation association) {
        return !association.isDerivedUnion() && !isInverseOfADerivedUnion(association);
    }

    private boolean isInverseOfADerivedUnion(IAssociation association) {
        try {
            if (association instanceof IPolicyCmptTypeAssociation) {
                return ((IPolicyCmptTypeAssociation)association).isInverseOfDerivedUnion();
            }
            return false;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}