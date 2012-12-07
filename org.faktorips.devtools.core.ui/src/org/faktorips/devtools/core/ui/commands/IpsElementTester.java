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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * Tests some properties of ips elemtns for example whether the element is ediable or not. The
 * receiver object needs to be an ips element.
 * 
 * @author dirmeier
 */
public class IpsElementTester extends PropertyTester {

    /**
     * Check wether the receiver element is editable
     */
    public static final String PROPERTY_EDITABLE = "isEditable"; //$NON-NLS-1$

    /**
     * Check wether the receivers container is editable
     */
    public static final String PROPERTY_CONTAINER_EDITABLE = "isContainerEditable"; //$NON-NLS-1$

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (!(receiver instanceof IIpsElement)) {
            return false;
        }
        IIpsElement ipsElement = (IIpsElement)receiver;
        if (PROPERTY_EDITABLE.equals(property)) {
            return isEditable(ipsElement);
        } else if (PROPERTY_CONTAINER_EDITABLE.equals(property)) {
            return isContainerEditable(ipsElement);
        } else {
            return false;
        }
    }

    private boolean isEditable(IIpsElement ipsElement) {
        if (IpsPlugin.getDefault().getIpsPreferences().isWorkingModeBrowse()) {
            return false;
        }
        if (ipsElement.isContainedInArchive()) {
            return false;
        }
        if (ipsElement instanceof IIpsObjectPartContainer) {
            return isEditable(((IIpsObjectPartContainer)ipsElement).getIpsSrcFile());
        }
        if (ipsElement instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
            return ipsSrcFile.isMutable();
        }
        IResource resource = (IResource)ipsElement.getAdapter(IResource.class);
        if (resource != null && resource.getResourceAttributes() != null) {
            return !resource.getResourceAttributes().isReadOnly();
        }
        return true;
    }

    private boolean isContainerEditable(IIpsElement ipsElement) {
        if (ipsElement.getParent() == null) {
            return isEditable(ipsElement);
        }
        return isEditable(ipsElement.getParent());
    }

}
