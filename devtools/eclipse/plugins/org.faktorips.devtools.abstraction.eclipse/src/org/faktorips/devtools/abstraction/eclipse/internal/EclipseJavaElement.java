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

import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.faktorips.devtools.abstraction.eclipse.mapping.PathMapping.toJavaPath;

import java.nio.file.Path;

import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.devtools.abstraction.AJavaElement;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AWrapper;

public class EclipseJavaElement extends AWrapper<IJavaElement> implements AJavaElement {

    protected EclipseJavaElement(IJavaElement javaElement) {
        super(javaElement);
    }

    IJavaElement javaElement() {
        return unwrap();
    }

    @Override
    public AResource getResource() {
        return wrap(javaElement().getResource()).as(AResource.class);
    }

    @Override
    public boolean exists() {
        return javaElement().exists();
    }

    @Override
    public AJavaProject getJavaProject() {
        return wrap(javaElement().getJavaProject()).as(AJavaProject.class);
    }

    @Override
    public Path getPath() {
        return toJavaPath(javaElement().getPath());
    }

}
