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

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.swt.graphics.Image;

/**
 * A view item to represent an association or link in the product component editor's link section.
 * 
 * @author widmaier
 */
public abstract interface LinkSectionViewItem {

    /**
     * Returns the text to be displayed for this item in the tree. To be used by label providers.
     */
    public String getText();

    /**
     * Returns the image to be displayed for this item in the tree. To be used by label providers.
     */
    public Image getImage();

    /**
     * Returns the the name of the corresponding product component type association.
     */
    public String getAssociationName();
}
