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

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Decorator for IPS Object path entries
 * @author Roman Grutza
 */
public class ObjectPathEntryLabelDecorator implements ILightweightLabelDecorator {


    private static final ImageDescriptor WARNING_IMAGE_DESCRIPTOR = IpsPlugin.getDefault().getImageDescriptor("size8/WarningMessage.gif"); //$NON-NLS-1$

    
    /**
     * {@inheritDoc}
     */
    public void decorate(Object element, IDecoration decoration) {
        
        MessageList ml = null;
        try {
            if (element instanceof IIpsObjectPathEntry) {
                ml = ((IIpsObjectPathEntry)element).validate();
            } else if (element instanceof IIpsObjectPathEntryAttribute) {
                ml = ((IIpsObjectPathEntryAttribute)element).validate();
            } else {
                return;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        // map error codes to _short_ messages
        String decoratedShortMessage = ""; //$NON-NLS-1$

        if (ml != null && ml.containsErrorMsg()) {
            decoration.addOverlay(WARNING_IMAGE_DESCRIPTOR);
            Message message = ml.getFirstMessage(Message.ERROR);

            if (element instanceof IIpsSrcFolderEntry) {
                decoratedShortMessage = getMessageForSrcFolderEntry(message.getCode());
            } else if (element instanceof IIpsProjectRefEntry) {
                decoratedShortMessage = getMessageForProjectRefEntry(message.getCode());
            } else if (element instanceof IIpsArchiveEntry) {
                decoratedShortMessage = getMessageForArchiveEntry(message.getCode());
            }

            decoration.addSuffix(decoratedShortMessage);
        }
    }

    /*
     * @param errorCode as defined in IIpsArchiveEntry @return short error description or an empty
     * String if not available
     */
    private String getMessageForArchiveEntry(String errorCode) {
        String decoratedShortMessage = ""; //$NON-NLS-1$
        
        if (errorCode.equals(IIpsArchiveEntry.MSGCODE_MISSING_ARCHVE)) {
            decoratedShortMessage = Messages.suffix_missing;
        }
        else if (errorCode.equals(IIpsArchiveEntry.MSGCODE_MISSING_FOLDER)) {
            decoratedShortMessage = Messages.suffix_missing_folder;
        }
        else if (errorCode.equals(IIpsArchiveEntry.MSGCODE_MISSING_PROJECT)) {
            decoratedShortMessage = Messages.suffix_missing_project;
        }
        else if (errorCode.equals(IIpsArchiveEntry.MSGCODE_PROJECT_NOT_SPECIFIED)) {
            decoratedShortMessage = Messages.suffix_project_not_specified;
        }
        return decoratedShortMessage;
    }

    /*
     * @param errorCode as defined in IIpsProjectRefEntry
     * @return short error description or an empty String if not available
     */
    private String getMessageForProjectRefEntry(String errorCode) {
        String decoratedShortMessage = ""; //$NON-NLS-1$
        
        if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_PROJECT_NOT_SPECIFIED)) {
            decoratedShortMessage = Messages.suffix_not_specified;
        }
        else if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_MISSING_PROJECT)) {
            decoratedShortMessage = Messages.suffix_missing;
        }
        return decoratedShortMessage;
    }

    /*
     * @param errorCode as defined in IIpsSrcFolderEntry
     * @return short error description or an empty String if not available
     */
    private String getMessageForSrcFolderEntry(String errorCode) {
        String decoratedShortMessage = ""; //$NON-NLS-1$
        
        if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_MISSING_FOLDER)) {
            decoratedShortMessage = Messages.suffix_missing;
        }
        else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROHECT)) {
            decoratedShortMessage = Messages.suffix_not_child_of_root;
        }
        else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING)) {
            decoratedShortMessage = Messages.suffix_derived_folder_undefined;
        }
        else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_DERIVED_DOESNT_EXIST)) {
            decoratedShortMessage = Messages.suffix_derived_folder_not_existing;
        }
        else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_DOESNT_EXIST)) {
            decoratedShortMessage = Messages.suffix_mergable_folder_not_existing;
        }
        else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING)) {
            decoratedShortMessage = Messages.suffix_mergable_folder_undefined;
        }
        return decoratedShortMessage;
    }
    
    /**
     * {@inheritDoc}
     */
    public void addListener(ILabelProviderListener listener) { /* nothing to do */ }

    /**
     * {@inheritDoc}
     */
    public void dispose() { /* nothing to do */ }

    /**
     * {@inheritDoc}
     */
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void removeListener(ILabelProviderListener listener) { /* nothing to do */ }

}
