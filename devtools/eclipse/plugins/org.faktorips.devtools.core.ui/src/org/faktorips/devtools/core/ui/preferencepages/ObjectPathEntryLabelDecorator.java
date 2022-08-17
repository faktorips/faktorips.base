/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.decorators.OverlayIcons;
import org.faktorips.devtools.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Decorator for IPS Object path entries
 * 
 * @author Roman Grutza
 */
public class ObjectPathEntryLabelDecorator implements ILightweightLabelDecorator {

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
        } catch (IpsException e) {
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

    /**
     * @param errorCode as defined in IIpsArchiveEntry
     * 
     * @return short error description or an empty String if not available
     */
    private String getMessageForArchiveEntry(String errorCode) {
        String decoratedShortMessage = ""; //$NON-NLS-1$

        if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_MISSING_ARCHVE)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_missing;
        } else if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_INVALID_ARCHVE)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_invalid;
        } else if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_MISSING_FOLDER)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_missing_folder;
        } else if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_MISSING_PROJECT)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_missing_project;
        } else if (errorCode.equals(IIpsObjectPathEntry.MSGCODE_PROJECT_NOT_SPECIFIED)) {
            decoratedShortMessage = Messages.ObjectPathEntryLabelDecorator_suffix_project_not_specified;
        }
        return decoratedShortMessage;
    }

    /**
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

    /**
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

    @Override
    public void addListener(ILabelProviderListener listener) {
        // nothing to do
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // nothing to do
    }

}
