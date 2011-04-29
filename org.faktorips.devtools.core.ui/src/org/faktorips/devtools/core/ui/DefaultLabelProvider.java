/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.IAdaptable;
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
 * For {@link IIpsSrcFile}s you could specify whether to use icon and label from the source file
 * itself or to look for the {@link IIpsObject} enclosed in the source file.
 * 
 * @author Cornelius Dirmeier
 */
public class DefaultLabelProvider extends LabelProvider {

    public DefaultLabelProvider() {
        super();
    }

    /**
     * Creates an DefaultLabelProvider with additional IpsSourceFile mapping support: In case of an
     * IpsSourceFile the text and the image of the corresponding IpsObject will be returned.
     */
    public static ILabelProvider createWithIpsSourceFileMapping() {
        return new DefaultLabelProvider();
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable)element;
            Image result = IpsUIPlugin.getImageHandling().getImage(adaptable);
            if (result != null) {
                return result;
            }
            // check adaptable to IIpsSrcFile
            IIpsSrcFile adaptedIpsSrcFile = (IIpsSrcFile)adaptable.getAdapter(IIpsSrcFile.class);
            if (adaptedIpsSrcFile != null) {
                return IpsUIPlugin.getImageHandling().getImage(adaptedIpsSrcFile);
            }
        }
        if (element instanceof Datatype) {
            return IpsUIPlugin.getImageHandling().getSharedImage("Datatype.gif", true); //$NON-NLS-1$
        } else if (element instanceof FlFunction) {
            return IpsUIPlugin.getImageHandling().getSharedImage("Function.gif", true); //$NON-NLS-1$
        } else if (element instanceof EnumTypeDatatypeAdapter) {
            return getImage(((EnumTypeDatatypeAdapter)element).getEnumType());
        }
        return super.getImage(element);
    }

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

    @Override
    public void dispose() {
        super.dispose();
    }

}
