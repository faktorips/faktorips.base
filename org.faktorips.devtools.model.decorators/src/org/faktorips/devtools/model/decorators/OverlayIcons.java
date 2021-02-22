/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators;

import org.eclipse.jface.resource.ImageDescriptor;

public interface OverlayIcons {

    public static final String ERROR_OVR = "overlays/error_ovr.gif"; //$NON-NLS-1$

    public static final String WARNING_OVR = "overlays/warning_co.gif"; //$NON-NLS-1$

    public static final String INFO_OVR = "overlays/info_ovr.gif"; //$NON-NLS-1$

    public static final String FAILURE_OVR = "overlays/failed_ovr.gif"; //$NON-NLS-1$

    public static final String SUCCESS_OVR = "overlays/success_ovr.gif"; //$NON-NLS-1$

    public static final String OVERRIDE_OVR = "overlays/OverrideIndicator.gif"; //$NON-NLS-1$

    public static final String PRODUCT_OVR = "overlays/ProductRelevantOverlay.gif"; //$NON-NLS-1$

    public static final String ABSTRACT_OVR = "overlays/AbstractIndicator.gif"; //$NON-NLS-1$

    public static final String KEY_OVR = "overlays/KeyOverlay.gif"; //$NON-NLS-1$

    public static final String DELETED_OVR = "overlays/DeleteOverlay.gif"; //$NON-NLS-1$

    public static final String ADDED_OVR = "overlays/AddOverlay.gif"; //$NON-NLS-1$

    public static final String MODIFIED_OVR = "overlays/ModifyOverlay.gif"; //$NON-NLS-1$

    public static final String NOT_CHANGEOVERTIME_OVR = "overlays/not_changingovertime_ovr.gif"; //$NON-NLS-1$

    public static final ImageDescriptor ERROR_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(ERROR_OVR);

    public static final ImageDescriptor WARNING_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(WARNING_OVR);

    public static final ImageDescriptor INFO_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(INFO_OVR);

    public static final ImageDescriptor FAILURE_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(FAILURE_OVR);

    public static final ImageDescriptor SUCCESS_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(SUCCESS_OVR);

    public static final ImageDescriptor OVERRIDE_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(OVERRIDE_OVR);

    public static final ImageDescriptor PRODUCT_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(PRODUCT_OVR);

    public static final ImageDescriptor ABSTRACT_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(ABSTRACT_OVR);

    public static final ImageDescriptor KEY_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(KEY_OVR);

    public static final ImageDescriptor DELETED_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(DELETED_OVR);

    public static final ImageDescriptor ADDED_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(ADDED_OVR);

    public static final ImageDescriptor MODIFIED_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(MODIFIED_OVR);

    public static final ImageDescriptor NOT_CHANGEOVERTIME_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(NOT_CHANGEOVERTIME_OVR);

}
