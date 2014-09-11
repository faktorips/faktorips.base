/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.jface.resource.ImageDescriptor;

public interface OverlayIcons {

    public static final String ERROR_OVR = "ovr16/error_ovr.gif"; //$NON-NLS-1$

    public static final String WARNING_OVR = "ovr16/warning_co.gif"; //$NON-NLS-1$

    public static final String INFO_OVR = "ovr16/info_ovr.gif"; //$NON-NLS-1$

    public static final String FAILURE_OVR = "ovr16/failed_ovr.gif"; //$NON-NLS-1$

    public static final String SUCCESS_OVR = "ovr16/success_ovr.gif"; //$NON-NLS-1$

    public static final String OVERRIDE_OVR = "OverrideIndicator.gif"; //$NON-NLS-1$

    public static final String PRODUCT_OVR = "ProductRelevantOverlay.gif"; //$NON-NLS-1$

    public static final String ABSTRACT_OVR = "AbstractIndicator.gif"; //$NON-NLS-1$

    public static final String KEY_OVR = "KeyOverlay.gif"; //$NON-NLS-1$

    public static final String DELETED_OVR = "DeleteOverlay.gif"; //$NON-NLS-1$

    public static final String ADDED_OVR = "AddOverlay.gif"; //$NON-NLS-1$

    public static final String MODIFIED_OVR = "ModifyOverlay.gif"; //$NON-NLS-1$

    public static final String NOT_CHANGEOVERTIME_OVR = "ovr16/not_changingovertime_ovr.gif"; //$NON-NLS-1$

    public static final ImageDescriptor ERROR_OVR_DESC = IpsUIPlugin.getImageHandling()
            .createImageDescriptor(ERROR_OVR);

    public static final ImageDescriptor WARNING_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            WARNING_OVR);

    public static final ImageDescriptor INFO_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(INFO_OVR);

    public static final ImageDescriptor FAILURE_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            FAILURE_OVR);

    public static final ImageDescriptor SUCCESS_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            SUCCESS_OVR);

    public static final ImageDescriptor OVERRIDE_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            OVERRIDE_OVR);

    public static final ImageDescriptor PRODUCT_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            PRODUCT_OVR);

    public static final ImageDescriptor ABSTRACT_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            ABSTRACT_OVR);

    public static final ImageDescriptor KEY_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(KEY_OVR);

    public static final ImageDescriptor DELETED_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            DELETED_OVR);

    public static final ImageDescriptor ADDED_OVR_DESC = IpsUIPlugin.getImageHandling()
            .createImageDescriptor(ADDED_OVR);

    public static final ImageDescriptor MODIFIED_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            MODIFIED_OVR);

    public static final ImageDescriptor NOT_CHANGEOVERTIME_OVR_DESC = IpsUIPlugin.getImageHandling()
            .createImageDescriptor(NOT_CHANGEOVERTIME_OVR);

}
