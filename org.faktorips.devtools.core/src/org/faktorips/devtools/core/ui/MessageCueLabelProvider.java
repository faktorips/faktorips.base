/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;


/**
 * The message cue label provider takes a given label provider and
 * decorates the image with a message cue image. The text is returned
 * unchanged.
 */
public class MessageCueLabelProvider extends LabelProvider {
    
    private ILabelProvider baseProvider;

    private static HashMap cachedProblemImageDescriptors = new HashMap();
    
    /**
     * 
     */
    public MessageCueLabelProvider(ILabelProvider baseProvider) {
        this.baseProvider = baseProvider;
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        MessageList list = null;
        try {
        	list = getMessages(element);    
        } catch (CoreException e) {
        	IpsPlugin.log(e);
        	return baseProvider.getImage(element);
        }

        Image baseImage = baseProvider.getImage(element);
        
        if (list.getSeverity()==Message.NONE) {
		    return baseImage;
		}
        
        // get the cached problem descriptor for the base image
        String key = getKey(baseImage, list.getSeverity());
        ProblemImageDescriptor descriptor = (ProblemImageDescriptor) cachedProblemImageDescriptors.get(key);
        if (descriptor == null){
            baseImage = baseProvider.getImage(element);
        	descriptor = new ProblemImageDescriptor(baseImage, list.getSeverity());
        	cachedProblemImageDescriptors.put(key, descriptor);
        }
		return IpsPlugin.getDefault().getImage(descriptor);
    }

    /*
     * Returns an unique key for the given image and severity compination.
     */
    private String getKey(Image image, int severity) {
    	if (image == null){
    		return null;
    	}
		return image.hashCode() + "_" + severity;
	}

	/**
     * Returns the message list applying to the given element.

     * @throws CoreException if an error occurs during the creation of the message list.
     */
    protected MessageList getMessages(Object element) throws CoreException {
        if (element instanceof IIpsObjectPart) {
            return ((IIpsObjectPart)element).validate();    
        } else {
            return new MessageList();
        }
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        return baseProvider.getText(element);
    }
    
	/**
	 * Disposes all images managed by this label provider.
	 */
	public void dispose() {
		for (Iterator iter = cachedProblemImageDescriptors.values().iterator(); iter.hasNext();) {
			ProblemImageDescriptor problemImageDescriptor = (ProblemImageDescriptor)iter.next();
			Image problemImage = IpsPlugin.getDefault().getImage(problemImageDescriptor);
			if (problemImage != null){
				problemImage.dispose();
			}
		}
		cachedProblemImageDescriptors.clear();
		super.dispose();
	}
}
