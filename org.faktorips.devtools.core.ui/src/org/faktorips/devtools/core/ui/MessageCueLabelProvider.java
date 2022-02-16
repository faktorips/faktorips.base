/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.views.IpsProblemOverlayIcon;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * The message cue label provider takes a given label provider and decorates the image with a
 * message cue image. The text is returned unchanged.
 * 
 * @author Jan Ortmann
 */
public class MessageCueLabelProvider extends LabelProvider implements IStyledLabelProvider {

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
        } catch (IpsException e) {
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
     * Returns the message list applying to the given IPS object part. The corresponding IPS object
     * is validated. Of all those validation messages the ones that apply to the part itself as well
     * as all of its children are returned.
     * <p>
     * Returns an empty list if the given object is not an IPS object part.
     * 
     * @throws IpsException If an error occurs during the creation of the message list.
     */
    public MessageList getMessages(Object element) {
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
            IIpsObjectPartContainer container) {

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

    @Override
    public StyledString getStyledText(Object element) {
        if (baseProvider instanceof IStyledLabelProvider) {
            return ((IStyledLabelProvider)baseProvider).getStyledText(element);
        }
        return new StyledString(baseProvider.getText(element));
    }

}
