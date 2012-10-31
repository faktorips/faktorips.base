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
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductClass;

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
        Set<XAssociation> result = new LinkedHashSet<XAssociation>();
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
     *         false otherwise
     */
    public boolean isImplementedInSuperclass(XType xType) {
        if (getTypeOfAssociation().equals(xType.getType())) {
            return false;
        }
        try {
            IType supertype = xType.getType().findSupertype(xType.getIpsProject());
            FindSubsetOfDerivedUnionVisitor findSubsetOfDerivedUnionVisitor = new FindSubsetOfDerivedUnionVisitor(
                    getAssociation(), xType.getIpsProject());
            findSubsetOfDerivedUnionVisitor.start(supertype);
            return findSubsetOfDerivedUnionVisitor.isSubsetFound();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns <code>true</code> if this derived union association is defined in the given class.
     * <code>false</code> else.
     */
    public boolean isDefinedIn(XType xType) {
        return getAssociation().getType().equals(xType.getType());
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
        return getTypeOfAssociation() instanceof IProductCmptType;
    }

    public boolean needOverride(XType currentContextType) {
        return (isImplementedInSuperclass(currentContextType) || getSourceType() != currentContextType);
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
        protected boolean visit(IType currentType) throws CoreException {
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
