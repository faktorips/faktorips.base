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

package org.faktorips.devtools.core.ui.editors;

import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;

public class AssociationsLabelProvider extends DefaultLabelProvider {

    @Override
    public String getText(Object element) {
        if (!(element instanceof IAssociation)) {
            return super.getText(element);
        }
        IAssociation association = (IAssociation)element;
        String targetName = association.getTarget();
        int pos = targetName.lastIndexOf('.');
        if (pos > 0) {
            targetName = targetName.substring(pos + 1);
        }

        String maxC;
        if (association.isQualified()) {
            // qualified associations are always unbounded as the max cardinality applies per
            // qualifier instance!
            maxC = "*"; //$NON-NLS-1$
        } else if (association.getMaxCardinality() == Integer.MAX_VALUE) {
            maxC = "*"; //$NON-NLS-1$
        } else {
            maxC = "" + association.getMaxCardinality(); //$NON-NLS-1$
        }
        String role = association.is1ToMany() ? association.getTargetRolePlural() : association.getTargetRoleSingular();
        return role + " : " + targetName + //$NON-NLS-1$
                " [" + association.getMinCardinality() + ".." + maxC + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
