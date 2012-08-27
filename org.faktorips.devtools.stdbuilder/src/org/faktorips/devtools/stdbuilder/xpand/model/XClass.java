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
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.INotificationSupport;

public abstract class XClass extends AbstractGeneratorModelNode {

    /**
     * The default java class name provider used by this class.
     * <p>
     * Never use this constant except in the method {@link #getJavaClassNameProvider()} because
     * {@link #getBaseSuperclassName()} may be overwritten in subclasses
     */
    private static final IJavaClassNameProvider JAVA_CLASS_NAME_PROVIDER = createJavaClassNamingProvider();

    private Set<XMethod> methods;

    public XClass(IType type, GeneratorModelContext context, ModelService modelService) {
        super(type, context, modelService);
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        methods = null;
    }

    public static IJavaClassNameProvider createJavaClassNamingProvider() {
        return new DefaultJavaClassNameProvider();
    }

    /**
     * Getting the {@link IJavaClassNameProvider} providing the java class name generated for this
     * {@link XClass}
     * 
     * @return The {@link IJavaClassNameProvider} to get the names of the generated java classes for
     *         this {@link XClass}
     */
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return JAVA_CLASS_NAME_PROVIDER;
    }

    public JavaClassNaming getJavaClassNaming() {
        return getContext().getJavaClassNaming();
    }

    public String getFileName(BuilderAspect aspect) {
        return getJavaClassNaming().getRelativeJavaFile(getIpsObjectPartContainer().getIpsSrcFile(), aspect,
                getJavaClassNameProvider()).toOSString();
    }

    @Override
    public IType getIpsObjectPartContainer() {
        return (IType)super.getIpsObjectPartContainer();
    }

    public IType getType() {
        return getIpsObjectPartContainer();
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

    /**
     * If the builder is configured to generate published interfaces, this method returns the name
     * of the published interface. Else the name of the implementation class is returned.
     */
    public String getPublishedInterfaceName() {
        return addImport(getSimpleName(BuilderAspect.getValue(isGeneratingPublishedInterfaces())));
    }

    public String getSimpleName(BuilderAspect aspect) {
        return addImport(getQualifiedName(aspect));
    }

    public String getQualifiedName(BuilderAspect aspect) {
        return getJavaClassNaming().getQualifiedClassName(getType(), aspect, getJavaClassNameProvider());
    }

    public String getPackageName() {
        return JavaPackageStructure.getPackageName(getType().getIpsSrcFile(), false, true);
    }

    public String getSuperclassName() {
        return getSuperclassName(BuilderAspect.IMPLEMENTATION);
    }

    private String getSuperclassName(BuilderAspect aspect) {
        try {
            if (getType().hasSupertype()) {
                IType superType = getType().findSupertype(getIpsProject());
                return addImport(getJavaClassNaming().getQualifiedClassName(superType, aspect,
                        getJavaClassNameProvider()));
            } else {
                return getBaseSuperclassName();
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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

    public <T extends XClass> Set<T> getClassHierarchy(Class<T> concreteClass) {
        try {
            SuperclassCollector<T> superclassCollector = new SuperclassCollector<T>(getIpsProject(), concreteClass);
            superclassCollector.start(getType());
            return superclassCollector.getSuperclasses();
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public abstract Set<? extends XClass> getClassHierarchy();

    /**
     * Returns the unqualified name of the base superclass that is used when the IPS type does not
     * have a super type. The qualified name must be added to the import declarations
     * 
     * @see #addImport(Class)
     * 
     * @return The unqualified name of the base superclass with added import statement.
     */
    protected abstract String getBaseSuperclassName();

    /**
     * Returns whether or not the published interface for this class extends other interfaces.
     */
    public boolean isExtendsInterface() {
        return !getExtendedInterfaces().isEmpty();
    }

    /**
     * Returns all interfaces the (generated) published interface for this class extends.
     */
    private List<String> getExtendedInterfaces() {
        ArrayList<String> list = new ArrayList<String>();
        // TODO FIPS-1059
        // TODO Testcase, development suspended as we create the class methods first
        if (hasSupertype()) {
            String importStatement = addImport(getJavaClassNaming().getQualifiedClassName(getType(),
                    BuilderAspect.INTERFACE, getJavaClassNameProvider()));
            list.add(importStatement);
        } else {
            list.add(addImport(IConfigurableModelObject.class));
            list.add(addImport(INotificationSupport.class));
        }
        return list;
    }

    /**
     * Returns whether or not the generated class implements interfaces.
     */
    public boolean isImplementsInterface() {
        return !getImplementedInterfaces().isEmpty();
    }

    /**
     * Returns all interfaces the generated class implements.
     */
    public List<String> getImplementedInterfaces() {
        // TODO FIPS-1059
        ArrayList<String> list = new ArrayList<String>();
        String name = getJavaClassNaming().getQualifiedClassName(getType(), BuilderAspect.INTERFACE,
                getJavaClassNameProvider());
        list.add(addImport(name));
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
     * @param associationClass the requires association class
     */
    protected <T extends IAssociation, X extends XAssociation> Set<T> findSubsettedDerivedUnions(Collection<X> associations,
            Class<T> associationClass) {
        Set<T> resultingAssociations = new LinkedHashSet<T>();
        for (X association : associations) {
            if (association.isSubsetOfADerivedUnion()) {
                XAssociation subsettedDerivedUnionNode = association.getSubsettedDerivedUnion();
                if (associationClass.isAssignableFrom(subsettedDerivedUnionNode.getAssociation().getClass())) {
                    @SuppressWarnings("unchecked")
                    // safe cast due to subclass check above
                    T typedDU = (T)subsettedDerivedUnionNode.getAssociation();
                    resultingAssociations.add(typedDU);
                }
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

    protected class SuperclassCollector<T extends XClass> extends TypeHierarchyVisitor<IType> {

        private final Class<T> nodeClass;

        private final Set<T> superclasses = new LinkedHashSet<T>();

        public SuperclassCollector(IIpsProject ipsProject, Class<T> nodeClass) {
            super(ipsProject);
            this.nodeClass = nodeClass;
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            getSuperclasses().add(getModelNode(currentType, nodeClass));
            return true;
        }

        public Set<T> getSuperclasses() {
            return superclasses;
        }

    }

    private class NonAbstractSupertypeFinder extends TypeHierarchyVisitor<IType> {
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
