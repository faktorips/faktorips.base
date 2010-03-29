package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

/**
 * {@link IpsElementPathUtil} for an {@link IIpsObject}
 * @author dicker
 *
 */
public class IpsObjectPathUtil extends AbstractIpsElementPathUtil<IIpsObject> {

    public IpsObjectPathUtil(IIpsObject ipsElement) {
        super(ipsElement);
    }

    /* (non-Javadoc)
     * @see org.faktorips.devtools.htmlexport.helper.path.AbstractIpsElementPathUtil#getFileName()
     */
    protected String getFileName() {
        StringBuilder builder = new StringBuilder();
        builder.append(getIpsElement().getIpsObjectType().getId());
        builder.append('_');
        builder.append(getIpsElement().getName());
        return builder.toString();
    }

    /* (non-Javadoc)
     * @see org.faktorips.devtools.htmlexport.helper.path.IpsElementPathUtil#getPathToRoot()
     */
    public String getPathToRoot() {
        return getPackageFragmentPathToRoot(getIpsPackageFragment());
    }

    /* (non-Javadoc)
     * @see org.faktorips.devtools.htmlexport.helper.path.AbstractIpsElementPathUtil#getIpsPackageFragment()
     */
    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        return getIpsElement().getIpsPackageFragment();
    }
}
