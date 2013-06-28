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

package org.faktorips.devtools.core.internal.fl;

import java.util.List;

import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.fl.IFlIdentifierFilterExtension;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * An {@link IdentifierFilter} is used to check whether an {@link IIpsObjectPartContainer} is
 * allowed to be used as a identifier in an formula expression.
 * 
 * @author frank
 */
public class IdentifierFilter {

    private final List<IFlIdentifierFilterExtension> flIdentifierFilters;

    public IdentifierFilter() {
        flIdentifierFilters = getFlIdentifierFilters();
    }

    /**
     * Returns the {@link IFlIdentifierFilterExtension}s that are registered at the according
     * extension-point.
     */
    private List<IFlIdentifierFilterExtension> getFlIdentifierFilters() {
        ExtensionPoints extensionPoints = new ExtensionPoints(IpsPlugin.getDefault().getExtensionRegistry(),
                IpsPlugin.PLUGIN_ID);
        return extensionPoints.createExecutableExtensions(ExtensionPoints.FL_IDENTIFIER_FILTER_EXTENSION,
                ExtensionPoints.FL_IDENTIFIER_FILTER_EXTENSION, ExtensionPoints.ATTRIBUTE_CLASS,
                IFlIdentifierFilterExtension.class);
    }

    public boolean isIdentifierAllowed(IIpsObjectPartContainer ipsObjectPartContainer) {
        for (IFlIdentifierFilterExtension flIdentifierFilterExtension : flIdentifierFilters) {
            if (!flIdentifierFilterExtension.isIdentifierAllowed(ipsObjectPartContainer)) {
                return false;
            }
        }
        return true;
    }

}
