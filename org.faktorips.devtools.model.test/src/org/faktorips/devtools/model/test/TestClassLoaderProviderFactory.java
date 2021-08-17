/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.test;

import org.faktorips.devtools.model.IClassLoaderProvider;
import org.faktorips.devtools.model.IClassLoaderProviderFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

// a copy of org.faktorips.devtools.core.internal.JavaRuntimeClassLoaderProviderFactory for tests
// without a dependency
// to faktorips.core
public class TestClassLoaderProviderFactory implements IClassLoaderProviderFactory {

    @Override
    public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject) {
        return new TestClassLoaderProvider(ipsProject.getJavaProject());
    }

    @Override
    public IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject, ClassLoader parent) {
        return new TestClassLoaderProvider(ipsProject.getJavaProject(), parent);
    }

}
