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

import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractAssociationFilter;

public class MasterToDetailWithoutSubsetsFilter extends AbstractAssociationFilter {

    /**
     * Returns <code>true</code> for all master to detail associations including derived unions but
     * not subsets of derived unions. Returns <code>true</code> for derived union associations that
     * are at the same time subsets of another derived union.
     */
    @Override
    public boolean isValidAssociation(IAssociation association) {
        return association.getAssociationType() == AssociationType.COMPOSITION_MASTER_TO_DETAIL
                && (association.isDerivedUnion() || !association.isSubsetOfADerivedUnion());
    }
}