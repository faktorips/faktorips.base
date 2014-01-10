/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper;

import java.io.ByteArrayOutputStream;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

/**
 * 
 * Utility-Class for the documentation
 * 
 * @author dicker
 * 
 */
public class ImageUtil {

    /**
     * converts {@link ImageData} to a byte[] using the given image format (e.g. png)
     * 
     */
    public byte[] convertImageDataToByteArray(ImageData imageData, int format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[] { imageData };
        imageLoader.save(out, format);

        return out.toByteArray();
    }
}
