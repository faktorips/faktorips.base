/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;

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

    /**
     * Returns the implementation name and adds the implementation name to the list of imports. Only
     * use this method if you require the implementation class explicitly. Use {@link #getName()} if
     * you just want to use the name for building other strings like method names.
     *
     */
    public String getImplClassName() {
        return getSimpleName(BuilderAspect.IMPLEMENTATION);
    }

    public String getInterfaceName() {
        return getSimpleName(BuilderAspect.INTERFACE);
    }

    public String getClassName(boolean isInterface) {
        return getSimpleName(BuilderAspect.getValue(isInterface));
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
        superType = getType().findSupertype(getIpsProject());
        if (superType == null) {
            throw new NullPointerException("Found no supertype for " + getName());
        }
        return getModelNode(superType, getClass());
    }

    public String getSuperclassName() {
        return getSuperclassName(BuilderAspect.IMPLEMENTATION);
    }

    public boolean hasSupertype() {
        return getType().hasSupertype();
    }

    public boolean hasNonAbstractSupertype() {
        IType superType = getType().findSupertype(getIpsProject());
        if (superType == null) {
            return false;
        } else {
            NonAbstractSupertypeFinder finder = new NonAbstractSupertypeFinder(getIpsProject());
            finder.start(superType);
            return finder.hasNonAbstractSupertype();
        }
    }

    public <T extends XType> Set<T> getClassHierarchy(Class<T> concreteClass) {
        SuperclassCollector<T> superclassCollector = new SuperclassCollector<>(this, concreteClass);
        superclassCollector.start(getType());
        return superclassCollector.getSuperclasses();
    }

    public abstract Set<? extends XType> getClassHierarchy();

    @Override
    public LinkedHashSet<String> getExtendedInterfaces() {
        LinkedHashSet<String> list = new LinkedHashSet<>();
        if (hasSupertype()) {
            String superInterfaceName = getSupertype().getQualifiedName(BuilderAspect.INTERFACE);
            list.add(addImport(superInterfaceName));
        }
        list.addAll(getExtendedOrImplementedInterfaces());
        return list;
    }

    @Override
    protected LinkedHashSet<String> getExtendedOrImplementedInterfaces() {
        return new LinkedHashSet<>();
    }

    @Override
    public LinkedHashSet<String> getImplementedInterfaces() {
        LinkedHashSet<String> list = new LinkedHashSet<>();
        if (getGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject())) {
            list.add(getInterfaceName());
        } else {
            list.addAll(getExtendedOrImplementedInterfaces());
        }
        return list;
    }

    public abstract Set<? extends XAttribute> getAttributes();

    /**
     * Returns the list of all attributes declared in the current type. For product component types,
     * this method does not differentiate between changing and not changing over time attributes.
     *
     * @return the list of all attributes declared of this type
     */
    public abstract Set<? extends XAttribute> getAllDeclaredAttributes();

    /**
     * @return The list of associations relevant for this type instance
     */
    public abstract Set<? extends XAssociation> getAssociations();

    /**
     * Returns the list of associations declared in the current type. For product component types,
     * this method does not differentiate between changing and not changing over time associations.
     *
     * @return the list of all associations declared in this type
     */
    public abstract Set<? extends XAssociation> getAllDeclaredAssociations();

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
    protected <X extends XAssociation> Set<XDerivedUnionAssociation> findSubsettedDerivedUnions(
            Collection<X> associations) {
        Set<XDerivedUnionAssociation> resultingAssociations = new LinkedHashSet<>();
        for (X association : associations) {
            if (association.isSubsetOfADerivedUnion()) {
                XAssociation subsettedDerivedUnionNode = association.getSubsettedDerivedUnion();
                resultingAssociations
                        .add(getModelNode(subsettedDerivedUnionNode.getAssociation(), XDerivedUnionAssociation.class));
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
        Set<T> result = new LinkedHashSet<>();
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

        private final Set<T> superclasses = new LinkedHashSet<>();

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
        private boolean hasNonAbstractSupertype = false;

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
