/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmptbuilder;

import org.faktorips.devtools.model.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

public class ProductBuilderJavaClassNameProvider implements IJavaClassNameProvider {

    @Override
    public String getImplClassName(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                .getImplementationClassName(ipsSrcFile.getIpsObjectName() + "Builder");
    }

    public String getDeclClassName(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                .getImplementationClassName(ipsSrcFile.getIpsObjectName());
    }

    @Override
    public boolean isImplClassInternalArtifact() {
        return true;
    }

    @Override
    public String getInterfaceName(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                .getPublishedInterfaceName(ipsSrcFile.getIpsObjectName());
    }

    @Override
    public boolean isInterfaceInternalArtifact() {
        return false;
    }

}
