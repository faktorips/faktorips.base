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

import java.util.List;

/* TODO CODE-REVIEW FIPS-1194: package-private sollte reichen */
public interface IModelOverviewNode {

    /*
     * TODO CODE-REVIEW FIPS-1194: Wenn im @return nichts besonderes mehr steht, das @return bitte
     * weglassen. Das null bzw. empty list hier einfach noch in die Zusammenfassung mit
     * reinschreiben.
     */

    /* TODO CODE-REVIEW FIPS-1194: Auf Typen mit {@link} verweisen, statt mit <tt> hervorheben */

    /**
     * Returns all children of this node.
     * 
     * @return the children or an empty <tt>List</tt> if there are no children.
     */
    List<IModelOverviewNode> getChildren();

    /* TODO CODE-REVIEW FIPS-1194: {@code null} statt <tt>null</tt> verwenden */
    /**
     * Returns the parent of this node.
     * 
     * @return the parent node or <tt>null</tt> if it has no parent
     */
    IModelOverviewNode getParent();

}
