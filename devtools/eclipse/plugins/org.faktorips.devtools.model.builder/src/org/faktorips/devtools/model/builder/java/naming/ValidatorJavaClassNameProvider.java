/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.naming;

import org.faktorips.devtools.model.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

public class ValidatorJavaClassNameProvider extends DefaultJavaClassNameProvider {

    public ValidatorJavaClassNameProvider(boolean isGeneratePublishedInterface) {
        super(isGeneratePublishedInterface);
    }

    @Override
    public String getImplClassName(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                .getImplementationClassName(ipsSrcFile.getIpsObjectName() + "Validator");
    }
}
