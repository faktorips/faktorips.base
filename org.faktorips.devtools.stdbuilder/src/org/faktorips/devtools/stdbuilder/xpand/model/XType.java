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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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

    private Set<XMethod> methods;

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

    @Override
    protected void clearCaches() {
        super.clearCaches();
        methods = null;
    }

    public Set<XMethod> getMethods() {
        checkForUpdate();
        if (methods == null) {
            synchronized (this) {
                if (methods == null) {
                    methods = initNodesForParts(getType().getMethods(), XMethod.class);
                }
            }
        }
        return new CopyOnWriteArraySet<XMethod>(methods);
    }

    private String getSuperclassName(BuilderAspect aspect) {
        if (getType().hasSupertype()) {
            XType xSuperType = getSupertype();
            return xSuperType.getSimpleName(aspect);
        } else {
            return getBaseSuperclassName();
        }
    }

    protected XType getSupertype() {
        IType superType;
        try {
            superType = getType().findSupertype(getIpsProject());
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
        try {
            SuperclassCollector<T> superclassCollector = new SuperclassCollector<T>(this, concreteClass);
            superclassCollector.start(getType());
            return superclassCollector.getSuperclasses();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public abstract Set<? extends XType> getClassHierarchy();

    /**
     * Returns all interfaces the (generated) published interface for this class extends.
     */
    @Override
    public List<String> getExtendedInterfaces() {
        ArrayList<String> list = new ArrayList<String>();
        if (hasSupertype() && isGeneratePublishedInterfaces()) {
            String importStatement = addImport(getJavaClassNaming().getQualifiedClassName(getType(),
                    BuilderAspect.INTERFACE, getJavaClassNameProvider()));
            list.add(importStatement);
        }
        return list;
    }

    /**
     * Returns all interfaces the generated class implements.
     */
    @Override
    public List<String> getImplementedInterfaces() {
        ArrayList<String> list = new ArrayList<String>();
        if (isGeneratePublishedInterfaces()) {
            String name = getJavaClassNaming().getQualifiedClassName(getType(), BuilderAspect.INTERFACE,
                    getJavaClassNameProvider());
            list.add(addImport(name));
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
        protected boolean visit(IType currentType) throws CoreException {
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
        protected boolean visit(IType currentType) throws CoreException {
            hasNonAbstractSupertype |= !currentType.isAbstract();
            if (hasNonAbstractSupertype) {
                return false;
            }
            return true;
        }
    }
}
