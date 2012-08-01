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

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.util.ArgumentCheck;

/**
 * This is the generator model node for a derived union association. It is very important to
 * remember, that the corresponding (derived union) association is not in the IType you currently
 * generate code for. Thats why many methods need to know the xClass in which context you currentyl
 * generate the code.
 * 
 * @author dirmeier
 */
public class XDerivedUnionAssociation extends XAssociation {

    /**
     * The default constructor, called by model service
     * 
     * @param association The derived union association
     * @param context The generator model context
     * @param modelService the model service used to instantiate new generator model nodes
     */
    public XDerivedUnionAssociation(IAssociation association, GeneratorModelContext context, ModelService modelService) {
        super(association, context, modelService);
        ArgumentCheck.isTrue(association.isDerivedUnion());
    }

    /**
     * For derived union the getNumOf... method is generated as internal method. e.g.
     * <code>getNumOfCoveragesInternal</code>
     * 
     * @return The name of the getNumOf...Internal method
     */
    public String getMethodNameGetNumOfInternal() {
        return getMethodNameGetNumOf() + "Internal";
    }

    /**
     * Returns a list of associations that are a subset of this derived union. The associations are
     * part of the given {@link XClass}.
     * 
     * @param xClass The type in which context you generate code
     * @return the list of associations that subsets this derived union
     */
    public Set<XAssociation> getSubsetAssociations(XClass xClass) {
        Set<XAssociation> result = new LinkedHashSet<XAssociation>();
        Set<? extends XAssociation> associations = xClass.getAssociations();
        for (XAssociation xAssociation : associations) {
            if (xAssociation.isSubsetOf(this)) {
                result.add(xAssociation);
            }
        }
        return result;
    }

    /**
     * Checks whether this derived union is already implemented in any superclass. This is the case
     * if there is any superclass that has already a subset of this derived union.
     * 
     * @param xClass The type in which context you generate the code
     * @return True if there is already an implementation in any super class (super call is needed),
     *         false otherwise
     */
    public boolean isImplementedInSuperclass(XClass xClass) {
        if (getAssociationType().equals(xClass.getType())) {
            return false;
        }
        try {
            IType supertype = xClass.getType().findSupertype(getIpsProject());
            FindSubsetOfDerivedUnion findSubsetOfDerivedUnionVisitor = new FindSubsetOfDerivedUnion(getAssociation(),
                    getIpsProject());
            findSubsetOfDerivedUnionVisitor.start(supertype);
            return findSubsetOfDerivedUnionVisitor.foundSubset;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private static class FindSubsetOfDerivedUnion extends TypeHierarchyVisitor<IType> {

        private final IAssociation derivedUnion;

        private boolean foundSubset = false;

        public FindSubsetOfDerivedUnion(IAssociation derivedUnion, IIpsProject ipsProject) {
            super(ipsProject);
            this.derivedUnion = derivedUnion;
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            List<IAssociation> associations = currentType.getAssociations();
            for (IAssociation aAssociation : associations) {
                if (aAssociation.getSubsettedDerivedUnion().equals(derivedUnion.getName())) {
                    foundSubset = true;
                    return false;
                }
            }
            if (currentType.equals(derivedUnion.getType())) {
                return false;
            }
            return true;
        }
    }

    public boolean hasInverseAssociationsOfDerivedUnionSubsets(XPolicyCmptClass policyClass) {
        return !getInverseAssociationsOfDerivedUnionSubsets(policyClass).isEmpty();
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
     * @param policyClass the class to search for inverse associations of this derived union
     * 
     */
    public Set<XPolicyAssociation> getInverseAssociationsOfDerivedUnionSubsets(XPolicyCmptClass policyClass) {
        Set<XPolicyAssociation> result = new LinkedHashSet<XPolicyAssociation>();
        Set<? extends XPolicyAssociation> associations = policyClass.getAssociations();
        for (XPolicyAssociation xAssociation : associations) {
            if (xAssociation.hasInverseAssociation() && xAssociation.getInverseAssociation().isSubsetOf(this)) {
                result.add(xAssociation);
            }
        }
        return result;
    }

    /**
     * Returns the setter name for derived unions on policy side, which does not capitalize the role
     * name until now erroneously. e.g. getpolicyPart() instead of getPolicyPart().
     */
    public String getMethodNameGetterForPolicy() {
        return "get" + StringUtils.uncapitalize(getName(false));
    }
}
