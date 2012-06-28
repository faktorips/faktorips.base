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

package org.faktorips.devtools.stdbuilder.xpand;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.internal.xpand2.model.XpandDefinition;
import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.xpand2.XpandExecutionContext;
import org.eclipse.xpand2.XpandExecutionContextImpl;
import org.eclipse.xtend.type.impl.java.JavaBeansMetaModel;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ImportHandler;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.faktorips.devtools.stdbuilder.xpand.stringout.StringOutlet;
import org.faktorips.devtools.stdbuilder.xpand.stringout.StringOutput;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * An abstract implementation to use XPAND templates for code generation. The implementation only
 * needs to provide the name of the template and the corresponding generator model.
 * 
 * @author dirmeier
 */
public abstract class XpandBuilder<T extends AbstractGeneratorModelNode> extends JavaSourceFileBuilder {

    private final static IJavaClassNameProvider JAVA_CLASS_NAMEING_PROVIDER = XClass.createJavaClassNamingProvider();

    private XpandDefinition templateDefinition;

    private XpandExecutionContextImpl xpandContext;

    private StringOutput out;

    private final ModelService modelService;

    private final GeneratorModelContext generatorModelContext;

    /**
     * The XPAND builder is associated to a builder set and need the {@link LocalizedStringsSet} for
     * translating for example java doc.
     * 
     * @param builderSet The builder set used with this builder
     * @param localizedStringsSet the {@link LocalizedStringsSet} for translations
     */
    public XpandBuilder(StandardBuilderSet builderSet, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, localizedStringsSet);
        modelService = new ModelService();
        generatorModelContext = new GeneratorModelContext(getBuilderSet().getConfig(), getBuilderSet()
                .getAnnotationGenerators());
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return JAVA_CLASS_NAMEING_PROVIDER;
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        super.beforeBuildProcess(project, buildKind);
        initTemplate();
        String charset = project.getProject().getDefaultCharset();
        StringOutlet outlet = (StringOutlet)getOut().getOutlet(null);
        if (outlet == null || !outlet.getFileEncoding().equals(charset)) {
            outlet = new StringOutlet(charset, null);
            getOut().addOutlet(outlet);
        }
    }

    /**
     * Initializes the template given by the concrete implementation of {@link #getTemplate()}.
     */
    protected void initTemplate() {
        setOut(new StringOutput());
        xpandContext = new XpandExecutionContextImpl(getOut(), null);
        JavaBeansMetaModel mm = new JavaBeansMetaModel();
        xpandContext.registerMetaModel(mm);

        final org.eclipse.xtend.typesystem.Type targetType = xpandContext.getTypeForName(getGeneratorModelNodeClass()
                .getName().replaceAll("\\.", SyntaxConstants.NS_DELIM));
        ArgumentCheck.notNull(targetType);
        final org.eclipse.xtend.typesystem.Type[] paramTypes = new org.eclipse.xtend.typesystem.Type[0];
        setTemplateDefinition(xpandContext.findDefinition(getTemplate(), targetType, paramTypes));
        ArgumentCheck.notNull(getTemplateDefinition());
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
        generatorModelContext.setImportHandler(new ImportHandler(getPackage()));
    }

    /**
     * Calls the template evaluation and provides the generated code.
     * 
     * @return the generated java source code
     */
    @Override
    protected String generate() throws CoreException {
        if (getIpsObject().isValid(getIpsProject())) {
            StringOutlet outlet = (StringOutlet)getOut().getOutlet(null);
            evaluateTemplate(getIpsSrcFile().getIpsObject());
            return outlet.getContent(getRelativeJavaFile(getIpsSrcFile()));
        } else {
            return null;
        }
    }

    private void evaluateTemplate(IIpsObject ipsObject) {
        getTemplateDefinition().evaluate((XpandExecutionContext)xpandContext.cloneWithoutVariables(),
                getGeneratorModelRoot(ipsObject));
    }

    /**
     * Getting the name of the template. The template name should be a static information. This
     * method is called when initializing the template.
     * <p>
     * The templates name needs to be in the XPAND qualifier syntax for example:
     * org::faktorips::devtools::stdbuilder::xpand::policycmpt::template::PolicyCmpt::main
     * 
     * @return The name qualified of the template
     */
    protected abstract String getTemplate();

    protected abstract Class<T> getGeneratorModelNodeClass();

    protected AbstractGeneratorModelNode getGeneratorModelRoot(IIpsObject ipsObject) {
        AbstractGeneratorModelNode xClass = getModelService().getModelNode(ipsObject, getGeneratorModelNodeClass(),
                getGeneratorModelContext());
        return xClass;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public GeneratorModelContext getGeneratorModelContext() {
        return generatorModelContext;
    }

    public StringOutput getOut() {
        return out;
    }

    public void setOut(StringOutput out) {
        this.out = out;
    }

    public XpandDefinition getTemplateDefinition() {
        return templateDefinition;
    }

    public void setTemplateDefinition(XpandDefinition templateDefinition) {
        this.templateDefinition = templateDefinition;
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {
        if (templateDefinition == null) {
            initTemplate();
            // TODO increase performance by creating a NullOutlet
            StringOutlet outlet = new StringOutlet("UTF-8", null);
            getOut().addOutlet(outlet);
        }
        // TODO NullImportHandler??
        generatorModelContext.setImportHandler(new ImportHandler(""));
        evaluateTemplate(ipsObjectPartContainer.getIpsObject());

        // TODO verschiedene Types?
        List<IType> javaTypes = getGeneratedJavaTypes(ipsObjectPartContainer.getIpsObject());

        Set<AbstractGeneratorModelNode> allModelNodes = modelService.getAllModelNodes(ipsObjectPartContainer);
        for (AbstractGeneratorModelNode generatorModelNode : allModelNodes) {
            javaElements.addAll(generatorModelNode.getGeneratedJavaElements(javaTypes.get(0)));
        }
    }

}
