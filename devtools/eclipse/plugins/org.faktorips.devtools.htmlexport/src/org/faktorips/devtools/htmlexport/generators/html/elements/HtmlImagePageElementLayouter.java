/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
