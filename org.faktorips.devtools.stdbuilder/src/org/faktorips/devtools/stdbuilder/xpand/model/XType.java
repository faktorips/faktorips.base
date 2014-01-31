/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;

public abstract class XType extends XClass {

    public XType(IType type, GeneratorModelContext context, ModelService modelService) {
        super(type, context, modelService);
    }

    @Override
    public IType getIpsObjectPartContainer() {
        return (IType)super.getIpsObjectPartContainer();
    }

    public IType getType() {
        return getIpsObjectPartContainer();
    }

    public String getImplClassName() {
        return addImport(getSimpleName(BuilderAspect.IMPLEMENTATION));
    }

    public String getInterfaceName() {
        return addImport(getSimpleName(BuilderAspect.INTERFACE));
    }

    public String getNameForVariable() {
        return getJavaNamingConvention().getMemberVarName(getName());
    }

    public Set<XMethod> getMethods() {
        if (isCached(XMethod.class)) {
            return getCachedObjects(XMethod.class);
        } else {
            Set<XMethod> nodesForParts = initNodesForParts(getType().getMethods(), XMethod.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    private String getSuperclassName(BuilderAspect aspect) {
        if (getType().hasSupertype()) {
            XType xSuperType = getSupertype();
            return xSuperType.getSimpleName(aspect);
        } else {
            return getBaseSuperclassName();
        }
    }

    public XType getSupertype() {
        IType superType;
        try {
            superType = getType().findSupertype(getIpsProject());
            if (superType == null) {
                throw new NullPointerException("Found no supertype for " + getName());
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        XType xSuperType = getModelNode(superType, getClass());
        return xSuperType;
    }

    public String getSuperclassName() {
        return getSuperclassName(BuilderAspect.IMPLEMENTATION);
    }

    public boolean hasSupertype() {
        return getType().hasSupertype();
    }

    public boolean hasNonAbstractSupertype() {
        try {
            IType superType = getType().findSupertype(getIpsProject());
            if (superType == null) {
                return false;
            } else {
                NonAbstractSupertypeFinder finder = new NonAbstractSupertypeFinder(getIpsProject());
                finder.start(superType);
                return finder.hasNonAbstractSupertype();
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public <T extends XType> Set<T> getClassHierarchy(Class<T> concreteClass) {
        SuperclassCollector<T> superclassCollector = new SuperclassCollector<T>(this, concreteClass);
        superclassCollector.start(getType());
        return superclassCollector.getSuperclasses();
    }

    public abstract Set<? extends XType> getClassHierarchy();

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        LinkedHashSet<String> list = new LinkedHashSet<String>();
        if (hasSupertype()) {
            String superInterfaceName = getSupertype().getQualifiedName(BuilderAspect.INTERFACE);
            list.add(addImport(superInterfaceName));
        }
        list.addAll(getExtendedOrImplementedInterfaces());
        return list;
    }

    @Override
    protected LinkedHashSet<String> getExtendedOrImplementedInterfaces() {
        LinkedHashSet<String> list = new LinkedHashSet<String>();
        return list;
    }

    @Override
    public LinkedHashSet<String> getImplementedInterfaces() {
        LinkedHashSet<String> list = new LinkedHashSet<String>();
        if (isGeneratePublishedInterfaces()) {
            list.add(getInterfaceName());
        } else {
            list.addAll(getExtendedOrImplementedInterfaces());
        }
        return list;
    }

    public abstract Set<? extends XAttribute> getAttributes();

    /**
     * Returns the list of associations in the current type.
     * 
     * @return The list of associations in this type except derived unions
     */
    public abstract Set<? extends XAssociation> getAssociations();

    /**
     * Getting the list of every derived union needs to be implemented. That means it is either
     * defined in this type or in any super type and this type defines a subset of it.
     * 
     * @return The list of derived unions you need to implement in this class
     */
    public abstract Set<XDerivedUnionAssociation> getSubsettedDerivedUnions();

    public boolean isAbstract() {
        return getType().isAbstract();
    }

    /**
     * Returns the derived unions (not subsets) of all given associations. Each given association is
     * checked whether it is a subset of a derived union:
     * <ul>
     * <li>If it is, the derived union that is subsetted by said association is retrieved and added
     * to the result</li>
     * <li>If it is not the association is ignored</li>
     * </ul>
     * 
     * @param associations all associations defined for this class
     */
    protected <X extends XAssociation> Set<XDerivedUnionAssociation> findSubsettedDerivedUnions(Collection<X> associations) {
        Set<XDerivedUnionAssociation> resultingAssociations = new LinkedHashSet<XDerivedUnionAssociation>();
        for (X association : associations) {
            if (association.isSubsetOfADerivedUnion()) {
                XAssociation subsettedDerivedUnionNode = association.getSubsettedDerivedUnion();
                resultingAssociations.add(getModelNode(subsettedDerivedUnionNode.getAssociation(),
                        XDerivedUnionAssociation.class));
            }
        }
        return resultingAssociations;
    }

    /**
     * Returns all master to detail associations of a type including derived unions but not subsets
     * of derived unions. Ignores associations of wrong association classes. e.g. returns an empty
     * list if you retrieve the {@link IPolicyCmptTypeAssociation policy associations} of an
     * {@link IProductCmptType}.
     */
    protected <T extends IAssociation> Set<T> getAssociations(IType type,
            Class<T> associationClass,
            AbstractAssociationFilter filter) {
        Set<T> result = new LinkedHashSet<T>();
        List<IAssociation> associations = type.getAssociations();
        for (IAssociation association : associations) {
            if (associationClass.isAssignableFrom(association.getClass()) && filter.isValidAssociation(association)) {
                @SuppressWarnings("unchecked")
                T typedAssociation = (T)association;
                result.add(typedAssociation);
            }
        }
        return result;
    }

    protected static class SuperclassCollector<T extends XType> extends TypeHierarchyVisitor<IType> {

        private final Class<T> nodeClass;

        private final Set<T> superclasses = new LinkedHashSet<T>();

        private final XType currentNode;

        public SuperclassCollector(XType currentNode, Class<T> nodeClass) {
            super(currentNode.getIpsProject());
            this.currentNode = currentNode;
            this.nodeClass = nodeClass;
        }

        @Override
        protected boolean visit(IType currentType) {
            getSuperclasses().add(currentNode.getModelNode(currentType, nodeClass));
            return true;
        }

        public Set<T> getSuperclasses() {
            return superclasses;
        }

    }

    private static class NonAbstractSupertypeFinder extends TypeHierarchyVisitor<IType> {
        boolean hasNonAbstractSupertype = false;

        public NonAbstractSupertypeFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        public boolean hasNonAbstractSupertype() {
            return hasNonAbstractSupertype;
        }

        @Override
        protected boolean visit(IType currentType) {
            hasNonAbstractSupertype |= !currentType.isAbstract();
            if (hasNonAbstractSupertype) {
                return false;
            }
            return true;
        }
    }
}
