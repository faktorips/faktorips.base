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

package org.faktorips.devtools.core.internal.model.pctype;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.util.ArgumentCheck;

public class ProductRelevantIcon extends CompositeImageDescriptor {
    private final Point DEFAULT_SIZE = new Point(16, 16);
    private ImageData baseImageData;

    public static Image createProductRelevantImage(Image baseImage) {
        return IpsPlugin.getDefault().getImage(new ProductRelevantIcon(baseImage));
    }

    private ProductRelevantIcon(Image baseImage) {
        ArgumentCheck.notNull(baseImage);
        baseImageData = baseImage.getImageData();
    }

    protected void drawCompositeImage(int width, int height) {
        drawImage(baseImageData, 0, 0);
        drawImage(IpsPlugin.getDefault().getImage("ProductRelevantOverlay.gif").getImageData(), 8, 0); //$NON-NLS-1$
    }

    protected Point getSize() {
        return DEFAULT_SIZE;
    }
}
