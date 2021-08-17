/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.decorators.internal;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.model.decorators.IIpsDecorators;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;

public class PathIconDesc implements IconDesc {

    private final IIpsProject ipsProject;
    private final String pathToImage;
    private ImageRegistry imageRegistry;

    public PathIconDesc(IIpsProject ipsProject, String pathToImage) {
        this.ipsProject = ipsProject;
        this.pathToImage = pathToImage;
    }

    private ImageRegistry getImageRegistry() {
        if (imageRegistry == null) {
            imageRegistry = new ImageRegistry(IIpsDecorators.getImageHandling().getResourceManager());
        }
        return imageRegistry;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        ImageDescriptor cachedImage = getImageRegistry().getDescriptor(pathToImage);
        if (cachedImage == null) {
            try {
                InputStream inputStream = ipsProject.getResourceAsStream(pathToImage);
                if (inputStream != null) {
                    Image loadedImage = new Image(Display.getDefault(), inputStream);
                    getImageRegistry().put(pathToImage, loadedImage);
                    ImageDescriptor imageDesc = getImageRegistry().getDescriptor(pathToImage);
                    inputStream.close();
                    return imageDesc;
                } else {
                    return ImageDescriptor.getMissingImageDescriptor();
                }
            } catch (IOException e) {
                IpsLog.log(e);
            }
        }
        return cachedImage;
    }

    String getPathToImage() {
        return pathToImage;
    }
}
