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
