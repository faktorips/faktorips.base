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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.internal.xpand2.model.XpandDefinition;
import org.eclipse.internal.xtend.expression.ast.SyntaxElement;
import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.xpand2.XpandExecutionContext;
import org.eclipse.xpand2.XpandExecutionContextImpl;
import org.eclipse.xpand2.output.Outlet;
import org.eclipse.xtend.expression.EvaluationException;
import org.eclipse.xtend.expression.ExceptionHandler;
import org.eclipse.xtend.expression.ExecutionContext;
import org.eclipse.xtend.expression.NullEvaluationHandler;
import org.eclipse.xtend.expression.Variable;
import org.eclipse.xtend.type.impl.java.JavaBeansMetaModel;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XClass;
import org.faktorips.devtools.stdbuilder.xpand.nullout.NullOutlet;
import org.faktorips.devtools.stdbuilder.xpand.stringout.StringOutlet;
import org.faktorips.devtools.stdbuilder.xpand.stringout.StringOutput;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.XtendBuilder;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * An abstract implementation to use XPAND templates for code generation. The implementation only
 * needs to provide the name of the template and the corresponding generator model.
 * <p>
 * 
 * @deprecated Since 3.22 we now use Xtend to generate templates for code generation. Use
 *             {@link XtendBuilder} instead and refactor your templates for Xtend.
 * @see XtendBuilder
 */
@Deprecated
public abstract class XpandBuilder<T extends XClass> extends JavaSourceFileBuilder {

    /**
     * If this debug switch is set to true we reload the template with every build!
     */
    public static final boolean DEBUG = false;

    private final IJavaClassNameProvider javaClassNameProvider;

    private final ThreadLocal<XpandDefinition> threadLocalTemplateDefinition = new ThreadLocal<XpandDefinition>();

    private final ThreadLocal<XpandExecutionContextImpl> threadLocalXpandContext = new ThreadLocal<XpandExecutionContextImpl>();

    private final ThreadLocal<StringOutput> threadLocalOut = new ThreadLocal<StringOutput>();

    private final ThreadLocal<ClassLoader> threadLocalOldClassLoader = new ThreadLocal<ClassLoader>();

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
        javaClassNameProvider = XClass
                .createJavaClassNamingProvider(modelContext.isGeneratePublishedInterfaces(builderSet.getIpsProject()));
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
        setCorrectClassLoader();
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

    private void setCorrectClassLoader() {
        Thread current = Thread.currentThread();
        setOldClassLoader(current.getContextClassLoader());
        current.setContextClassLoader(StdBuilderPlugin.getDefault().getContextFinder());
    }

    @Override
    public void afterBuildProcess(IIpsProject project, int buildKind) throws CoreException {
        try {
            super.afterBuildProcess(project, buildKind);
        } finally {
            resetOldClassLoader();
        }
    }

    private ClassLoader getOldClassLoader() {
        return threadLocalOldClassLoader.get();
    }

    private void setOldClassLoader(ClassLoader classLoader) {
        threadLocalOldClassLoader.set(classLoader);
    }

    private void resetOldClassLoader() {
        Thread.currentThread().setContextClassLoader(getOldClassLoader());
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
     * Initializes the template given by the concrete implementation of {@link #getTemplate()}.
     */
    protected void initTemplate() {
        setOut(new StringOutput());
        XpandExecutionContextImpl context = createXpandContext();
        threadLocalXpandContext.set(context);
        JavaBeansMetaModel mm = new JavaBeansMetaModel();
        getXpandContext().registerMetaModel(mm);

        final org.eclipse.xtend.typesystem.Type targetType = getXpandContext()
                .getTypeForName(getGeneratorModelNodeClass().getName().replaceAll("\\.", SyntaxConstants.NS_DELIM));
        ArgumentCheck.notNull(targetType);
        final org.eclipse.xtend.typesystem.Type[] paramTypes = new org.eclipse.xtend.typesystem.Type[0];
        setTemplateDefinition(getXpandContext().findDefinition(getTemplate(), targetType, paramTypes));
        ArgumentCheck.notNull(getTemplateDefinition());
    }

    protected XpandExecutionContextImpl createXpandContext() {
        ProgressMonitor progressMonitor = null;
        ExceptionHandler exceptionHandler = new ExceptionHandler() {
            @Override
            public void handleRuntimeException(RuntimeException ex,
                    SyntaxElement element,
                    ExecutionContext ctx,
                    Map<String, Object> additionalContextInfo) {
                if (DEBUG) {
                    ex.printStackTrace();
                }
                if (getIpsObject() != null) {
                    addToBuildStatus(new Status(IStatus.ERROR, StdBuilderPlugin.PLUGIN_ID,
                            "Error while parsing code generation template.", ex));
                }
            }
        };
        NullEvaluationHandler nullEvaluationHandler = new NullEvaluationHandler() {

            @Override
            public Object handleNullEvaluation(SyntaxElement element, ExecutionContext ctx) {
                if (DEBUG) {
                    new NullPointerException().printStackTrace();
                }
                if (getIpsObject() != null) {
                    addToBuildStatus(new Status(IStatus.ERROR, StdBuilderPlugin.PLUGIN_ID,
                            "Nullpointer in code generation at statement " + element,
                            new EvaluationException("null evaluation", element, ctx)));
                }
                return "null";
            }
        };
        XpandExecutionContextImpl context = new XpandExecutionContextImpl(
                getGeneratorModelContext().getResourceManager(), getOut(), null, getGlobalVars(), progressMonitor,
                exceptionHandler, nullEvaluationHandler, null);
        return context;
    }

    protected Map<String, Variable> getGlobalVars() {
        return new HashMap<String, Variable>();
    }

    /**
     * Calls the template evaluation and provides the generated code.
     * 
     * @return the generated java source code
     */
    @Override
    protected String generate() throws CoreException {
        if (getGeneratorModelRoot(getIpsObject()).isValidForCodeGeneration()) {
            StringOutlet outlet = (StringOutlet)getOut().getOutlet(null);
            try {
                evaluateTemplate(getIpsObject());
                return outlet.getContent(getRelativeJavaFile(getIpsSrcFile()));
            } finally {
                outlet.clear();
            }
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

    protected T getGeneratorModelRoot(IIpsObject ipsObject) {
        T xClass = getModelService().getModelNode(ipsObject, getGeneratorModelNodeClass(), getGeneratorModelContext());
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
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {
        getGeneratedArtifacts(getSupportedIpsObject(ipsObjectPartContainer), ipsObjectPartContainer, javaElements);
    }

    /**
     * Get the generated artifacts by evaluating the template using the given {@link IIpsObject}.
     * The generated artifacts are collected by the specified {@link IIpsObjectPartContainer}. The
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
        if (getTemplateDefinition() == null) {
            initTemplate();
        }

        getOut().addOutlet(new NullOutlet());
        generatorModelContext.resetContext(null);

        evaluateTemplate(ipsObject);

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

    /**
     * Returns true if this builder is generating artifacts for the specified
     * {@link IIpsObjectPartContainer}.
     * <p>
     * For example a product component builder may generate artifacts for a product configured
     * policy component type attribute.
     * <p>
     * It is not strictly necessary that there are really generated artifacts for this
     * {@link IIpsObjectPartContainer} because this may depend on very much circumstances. But it is
     * strictly necessary that {@link #getSupportedIpsObject(IIpsObjectPartContainer)} does not
     * return null if this method returns <code>true</code>.
     */
    public abstract boolean isGenerateingArtifactsFor(IIpsObjectPartContainer ipsObjectPartContainer);

    /**
     * Returns the {@link IIpsObject} that is supported by this builder for a
     * {@link IIpsObjectPartContainer} for which this builder seems to generate artifacts.
     * <p>
     * For example the {@link IIpsObjectPartContainer} may be a policy attribute and this builder is
     * responsible to build product component generations. If the policy attribute is configured by
     * a product component, then this builder needs to generate some artifacts for this policy
     * attribute. This method would return the corresponding product component type so this (product
     * component) builder could parse the templates and collect possible generated artifacts.
     * 
     * @param ipsObjectPartContainer The {@link IIpsObjectPartContainer} for which we want to get
     *            the {@link IIpsObject} which is supported by this builder
     * @return The {@link IIpsObject} that is supported by this builder
     */
    protected abstract IIpsObject getSupportedIpsObject(IIpsObjectPartContainer ipsObjectPartContainer);
}
