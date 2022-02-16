/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model.testcase;

import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.IRuntimeRepository;

public interface ITocTreeFromDependencyManagerLoader {

    String EXTENSION_POINT_ID_NEW_PRODUCT_DEFINITION_OPERATION = "loadTocTreeFromDependencyManager"; //$NON-NLS-1$

    /**
     * Whether this instance is capable to load the TOC files for creating a
     * {@link IRuntimeRepository} for the given {@link IIpsProject}.
     * 
     * @param ipsProject the project to check
     * @return {@code true} if the instance can load one or more TOC files
     */
    boolean isResponsibleFor(IIpsProject ipsProject);

    /**
     * Adds all TOC-files of the given F-IPS project and its dependent F-IPS projects to the
     * provided list. The string representation must reflect the actual dependency tree of the F-IPS
     * project.
     * <p>
     * For example: for the F-IPS project <strong>Produkte</strong> with the following dependency
     * structure we would expect following result.
     *
     * <pre>
     *     Produkte
     *      /     \
     *   Sparte   VO
     *      \     /
     *       Basis
     * </pre>
     * 
     * Content of the repositoryPackages list:<br>
     * <ul>
     * <li>Produkte-toc.xml</li>
     * <li>Sparte-toc.xml[&lt;Base-toc.xml&gt;]</li>
     * <li>VO-toc.xml[&lt;Base-toc.xml&gt;]</li>
     * </ul>
     * <p>
     *
     * @param ipsProject the project to load the dependencies from
     * @param repositoryPackages the result as serialized tree a described above
     * @throws IpsException from e.g. m2e eclipse plugin or gradle buildship plugin
     */
    void loadTocTreeFromDependencyManager(IIpsProject ipsProject, List<String> repositoryPackages) throws IpsException;
}
