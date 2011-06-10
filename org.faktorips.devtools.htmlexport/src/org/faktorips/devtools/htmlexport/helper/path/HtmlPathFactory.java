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
