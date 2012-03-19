/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.ui.actions.Messages;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * NewResourceNameValidator for new resource name.
 * 
 * @author Thorsten Guenther
 */
public class NewResourceNameValidator implements IInputValidator {

    private final IPath root;
    private final String extension;
    private final int resourceType;
    private final IIpsProjectNamingConventions namingConventions;
    private final IpsObjectType ipsObjectType;

    public NewResourceNameValidator(IPath root, int resourceType, String extension, IIpsSrcFile sourceIpsSrcFile) {
        this.root = root;
        this.extension = extension;
        this.resourceType = resourceType;
        namingConventions = sourceIpsSrcFile == null ? null : sourceIpsSrcFile.getIpsProject().getNamingConventions();
        ipsObjectType = sourceIpsSrcFile == null ? null : sourceIpsSrcFile.getIpsObjectType();
    }

    @Override
    public String isValid(String newText) {
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        IResource test = null;
        if (resourceType == IResource.FILE) {
            if (namingConventions != null) {
                try {
                    MessageList messageList = namingConventions
                            .validateUnqualifiedIpsObjectName(ipsObjectType, newText);
                    if (messageList.containsErrorMsg()) {
                        return messageList.getFirstMessage(Message.ERROR).getText();
                    }
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e.getMessage(), e);
                }
            }
            test = wsRoot.getFile(root.append(newText + extension));
        } else if (resourceType == IResource.FOLDER) {
            test = wsRoot.getFolder(root.append(newText));
        }
        if (test != null && test.exists()) {
            return newText + extension + Messages.IpsPasteAction_msgFileAllreadyExists;
        }
        return null;
    }

    /**
     * Returns a valid resource name for the given resource name.
     * <ul>
     * <li>If a resource with the given name does not yet exist, the name itself is directly
     * returned.
     * <li>If a resource with the given name exists, a locale-specific prefix is attached to the
     * name and the catenation is returned.
     * <li>If a resource with the given name and locale-specific prefix already exists, a discrete
     * number is added to the locale-specific prefix until a valid name has been found.
     * </ul>
     * 
     * @param resourceName the resource name to obtain a valid version of
     */
    public String getValidResourceName(String resourceName) {
        String validResourceName = resourceName;
        for (int count = 0; isValid(validResourceName) != null; count++) {
            if (count == 0) {
                validResourceName = Messages.NewResourceNameValidator_suggestedNamePrefixSimple + resourceName;
            } else {
                validResourceName = NLS.bind(Messages.NewResourceNameValidator_suggestedNamePrefixComplex, new Integer(
                        count), resourceName);
            }
        }
        return validResourceName;
    }

}