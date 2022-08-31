/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * 
 * Utility for choosing the right {@link IHtmlPath}
 * 
 * @author dicker
 * 
 */
public class HtmlPathFactory {

    private HtmlPathFactory() {
        // Utility class not to be instantiated
    }

    /**
     * returns {@link IHtmlPath} for the given {@link IIpsElement}
     * 
     */
    public static IHtmlPath createPathUtil(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsProject) {
            return new IpsProjectHtmlPath((IIpsProject)ipsElement);
        }
        if (ipsElement instanceof IIpsPackageFragment) {
            return new IpsPackageFragmentHtmlPath((IIpsPackageFragment)ipsElement);
        }
        if (ipsElement instanceof IIpsSrcFile) {
            return new IpsSrcFileHtmlPath((IIpsSrcFile)ipsElement);
        }
        if (ipsElement instanceof IIpsObject) {
            return new IpsSrcFileHtmlPath(((IIpsObject)ipsElement).getIpsSrcFile());
        }
        throw new UnsupportedOperationException(
                "There is no IIpsElementHtmlPath for the IIpsElement of the type " //$NON-NLS-1$
                        + ipsElement.getClass().getCanonicalName());
    }

    public static IHtmlPath createPathUtil(IpsObjectType ipsObjectType) {
        return new IpsObjectTypeHtmlPath(ipsObjectType);
    }
}
