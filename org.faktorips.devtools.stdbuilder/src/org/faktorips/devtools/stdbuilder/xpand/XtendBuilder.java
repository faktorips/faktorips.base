/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.faktorips.devtools.stdbuilder.xpand.template.CommonDefinitionsTmpl;
import org.faktorips.devtools.stdbuilder.xpand.template.CommonGeneratorExtensionsTmpl;
import org.faktorips.util.LocalizedStringsSet;

/**
 * An abstract implementation to use XTEND templates for code generation.
 * 
 */
public abstract class XtendBuilder<T extends XClass> extends JavaSourceFileBuilder {

    private final ModelService modelService;
    private final IJavaClassNameProvider javaClassNameProvider;
    private final GeneratorModelContext generatorModelContext;

    public XtendBuilder(DefaultBuilderSet builderSet, GeneratorModelContext modelContext, ModelService modelService,
            LocalizedStringsSet localizedStringsSet) {
        super(builderSet, localizedStringsSet);
        this.javaClassNameProvider = XClass
                .createJavaClassNamingProvider(modelContext.isGeneratePublishedInterfaces(builderSet.getIpsProject()));
        setMergeEnabled(true);
        this.generatorModelContext = modelContext;
        this.modelService = modelService;
    }

    @Override
    protected String generate() throws CoreException {
        if (getGeneratorModelRoot(getIpsObject()).isValidForCodeGeneration()) {
            CommonGeneratorExtensionsTmpl.setGenInterface(generatesInterface());
            String body = generateBody(getIpsObject());
            String packageDef = generatePackageDef();
            String importBlock = generateImportBlock();
            return packageDef + importBlock + body;
        } else {
            return null;
        }
    }

    protected String generatePackageDef() {
        return CommonDefinitionsTmpl.packageDef(getGeneratorModelRoot(getIpsObject()),
                BuilderAspect.getValue(generatesInterface()));
    }

    protected String generateImportBlock() {
        return CommonDefinitionsTmpl.importBlock(generatorModelContext);
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();

    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        generatorModelContext.resetContext(getPackage());
    }

    @Override
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException {
        super.afterBuild(ipsSrcFile);
        generatorModelContext.resetContext(null);
    }

    /**
     * Implementations of this class must override this method to provide the the body of the java
     * source file.
     * 
     * @param ipsObject that is used to create the body.
     * @return the body for a java source as a String.
     */
    protected abstract String generateBody(IIpsObject ipsObject);

    protected T getGeneratorModelRoot(IIpsObject ipsObject) {
        T xClass = getModelService().getModelNode(ipsObject, getGeneratorModelRootType(), generatorModelContext);
        return xClass;
    }

    public ModelService getModelService() {
        return modelService;
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    protected abstract Class<T> getGeneratorModelRootType();

    /**
     * Returns true if this builder is generating artifacts for the specified
     * {@link IIpsObjectPartContainer}.
     * <p>
     * For example a product component builder may generate artifacts for a product configured policy
     * component type attribute.
     * <p>
     * It is not strictly necessary that there are really generated artifacts for this
     * {@link IIpsObjectPartContainer} because this may depend on very much circumstances. But it is
     * strictly necessary that {@link #getSupportedIpsObject(IIpsObjectPartContainer)} does not return
     * null if this method returns <code>true</code>.
     */
    public abstract boolean isGeneratingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer);

    /**
     * Returns the {@link IIpsObject} that is supported by this builder for a
     * {@link IIpsObjectPartContainer} for which this builder seems to generate artifacts.
     * <p>
     * For example the {@link IIpsObjectPartContainer} may be a policy attribute and this builder is
     * responsible to build product component generations. If the policy attribute is configured by a
     * product component, then this builder needs to generate some artifacts for this policy attribute.
     * This method would return the corresponding product component type so this (product component)
     * builder could parse the templates and collect possible generated artifacts.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} for which we want to get the
     *            {@link IIpsObject} which is supported by this builder
     * @return The {@link IIpsObject} that is supported by this builder
     */
    protected abstract IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer);

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {
        getGeneratedArtifacts(getSupportedIpsObject(ipsObjectPartContainer), ipsObjectPartContainer, javaElements);
    }

    /**
     * Get the generated artifacts by evaluating the template using the given {@link IIpsObject}. The
     * generated artifacts are collected by the specified {@link IIpsObjectPartContainer}. The
     * {@link IIpsObject} may differ from the IPS object of the part.
     * 
     * @param ipsObject The object used to parse the template
     * @param ipsObjectPartContainer the {@link IIpsObjectPartContainer} for which we collect the
     *            generated artifacts
     * @param javaElements the list of java elements where we add our result to
     */
    private void getGeneratedArtifacts(IIpsObject ipsObject,
            IIpsObjectPartContainer ipsObjectPartContainer,
            List<IJavaElement> javaElements) {

        generatorModelContext.resetContext(null);

        generateBody(ipsObject);

        // At the moment only one java type per generator is supported. Multiple types are only
        // generated for adjustments implementing formulas
        List<IType> generatedJavaTypes = getGeneratedJavaTypes(ipsObject);
        if (generatedJavaTypes.size() > 1) {
            throw new IllegalArgumentException(
                    "Found more than one " + generatedJavaTypes + " for " + ipsObjectPartContainer);
        }
        IType javaType = generatedJavaTypes.get(0);

        Set<AbstractGeneratorModelNode> allModelNodes = modelService.getAllModelNodes(ipsObjectPartContainer);
        for (AbstractGeneratorModelNode generatorModelNode : allModelNodes) {
            javaElements.addAll(generatorModelNode.getGeneratedJavaElements(javaType));
        }
    }

    public GeneratorModelContext getGeneratorModelContext() {
        return generatorModelContext;
    }

}
