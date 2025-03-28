/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.Severity;

public class IpsProblemOverlayIcon {

    private IpsProblemOverlayIcon() {
        // Utility class not to be instantiated
    }

    /**
     * Get overlay error overlay image descriptor
     */
    private static ImageDescriptor getErrorOverlay() {
        return OverlayIcons.ERROR_OVR_DESC;
    }

    /**
     * Get overlay warning overlay image descriptor
     */
    private static ImageDescriptor getWarningOverlay() {
        return OverlayIcons.WARNING_OVR_DESC;
    }

    /**
     * Get overlay info overlay image descriptor
     */
    private static ImageDescriptor getInfoOverlay() {
        return OverlayIcons.INFO_OVR_DESC;
    }

    /**
     * Get the overly image descriptor for the specified severity @see {@link Message}
     */
    public static ImageDescriptor getOverlay(Severity ipsMessageSeverity) {
        return switch (ipsMessageSeverity) {
            case ERROR -> getErrorOverlay();
            case WARNING -> getWarningOverlay();
            case INFO -> getInfoOverlay();
            default -> null;
        };
    }

    /**
     * Create a new image descriptor with overlays for the given severity over the baseImage
     */
    public static ImageDescriptor createOverlayIcon(Image baseImage, Severity ipsMessageSeverity) {
        if (baseImage != null) {
            return new DecorationOverlayIcon(baseImage, new ImageDescriptor[] { null, null,
                    getOverlay(ipsMessageSeverity), null });
        } else {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    /**
     * Get the overlay icon for the specified {@link IMarker} severity
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
