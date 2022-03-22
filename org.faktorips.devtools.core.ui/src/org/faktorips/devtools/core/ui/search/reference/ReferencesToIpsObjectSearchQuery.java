/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.reference;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Query to search the references of a given {@link IIpsObject}.
 */
public class ReferencesToIpsObjectSearchQuery extends ReferenceSearchQuery {

    public ReferencesToIpsObjectSearchQuery(IIpsObject referenced) {
        super(referenced);
    }

    /**
     * Finds all {@link IIpsElement IIpsElements} referencing the {@link IIpsObject} passed to this
     * class.
     */
    @Override
    protected IIpsElement[] findReferences() {
        Set<IIpsElement> result = new LinkedHashSet<>();
        IIpsProject[] referencingProjects = referenced.getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject referencingProject : referencingProjects) {
            Set<IIpsElement> foundResults = findReferencingIpsObjTypes(referencingProject);
            result.addAll(foundResults);
        }
        return result.toArray(new IIpsElement[result.size()]);

    }

    protected Set<IIpsElement> findReferencingIpsObjTypes(IIpsProject referencingIpsProject) {
        Set<IIpsElement> resultSet = new LinkedHashSet<>();
        List<IIpsSrcFile> ipsSrcFiles = referencingIpsProject.findAllIpsSrcFiles();
        return checkIIPsSrcFileDependencies(resultSet, ipsSrcFiles);
    }

    protected Set<IIpsElement> checkIIPsSrcFileDependencies(Set<IIpsElement> resultSet, List<IIpsSrcFile> ipsSrcFiles) {
        for (IIpsSrcFile iIpsSrcFile : ipsSrcFiles) {
            IIpsObject object = iIpsSrcFile.getIpsObject();
            IDependency[] dependencies = object.dependsOn();
            for (IDependency dependency : dependencies) {
                if (dependency.getTarget().equals(referenced.getQualifiedNameType())
                        || dependency.getTarget().equals(referenced.getQualifiedName())) {
                    fillResultSet(resultSet, object, dependency);
                }
            }
        }
        return resultSet;
    }

    private void fillResultSet(Set<IIpsElement> resultSet, IIpsObject object, IDependency dependency) {
        addDependencyDetails(resultSet, object, dependency);
        resultSet.add(object);
    }

    protected void addDependencyDetails(Set<IIpsElement> set, IIpsObject object, IDependency dependency) {
        List<IDependencyDetail> dependencyDetails = object.getDependencyDetails(dependency);

        for (IDependencyDetail dependencyIPSObjPart : dependencyDetails) {
            set.add(dependencyIPSObjPart.getPart());
        }
    }

    @Override
    protected Object[] getDataForResult(IIpsElement object) {
        return new Object[] { object };
    }
}
