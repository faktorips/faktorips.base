package org.faktorips.devtools.core.ui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.util.ArgumentCheck;


/**
 *
 */
public class OverrideImageDescriptor extends CompositeImageDescriptor {

	private final static Point DEFAULT_SIZE = new Point(16, 16);
	
	private Image baseImage;
	private Point size = DEFAULT_SIZE;
	
    public OverrideImageDescriptor(Image image) {
	    ArgumentCheck.notNull(image);
		baseImage = image;
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
     */
    protected void drawCompositeImage(int width, int height) {
		drawImage(baseImage.getImageData(), 0, 0);
		drawImage(IpsPlugin.getDefault().getImage("OverrideIndicator.gif").getImageData(), 8, 8);
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
     */
    protected Point getSize() {
        return size;
    }

}
