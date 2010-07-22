/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.htmlexport.helper.path;

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * Utility for choosing the right {@link IpsElementPathUtil}
 * 
 * @author dicker
 * 
 */
public class PathUtilFactory {

    /**
     * returns {@link IpsElementPathUtil} for the given {@link IIpsElement}
     * 
     * @param ipsElement
     * @return
     */
    public static IpsElementPathUtil createPathUtil(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsProject) {
            return new IpsProjectPathUtil();
        }
        if (ipsElement instanceof IIpsPackageFragment) {
            return new IpsPackageFragmentPathUtil((IIpsPackageFragment)ipsElement);
        }
        if (ipsElement instanceof IIpsSrcFile) {
            return new IpsSrcFilePathUtil((IIpsSrcFile)ipsElement);
        }
        if (ipsElement instanceof IIpsObject) {
            return new IpsSrcFilePathUtil(((IIpsObject)ipsElement).getIpsSrcFile());
        }
        throw new NotImplementedException(
                "There is no IpsElementPathUtil for the IIpsElement of the type " + ipsElement.getClass().getCanonicalName()); //$NON-NLS-1$
    }
}
