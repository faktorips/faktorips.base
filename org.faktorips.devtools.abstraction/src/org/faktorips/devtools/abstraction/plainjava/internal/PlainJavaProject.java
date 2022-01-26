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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AProject;

public class PlainJavaProject extends PlainJavaFolder implements AProject {

    /**
     * The file extension for IPS projects with a leading dot.
     */
    private static final String PROPERTY_FILE_EXTENSION_INCL_DOT = ".ipsproject"; //$NON-NLS-1$

    public PlainJavaProject(File directory) {
        super(directory);
    }

    @Override
    public AResourceType getType() {
        return AResourceType.PROJECT;
    }

    @Override
    public boolean isIpsProject() {
        return directory().toPath().resolve(PROPERTY_FILE_EXTENSION_INCL_DOT).toFile().exists();
    }

    @Override
    public Set<AProject> getReferencedProjects() {
        // TODO über Maven auflösen?
        return Set.of();
    }

    @Override
    public void delete(IProgressMonitor monitor) {
        if (directory().exists()) {
            delete(monitor);
        }
    }

    @Override
    void create() {
        super.create();
        // TODO muss noch etwas angelegt werden, um den Ordner als Projekt zu markieren? Evtl.
        // eine pom.xml wenn wir ein Maven-Projekt anlegen?
    }

    @Override
    public void build(ABuildKind incrementalBuild, IProgressMonitor monitor) {
        // TODO später...
    }

    @Override
    public Charset getDefaultCharset() {
        // TODO von Maven / Filesystem abfragen?
        return StandardCharsets.UTF_8;
    }

}