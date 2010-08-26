/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.deltapresentation;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.OverlayIcons;

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
