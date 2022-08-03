/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.swt.graphics.Image;

/**
 * A view item to represent an association or link in the product component editor's link section.
 * 
 * @author widmaier
 */
public interface ILinkSectionViewItem {

    /**
     * Returns the text to be displayed for this item in the tree. To be used by label providers.
     */
    String getText();

    /**
     * Returns the image to be displayed for this item in the tree. To be used by label providers.
     */
    Image getImage();

    /**
     * Returns the the name of the corresponding product component type association.
     */
    String getAssociationName();
}
