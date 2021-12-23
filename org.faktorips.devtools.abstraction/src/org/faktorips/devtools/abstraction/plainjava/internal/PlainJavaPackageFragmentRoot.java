/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import java.io.File;

import org.faktorips.devtools.abstraction.APackageFragmentRoot;

public class PlainJavaPackageFragmentRoot extends PlainJavaJavaElement implements APackageFragmentRoot {

    PlainJavaPackageFragmentRoot(File packageFragmentRoot) {
        super(PlainJavaFileUtil.directory(packageFragmentRoot));
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