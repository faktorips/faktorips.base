/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation.mavenversion;

import java.util.Optional;
import java.util.Set;

import org.faktorips.devtools.model.IVersionProviderFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.maven.plugin.validation.IpsDependency;

public class MavenVersionProviderFactory implements IVersionProviderFactory {

    private final Set<IpsDependency> ipsDependencies;

    public MavenVersionProviderFactory(Set<IpsDependency> ipsDependencies) {
        this.ipsDependencies = ipsDependencies;
    }

    @Override
    public MavenVersionProvider createVersionProvider(IIpsProject ipsProject) {
        return ipsDependencies.stream()
                .filter(d -> ipsProject.equals(d.ipsProject()))
                .map(IpsDependency::getMavenProject)
                .flatMap(Optional::stream).findFirst()
                .map(MavenVersionProvider::new)
                .orElseThrow(() -> new IllegalArgumentException(
                        "The project " + ipsProject.getName() + " is not a known maven project."));
    }
}