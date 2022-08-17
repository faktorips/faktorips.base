/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.model.type.IAssociation;

public class AssociationsLabelProvider extends DefaultLabelProvider {

    @Override
    public String getText(Object element) {
        if (!(element instanceof IAssociation)) {
            return super.getText(element);
        }
        IAssociation association = (IAssociation)element;
        String prefix = ""; //$NON-NLS-1$
        if (association.isDerivedUnion()) {
            prefix = "/ "; //$NON-NLS-1$
        }
        String targetName = association.getTarget();
        int pos = targetName.lastIndexOf('.');
        if (pos > 0) {
            targetName = targetName.substring(pos + 1);
        }

        String maxC;
        if (association.isQualified()
                || (association.getMaxCardinality() == Integer.MAX_VALUE)) {
            // qualified associations are always unbounded as the max cardinality applies per
            // qualifier instance!
            maxC = "*"; //$NON-NLS-1$
        } else {
            maxC = "" + association.getMaxCardinality(); //$NON-NLS-1$
        }
        String role = association.is1ToMany() ? association.getTargetRolePlural() : association.getTargetRoleSingular();
        return prefix + role + " : " + targetName //$NON-NLS-1$
                + " [" + association.getMinCardinality() + ".." + maxC + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
