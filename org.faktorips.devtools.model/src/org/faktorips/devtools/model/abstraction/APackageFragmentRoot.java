/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import java.io.File;

import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * A package fragment root is a folder or archive file containing {@link AJavaElement Java elements}
 * and packages.
 */
public interface APackageFragmentRoot extends AJavaElement {

    public static class AEclipsePackageFragmentRoot extends AEclipseJavaElement implements APackageFragmentRoot {

        AEclipsePackageFragmentRoot(IPackageFragmentRoot packageFragmentRoot) {
            super(packageFragmentRoot);
        }

        @SuppressWarnings("unchecked")
        @Override
        public IPackageFragmentRoot unwrap() {
            return (IPackageFragmentRoot)super.unwrap();
        }

        IPackageFragmentRoot packageFragmentRoot() {
            return unwrap();
        }

    }

    public static class PlainJavaPackageFragmentRoot extends PlainJavaJavaElement implements APackageFragmentRoot {

        PlainJavaPackageFragmentRoot(File packageFragmentRoot) {
            super(PlainJavaUtil.directory(packageFragmentRoot));
        }

        @SuppressWarnings("unchecked")
        @Override
        public File unwrap() {
            return super.unwrap();
        }

        File packageFragmentRoot() {
            return unwrap();
        }

    }

}
