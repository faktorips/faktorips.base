/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.naming;

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IJavaNamingConvention;

/**
 * The default java name provider. Simply use the the configured {@link IJavaNamingConvention} and
 * the name of the {@link IIpsSrcFile} to get the implementation or interface name.
 * 
 * @author dirmeier
 */
public class DefaultJavaClassNameProvider implements IJavaClassNameProvider {

    private final boolean isGeneratePublishedInterface;

    public DefaultJavaClassNameProvider(boolean isGeneratePublishedInterface) {
        this.isGeneratePublishedInterface = isGeneratePublishedInterface;
    }

    @Override
    public String getImplClassName(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                .getImplementationClassName(ipsSrcFile.getIpsObjectName());
    }

    @Override
    public boolean isImplClassInternalArtifact() {
        return isGeneratePublishedInterface;
    }

    @Override
    public final String getInterfaceName(IIpsSrcFile ipsSrcFile) {
        if (!isGeneratePublishedInterface) {
            return getImplClassName(ipsSrcFile);
        } else {
            return getInterfaceNameInternal(ipsSrcFile);
        }
    }

    /**
     * Returns the name of the generated interface and does not check if interfaces are generated at
     * all.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to get the generated interface name for
     * 
     * @return The name of the generated interface
     */
    protected String getInterfaceNameInternal(IIpsSrcFile ipsSrcFile) {
        return ipsSrcFile.getIpsProject().getJavaNamingConvention()
                .getPublishedInterfaceName(ipsSrcFile.getIpsObjectName());
    }

    @Override
    public boolean isInterfaceInternalArtifact() {
        return false;
    }

}
