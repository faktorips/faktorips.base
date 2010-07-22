/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.bf.draw2d;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.OverlayIcons;

/**
 * The figure for the start business function element.
 * 
 * @author Peter Erzberger
 */
public class StartFigure extends Shape {

    private Image errorImage;
    private boolean showError;

    public StartFigure() {
        errorImage = IpsUIPlugin.getImageHandling().getImage(OverlayIcons.ERROR_OVR_DESC);
    }

    @Override
    protected void fillShape(Graphics graphics) {
        Rectangle bounds = getBounds();
        graphics.pushState();
        graphics.translate(bounds.x, bounds.y);
        graphics.setBackgroundColor(ColorConstants.black);
        graphics.fillOval(6, 6, bounds.width - 12, bounds.height - 12);
        if (showError) {
            graphics.drawImage(errorImage, new Point(1, 1));
        }
        graphics.popState();
    }

    public void showError(boolean show) {
        showError = show;
    }

    @Override
    protected void outlineShape(Graphics graphics) {
        Rectangle bounds = getBounds();
        graphics.pushState();
        graphics.translate(bounds.x, bounds.y);
        graphics.setBackgroundColor(ColorConstants.black);
        graphics.drawOval(6, 6, bounds.width - 12, bounds.height - 12);
        graphics.popState();
    }
}
