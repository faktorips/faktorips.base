package org.faktorips.devtools.htmlexport.helper.html.path;

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.htmlexport.helper.Util;

public class IpsPackageFragmentHtmlPathUtil extends AbstractIpsElementHtmlPathUtil<IIpsPackageFragment> {
    private static String PACKAGE_INDEX_FILE_NAME = "package_index.html";

    public IpsPackageFragmentHtmlPathUtil(IIpsPackageFragment ipsElement) {
        super(ipsElement);
    }

    public String getPathFromRoot(LinkedFileTypes linkedFileType) {
        if (getIpsElement().isDefaultPackage()) {
            return PACKAGE_INDEX_FILE_NAME;
        }
        return super.getPathFromRoot(linkedFileType);
    }

    public String getPathToRoot() {
        if (getIpsElement().isDefaultPackage()) return "";
        return PATH_UP + getPackageFragmentPathToRoot(getIpsElement());
    }

    @Override
    public String getLinkText(boolean withImage) {
        return Util.getIpsPackageName(getIpsElement());
    }


    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        return getIpsElement();
    }
}
