/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import java.nio.file.Path;

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.abstraction.APackageFragmentRoot;
import org.faktorips.devtools.abstraction.eclipse.mapping.PathMapping;
import org.faktorips.devtools.abstraction.exception.IpsException;

public class EclipsePackageFragmentRoot extends EclipseJavaElement implements APackageFragmentRoot {

    EclipsePackageFragmentRoot(IPackageFragmentRoot packageFragmentRoot) {
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

    @Override
    public Path getOutputLocation() {
        try {
            return PathMapping.toJavaPath(unwrap().getRawClasspathEntry().getOutputLocation());
        } catch (JavaModelException e) {
            throw new IpsException(e);
        }
    }

}
