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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.type.IType;

public abstract class XClass extends AbstractGeneratorModelNode {

    private static final IJavaClassNameProvider JAVA_CLASS_NAMEING_PROVIDER = createJavaClassNamingProvider();

    public XClass(IIpsObjectPartContainer ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        super(ipsObjectPartContainer, context, modelService);
    }

    /**
     * 
     * @param parts the parts to create {@link AbstractGeneratorModelNode nodes} for
     * @param nodeClass the expected concrete generator model class (subclass of
     *            {@link AbstractGeneratorModelNode})
     * @return a list containing one {@link AbstractGeneratorModelNode} for every
     *         {@link IIpsObjectPart} in the given list.
     */
    protected <T extends AbstractGeneratorModelNode> List<T> initNodesForParts(List<? extends IIpsObjectPart> parts,
            Class<T> nodeClass) {
        List<T> nodes = new ArrayList<T>();
        for (IIpsObjectPart part : parts) {
            nodes.add(getModelService().getModelNode(part, nodeClass, getModelContext()));
        }
        return nodes;
    }

    public static IJavaClassNameProvider createJavaClassNamingProvider() {
        return new DefaultJavaClassNameProvider() {
            // TODO FOR DEVELOPMENT ONLY!!!
            @Override
            public String getImplClassName(IIpsSrcFile ipsSrcFile) {
                return super.getImplClassName(ipsSrcFile) + "_X";
            }

            @Override
            public String getInterfaceName(IIpsSrcFile ipsSrcFile) {
                return super.getInterfaceName(ipsSrcFile) + "_X";
            }
        };
    }

    /**
     * Getting the {@link IJavaClassNameProvider} providing the java class name generated for this
     * {@link XClass}
     * 
     * @return The {@link IJavaClassNameProvider} to get the names of the generated java classes for
     *         this {@link XClass}
     */
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return JAVA_CLASS_NAMEING_PROVIDER;
    }

    public JavaClassNaming getJavaClassNaming() {
        return getModelContext().getImplClassNaming();
    }

    public JavaClassNaming getInterfaceNaming() {
        return getModelContext().getImplClassNaming();
    }

    public String getFileName(BuilderAspect aspect) {
        return getJavaClassNaming().getRelativeJavaFile(getIpsObjectPartContainer().getIpsSrcFile(), aspect,
                JAVA_CLASS_NAMEING_PROVIDER).toOSString();
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
                JAVA_CLASS_NAMEING_PROVIDER);
    }

    public String getQualifiedName(BuilderAspect aspect) {
        return getJavaClassNaming().getQualifiedClassName(getType(), aspect, JAVA_CLASS_NAMEING_PROVIDER);
    }

    public String getPackageName() {
        return JavaPackageStructure.getPackageName(getType().getIpsSrcFile(), false, true);
    }

    public String getSuperclassName() {
        try {
            if (getType().hasSupertype()) {
                IType superType = getType().findSupertype(getIpsProject());
                if (superType != null) {
                    return addImport(getJavaClassNaming().getQualifiedClassName(superType,
                            BuilderAspect.IMPLEMENTATION, JAVA_CLASS_NAMEING_PROVIDER));
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

    public boolean isImplementsInterface() {
        return !getImplementedInterface().isEmpty();
    }

    public List<String> getImplementedInterface() {
        ArrayList<String> list = new ArrayList<String>();
        addImport(getInterfaceNaming().getQualifiedClassName(getType(), BuilderAspect.INTERFACE,
                JAVA_CLASS_NAMEING_PROVIDER));
        return list;
    }

}
