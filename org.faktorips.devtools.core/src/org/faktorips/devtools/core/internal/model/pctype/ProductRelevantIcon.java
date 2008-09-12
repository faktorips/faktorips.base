/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
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