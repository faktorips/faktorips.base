/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;

public class AEclipsePackageFragmentRoot extends AEclipseJavaElement implements APackageFragmentRoot {

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