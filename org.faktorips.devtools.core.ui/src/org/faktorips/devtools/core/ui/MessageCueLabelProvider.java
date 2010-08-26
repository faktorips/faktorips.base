/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
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

    private ResourceManager resourceManager;

    private ILabelProvider baseProvider;

    private IIpsProject ipsProject;

    /**
     * Creates a new <code>MessageCueLabelProvider</code>.
     * 
     * @param baseProvider The label provider to decorate the image for.
     * 
     * @throws NullPointerException If baseProvider or ipsProject is <code>null</code>.
     */
    public MessageCueLabelProvider(ILabelProvider baseProvider, IIpsProject ipsProject) {
        ArgumentCheck.notNull(baseProvider, this);
        ArgumentCheck.notNull(ipsProject, this);

        this.baseProvider = baseProvider;
        this.ipsProject = ipsProject;
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

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
        ImageDescriptor descriptor = IpsProblemOverlayIcon.createOverlayIcon(baseImage, list.getSeverity());
        return (Image)resourceManager.get(descriptor);
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
        for (IIpsElement child : childs) {
            if (child instanceof IIpsObjectPartContainer) {
                collectMessagesForIpsObjectPartContainer(result, msgList, (IIpsObjectPartContainer)child);
            }
        }
    }

    @Override
    public String getText(Object element) {
        return baseProvider.getText(element);
    }

    /**
     * Disposes all images managed by this label provider.
     */
    @Override
    public void dispose() {
        resourceManager.dispose();
        super.dispose();
    }

}
