/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.reference;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

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
    protected IIpsElement[] findReferences() throws CoreException {
        Set<IIpsElement> result = new LinkedHashSet<IIpsElement>();
        IIpsProject[] referencingProjects = referenced.getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject referencingProject : referencingProjects) {
            Set<IIpsElement> foundResults = findReferencingIpsObjTypes(referencingProject);
            result.addAll(foundResults);
        }
        return result.toArray(new IIpsElement[result.size()]);

    }

    protected Set<IIpsElement> findReferencingIpsObjTypes(IIpsProject referencingIpsProject) throws CoreException {
        Set<IIpsElement> resultSet = new LinkedHashSet<IIpsElement>();
        List<IIpsSrcFile> ipsSrcFiles = referencingIpsProject.findAllIpsSrcFiles();
        return checkIIPsSrcFileDependencies(resultSet, ipsSrcFiles);
    }

    protected Set<IIpsElement> checkIIPsSrcFileDependencies(Set<IIpsElement> resultSet, List<IIpsSrcFile> ipsSrcFiles)
            throws CoreException {
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

    private void fillResultSet(Set<IIpsElement> resultSet, IIpsObject object, IDependency dependency)
            throws CoreException {
        if (object instanceof IProductCmpt && !(referenced instanceof IProductCmptType)) {
            addProdCmpGenerations(resultSet, object, dependency);
        } else {
            resultSet.add(object);
        }
    }

    protected void addProdCmpGenerations(Set<IIpsElement> set, IIpsObject object, IDependency dependency)
            throws CoreException {
        List<IDependencyDetail> dependencyDetails = ((IProductCmpt)object).getDependencyDetails(dependency);

        for (IDependencyDetail dependencyIPSObjPart : dependencyDetails) {
            set.add(dependencyIPSObjPart.getPart().getParent());
        }
    }

    @Override
    protected Object[] getDataForResult(IIpsElement object) throws CoreException {
        return new Object[] { object };
    }
}
