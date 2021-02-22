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
    static final String OVERLAYS_FOLDER = "overlays/"; //$NON-NLS-1$

    static final String ABSTRACT = OVERLAYS_FOLDER + "AbstractIndicator.gif"; //$NON-NLS-1$
    static final String ADDED = OVERLAYS_FOLDER + "AddOverlay.gif"; //$NON-NLS-1$
    static final String DELETED = OVERLAYS_FOLDER + "DeleteOverlay.gif"; //$NON-NLS-1$
    static final String ERROR = OVERLAYS_FOLDER + "error_ovr.gif"; //$NON-NLS-1$
    static final String FAILURE = OVERLAYS_FOLDER + "failed_ovr.gif"; //$NON-NLS-1$
    static final String INFO = OVERLAYS_FOLDER + "info_ovr.gif"; //$NON-NLS-1$
    static final String KEY = OVERLAYS_FOLDER + "KeyOverlay.gif"; //$NON-NLS-1$
    static final String LINK = OVERLAYS_FOLDER + "LinkOverlay.gif"; //$NON-NLS-1$
    static final String MODIFIED = OVERLAYS_FOLDER + "ModifyOverlay.gif"; //$NON-NLS-1$
    static final String OVERRIDE = OVERLAYS_FOLDER + "OverrideIndicator.gif"; //$NON-NLS-1$
    static final String PRODUCT_RELEVANT = OVERLAYS_FOLDER + "ProductRelevantOverlay.gif"; //$NON-NLS-1$
    static final String STATIC = OVERLAYS_FOLDER + "not_changingovertime_ovr.gif"; //$NON-NLS-1$
    static final String SUCCESS = OVERLAYS_FOLDER + "success_ovr.gif"; //$NON-NLS-1$
    static final String TEMPLATE = OVERLAYS_FOLDER + "ProductTemplateOverlay.gif"; //$NON-NLS-1$
    static final String WARNING = OVERLAYS_FOLDER + "warning_co.gif"; //$NON-NLS-1$

    static final ImageDescriptor ERROR_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(ERROR);
    static final ImageDescriptor WARNING_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(WARNING);
    static final ImageDescriptor INFO_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(INFO);
    static final ImageDescriptor FAILURE_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(FAILURE);
    static final ImageDescriptor SUCCESS_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(SUCCESS);
    static final ImageDescriptor OVERRIDE_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(OVERRIDE);
    static final ImageDescriptor PRODUCT_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(PRODUCT_RELEVANT);
    static final ImageDescriptor ABSTRACT_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(ABSTRACT);
    static final ImageDescriptor KEY_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(KEY);
    static final ImageDescriptor DELETED_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(DELETED);
    static final ImageDescriptor ADDED_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(ADDED);
    static final ImageDescriptor MODIFIED_OVR_DESC = IIpsDecorators.getImageHandling().createImageDescriptor(MODIFIED);
    static final ImageDescriptor NOT_CHANGEOVERTIME_OVR_DESC = IIpsDecorators.getImageHandling()
            .createImageDescriptor(STATIC);
}
