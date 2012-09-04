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

import java.util.List;

import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.JavaClassNaming;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;

public abstract class XClass extends AbstractGeneratorModelNode {

    /**
     * The default java class name provider used by this class.
     * <p>
     * Never use this constant except in the method {@link #getJavaClassNameProvider()} because
     * {@link #getBaseSuperclassName()} may be overwritten in subclasses
     */
    private final IJavaClassNameProvider javaClassNameProvider;

    public XClass(IIpsObject ipsObject, GeneratorModelContext context, ModelService modelService) {
        super(ipsObject, context, modelService);
        javaClassNameProvider = createJavaClassNamingProvider(context.isGeneratePublishedInterfaces());
    }

    public static IJavaClassNameProvider createJavaClassNamingProvider(boolean generatePublishedInterface) {
        return new DefaultJavaClassNameProvider(generatePublishedInterface);
    }

    /**
     * Getting the {@link IJavaClassNameProvider} providing the java class name generated for this
     * {@link XClass}
     * 
     * @return The {@link IJavaClassNameProvider} to get the names of the generated java classes for
     *         this {@link XClass}
     */
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    public JavaClassNaming getJavaClassNaming() {
        return getContext().getJavaClassNaming();
    }

    public String getFileName(BuilderAspect aspect) {
        return getJavaClassNaming().getRelativeJavaFile(getIpsObjectPartContainer().getIpsSrcFile(), aspect,
                getJavaClassNameProvider()).toOSString();
    }

    @Override
    public IIpsObject getIpsObjectPartContainer() {
        return (IIpsObject)super.getIpsObjectPartContainer();
    }

    /**
     * If the builder is configured to generate published interfaces, this method returns the name
     * of the published interface. Else the name of the implementation class is returned.
     */
    public String getPublishedInterfaceName() {
        return addImport(getSimpleName(BuilderAspect.getValue(isGeneratePublishedInterfaces())));
    }

    public String getSimpleName(BuilderAspect aspect) {
        return addImport(getQualifiedName(aspect));
    }

    public String getQualifiedName(BuilderAspect aspect) {
        return getJavaClassNaming().getQualifiedClassName(getIpsObjectPartContainer(), aspect,
                getJavaClassNameProvider());
    }

    public String getPackageName(BuilderAspect aspect) {
        return getJavaClassNaming().getPackageName(getIpsObjectPartContainer().getIpsSrcFile(), aspect,
                getJavaClassNameProvider());
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

    public abstract List<String> getExtendedInterfaces();

    /**
     * Returns whether or not the generated class implements interfaces.
     */
    public boolean isImplementsInterface() {
        return !getImplementedInterfaces().isEmpty();
    }

    public abstract List<String> getImplementedInterfaces();

}
