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


    private static final ImageDescriptor WARNING_IMAGE_DESCRIPTOR = IpsPlugin.getDefault().getImageDescriptor("size8/WarningMessage.gif");

    
    /**
     * {@inheritDoc}
     */
    public void decorate(Object element, IDecoration decoration) {
        
        try {
            if (! (element instanceof IIpsObjectPathEntry)) {
                return;
            }
            
            IIpsObjectPathEntry entry = (IIpsObjectPathEntry) element;
            
            // map error codes to _short_ messages
            String decoratedShortMessage = "";
            MessageList ml = entry.validate();

            if (ml.containsErrorMsg()) {
                decoration.addOverlay(WARNING_IMAGE_DESCRIPTOR);
                Message message = ml.getFirstMessage(Message.ERROR);
                
                if (element instanceof IIpsSrcFolderEntry) {
                    decoratedShortMessage = getMessageForSrcFolderEntry(message.getCode());
                } 
                else if (element instanceof IIpsProjectRefEntry) {
                    decoratedShortMessage = getMessageForProjectRefEntry(message.getCode());
                } 
                else if (element instanceof IIpsArchiveEntry) {
                    decoratedShortMessage = getMessageForArchiveEntry(message.getCode());
                } 

                decoration.addSuffix(decoratedShortMessage);
            }
        } catch(CoreException e) {
            IpsPlugin.log(e);
        }

    }

    /*
     * @param errorCode as defined in IIpsArchiveEntry
     * @return short error description or an empty String if not available
     */
    private String getMessageForArchiveEntry(String errorCode) {
        String decoratedShortMessage = "";
        
        if (errorCode.equals(IIpsArchiveEntry.MSGCODE_MISSING_ARCHVE)) {
            decoratedShortMessage = " (missing)";
        }
        else if (errorCode.equals(IIpsArchiveEntry.MSGCODE_MISSING_FOLDER)) {
            decoratedShortMessage = " (missing folder)";
        }
        else if (errorCode.equals(IIpsArchiveEntry.MSGCODE_MISSING_PROJECT)) {
            decoratedShortMessage = " (missing project)";
        }
        else if (errorCode.equals(IIpsArchiveEntry.MSGCODE_PROJECT_NOT_SPECIFIED)) {
            decoratedShortMessage = " (project not specified)";
        }
        return decoratedShortMessage;
    }

    /*
     * @param errorCode as defined in IIpsProjectRefEntry
     * @return short error description or an empty String if not available
     */
    private String getMessageForProjectRefEntry(String errorCode) {
        String decoratedShortMessage = "";
        
        if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_PROJECT_NOT_SPECIFIED)) {
            decoratedShortMessage = " (not specified)";
        }
        else if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_MISSING_PROJECT)) {
            decoratedShortMessage = " (missing)";
        }
        return decoratedShortMessage;
    }

    /*
     * @param errorCode as defined in IIpsSrcFolderEntry
     * @return short error description or an empty String if not available
     */
    private String getMessageForSrcFolderEntry(String errorCode) {
        String decoratedShortMessage = "";
        
        if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_MISSING_FOLDER)) {
            decoratedShortMessage = " (missing)";
        }
        else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROHECT)) {
            decoratedShortMessage = " (not direct child of project root)";
        }
        else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING)) {
            decoratedShortMessage = " (derived folder undefined)";
        }
        else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_DERIVED_DOESNT_EXIST)) {
            decoratedShortMessage = " (derived folder doesnt exist)";
        }
        else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_DOESNT_EXIST)) {
            decoratedShortMessage = " (mergable folder doesnt exist)";
        }
        else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING)) {
            decoratedShortMessage = " (mergable folder undefined)";
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
