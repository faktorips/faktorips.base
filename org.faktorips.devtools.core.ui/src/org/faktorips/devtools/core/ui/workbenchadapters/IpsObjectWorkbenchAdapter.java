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

package org.faktorips.devtools.core.ui.workbenchadapters;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.StringUtil;

public abstract class IpsObjectWorkbenchAdapter extends IpsElementWorkbenchAdapter {

    public IpsObjectWorkbenchAdapter() {
        super();
    }

    @Override
    protected final ImageDescriptor getImageDescriptor(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
            return getImageDescriptor(ipsSrcFile);
        } else if (ipsElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
            return getImageDescriptor(ipsObject);
        }
        return null;
    }

    protected abstract ImageDescriptor getImageDescriptor(IIpsSrcFile ipsSrcFile);

    protected abstract ImageDescriptor getImageDescriptor(IIpsObject ipsObject);

    @Override
    protected final String getLabel(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
            return getLabel(ipsSrcFile);
        } else if (ipsElement instanceof IIpsObject) {
            IIpsObject ipsObject = (IIpsObject)ipsElement;
            return getLabel(ipsObject);
        }
        return ipsElement.getName();
    }

    protected String getLabel(IIpsSrcFile ipsSrcFile) {
        return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName());
    }

    protected String getLabel(IIpsObject ipsObject) {
        return ipsObject.getName();
    }

    protected ImageDescriptor getImageDescriptorForPath(IIpsProject ipsProject, String path) {
        ImageDescriptor cachedImage = IpsUIPlugin.getDefault().getImageRegistry().getDescriptor(path);
        if (cachedImage == null) {
            try {
                InputStream inputStream = ipsProject.getResourceAsStream(path);
                if (inputStream != null) {
                    Image loadedImage = new Image(Display.getDefault(), inputStream);
                    IpsUIPlugin.getDefault().getImageRegistry().put(path, loadedImage);
                    ImageDescriptor imageDesc = IpsUIPlugin.getDefault().getImageRegistry().getDescriptor(path);
                    inputStream.close();
                    return imageDesc;
                } else {
                    return ImageDescriptor.getMissingImageDescriptor();
                }
            } catch (IOException e) {
                IpsPlugin.log(e);
            }
        }
        return cachedImage;
    }
}