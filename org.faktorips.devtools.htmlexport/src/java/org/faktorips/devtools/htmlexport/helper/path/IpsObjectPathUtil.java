package org.faktorips.devtools.htmlexport.helper.path;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

public class IpsObjectPathUtil extends AbstractIpsElementPathUtil<IIpsObject> {

    public IpsObjectPathUtil(IIpsObject ipsElement) {
        super(ipsElement);
    }

    protected String getFileName() {
        StringBuilder builder = new StringBuilder();
        builder.append(getIpsElement().getIpsObjectType().getId());
        builder.append('_');
        builder.append(getIpsElement().getName());
        return builder.toString();
    }

    public String getPathToRoot() {
        return PATH_UP + getPackageFragmentPathToRoot(getIpsPackageFragment());
    }

    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        return getIpsElement().getIpsPackageFragment();
    }
}
