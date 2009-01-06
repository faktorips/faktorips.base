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
     * {@inheritDoc}
     */
    protected void drawCompositeImage(int width, int height) {
		drawImage(baseImage.getImageData(), 0, 0);
		drawImage(IpsPlugin.getDefault().getImage("OverrideIndicator.gif").getImageData(), 8, 8); //$NON-NLS-1$
    }

    /** 
     * {@inheritDoc}
     */
    protected Point getSize() {
        return size;
    }

}
