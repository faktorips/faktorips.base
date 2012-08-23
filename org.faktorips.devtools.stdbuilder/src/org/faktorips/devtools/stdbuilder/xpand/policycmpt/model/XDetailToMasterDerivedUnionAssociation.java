/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
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
        return getDerivedUnion().isImplementedInSuperclass(xClass);
    }

    /**
     * Returns all detail to master associations in the given policy class that are a subset of this
     * detail to master derived union.
     * 
     * @param policyClass the policy class to search for detail to master subsets
     */
    public Set<XPolicyAssociation> getDetailToMasterSubsetAssociations(XPolicyCmptClass policyClass) {
        Set<XPolicyAssociation> subsets = new LinkedHashSet<XPolicyAssociation>();
        for (XPolicyAssociation assoc : policyClass.getAssociations()) {
            if (assoc.isCompositionDetailToMaster() && !assoc.isDerived()
                    && assoc.getInverseAssociation().isRecursiveSubsetOf(getDerivedUnion())) {
                subsets.add(assoc);
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
     * Returns the name for the get-parent method using the uncapitalized association name. e.g.
     * getparentPolicy() instead of getParentPolicy() if the role name is "parentPolicy".
     * 
     */
    public String getMethodNameGetParent() {
        return "get" + getName(false);
    }

    /**
     * Searches the given type (and the super type hierarchy) for subsets of the detail to master
     * derived union specified when creating the visitor.
     * 
     */
    class FindSubsetOfDetailToMasterDerivedUnionVisitor extends TypeHierarchyVisitor<IPolicyCmptType> {

        private final IAssociation derivedUnion;

        boolean foundSubset = false;

        public FindSubsetOfDetailToMasterDerivedUnionVisitor(IPolicyCmptTypeAssociation masterToDetailDerivedUnion,
                IIpsProject ipsProject) {
            super(ipsProject);
            this.derivedUnion = masterToDetailDerivedUnion;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            List<IPolicyCmptTypeAssociation> associations = currentType.getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation assoc : associations) {
                if (assoc.isCompositionDetailToMaster()) {
                    IPolicyCmptTypeAssociation inverseAssociation = assoc.findInverseAssociation(getIpsProject());
                    if (inverseAssociation.getSubsettedDerivedUnion().equals(derivedUnion.getName())) {
                        foundSubset = true;
                        return false;
                    }
                }
            }
            if (currentType.equals(derivedUnion.getType())) {
                return false;
            }
            return true;
        }

        public boolean isSubsetFound() {
            return foundSubset;
        }
    }
}
