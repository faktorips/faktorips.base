/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import org.eclipse.swt.graphics.ImageData;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

public class ImagePageElement extends AbstractPageElement {

    protected ImageData imageData;
    protected String title;
    protected String fileName;

    public ImagePageElement() {
        super();
    }

    public ImagePageElement(ImageData imageData, String title, String path) {
        this.imageData = imageData;
        this.title = title;
        this.fileName = path;
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutImagePageElement(this);
    }

    /**
     * @return {@link ImageData} of the image
     */
    public ImageData getImageData() {
        return imageData;
    }

    /**
     * @return title of the image
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return fileName of the image
     */
    public String getFileName() {
        return fileName;
    }
}
