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

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The message cue label provider takes a given label provider and decorates the image with a
 * message cue image. The text is returned unchanged.
 * 
 * @author Jan Ortmann
 */
public class MessageCueLabelProvider extends LabelProvider {

    private ILabelProvider baseProvider;
    private IIpsProject ipsProject;
    private HashMap<String, ProblemImageDescriptor> cachedProblemImageDescriptors = new HashMap<String, ProblemImageDescriptor>();

    /**
     * Creates a new <code>MessageCueLabelProvider</code>.
     * 
     * @param baseProvider The label provider to decorate the image for.
     * @param ipsProject
     * 
     * @throws NullPointerException If baseProvider or ipsProject is <code>null</code>.
     */
    public MessageCueLabelProvider(ILabelProvider baseProvider, IIpsProject ipsProject) {
        ArgumentCheck.notNull(baseProvider, this);
        ArgumentCheck.notNull(ipsProject, this);
        
        this.baseProvider = baseProvider;
        this.ipsProject = ipsProject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage(Object element) {
        MessageList list = null;
        try {
            list = getMessages(element);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return baseProvider.getImage(element);
        }

        Image baseImage = baseProvider.getImage(element);

        if (list.getSeverity() == Message.NONE) {
            return baseImage;
        }

        // get the cached problem descriptor for the base image
        String key = getKey(baseImage, list.getSeverity());
        ProblemImageDescriptor descriptor = (ProblemImageDescriptor)cachedProblemImageDescriptors.get(key);
        if (descriptor == null && baseImage != null) {
            descriptor = new ProblemImageDescriptor(baseImage, list.getSeverity());
            cachedProblemImageDescriptors.put(key, descriptor);
        }
        
        return IpsPlugin.getDefault().getImage(descriptor);
    }

    // Returns a unique key for the given image and severity compination
    private String getKey(Image image, int severity) {
        if (image == null) {
            return null;
        }
        
        return image.hashCode() + "_" + severity; //$NON-NLS-1$
    }

    /**
     * Returns the message list applying to the given element.
     * 
     * @throws CoreException If an error occurs during the creation of the message list.
     */
    public MessageList getMessages(Object element) throws CoreException {
        MessageList result = new MessageList();
        if (element instanceof IIpsObjectPartContainer) {
            IIpsObjectPartContainer part = (IIpsObjectPartContainer)element;
            MessageList msgList = part.getIpsObject().validate(ipsProject);
            collectMessagesForIpsObjectPartContainer(result, msgList, part);
        }
        
        return result;
    }

    private void collectMessagesForIpsObjectPartContainer(MessageList result,
            MessageList msgList,
            IIpsObjectPartContainer container) throws CoreException {
        
        result.add(msgList.getMessagesFor(container));
        IIpsElement[] childs = container.getChildren();
        for (int i = 0; i < childs.length; i++) {
            if (childs[i] instanceof IIpsObjectPartContainer) {
                collectMessagesForIpsObjectPartContainer(result, msgList, (IIpsObjectPartContainer)childs[i]);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText(Object element) {
        return baseProvider.getText(element);
    }

    /**
     * Disposes all images managed by this label provider.
     */
    public void dispose() {
        for (Iterator<ProblemImageDescriptor> iter = cachedProblemImageDescriptors.values().iterator(); iter.hasNext();) {
            ProblemImageDescriptor problemImageDescriptor = (ProblemImageDescriptor)iter.next();
            Image problemImage = IpsPlugin.getDefault().getImage(problemImageDescriptor);
            if (problemImage != null) {
                problemImage.dispose();
            }
        }
        
        cachedProblemImageDescriptors.clear();
        super.dispose();
    }
}
