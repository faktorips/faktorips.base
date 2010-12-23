/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

public class IpsObjectTypePathUtil implements PathUtil {
    private final IpsObjectType ipsObjectType;

    public IpsObjectTypePathUtil(IpsObjectType ipsObjectType) {
        this.ipsObjectType = ipsObjectType;
    }

    @Override
    public String getPathToRoot() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public String getPathFromRoot(LinkedFileType linkedFileType) {
        return ipsObjectType.getFileExtension() + "_index"; //$NON-NLS-1$
    }

    @Override
    public String getLinkText(boolean withImage) {
        return ipsObjectType.getDisplayName();
    }

}
