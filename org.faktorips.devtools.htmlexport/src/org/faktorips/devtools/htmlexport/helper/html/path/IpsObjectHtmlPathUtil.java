package org.faktorips.devtools.htmlexport.helper.html.path;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;

public class IpsObjectHtmlPathUtil extends AbstractIpsElementHtmlPathUtil<IIpsObject> {

    public IpsObjectHtmlPathUtil(IIpsObject ipsElement) {
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
