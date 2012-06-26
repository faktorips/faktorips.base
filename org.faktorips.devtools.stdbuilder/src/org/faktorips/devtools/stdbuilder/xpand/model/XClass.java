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
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.type.IType;
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

    public XClass(IIpsObjectPartContainer ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        super(ipsObjectPartContainer, context, modelService);
    }

    public static IJavaClassNameProvider createJavaClassNamingProvider() {
        return new DefaultJavaClassNameProvider();
    }

    public boolean hasSupertype() {
        return getType().hasSupertype();
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
        return getModelContext().getImplClassNaming();
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

    public String getSimpleName(BuilderAspect aspect) {
        return getJavaClassNaming().getUnqualifiedClassName(getType().getIpsSrcFile(), aspect,
                getJavaClassNameProvider());
    }

    public String getQualifiedName(BuilderAspect aspect) {
        return getJavaClassNaming().getQualifiedClassName(getType(), aspect, getJavaClassNameProvider());
    }

    public String getPackageName() {
        return JavaPackageStructure.getPackageName(getType().getIpsSrcFile(), false, true);
    }

    public String getSuperclassName() {
        return getSuperClassOrInterfaceName(BuilderAspect.IMPLEMENTATION);
    }

    private String getSuperClassOrInterfaceName(BuilderAspect aspect) {
        try {
            if (getType().hasSupertype()) {
                IType superType = getType().findSupertype(getIpsProject());
                if (superType != null) {
                    return addImport(getJavaClassNaming().getQualifiedClassName(superType, aspect,
                            getJavaClassNameProvider()));
                } else {
                    return "";
                }
            } else {
                return getBaseSuperclassName();
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

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
     * Returns the list of associations in the current type. Does not add derived union
     * associations!
     * 
     * @see #getDerivedUnionAssociations()
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
    public abstract Set<XDerivedUnionAssociation> getDerivedUnionAssociations();

}
