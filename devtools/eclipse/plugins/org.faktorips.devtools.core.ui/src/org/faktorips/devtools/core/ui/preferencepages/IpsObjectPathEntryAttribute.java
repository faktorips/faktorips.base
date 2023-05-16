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

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;

/**
 * Implementation of IIpsObjectPathEntryAttribute
 * 
 * @author Roman Grutza
 */
public class IpsObjectPathEntryAttribute implements IIpsObjectPathEntryAttribute {

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "IIPSOBJECTPATHENTRYATTRIBUTE-"; //$NON-NLS-1$
    /**
     * Validation message code to indicate that a related folder is missing.
     */
    public static final String MSGCODE_MISSING_FOLDER = MSGCODE_PREFIX + "MissingFolder"; //$NON-NLS-1$
    /**
     * Message code constant to indicate the source folder must be a direct child of the project and
     * it isn't.
     */
    public static final String MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROJECT = "SourceFolder must be a direct child of the project."; //$NON-NLS-1$

    private String type;

    private Object value;

    /**
     * 
     * @param type of the attribute, can be one of the defined String constants as defined in
     *            IIpsObjectPathEntryAttribute
     * @param value object to be set
     */
    public IpsObjectPathEntryAttribute(String type, Object value) {
        if (isDerivedOrMergable(type)) {
            this.type = type;
            this.value = value;
            return;
        }
        if (value == null) {
            MessageDialog.openWarning(null, "ERROR", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
            throw new IllegalArgumentException("value == null !"); //$NON-NLS-1$
        }
        throw new IllegalArgumentException(
                "Attribute type must be one of the constants defined in IIpsObjectPathEntryAttribute"); //$NON-NLS-1$
    }

    private boolean isDerivedOrMergable(String type) {
        return isDerived(type) || isMergable(type) || IIpsObjectPathEntryAttribute.SPECIFIC_TOC_PATH.equals(type);
    }

    private boolean isDerived(String type) {
        return IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED.equals(type)
                || IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type);
    }

    private boolean isMergable(String type) {
        return IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_MERGABLE.equals(type)
                || IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_MERGABLE.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        // DEBUG
        if (value == null) {
            MessageDialog.openWarning(null, "ERROR", "ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
            throw new IllegalArgumentException("value == null !"); //$NON-NLS-1$
        }
        this.value = value;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isFolderForDerivedSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES
                .equals(type));
    }

    @Override
    public boolean isFolderForMergableSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES
                .equals(type));
    }

    @Override
    public boolean isTocPath() {
        return (IIpsObjectPathEntryAttribute.SPECIFIC_TOC_PATH.equals(type));
    }

    @Override
    public boolean isPackageNameForDerivedSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED.equals(type)
                || (IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED
                        .equals(type)));
    }

    @Override
    public boolean isPackageNameForMergableSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_MERGABLE.equals(type)
                || (IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_MERGABLE
                        .equals(type)));
    }

    @Override
    public MessageList validate() {
        MessageList result = new MessageList();

        if (isFolderForDerivedSources() || isFolderForMergableSources()) {
            IFolder sourceFolder = (IFolder)value;

            // FIXME: workaround for NPE, find root cause!!
            if (sourceFolder == null) {
                result.add(new Message("Folder invalid", "Folder invalid", Message.ERROR, this)); //$NON-NLS-1$ //$NON-NLS-2$
                return result;
            }

            result.add(validateIfFolderExists(sourceFolder));
        }

        return result;
    }

    /**
     * Validate that the given folder exists.
     */
    private MessageList validateIfFolderExists(IFolder folder) {
        MessageList result = new MessageList();
        if (!folder.exists()) {
            String text = NLS.bind("The folder \"{0}\" does not exist.", folder.getProjectRelativePath().toString()); //$NON-NLS-1$
            Message msg = new Message(MSGCODE_MISSING_FOLDER, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }

}
