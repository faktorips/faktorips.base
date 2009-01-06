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

package org.faktorips.devtools.core.ui.editors.testcase.deltapresentation;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Composite image to indicate that an object will be removed. A delete image will be rendered above
 * a given base image.
 * 
 * @author Joerg Ortmann
 */
public class DeltaCompositeIcon extends CompositeImageDescriptor {
    private final Point DEFAULT_SIZE = new Point(16, 16);
    private Image baseImage;
    private static int NEW = 1;
    private static int TO_BE_DELETED = 2;
    private int imageFor = 0;
    
    public static Image createToBeDeletedImage(Image baseImage){
        return IpsPlugin.getDefault().getImage(new DeltaCompositeIcon(baseImage, TO_BE_DELETED));
    }
    
    public static Image createNewImage(Image baseImage){
        return IpsPlugin.getDefault().getImage(new DeltaCompositeIcon(baseImage, NEW));
    }
    
    private DeltaCompositeIcon(Image baseImage, int imageFor){
        this.imageFor = imageFor;
        this.baseImage = baseImage;
    }
    
    protected void drawCompositeImage(int width, int height) {
        if (baseImage == null){
            return;
        }
            
        drawImage(baseImage.getImageData(), 0, 0);
        if (imageFor == TO_BE_DELETED)
            drawImage(IpsPlugin.getDefault().getImage("DeleteOverlay.gif").getImageData(), 0, 0); //$NON-NLS-1$
        else if (imageFor == NEW)
            drawImage(IpsPlugin.getDefault().getImage("AddOverlay.gif").getImageData(), 0, 0);         //$NON-NLS-1$
    }

    protected Point getSize() {
        return DEFAULT_SIZE;
    }
}
