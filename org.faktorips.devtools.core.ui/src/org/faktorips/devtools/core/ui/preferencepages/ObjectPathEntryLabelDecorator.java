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

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.ui.OverlayIcons;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Decorator for IPS Object path entries
 * 
 * @author Roman Grutza
 */
public class ObjectPathEntryLabelDecorator implements ILightweightLabelDecorator {

    /**
     * {@inheritDoc}
     */
    @Override
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
            decoration.addOverlay(OverlayIcons.WARNING_OVR_DESC);
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
     * @param errorCode as defined in IIpsArchiveEntry
     * 
     * @return short error description or an empty String if not available
     */
    private String getMessageForArchiveEntry(String errorCode) {
        String decoratedShortMessage = ""; //$NON-NLS-1$

        if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_MISSING_ARCHVE)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_missing;
        } else if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_MISSING_FOLDER)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_missing_folder;
        } else if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_MISSING_PROJECT)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_missing_project;
        } else if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_PROJECT_NOT_SPECIFIED)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_project_not_specified;
        }
        return decoratedShortMessage;
    }

    /*
     * @param errorCode as defined in IIpsProjectRefEntry
     * 
     * @return short error description or an empty String if not available
     */
    private String getMessageForProjectRefEntry(String errorCode) {
        String decoratedShortMessage = ""; //$NON-NLS-1$

        if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_PROJECT_NOT_SPECIFIED)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_not_specified;
        } else if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_MISSING_PROJECT)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_missing;
        }
        return decoratedShortMessage;
    }

    /*
     * @param errorCode as defined in IIpsSrcFolderEntry
     * 
     * @return short error description or an empty String if not available
     */
    private String getMessageForSrcFolderEntry(String errorCode) {
        String decoratedShortMessage = ""; //$NON-NLS-1$

        if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_MISSING_FOLDER)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_missing;
        } else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROHECT)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_not_child_of_root;
        } else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_DERIVED_MISSING)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_derived_folder_undefined;
        } else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_DERIVED_DOESNT_EXIST)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_derived_folder_not_existing;
        } else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_DOESNT_EXIST)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_mergable_folder_not_existing;
        } else if (errorCode.equals(IIpsSrcFolderEntry.MSGCODE_OUTPUT_FOLDER_MERGABLE_MISSING)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_mergable_folder_undefined;
        }
        return decoratedShortMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(ILabelProviderListener listener) { /* nothing to do */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() { /* nothing to do */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(ILabelProviderListener listener) { /* nothing to do */
    }

}
