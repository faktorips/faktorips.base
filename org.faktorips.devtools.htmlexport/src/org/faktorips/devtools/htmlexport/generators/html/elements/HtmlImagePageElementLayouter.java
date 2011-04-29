/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.generators.html.elements;

import org.eclipse.swt.SWT;
import org.faktorips.devtools.htmlexport.generators.LayoutResource;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.helper.ImageUtil;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;

public class HtmlImagePageElementLayouter extends AbstractHtmlPageElementLayouter<ImagePageElement> {

    public HtmlImagePageElementLayouter(ImagePageElement pageElement, HtmlLayouter layouter) {
        super(pageElement, layouter);
    }

    @Override
    protected void layoutInternal() {
        String path = layouter.getResourcePath() + "/images/" + pageElement.getFileName() + ".png"; //$NON-NLS-1$ //$NON-NLS-2$
        layouter.addLayoutResource(new LayoutResource(path, new ImageUtil().convertImageDataToByteArray(
                pageElement.getImageData(), SWT.IMAGE_PNG)));

        append(htmlUtil.createImage(layouter.getPathToRoot() + path, pageElement.getTitle()));
    }
}
