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
import org.eclipse.swt.graphics.Point;
import org.faktorips.devtools.core.IpsPlugin;

class ProductRelevantIcon extends CompositeImageDescriptor {
    private final Point DEFAULT_SIZE = new Point(16, 16);
    private Image baseImage;

    public static Image createProductRelevantImage(Image baseImage) {
        return IpsPlugin.getDefault().getImage(new ProductRelevantIcon(baseImage));
    }

    private ProductRelevantIcon(Image baseImage) {
        this.baseImage = baseImage;
    }

    protected void drawCompositeImage(int width, int height) {
        if (baseImage == null) {
            return;
        }
        drawImage(baseImage.getImageData(), 0, 0);
        drawImage(IpsPlugin.getDefault().getImage("ProductRelevantOverlay.gif").getImageData(), 8, 0); //$NON-NLS-1$
    }

    protected Point getSize() {
        return DEFAULT_SIZE;
    }
}