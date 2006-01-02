package org.faktorips.devtools.core.ui;

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
        if (element instanceof IIpsObjectPart) {
            try {
                list = ((IIpsObjectPart)element).validate();    
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return baseProvider.getImage(element);
            }
        } else {
            return baseProvider.getImage(element);
        }
		Image image = baseProvider.getImage(element);
		if (list.getSeverity()==Message.NONE) {
		    return baseProvider.getImage(element);
		}
		ProblemImageDescriptor descriptor = new ProblemImageDescriptor(image, list.getSeverity());
		return IpsPlugin.getDefault().getImage(descriptor);
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        return baseProvider.getText(element);
    }
    
}
