/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.policycmpt;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XDerivedUnionAssociation;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;

/**
 * Represents the inverse (or detail to master) of a derived union association. Only used for policy
 * associations as there are no detail to master associations on product side.
 * 
 * @author widmaier
 */
public class XDetailToMasterDerivedUnionAssociation extends XDerivedUnionAssociation {

    public XDetailToMasterDerivedUnionAssociation(IPolicyCmptTypeAssociation association, GeneratorModelContext context,
            ModelService modelService) {
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
        if (getSourceType().equals(xClass.getType())) {
            return false;
        }
        IPolicyCmptType supertype = (IPolicyCmptType)xClass.getType().findSupertype(xClass.getIpsProject());
        FindSubsetOfDerivedUnionVisitor findSubsetOfDerivedUnionVisitor = new FindSubsetOfDerivedUnionVisitor(
                getAssociation(), xClass.getIpsProject());
        findSubsetOfDerivedUnionVisitor.start(supertype);
        return findSubsetOfDerivedUnionVisitor.isSubsetFound();
    }

    /**
     * Returns all detail to master associations in the given policy class that are a subset of this
     * detail to master derived union.
     * 
     * @param policyClass the policy class to search for detail to master subsets
     */
    public Set<XPolicyAssociation> getDetailToMasterSubsetAssociations(XPolicyCmptClass policyClass) {
        Set<XPolicyAssociation> subsets = new LinkedHashSet<>();
        for (XPolicyAssociation detailToMaster : policyClass.getAssociations()) {
            if (detailToMaster.isCompositionDetailToMaster()) {
                if (detailToMaster.isSharedAssociation()) {
                    if (getName().equals(detailToMaster.getName())
                            && !detailToMaster.isSharedAssociationImplementedInSuperclass()) {
                        subsets.add(detailToMaster);
                    }
                } else {
                    XPolicyAssociation inverseAssociation = detailToMaster.getInverseAssociation();
                    if (!inverseAssociation.isDerived() && (getName().equals(detailToMaster.getName())
                            || inverseAssociation.isRecursiveSubsetOf(getDerivedUnion()))) {
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
        return getAssociation().findInverseAssociation(getIpsProject());
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
            for (IPolicyCmptTypeAssociation asso : associations) {
                if (asso != detailToMasterDU && asso.isCompositionDetailToMaster()) {
                    if (asso.isSharedAssociation()) {
                        IPolicyCmptTypeAssociation sharedAssociationHost = asso
                                .findSharedAssociationHost(getIpsProject());
                        if (sharedAssociationHost.equals(detailToMasterDU)) {
                            foundSubset = true;
                            return false;
                        }
                    } else {
                        IPolicyCmptTypeAssociation masterToDetail = asso.findInverseAssociation(getIpsProject());
                        if (!masterToDetail.isDerivedUnion() && masterToDetail.getSubsettedDerivedUnion()
                                .equals(detailToMasterDU.getInverseAssociation())) {
                            foundSubset = true;
                            return false;
                        }
                    }
                }
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
