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

package org.faktorips.devtools.core.ui;

import org.eclipse.jface.resource.ImageDescriptor;

public interface OverlayIcons {

    public final static String ERROR_OVR = "ovr16/error_ovr.gif"; //$NON-NLS-1$

    public final static String WARNING_OVR = "ovr16/warning_co.gif"; //$NON-NLS-1$

    // TODO eigenes INFO Bild!
    public final static String INFO_OVR = "ovr16/warning_co.gif"; //$NON-NLS-1$

    public final static String FAILURE_OVR = "ovr16/failed_ovr.gif"; //$NON-NLS-1$

    public final static String SUCCESS_OVR = "ovr16/success_ovr.gif"; //$NON-NLS-1$

    public final static String OVERRIDE_OVR = "OverrideIndicator.gif"; //$NON-NLS-1$

    public final static String PRODUCT_OVR = "ProductRelevantOverlay.gif"; //$NON-NLS-1$

    public final static String ABSTRACT_OVR = "AbstractIndicator.gif"; //$NON-NLS-1$

    public final static String KEY_OVR = "KeyOverlay.gif"; //$NON-NLS-1$

    public final static String DELETED_OVR = "DeleteOverlay.gif"; //$NON-NLS-1$

    public final static String ADDED_OVR = "AddOverlay.gif"; //$NON-NLS-1$

    public final static String MODIFIED_OVR = "ModifyOverlay.gif"; //$NON-NLS-1$

    public final static ImageDescriptor ERROR_OVR_DESC = IpsUIPlugin.getImageHandling()
            .createImageDescriptor(ERROR_OVR);

    public final static ImageDescriptor WARNING_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            WARNING_OVR);

    public final static ImageDescriptor INFO_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(INFO_OVR);

    public final static ImageDescriptor FAILURE_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            FAILURE_OVR);

    public final static ImageDescriptor SUCCESS_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            SUCCESS_OVR);

    public final static ImageDescriptor OVERRIDE_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            OVERRIDE_OVR);

    public final static ImageDescriptor PRODUCT_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            PRODUCT_OVR);

    public final static ImageDescriptor ABSTRACT_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            ABSTRACT_OVR);

    public final static ImageDescriptor KEY_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(KEY_OVR);

    public final static ImageDescriptor DELETED_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            DELETED_OVR);

    public final static ImageDescriptor ADDED_OVR_DESC = IpsUIPlugin.getImageHandling()
            .createImageDescriptor(ADDED_OVR);

    public final static ImageDescriptor MODIFIED_OVR_DESC = IpsUIPlugin.getImageHandling().createImageDescriptor(
            MODIFIED_OVR);

}
