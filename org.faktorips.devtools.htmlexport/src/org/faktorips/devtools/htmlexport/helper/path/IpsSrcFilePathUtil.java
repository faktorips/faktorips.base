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

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * {@link IpsElementPathUtil} for an {@link IIpsObject}
 * 
 * @author dicker
 * 
 */
public class IpsSrcFilePathUtil extends AbstractIpsElementPathUtil<IIpsSrcFile> {

    public IpsSrcFilePathUtil(IIpsSrcFile ipsElement) {
        super(ipsElement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.helper.path.AbstractIpsElementPathUtil#getFileName()
     */
    @Override
    protected String getFileName() {
        StringBuilder builder = new StringBuilder();
        /*
         * builder.append(getIpsElement().getIpsObjectType().getId()); builder.append('_');
         */
        builder.append(getIpsElement().getName());
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.helper.path.IpsElementPathUtil#getPathToRoot()
     */
    @Override
    public String getPathToRoot() {
        return getPackageFragmentPathToRoot(getIpsPackageFragment());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.htmlexport.helper.path.AbstractIpsElementPathUtil#getIpsPackageFragment
     * ()
     */
    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        return getIpsElement().getIpsPackageFragment();
    }
}
