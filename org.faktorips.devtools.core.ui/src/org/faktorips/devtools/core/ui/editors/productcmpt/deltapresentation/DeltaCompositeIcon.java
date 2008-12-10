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

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Composite image to indicate changes to an object
 * 
 * @author Thorsten Guenther
 */
public class DeltaCompositeIcon extends CompositeImageDescriptor {
    private final Point DEFAULT_SIZE = new Point(16, 16);
    private Image baseImage;
    
    private static final int ADD = 1;
    private static final int DELETE = 2;
    private static final int MODIFY = 3;
    
    private int imageFor = 0;
    
    public static Image createDeleteImage(Image baseImage){
        return IpsPlugin.getDefault().getImage(new DeltaCompositeIcon(baseImage, DELETE));
    }
    
    public static Image createAddImage(Image baseImage){
        return IpsPlugin.getDefault().getImage(new DeltaCompositeIcon(baseImage, ADD));
    }
    
    public static Image createModifyImage(Image baseImage){
        return IpsPlugin.getDefault().getImage(new DeltaCompositeIcon(baseImage, MODIFY));
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
        
        switch (imageFor) {
            case ADD:
                drawImage(IpsPlugin.getDefault().getImage("AddOverlay.gif").getImageData(), 8, 0); //$NON-NLS-1$
                break;
            case DELETE:
                drawImage(IpsPlugin.getDefault().getImage("DeleteOverlay.gif").getImageData(), 8, 0); //$NON-NLS-1$
                break;
            case MODIFY:
                drawImage(IpsPlugin.getDefault().getImage("ModifyOverlay.gif").getImageData(), 8, 0); //$NON-NLS-1$
                break;
        }
    }

    protected Point getSize() {
        return DEFAULT_SIZE;
    }
}
