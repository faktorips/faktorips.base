/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.deltapresentation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.model.decorators.OverlayIcons;

/**
 * Decoration Oeverlay Icon to indicate changes to an object
 * 
 * @author Thorsten Guenther
 * @author dirmeier
 */
public class DeltaCompositeIcon extends DecorationOverlayIcon {

    public DeltaCompositeIcon(Image baseImage, ImageDescriptor overlayImage, int quadrant) {
        super(baseImage, overlayImage, quadrant);
    }

    public static ImageDescriptor createDeleteImage(Image baseImage) {
        return new DeltaCompositeIcon(baseImage, OverlayIcons.DELETED_OVR_DESC, IDecoration.TOP_RIGHT);
    }

    public static ImageDescriptor createAddImage(Image baseImage) {
        return new DeltaCompositeIcon(baseImage, OverlayIcons.ADDED_OVR_DESC, IDecoration.TOP_RIGHT);
    }

    public static ImageDescriptor createModifyImage(Image baseImage) {
        return new DeltaCompositeIcon(baseImage, OverlayIcons.MODIFIED_OVR_DESC, IDecoration.TOP_RIGHT);
    }

}
