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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.fl.FlFunction;

/**
 * Label provider that provides default images and labels (texts) for ips elements.
 * <p>
 * The default label provider known until version 2.4 is deprecated. Most of the implementation
 * could be found in
 * {@link org.faktorips.devtools.core.ui.workbenchadapters.DefaultPresentationObject}.
 * <p>
 * This label provider uses the {@link IPresentationObject} provided by the {@link IpsUIPlugin} to
 * return icons and text labels. For {@link IIpsSrcFile}s you could specify whether to use icon and
 * label from the source file itself or to look for the {@link IIpsObject} enclosed in the source
 * file.
 * 
 * @author Cornelius Dirmeier
 * 
 * 
 */
public class DefaultLabelProvider extends LabelProvider {

    private ImageDescriptor datatypeImageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "Datatype.gif");

    private ImageDescriptor functionImageDescriptor = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "Function.gif");

    private ResourceManager resourceManager;

    public DefaultLabelProvider() {
        super();
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    /**
     * Creates an DefaultLabelProvider with additional IpsSourceFile mapping support: In case of an
     * IpsSourceFile the text and the image of the corresponding IpsObject will be returned.
     */
    public static ILabelProvider createWithIpsSourceFileMapping() {
        return new DefaultLabelProvider();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage(Object element) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable)element;
            return IpsUIPlugin.getImageHandling().getImage(adaptable);
        }
        if (element instanceof Datatype) {
            return (Image)resourceManager.get(datatypeImageDescriptor);
        } else if (element instanceof FlFunction) {
            return (Image)resourceManager.get(functionImageDescriptor);
        } else if (element instanceof EnumTypeDatatypeAdapter) {
            return getImage(((EnumTypeDatatypeAdapter)element).getEnumType());
        } else if (element instanceof IpsSrcFileViewItem) {
            return getImage(((IpsSrcFileViewItem)element).getIpsSrcFile());
        }
        return super.getImage(element);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText(Object element) {
        if (element == null) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        if (element instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)element;
            return IpsUIPlugin.getLabel(ipsElement);
        }
        if (element instanceof IpsSrcFileViewItem) {
            return getText(((IpsSrcFileViewItem)element).getIpsSrcFile());
        } else if (element instanceof EnumTypeDatatypeAdapter) {
            return getText(((EnumTypeDatatypeAdapter)element).getEnumType());
        }
        return element.toString();
    }

}
