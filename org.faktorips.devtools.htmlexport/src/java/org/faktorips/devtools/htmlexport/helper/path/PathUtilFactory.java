package org.faktorips.devtools.htmlexport.helper.path;

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * Utility for choosing the right {@link IpsElementPathUtil}
 * @author dicker
 *
 */
public class PathUtilFactory {

    /**
     * returns {@link IpsElementPathUtil} for the given {@link IIpsElement}
     * @param ipsElement
     * @return
     */
    public static IpsElementPathUtil createPathUtil(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsProject) return new IpsProjectPathUtil();
        if (ipsElement instanceof IIpsPackageFragment) return new IpsPackageFragmentPathUtil((IIpsPackageFragment) ipsElement);
        if (ipsElement instanceof IIpsObject) return new IpsObjectPathUtil((IIpsObject) ipsElement);
        throw new NotImplementedException("There is no IpsElementPathUtil for the IIpsElement of the type " + ipsElement.getClass().getCanonicalName()); //$NON-NLS-1$
    }
}
