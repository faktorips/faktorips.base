package org.faktorips.devtools.htmlexport.helper.path;

import org.apache.commons.lang.NotImplementedException;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

class IpsProjectPathUtil extends AbstractIpsElementPathUtil<IIpsProject> {

    private static final String INDEX_HTML = "indes.html";

    public String getPathFromRoot(LinkedFileType linkedFileType) {
        return INDEX_HTML;
    }

    public String getPathToRoot() {
        return "";
    }

    @Override
    protected IIpsPackageFragment getIpsPackageFragment() {
        throw new NotImplementedException();
    }

}
