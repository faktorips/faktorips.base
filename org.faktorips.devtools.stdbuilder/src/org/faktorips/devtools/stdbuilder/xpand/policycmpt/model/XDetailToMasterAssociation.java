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

import java.util.Collections;
import java.util.HashSet;
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

public class XDetailToMasterAssociation extends XPolicyAssociation {

    public XDetailToMasterAssociation(IPolicyCmptTypeAssociation ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        super(ipsObjectPartContainer, context, modelService);
    }

    /**
     * FIXME behandelt bisher nicht derived unions, die selbst subsets sind!
     * 
     */
    public Set<XDerivedUnionAssociation> getCorrespondingDerivedUnionAssociations() {
        if (!isInverseDerivedUnionSubset()) {
            return Collections.emptySet();
        }
        XPolicyAssociation subsetAssociation = getInverseAssociation();
        HashSet<XDerivedUnionAssociation> result = new LinkedHashSet<XDerivedUnionAssociation>();
        result.add(subsetAssociation.getSubsettedDerivedUnion());
        return result;
    }

    /**
     * Checks whether a superclass of the given policy class contains the method for retrieving the
     * parent (of the derived union association).
     * <p>
     * If <code>true</code> super may be called when implementing the get method.
     * <p>
     * An example: AbstractPolicy defines a derived union association
     * ("DUAbstractCoverageAssociation") to AbstractCoverage. AbstractCoverage in turn defines an
     * inverse association (detail to master composition) with the name
     * "DUAbstractPolicyAssociation". AbstractPolicy's subclass "Policy" defines an association
     * ("CoverageAssociation") to Coverage (AbstractCoverage's subclass) as a subset of the derived
     * union DUAbstractCoverage. Coverage defines an inverse association ("PolicyAssociation") to
     * this subset.
     * 
     * In the generated Class Coverage.java (in addition to the method getParentModelObject()) a
     * method named "getDUAbstractPolicy()" is generated, that returns an AbstractPolicy. Note that
     * the Method name is not derived from the subset nor the subset's inverse association, but only
     * the derived union's inverse association.
     * 
     * When developing subclasses for Policy and Coverage (e.g. SubPolicy and SubCoverage) that
     * contain a subset of DUAbstractCoverageAssociation and the respective inverse association
     * ("SubPolicyAssociation") themselves, the method "getDUAbstractPolicy()" will also be
     * generated in SubCoverage.java.
     * 
     * An {@link XDetailToMasterAssociation} representing PolicyAssociation will yield
     * <code>false</code> as a result, SubPolicyAssociation, however, will yield <code>true</code>
     * as a result when calling this method.
     */
    public boolean isImplementedInSuperclassOf(XPolicyCmptClass policyClass, XDerivedUnionAssociation derivedUnion) {
        FindInverseSubsetOfDerivedUnionVisitor visitor = new FindInverseSubsetOfDerivedUnionVisitor(
                derivedUnion.getAssociation(), getIpsProject());
        try {
            visitor.start((IPolicyCmptType)policyClass.getPolicyCmptType().findSupertype(getIpsProject()));
            return visitor.isInverseSubsetFound();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Note: Determines whether or not this association is the inverse of a derived union
     * association. Will return <code>false</code> if it is the inverse of a (derived union-) subset
     * association.
     * 
     * @see #isInverseDerivedUnion()
     */
    public boolean isInverseDerivedUnion() {
        return getInverseAssociation().isDerivedUnion();
    }

    /**
     * Note: Determines whether or not this association is the inverse of a (derived union-) subset
     * association. Will return <code>false</code> if it is the inverse of a derived union
     * association.
     * 
     * @see #isInverseDerivedUnion()
     */
    public boolean isInverseDerivedUnionSubset() {
        return getInverseAssociation().isSubsetOfADerivedUnion();
    }

    /**
     * Returns the inverse associations defined in the given policy class that are subsets of this
     * derived union association.
     * <p>
     * In other words: Searches the given policy class for inverse associations. For each inverse
     * finds their original association and checks whether or not it is a subset of this derived
     * union. If it is, the inverse association (of the given policy class) is added to the list.
     * 
     * TODO: wenn subsets dieser derived union selbst noch DUs sind, muss dann auch gegen diese
     * getestet werden?
     * 
     * Hmm. Eigentlich sollten doch alle übergeordneten DUs schon ermittelt und in der dieser
     * Methode abgearbeitet werden. evtl. is das dann überflüssig...
     * 
     * Methode nie für VertragsTeil aufgerufen. Werden eigentlich DUs für seine Assocs gefunden?!
     * 
     * @param policyClass the class to search for inverse associations of this derived union
     * 
     */
    // public Set<XPolicyAssociation> getInverseAssociationsOfDerivedUnionSubsets(XPolicyCmptClass
    // policyClass) {
    // Set<XPolicyAssociation> result = new LinkedHashSet<XPolicyAssociation>();
    // Set<? extends XPolicyAssociation> associations = policyClass.getAssociations();
    // for (XPolicyAssociation xAssociation : associations) {
    // if (xAssociation.hasInverseAssociation() &&
    // xAssociation.getInverseAssociation().isSubsetOf(this)) {
    // result.add(xAssociation);
    // }
    // }
    // return result;
    // }

    public String getInterfaceName() {
        return addImport(getInverseAssociation().getTargetInterfaceName());
    }

    /**
     * Searches the given type (and the super type hierarchy) for inverse associations of subsets of
     * the derived union specified when creating the visitor.
     * 
     */
    class FindInverseSubsetOfDerivedUnionVisitor extends TypeHierarchyVisitor<IPolicyCmptType> {

        private final IAssociation derivedUnion;

        boolean foundSubset = false;

        public FindInverseSubsetOfDerivedUnionVisitor(IAssociation derivedUnion, IIpsProject ipsProject) {
            super(ipsProject);
            this.derivedUnion = derivedUnion;
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            List<IPolicyCmptTypeAssociation> associations = currentType.getPolicyCmptTypeAssociations();
            for (IPolicyCmptTypeAssociation assoc : associations) {
                if (assoc.isCompositionDetailToMaster()) {
                    IPolicyCmptTypeAssociation inverse = assoc.findInverseAssociation(getIpsProject());
                    if (inverse.getSubsettedDerivedUnion().equals(derivedUnion.getName())) {
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

        public boolean isInverseSubsetFound() {
            return foundSubset;
        }
    }

    public boolean isInverseSubsetOf(XDerivedUnionAssociation derivedUnion) {
        try {
            return getInverseAssociation().getAssociation().isSubsetOfDerivedUnion(derivedUnion.getAssociation(),
                    getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

}
