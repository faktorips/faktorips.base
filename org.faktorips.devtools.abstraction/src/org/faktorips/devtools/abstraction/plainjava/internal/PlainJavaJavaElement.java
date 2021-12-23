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

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import java.io.File;
import java.nio.file.Path;

import org.faktorips.devtools.abstraction.AJavaElement;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AWrapper;

// TODO gibt es andere Elemente als Files?
public class PlainJavaJavaElement extends AWrapper<File> implements AJavaElement {

    protected PlainJavaJavaElement(File javaFile) {
        super(javaFile);
    }

    File javaElement() {
        return unwrap();
    }

    @Override
    public AResource getResource() {
        return wrap(javaElement()).as(AResource.class);
    }

    @Override
    public boolean exists() {
        return javaElement().exists();
    }

    @Override
    public AJavaProject getJavaProject() {
        AProject project = getResource().getProject();
        return project == null ? null : wrap(project.unwrap()).as(AJavaProject.class);
    }

    @Override
    public Path getPath() {
        return javaElement().toPath();
    }

}