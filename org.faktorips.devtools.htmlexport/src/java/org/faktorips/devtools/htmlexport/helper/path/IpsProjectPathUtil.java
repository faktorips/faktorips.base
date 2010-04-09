package org.faktorips.devtools.htmlexport.helper.path;

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * {@link IpsElementPathUtil} for an {@link IIpsProject}
 * @author dicker
 *
 */
public class IpsProjectPathUtil extends AbstractIpsElementPathUtil<IIpsProject> {

    private static final String INDEX_HTML = "indes.html";

    /* (non-Javadoc)
     * @see org.faktorips.devtools.htmlexport.helper.path.AbstractIpsElementPathUtil#getPathFromRoot(org.faktorips.devtools.htmlexport.helper.path.LinkedFileType)
     */
    public String getPathFromRoot(LinkedFileType linkedFileType) {
        return INDEX_HTML;
    }

    /* (non-Javadoc)
     * @see org.faktorips.devtools.htmlexport.helper.path.IpsElementPathUtil#getPathToRoot()
     */
    public String getPathToRoot() {
        return "";
    }

    /* (non-Javadoc)
     * @see org.faktorips.devtools.htmlexport.helper.path.AbstractIpsElementPathUtil#getIpsPackageFragment()
     */
    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        throw new NotImplementedException("An IpsProject has no IpsPackageFragment");
    }

}
