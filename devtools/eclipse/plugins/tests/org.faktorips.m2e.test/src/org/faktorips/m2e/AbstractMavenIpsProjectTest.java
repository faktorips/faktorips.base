/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.m2e;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;

public abstract class AbstractMavenIpsProjectTest extends AbstractIpsPluginTest {

    public AbstractMavenIpsProjectTest() {
        super();
    }

    @Override
    protected AJavaProject addJavaCapabilities(AProject project) throws CoreException {
            AJavaProject aJavaProject = super.addJavaCapabilities(project);
            IJavaProject javaProject = aJavaProject.unwrap();
    //we need only the JRE Container, source folders are added by the Maven configurator
            IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
            IClasspathEntry[] newEntries = new IClasspathEntry[1];
            newEntries[0] = oldEntries[2];
            javaProject.setRawClasspath(newEntries, null);
            return aJavaProject;
        }

}