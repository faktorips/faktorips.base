/**
 * 
 */
package org.faktorips.devtools.bf.ui.draw2d;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * The figure for the start business function element.
 * 
 * @author Peter Erzberger
 */
public class StartFigure extends Shape {

    private Image errorImage;
    private boolean showError;

    public StartFigure() {
        errorImage = IpsPlugin.getDefault().getImage("size8/ErrorMessage.gif"); //$NON-NLS-1$
    }

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
        this.showError = show;
    }

    protected void outlineShape(Graphics graphics) {
        Rectangle bounds = getBounds();
        graphics.pushState();
        graphics.translate(bounds.x, bounds.y);
        graphics.setBackgroundColor(ColorConstants.black);
        graphics.drawOval(6, 6, bounds.width - 12, bounds.height - 12);
        graphics.popState();
    }
}