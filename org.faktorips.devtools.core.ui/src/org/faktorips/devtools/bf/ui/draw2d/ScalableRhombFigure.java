/**
 * 
 */
package org.faktorips.devtools.bf.ui.draw2d;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.util.message.MessageList;

/**
 * The figure for the decision and merge business function elements.
 * 
 * @author Peter Erzberger
 */
public class ScalableRhombFigure extends Shape {

    private final PointList myTemplate = new PointList();
    private Rectangle myTemplateBounds;
    private Image errorImage;
    private boolean showError;
    
    
    public ScalableRhombFigure() {
        this.addPoint(new Point(20, 0));
        this.addPoint(new Point(40, 20));
        this.addPoint(new Point(20, 40));
        this.addPoint(new Point(0, 20));
        errorImage = IpsPlugin.getDefault().getImage("size8/ErrorMessage.gif"); //$NON-NLS-1$
    }

    public void addPoint(Point point) {
        myTemplate.addPoint(point);
        myTemplateBounds = null;
    }

    protected void fillShape(Graphics graphics) {
        Rectangle bounds = getBounds();
        graphics.pushState();
        graphics.translate(bounds.x, bounds.y);
        int[] points = scalePointList();
        graphics.fillPolygon(points);
        if(showError){
            graphics.drawImage(errorImage, new Point(points[6] + 10, points[7] - 3));
        }
        graphics.popState();
    }

    public void showError(boolean show){
        this.showError = show;
    }
    
    protected void outlineShape(Graphics graphics) {
        Rectangle bounds = getBounds();
        graphics.pushState();
        graphics.translate(bounds.x, bounds.y);
        graphics.drawPolygon(scalePointList());
        graphics.popState();
    }

    public void showError(MessageList msgList){
        
    }
    
    private Rectangle getTemplateBounds() {
        if (myTemplateBounds == null) {
            myTemplateBounds = myTemplate.getBounds().getCopy().union(0, 0);
            // just safety -- we are going to use this as divider
            if (myTemplateBounds.width < 1) {
                myTemplateBounds.width = 1;
            }
            if (myTemplateBounds.height < 1) {
                myTemplateBounds.height = 1;
            }
        }
        return myTemplateBounds;
    }

    private int[] scalePointList() {
        Rectangle pointsBounds = getTemplateBounds();
        Rectangle actualBounds = getBounds();

        float xScale = ((float)actualBounds.width) / pointsBounds.width;
        float yScale = ((float)actualBounds.height) / pointsBounds.height;

        if (xScale == 1 && yScale == 1) {
            return myTemplate.toIntArray();
        }
        int[] scaled = (int[])myTemplate.toIntArray().clone();
        for (int i = 0; i < scaled.length; i += 2) {
            scaled[i] = (int)Math.floor(scaled[i] * xScale);
            scaled[i + 1] = (int)Math.floor(scaled[i + 1] * yScale);
        }
        return scaled;
    }

}