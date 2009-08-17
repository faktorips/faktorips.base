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
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
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
 * This class extends the StyledCellLabelProvider to support styled labels. Note that the
 * StyledCellLabelProvider use the update method instead of the getImage and getText Methods. (see
 * implementation classes of CellLabelProvider#update(ViewerCell)).
 * 
 * Warning: This class delegates only the update method to the underlying styled cell label provider
 * and decorates the image. All other methods e.g. setting tooltip are not supported. This
 * functionality of the base label provider doesn't work after using this label provider as
 * decoration.
 * 
 * This class is based on the MessageCueLabelProvider. After using a new decoration functionality,
 * we should cleanup / remove this class.
 * 
 * @author Joerg Ortmann
 */
public class StyledCellMessageCueLabelProvider extends StyledCellLabelProvider {

    private StyledCellLabelProvider baseProvider;
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
    public StyledCellMessageCueLabelProvider(StyledCellLabelProvider baseProvider, IIpsProject ipsProject) {
        ArgumentCheck.notNull(baseProvider, this);
        ArgumentCheck.notNull(ipsProject, this);

        this.baseProvider = baseProvider;
        this.ipsProject = ipsProject;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage(Object element) {
        MessageList list = null;
        try {
            list = getMessages(element);
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return ((ILabelProvider)baseProvider).getImage(element);
        }

        Image baseImage = ((ILabelProvider)baseProvider).getImage(element);

        if (list.getSeverity() == Message.NONE) {
            return baseImage;
        }

        // get the cached problem descriptor for the base image
        String key = getKey(baseImage, list.getSeverity());
        ProblemImageDescriptor descriptor = cachedProblemImageDescriptors.get(key);
        if (descriptor == null && baseImage != null) {
            descriptor = new ProblemImageDescriptor(baseImage, list.getSeverity());
            cachedProblemImageDescriptors.put(key, descriptor);
        }

        return IpsPlugin.getDefault().getImage(descriptor);
    }

    /*
     * Returns a unique key for the given image and severity compination
     */
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

    @Override
    public void update(ViewerCell cell) {
        baseProvider.update(cell);
        // decorate the image if there are messages for the corresponding object
        cell.setImage(getImage(cell.getElement()));
    }

    /**
     * Disposes all images managed by this label provider.
     */
    @Override
    public void dispose() {
        for (Iterator<ProblemImageDescriptor> iter = cachedProblemImageDescriptors.values().iterator(); iter.hasNext();) {
            ProblemImageDescriptor problemImageDescriptor = iter.next();
            Image problemImage = IpsPlugin.getDefault().getImage(problemImageDescriptor);
            if (problemImage != null) {
                problemImage.dispose();
            }
        }

        cachedProblemImageDescriptors.clear();
        super.dispose();
    }
}
