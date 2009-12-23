package org.faktorips.devtools.htmlexport.helper.path;

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * 
 * UtilityKlasse zum Erzeugen von Links von einem <code>IIpsElement</code> zu einem anderen <code>IIpsElement</code>
 * @author dicker
 *
 */
public class PathUtilFactory {

    public static IpsElementPathUtil createPathUtil(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsProject) return new IpsProjectPathUtil();
        if (ipsElement instanceof IIpsPackageFragment) return new IpsPackageFragmentPathUtil((IIpsPackageFragment) ipsElement);
        if (ipsElement instanceof IIpsObject) return new IpsObjectPathUtil((IIpsObject) ipsElement);
        throw new NotImplementedException("Fuer das IIpsElement vom Typ " + ipsElement.getClass().getCanonicalName() + " gibt es noch kein PathUtil");
    }
}
