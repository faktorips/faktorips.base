/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;

/**
 * Represents the inverse (or detail to master) of a derived union association. Only used for policy
 * associations as there are no detail to master associations on product side.
 * 
 * @author widmaier
 */
public class XDetailToMasterDerivedUnionAssociation extends XDerivedUnionAssociation {

    public XDetailToMasterDerivedUnionAssociation(IPolicyCmptTypeAssociation association,
            GeneratorModelContext context, ModelService modelService) {
        super(association, context, modelService);
    }

    @Override
    public IPolicyCmptTypeAssociation getAssociation() {
        return (IPolicyCmptTypeAssociation)super.getAssociation();
    }

    /**
     * Implemented in this case means that the super class contains a detail to master association
     * which is a subset of this association.
     */
    public boolean isImplementedInSuperclass(XPolicyCmptClass xClass) {
        if (getTypeOfAssociation().equals(xClass.getType())) {
            return false;
        }
        try {
            IPolicyCmptType supertype = (IPolicyCmptType)xClass.getType().findSupertype(xClass.getIpsProject());
            FindSubsetOfDerivedUnionVisitor findSubsetOfDerivedUnionVisitor = new FindSubsetOfDerivedUnionVisitor(
                    getAssociation(), xClass.getIpsProject());
            findSubsetOfDerivedUnionVisitor.start(supertype);
            return findSubsetOfDerivedUnionVisitor.isSubsetFound();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns all detail to master associations in the given policy class that are a subset of this
     * detail to master derived union.
     * 
     * @param policyClass the policy class to search for detail to master subsets
     */
    public Set<XPolicyAssociation> getDetailToMasterSubsetAssociations(XPolicyCmptClass policyClass) {
        Set<XPolicyAssociation> subsets = new LinkedHashSet<XPolicyAssociation>();
        for (XPolicyAssociation detailToMaster : policyClass.getAssociations()) {
            if (detailToMaster.isCompositionDetailToMaster()) {
                if (detailToMaster.isSharedAssociation()) {
                    if (getName().equals(detailToMaster.getName())
                            && !detailToMaster.isSharedAssociationImplementedInSuperclass()) {
                        subsets.add(detailToMaster);
                    }
                } else {
                    XPolicyAssociation inverseAssociation = detailToMaster.getInverseAssociation();
                    if (!inverseAssociation.isDerived()
                            && (getName().equals(detailToMaster.getName()) || inverseAssociation
                                    .isRecursiveSubsetOf(getDerivedUnion()))) {
                        subsets.add(detailToMaster);
                    }
                }
            }
        }
        return subsets;
    }

    public XDerivedUnionAssociation getDerivedUnion() {
        return getModelNode(getDerviedUnionAssociation(), XDerivedUnionAssociation.class);
    }

    private IPolicyCmptTypeAssociation getDerviedUnionAssociation() {
        try {
            return getAssociation().findInverseAssociation(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * TODO @see {@link XPolicyAssociation#getMethodNameGetter()}
     * 
     * {@inheritDoc}
     */
    @Override
    public String getMethodNameGetter() {
        return "get" + getName(false);
    }

    /**
     * Searches the given type (and the super type hierarchy) for subsets of the derived union
     * specified when creating the visitor.
     * 
     */
    private static class FindSubsetOfDerivedUnionVisitor extends TypeHierarchyVisitor<IPolicyCmptType> {

        private final IPolicyCmptTypeAssociation detailToMasterDU;

        private boolean foundSubset = false;

        public FindSubsetOfDerivedUnionVisitor(IPolicyCmptTypeAssociation detailToMasterDU, IIpsProject ipsProject) {
            super(ipsProject);
            this.detailToMasterDU = detailToMasterDU;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            List<IPolicyCmptTypeAssociation> associations = currentType.getPolicyCmptTypeAssociations();
            try {
                for (IPolicyCmptTypeAssociation asso : associations) {
                    if (asso != detailToMasterDU && asso.isCompositionDetailToMaster()) {
                        if (asso.isSharedAssociation()) {
                            IPolicyCmptTypeAssociation sharedAssociationHost = asso
                                    .findSharedAssociationHost(ipsProject);
                            if (sharedAssociationHost.equals(detailToMasterDU)) {
                                foundSubset = true;
                                return false;
                            }
                        } else {
                            IPolicyCmptTypeAssociation masterToDetail = asso.findInverseAssociation(ipsProject);
                            if (!masterToDetail.isDerivedUnion()
                                    && masterToDetail.getSubsettedDerivedUnion().equals(
                                            detailToMasterDU.getInverseAssociation())) {
                                foundSubset = true;
                                return false;
                            }
                        }
                    }
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
            if (currentType.equals(detailToMasterDU.getType())) {
                return false;
            }
            return true;
        }

        public boolean isSubsetFound() {
            return foundSubset;
        }
    }

}
