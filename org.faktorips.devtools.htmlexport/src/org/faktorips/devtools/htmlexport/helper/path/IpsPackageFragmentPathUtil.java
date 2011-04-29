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

package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * {@link IpsElementPathUtil} for an {@link IIpsPackageFragment}
 * 
 * @author dicker
 * 
 */
public class IpsPackageFragmentPathUtil extends AbstractIpsElementPathUtil<IIpsPackageFragment> {
    private static String PACKAGE_INDEX_FILE_NAME = "package_index"; //$NON-NLS-1$

    public IpsPackageFragmentPathUtil(IIpsPackageFragment ipsElement) {
        super(ipsElement);
    }

    @Override
    public String getPathFromRoot(LinkedFileType linkedFileType) {
        if (getIpsElement().isDefaultPackage()) {
            return PACKAGE_INDEX_FILE_NAME;
        }
        return super.getPathFromRoot(linkedFileType);
    }

    @Override
    public String getPathToRoot() {
        if (getIpsElement().isDefaultPackage()) {
            return ""; //$NON-NLS-1$
        }
        return getPackageFragmentPathToRoot(getIpsElement());
    }

    @Override
    public String getLinkText(boolean withImage) {
        return IpsUIPlugin.getLabel(getIpsElement());
    }

    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        return getIpsElement();
    }
}
