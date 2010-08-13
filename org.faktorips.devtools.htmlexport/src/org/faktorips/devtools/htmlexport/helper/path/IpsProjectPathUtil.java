/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * {@link IpsElementPathUtil} for an {@link IIpsProject}
 * 
 * @author dicker
 * 
 */
public class IpsProjectPathUtil extends AbstractIpsElementPathUtil<IIpsProject> {

    private static final String INDEX_HTML = "indes.html"; //$NON-NLS-1$

    public IpsProjectPathUtil(IIpsProject ipsElement) {
        super(ipsElement);
    }

    @Override
    public String getPathFromRoot(LinkedFileType linkedFileType) {
        return INDEX_HTML;
    }

    @Override
    public String getPathToRoot() {
        return ""; //$NON-NLS-1$
    }

    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        throw new NotImplementedException("An IpsProject has no IpsPackageFragment"); //$NON-NLS-1$
    }
}
