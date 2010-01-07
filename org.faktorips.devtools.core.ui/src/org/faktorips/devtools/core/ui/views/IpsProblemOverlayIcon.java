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

package org.faktorips.devtools.core.ui.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.util.message.Message;

public class IpsProblemOverlayIcon {

    /**
     * Get overlay error overlay image descriptor
     * 
     * @return
     */
    private static ImageDescriptor getErrorOverlay() {
        return OverlayIcons.ERROR_OVR_DESC;
    }

    /**
     * Get overlay warning overlay image descriptor
     * 
     * @return
     */
    private static ImageDescriptor getWarningOverlay() {
        return OverlayIcons.WARNING_OVR_DESC;
    }

    /**
     * Get overlay info overlay image descriptor
     * 
     * @return
     */
    private static ImageDescriptor getInfoOverlay() {
        return OverlayIcons.INFO_OVR_DESC;
    }

    /**
     * Get the overly image descriptor for the specified severity @see {@link Message}
     * 
     * @param ipsMessageSeverity
     * @return
     */
    public static ImageDescriptor getOverlay(int ipsMessageSeverity) {
        if (ipsMessageSeverity == Message.ERROR) {
            return getErrorOverlay();
        } else if (ipsMessageSeverity == Message.WARNING) {
            return getWarningOverlay();
        } else if (ipsMessageSeverity == Message.INFO) {
            return getInfoOverlay();
        }
        return null;
    }

    /**
     * Create a new image descriptor with overlays for the given severity over the baseImage
     * 
     * @param baseImage
     * @param ipsMessageSeverity
     * @return
     */
    public static ImageDescriptor createOverlayIcon(Image baseImage, int ipsMessageSeverity) {
        if (baseImage != null) {
            return new DecorationOverlayIcon(baseImage, new ImageDescriptor[] { null, null,
                    getOverlay(ipsMessageSeverity), null });
        } else {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    /**
     * Get the overlay icon for the specified {@link IMarker} severity
     * 
     * @param markerSeverity
     * @return
     */
    public static ImageDescriptor getMarkerOverlay(int markerSeverity) {
        if (markerSeverity <= 0) {
            return null;
        }
        if ((markerSeverity & IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_ERROR) {
            return getOverlay(Message.ERROR);
        } else if ((markerSeverity & IMarker.SEVERITY_WARNING) == IMarker.SEVERITY_WARNING) {
            return getOverlay(Message.WARNING);
        } else if ((markerSeverity & IMarker.SEVERITY_INFO) == IMarker.SEVERITY_INFO) {
            return getOverlay(Message.INFO);
        } else {
            return null;
        }
    }

    /**
     * Create a new image descriptor with overlays for the given {@link IMarker} severity over the
     * baseImage
     * 
     * @param baseImage
     * @param markerSeverity
     * @return
     */
    public static ImageDescriptor createMarkerOverlayIcon(Image baseImage, int markerSeverity) {
        if (baseImage != null) {
            return new DecorationOverlayIcon(baseImage, new ImageDescriptor[] { null, null,
                    getMarkerOverlay(markerSeverity), null });
        } else {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

}
