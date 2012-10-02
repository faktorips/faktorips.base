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
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.internal.xpand2.model.XpandDefinition;
import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.xpand2.XpandExecutionContext;
import org.eclipse.xpand2.XpandExecutionContextImpl;
import org.eclipse.xpand2.output.Outlet;
import org.eclipse.xtend.expression.ExceptionHandler;
import org.eclipse.xtend.expression.NullEvaluationHandler;
import org.eclipse.xtend.type.impl.java.JavaBeansMetaModel;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.ImportHandler;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XClass;
import org.faktorips.devtools.stdbuilder.xpand.nullout.NullOutlet;
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

    /*
     * If this debug switch is set to true we reload the template with every build!
     */
    public static final boolean DEBUG = true;

    private final IJavaClassNameProvider javaClassNameProvider;

    private final ThreadLocal<XpandDefinition> threadLocalTemplateDefinition = new ThreadLocal<XpandDefinition>();

    private final ThreadLocal<XpandExecutionContextImpl> threadLocalXpandContext = new ThreadLocal<XpandExecutionContextImpl>();

    private final ThreadLocal<StringOutput> threadLocalOut = new ThreadLocal<StringOutput>();

    private final ModelService modelService;

    private final GeneratorModelContext generatorModelContext;

    /**
     * The XPAND builder is associated to a builder set and need the {@link LocalizedStringsSet} for
     * translating for example java doc.
     * 
     * @param builderSet The builder set used with this builder
     * @param modelContext the generator model context holding necessary context information
     * @param modelService the model service to get and create generator model nodes
     * @param localizedStringsSet the {@link LocalizedStringsSet} for translations
     */
    public XpandBuilder(StandardBuilderSet builderSet, GeneratorModelContext modelContext, ModelService modelService,
            LocalizedStringsSet localizedStringsSet) {
        super(builderSet, localizedStringsSet);
        javaClassNameProvider = XClass.createJavaClassNamingProvider(modelContext.isGeneratePublishedInterfaces());
        setMergeEnabled(true);
        generatorModelContext = modelContext;
        this.modelService = modelService;
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    public void beforeBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        super.beforeBuildProcess(project, buildKind);
        if (getTemplateDefinition() == null || DEBUG) {
            initTemplate();
        }
        String charset = project.getProject().getDefaultCharset();
        Outlet outlet = getOut().getOutlet(null);
        if (!(outlet instanceof StringOutlet) || !outlet.getFileEncoding().equals(charset)) {
            outlet = new StringOutlet(charset, null);
            getOut().addOutlet(outlet);
        }
    }

    /**
     * Initializes the template given by the concrete implementation of {@link #getTemplate()}.
     */
    protected void initTemplate() {
        setOut(new StringOutput());
        XpandExecutionContextImpl context = createXpandContext();
        threadLocalXpandContext.set(context);
        JavaBeansMetaModel mm = new JavaBeansMetaModel();
        getXpandContext().registerMetaModel(mm);

        final org.eclipse.xtend.typesystem.Type targetType = getXpandContext().getTypeForName(
                getGeneratorModelNodeClass().getName().replaceAll("\\.", SyntaxConstants.NS_DELIM));
        ArgumentCheck.notNull(targetType);
        final org.eclipse.xtend.typesystem.Type[] paramTypes = new org.eclipse.xtend.typesystem.Type[0];
        setTemplateDefinition(getXpandContext().findDefinition(getTemplate(), targetType, paramTypes));
        ArgumentCheck.notNull(getTemplateDefinition());
    }

    protected XpandExecutionContextImpl createXpandContext() {
        if (DEBUG) {
            return new XpandExecutionContextImpl(getOut(), null);
        } else {
            // TODO maybe we want to instantiate one of these by our own?
            ProgressMonitor progressMonitor = null;
            ExceptionHandler exceptionHandler = null;
            NullEvaluationHandler nullEvaluationHandler = null;
            XpandExecutionContextImpl context = new XpandExecutionContextImpl(getGeneratorModelContext()
                    .getResourceManager(), getOut(), null, null, progressMonitor, exceptionHandler,
                    nullEvaluationHandler, null);
            return context;
        }
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
        getTemplateDefinition().evaluate((XpandExecutionContext)getXpandContext().cloneWithoutVariables(),
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

    public XpandExecutionContextImpl getXpandContext() {
        return threadLocalXpandContext.get();
    }

    public GeneratorModelContext getGeneratorModelContext() {
        return generatorModelContext;
    }

    public StringOutput getOut() {
        return threadLocalOut.get();
    }

    public void setOut(StringOutput out) {
        threadLocalOut.set(out);
    }

    public XpandDefinition getTemplateDefinition() {
        return threadLocalTemplateDefinition.get();
    }

    public void setTemplateDefinition(XpandDefinition templateDefinition) {
        threadLocalTemplateDefinition.set(templateDefinition);
    }

    @Override
    public boolean isGeneratsArtifactsFor(IIpsSrcFile ipsSrcFile) {
        return super.isGeneratsArtifactsFor(ipsSrcFile);
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {
        if (getTemplateDefinition() == null) {
            initTemplate();
        }
        getOut().addOutlet(new NullOutlet());
        // TODO NullImportHandler??
        generatorModelContext.setImportHandler(new ImportHandler(""));

        try {
            IIpsObject ipsObject = getSupportedIpsObject(ipsObjectPartContainer);
            if (ipsObject == null) {
                return;
            }
            evaluateTemplate(ipsObject);

            // At the moment only one java type per generator is supported. Multiple types are only
            // generated for adjustments implementing formulas
            List<IType> generatedJavaTypes = getGeneratedJavaTypes(ipsObject);
            if (generatedJavaTypes.size() > 1) {
                throw new RuntimeException("more than one " + generatedJavaTypes);
            }
            IType javaType = generatedJavaTypes.get(0);

            Set<AbstractGeneratorModelNode> allModelNodes = modelService.getAllModelNodes(ipsObjectPartContainer);
            for (AbstractGeneratorModelNode generatorModelNode : allModelNodes) {
                javaElements.addAll(generatorModelNode.getGeneratedJavaElements(javaType));
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception while parsing template for " + ipsObjectPartContainer, e);
        }
    }

    /**
     * Getting the IPS object that is supported by this builder.
     * <p>
     * This method is called to get the generated artifacts for an ipsObjectPartContainer. The given
     * {@link IIpsObject} {@link IIpsObjectPartContainer} may not be of a supported type. For
     * example a policy component attribute has generated artifacts in the product component. Hence
     * we need to parse the policy and the product component template.
     * 
     * @param ipsObjectPartContainer A {@link IIpsObjectPartContainer} for which we want to get the
     *            {@link IIpsObject} that is supported by this builder
     * @return The {@link IIpsObject} that is supported by this builder.
     */
    protected abstract IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer);

}
