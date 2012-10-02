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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

/**
 * This Class filters empty associations of an {@link IProductCmptGeneration}.
 * <p>
 * It works, when an association has an {@link IProductCmptGeneration} as parentElement in the
 * Viewer. The association must be represented by a String.
 * </p>
 * 
 * @author dicker
 */
final class EmptyAssociationFilter extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (parentElement instanceof IProductCmptGeneration && element instanceof String) {
            IProductCmptGeneration generation = (IProductCmptGeneration)parentElement;
            IProductCmptLink[] links = generation.getLinks((String)element);
            return links.length != 0;
        }
        return true;
    }
}