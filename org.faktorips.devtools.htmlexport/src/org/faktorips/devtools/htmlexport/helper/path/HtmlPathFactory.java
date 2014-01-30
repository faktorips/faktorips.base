/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper.path;

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * Utility for choosing the right {@link IHtmlPath}
 * 
 * @author dicker
 * 
 */
public class HtmlPathFactory {

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
        throw new NotImplementedException(
                "There is no IIpsElementHtmlPath for the IIpsElement of the type " + ipsElement.getClass().getCanonicalName()); //$NON-NLS-1$
    }

    public static IHtmlPath createPathUtil(IpsObjectType ipsObjectType) {
        return new IpsObjectTypeHtmlPath(ipsObjectType);
    }
}
