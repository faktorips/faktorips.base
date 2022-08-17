/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;

/**
 * This is the generator model node for a derived union association. It is very important to
 * remember, that the corresponding (derived union) association is not in the IType you currently
 * generate code for. Thats why many methods need to know the xClass in which context you currently
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
    public XDerivedUnionAssociation(IAssociation association, GeneratorModelContext context,
            ModelService modelService) {
        super(association, context, modelService, new LocalizedStringsSet(
                association instanceof IPolicyCmptTypeAssociation ? XPolicyAssociation.class
                        : XProductAssociation.class));
    }

    /**
     * For derived union the getNumOf... method is generated as internal method. e.g.
     * <code>getNumOfCoveragesInternal</code>
     * 
     * @return The name of the getNumOf...Internal method
     */
    public String getMethodNameGetNumOfInternal() {
        // TODO Bad hack to be compatible with old code generator
        if (XProductClass.class.isAssignableFrom(getModelNodeType(false))) {
            return getJavaNamingConvention().getGetterMethodName(
                    "NumOf" + StringUtils.capitalize(getAssociation().getTargetRolePlural()))
                    + "Internal";
        }
        return getMethodNameGetNumOf() + "Internal";
    }

    /**
     * Returns a list of associations that are a subset of this derived union. This includes derived
     * union associations that are at the same time subsets of this derived union. The associations
     * are part of the given {@link XClass}.
     * 
     * @param xType The type in which context you generate code
     * @return the list of associations that subsets this derived union
     */
    public Set<XAssociation> getSubsetAssociations(XType xType) {
        Set<XAssociation> result = new LinkedHashSet<>();
        Set<? extends XAssociation> associations = xType.getAssociations();
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
     * @param xType The type in which context you generate the code
     * @return True if there is already an implementation in any super class (super call is needed),
     *             false otherwise
     */
    public boolean isImplementedInSuperclass(XType xType) {
        if (isDefinedIn(xType)) {
            return false;
        }
        IType supertype = xType.getType().findSupertype(xType.getIpsProject());
        FindSubsetOfDerivedUnionVisitor findSubsetOfDerivedUnionVisitor = new FindSubsetOfDerivedUnionVisitor(
                getAssociation(), xType.getIpsProject());
        findSubsetOfDerivedUnionVisitor.start(supertype);
        return findSubsetOfDerivedUnionVisitor.isSubsetFound();
    }

    /**
     * Returns <code>true</code> if this derived union association is defined in the given class.
     * <code>false</code> else.
     */
    public boolean isDefinedIn(XType xType) {
        return getSourceType().equals(xType.getType());
    }

    /**
     * Returns <code>true</code> when the method getNumOfXXInternal() should call super. This is the
     * case if this derived union is not defined in the given class and at the same time is
     * subsetted by an association of the given class. IOW returns <code>true</code> if there is a
     * method in the super class that can be called. <code>false</code> otherwise.
     * 
     * @param xType the class in which a super call could be generated or not.
     */
    public boolean generateGetNumOfInternalSuperCall(XType xType) {
        return !isDefinedIn(xType) && isImplementedInSuperclass(xType);
    }

    /**
     * TODO Only needed because of some strange code that was generated in the old code generator.
     * FIPS-1141
     * 
     */
    public boolean isProductCmptTypeAssociation() {
        return getSourceType() instanceof IProductCmptType;
    }

    public boolean needOverride(XType currentContextType) {
        return !isDefinedIn(currentContextType) || isImplementedInSuperclass(currentContextType);
    }

    @Override
    protected Class<? extends XAssociation> getMatchingClass() {
        return XDerivedUnionAssociation.class;
    }

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementTypeForGetter() {
        if (isProductCmptTypeAssociation()) {
            return AnnotatedJavaElementType.PRODUCT_CMPT_DECL_CLASS_ASSOCIATION_GETTER;
        } else {
            return AnnotatedJavaElementType.POLICY_CMPT_DECL_CLASS_ASSOCIATION_GETTER;
        }
    }

    /**
     * Searches the given type (and the super type hierarchy) for subsets of the derived union
     * specified when creating the visitor.
     * 
     */
    private static class FindSubsetOfDerivedUnionVisitor extends TypeHierarchyVisitor<IType> {

        private final IAssociation derivedUnion;

        private boolean foundSubset = false;

        public FindSubsetOfDerivedUnionVisitor(IAssociation derivedUnion, IIpsProject ipsProject) {
            super(ipsProject);
            this.derivedUnion = derivedUnion;
        }

        @Override
        protected boolean visit(IType currentType) {
            List<IAssociation> associations = currentType.getAssociations();
            for (IAssociation aAssociation : associations) {
                if (aAssociation.getSubsettedDerivedUnion() != null
                        && aAssociation.getSubsettedDerivedUnion().equals(derivedUnion.getName())) {
                    foundSubset = true;
                    return false;
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
