package org.faktorips.devtools.htmlexport.helper.html.path;

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
public class HtmlPathUtilFactory {

    public static IpsElementHtmlPathUtil createPathUtil(IIpsElement ipsElement) {
        if (ipsElement instanceof IIpsProject) return new IpsProjectHtmlPathUtil();
        if (ipsElement instanceof IIpsPackageFragment) return new IpsPackageFragmentHtmlPathUtil((IIpsPackageFragment) ipsElement);
        if (ipsElement instanceof IIpsObject) return new IpsObjectHtmlPathUtil((IIpsObject) ipsElement);
        throw new NotImplementedException("Fuer das IIpsElement vom Typ " + ipsElement.getClass().getCanonicalName() + " gibt es noch kein PathUtil");
    }
}
