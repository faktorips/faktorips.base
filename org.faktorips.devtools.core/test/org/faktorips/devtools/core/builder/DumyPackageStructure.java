package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsSrcFile;

class DumyPackageStructure implements IJavaPackageStructure {

    public String getPackage(String kind, IIpsSrcFile ipsSrcFile) throws CoreException {
        return null;
    }

    public String getQualifiedClassName(String kind, IIpsObject ipsObject) throws CoreException {
        return null;
    }

    public String getUnqualifiedClassName(String kind, IIpsObject ipsObject)
            throws CoreException {
        return null;
    }
}