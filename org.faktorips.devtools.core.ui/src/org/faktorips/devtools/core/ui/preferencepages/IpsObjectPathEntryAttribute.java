/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.preferencepages;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsproject.Messages;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * Implementation of IIpsObjectPathEntryAttribute
 * 
 * @author Roman Grutza
 */
public class IpsObjectPathEntryAttribute implements IIpsObjectPathEntryAttribute {

    String type;
    private Object value;

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "IIPSOBJECTPATHENTRYATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a related folder is missing.
     */
    public final static String MSGCODE_MISSING_FOLDER = MSGCODE_PREFIX + "MissingFolder"; //$NON-NLS-1$

    /**
     * Message code constant to indicate the source folder must be a direct child of the project and
     * it isn't.
     */
    public final static String MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROJECT = "SourceFolder must be a direct child of the project."; //$NON-NLS-1$

    /**
     * 
     * @param type of the attribute, can be one of the defined String constants as defined in
     *            IIpsObjectPathEntryAttribute
     * @param value object to be set
     */
    public IpsObjectPathEntryAttribute(String type, Object value) {
        if (IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED.equals(type)
                || IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_MERGABLE.equals(type)
                || IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_MERGABLE.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type)
                || IIpsObjectPathEntryAttribute.SPECIFIC_TOC_PATH.equals(type)) {
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
        return (IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_DERIVED_SOURCES.equals(type) || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_DERIVED_SOURCES
                .equals(type));
    }

    @Override
    public boolean isFolderForMergableSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES.equals(type) || IIpsObjectPathEntryAttribute.SPECIFIC_OUTPUT_FOLDER_FOR_MERGABLE_SOURCES
                .equals(type));
    }

    @Override
    public boolean isTocPath() {
        return (IIpsObjectPathEntryAttribute.SPECIFIC_TOC_PATH.equals(type));
    }

    @Override
    public boolean isPackageNameForDerivedSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_DERIVED.equals(type) || (IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_DERIVED
                .equals(type)));
    }

    @Override
    public boolean isPackageNameForMergableSources() {
        return (IIpsObjectPathEntryAttribute.DEFAULT_BASE_PACKAGE_MERGABLE.equals(type) || (IIpsObjectPathEntryAttribute.SPECIFIC_BASE_PACKAGE_MERGABLE
                .equals(type)));
    }

    @Override
    public MessageList validate() throws CoreException {
        MessageList result = new MessageList();

        if (isFolderForDerivedSources() || isFolderForMergableSources()) {
            IFolder sourceFolder = (IFolder)value;

            // FIXME: workaround for NPE, find root cause!!
            if (sourceFolder == null) {
                result.add(new Message("Folder invalid", "Folder invalid", Message.ERROR, this)); //$NON-NLS-1$ //$NON-NLS-2$
                return result;
            }

            result.add(validateIfFolderExists(sourceFolder));

            if (sourceFolder.getProjectRelativePath().segmentCount() > 1) {
                String text = NLS.bind(Messages.IpsSrcFolderEntry_srcFolderMustBeADirectChildOfTheProject, sourceFolder
                        .getProjectRelativePath().toString());
                Message msg = new Message(MSGCODE_SRCFOLDER_MUST_BE_A_DIRECT_CHILD_OF_THE_PROJECT, text, Message.ERROR,
                        this);
                result.add(msg);
            }
        }

        return result;
    }

    /**
     * Validate that the given folder exists.
     */
    private MessageList validateIfFolderExists(IFolder folder) {
        MessageList result = new MessageList();
        if (!folder.exists()) {
            String text = NLS.bind("The folder \"{0}\" does not exist.", folder.getName()); //$NON-NLS-1$
            Message msg = new Message(MSGCODE_MISSING_FOLDER, text, Message.ERROR, this);
            result.add(msg);
        }
        return result;
    }

}
