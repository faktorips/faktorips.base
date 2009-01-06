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
import org.faktorips.util.message.Message;


/**
 * An image descriptor which adds an error image to the image provided to it.
 * 
 * @author Jan Ortmann
 */
public class ProblemImageDescriptor extends CompositeImageDescriptor
{
	private final static Point DEFAULT_SIZE = new Point(16, 16);
	
	private Image baseImage;
	private int severity;
	private Point size = DEFAULT_SIZE;
	
	/**
	 * Instantiates a new ProblemImgeDescriptor. It keeps a reference to 
	 * the provided image.
	 */
	public ProblemImageDescriptor(Image image, int severity) {
	    ArgumentCheck.notNull(image);
		baseImage = image;
		this.severity = severity;
	}
	
	/** 
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
	 */
	protected void drawCompositeImage(int width, int height) {
		drawImage(baseImage.getImageData(), 0, 0);
		drawImage(getSeverityImage(severity).getImageData(), 0, 0);
	}
	
	/** 
	 * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
	 */
	protected Point getSize() {
		return size;
	}
	
	/**
	 * Changes the size for the image represented by this image descriptor.
	 */
	public void setSize(Point size) {
		ArgumentCheck.notNull(size);
		this.size = size;
	}
	
	/**
	 * Returns the cue image based on the message list's severity.
	 */
	private Image getSeverityImage(int severity) {
	    String imageName;
	    switch (severity) {
	    	case Message.ERROR: {
	    	    imageName = "size8/ErrorMessage.gif"; //$NON-NLS-1$
	    	    break;
	    	}
	    	case Message.WARNING: {
	    	    imageName = "size8/WarningMessage.gif"; //$NON-NLS-1$
	    	    break;
	    	}
	    	case Message.INFO: {
	    	    imageName = "size8/InfoMessage.gif"; //$NON-NLS-1$
	    	    break;
	    	}
	    	default:
	    	    imageName = "size8/NullMessage.gif"; //$NON-NLS-1$
	    }
        return IpsPlugin.getDefault().getImage(imageName);
	}
}
