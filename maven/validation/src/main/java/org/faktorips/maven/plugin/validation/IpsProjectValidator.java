/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.maven.plugin.validation;

import java.util.Arrays;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;

/**
 * Validates a Faktor-IPS project.
 */
record IpsProjectValidator(IIpsProject ipsProject, MavenProject project, Log log) {

    /**
     * Validates the {@link IIpsProject}.
     * 
     * @param failBuildOnValidationErrors whether to fail the build on validation errors
     * @throws MojoFailureException if {@code failBuildOnValidationErrors} is {@code true} and the
     *             validation results in an error or fails due to an exception
     */
    void validate(boolean failBuildOnValidationErrors)
            throws MojoFailureException {
        try {
            MessageList validationResults = validate(ipsProject);

            new IpsValidationMessageMapper(log, project).logMessages(validationResults);

            if (failBuildOnValidationErrors && validationResults.containsErrorMsg()) {
                throw new MojoFailureException(IpsValidationMojo.BUILD_FAILURE_MESSAGE);
            }
        } catch (IpsException e) {
            log.error(IpsValidationMessageMapper.MOJO_NAME + ' ' + e);
            if (failBuildOnValidationErrors) {
                throw new MojoFailureException(IpsValidationMojo.BUILD_FAILURE_MESSAGE);
            }
        }
    }

    private static MessageList validate(IIpsProject ipsProject) {
        MessageList validationResults = ipsProject.validate();

        // TODO FIPS-9513 -> parallelStream()
        Arrays.stream(ipsProject.getIpsPackageFragmentRoots(false))
                .filter(IIpsPackageFragmentRoot::isBasedOnSourceFolder)
                .map(IIpsPackageFragmentRoot::getIpsPackageFragments)
                .flatMap(Arrays::stream)
                .map(IIpsPackageFragment::getChildren)
                .flatMap(Arrays::stream)
                .filter(IIpsSrcFile.class::isInstance)
                .map(IIpsSrcFile.class::cast)
                .map(IIpsSrcFile::getIpsObject)
                .map(o -> o.validate(ipsProject))
                .forEach(validationResults::add);
        return validationResults;
    }
}