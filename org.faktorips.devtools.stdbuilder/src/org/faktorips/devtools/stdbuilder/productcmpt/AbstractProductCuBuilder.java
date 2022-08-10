/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.BuilderHelper;
import org.faktorips.devtools.model.builder.TypeSection;
import org.faktorips.devtools.model.builder.java.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.model.builder.java.JavaSourceFileBuilder;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.datatype.util.LocalizedStringsSet;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Abstract class to generates the compilation unit that represents the product component or product
 * component generation.
 * 
 */
public abstract class AbstractProductCuBuilder<T extends IPropertyValueContainer> extends DefaultJavaSourceFileBuilder {

    // property key for the constructor's Javadoc.
    public static final String CONSTRUCTOR_JAVADOC = "CONSTRUCTOR_JAVADOC"; //$NON-NLS-1$

    private MultiStatus buildStatus;

    private Class<?> cuBuilderClazz;

    private T propertyValueContainer;

    public AbstractProductCuBuilder(StandardBuilderSet builderSet, Class<?> clazz) {
        super(builderSet, new LocalizedStringsSet(clazz));
        this.cuBuilderClazz = clazz;
    }

    @CheckForNull
    public MultiStatus getBuildStatus() {
        return buildStatus;
    }

    /**
     * We need the {@link StandardBuilderSet} for formula compilation {@inheritDoc}
     */
    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) {
        super.beforeBuild(ipsSrcFile, status);
        buildStatus = status;
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {
        // no IJavaElement generated here
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

    @Override
    protected void generateCodeForJavatype() {
        if (getPropertyValueContainer() == null) {
            addToBuildStatus(new IpsStatus("The product component needs to be set for this " + cuBuilderClazz)); //$NON-NLS-1$
            return;
        }

        TypeSection mainSection = getMainTypeSection();
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setUnqualifiedName(getUnqualifiedClassName());
        mainSection.setSuperClass(getSuperClassQualifiedClassName(getPropertyValueContainer()));

        buildConstructor(mainSection.getConstructorBuilder());
        List<IFormula> formulas = getFormulas();
        for (final IFormula formula : formulas) {
            if (isGenerateFormula(formula)) {
                generateMethodForFormula(formula, mainSection.getMethodBuilder());
            }
        }
    }

    public void callBuildProcess(T propertyValueContainer, MultiStatus buildStatus) {
        IIpsSrcFile ipsSrcFile = getVirtualIpsSrcFile(propertyValueContainer);
        setPropertyValueContainer(propertyValueContainer);
        beforeBuild(ipsSrcFile, buildStatus);
        if (GeneratorConfig.forIpsSrcFile(ipsSrcFile).getFormulaCompiling().isCompileToSubclass()) {
            build(ipsSrcFile);
        }
        afterBuild(ipsSrcFile);
    }

    /**
     * Returns the prefix that is common to the Java source file for all generations.
     */
    public String getJavaSrcFilePrefix(IIpsSrcFile file) {
        return file.getIpsProject().getProductCmptNamingStrategy()
                .getJavaClassIdentifier(getUnchangedJavaSrcFilePrefix(file));
    }

    /**
     * Returns the prefix that is common to the Java source file for all generations before the
     * project's naming strategy is applied to replace characters that aren't allowed in Java class
     * names.
     */
    protected String getUnchangedJavaSrcFilePrefix(IIpsSrcFile file) {
        return file.getQualifiedNameType().getUnqualifiedName();
    }

    public String getQualifiedClassName(T propertyValueContainer) {
        IIpsSrcFile file = getVirtualIpsSrcFile(propertyValueContainer);
        return getQualifiedClassName(file);
    }

    private boolean isGenerateFormula(final IFormula formula) {
        try {
            if (!formula.isValid(getIpsProject())) {
                return false;
            }
        } catch (IpsException e) {
            StdBuilderPlugin.log(e);
            return false;
        }
        if (formula.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Generates the method to compute a value as specified by a formula configuration element and
     */
    private void generateMethodForFormula(IFormula formula, JavaCodeFragmentBuilder builder) {
        generateMethodForFormula(formula, builder, true);
    }

    private void generateMethodForFormula(IFormula formula,
            JavaCodeFragmentBuilder builder,
            boolean addOverrideAnnotationIfNecessary) {
        IProductCmptTypeMethod method = formula.findFormulaSignature(getIpsProject());
        if (method.validate(getIpsProject()).containsErrorMsg()) {
            return;
        }

        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        if (addOverrideAnnotationIfNecessary) {
            // if the formula is also compiled to XML we have a standard implementation of this
            // method
            builder.annotationLn(JavaSourceFileBuilder.ANNOTATION_OVERRIDE);
        }

        generateSignatureForModelMethod(method, builder);

        builder.openBracket();
        builder.append("try {"); //$NON-NLS-1$
        builder.append("return "); //$NON-NLS-1$
        builder.append(ExpressionBuilderHelper.compileFormulaToJava(formula, method, buildStatus));
        builder.appendln(";"); //$NON-NLS-1$
        builder.append("} catch (Exception e) {"); //$NON-NLS-1$
        builder.appendClassName(StringBuilder.class);
        builder.append(" parameterValues=new StringBuilder();"); //$NON-NLS-1$
        // in formula tests the input will not printed in case of an exception
        // because the input is stored in the formula test
        IParameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                builder.append("parameterValues.append(\", \");"); //$NON-NLS-1$
            }
            builder.append("parameterValues.append(\"" + parameters[i].getName() + "=\");"); //$NON-NLS-1$ //$NON-NLS-2$
            ValueDatatype valuetype = getIpsProject().findValueDatatype(parameters[i].getDatatype());
            if (valuetype != null && valuetype.isPrimitive()) {
                // optimization: we search for value types only as only those can be primitives!
                builder.append("parameterValues.append(" + parameters[i].getName() + ");"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                builder.append("parameterValues.append(" + parameters[i].getName() + " == null ? \"null\" : " //$NON-NLS-1$ //$NON-NLS-2$
                        + parameters[i].getName() + ".toString());"); //$NON-NLS-1$
            }
        }
        builder.append("throw new "); //$NON-NLS-1$
        builder.appendClassName(FormulaExecutionException.class);
        builder.append("(toString(), "); //$NON-NLS-1$
        builder.appendQuoted(StringEscapeUtils.escapeJava(formula.getExpression()));
        builder.appendln(", parameterValues.toString(), e);"); //$NON-NLS-1$
        builder.appendln("}"); //$NON-NLS-1$

        builder.closeBracket();
    }

    private void generateSignatureForModelMethod(IProductCmptTypeMethod method,
            JavaCodeFragmentBuilder methodsBuilder) {

        IParameter[] parameters = method.getParameters();
        int modifier = method.getJavaModifier();
        boolean resolveTypesToPublishedInterface = method.getModifier().isPublished();
        String returnClass = StdBuilderHelper.transformDatatypeToJavaClassName(method.getDatatype(),
                resolveTypesToPublishedInterface, getBuilderSet(), method.getIpsProject());

        String[] parameterNames = null;
        parameterNames = BuilderHelper.extractParameterNames(parameters);
        String[] parameterTypes = StdBuilderHelper.transformParameterTypesToJavaClassNames(parameters,
                resolveTypesToPublishedInterface, getBuilderSet(), method.getIpsProject());
        String[] parameterInSignatur = parameterNames;
        String[] parameterTypesInSignatur = parameterTypes;
        parameterInSignatur = parameterNames;
        parameterTypesInSignatur = parameterTypes;

        String methodName = method.getName();
        // extend the method signature with the given parameter names
        methodsBuilder.signature(modifier, returnClass, methodName, parameterInSignatur, parameterTypesInSignatur,
                true);

        methodsBuilder.append(" throws ");
        methodsBuilder.appendClassName(FormulaExecutionException.class);
    }

    protected void setPropertyValueContainer(T propertyValueContainer) {
        ArgumentCheck.notNull(propertyValueContainer);
        this.propertyValueContainer = propertyValueContainer;
    }

    protected T getPropertyValueContainer() {
        return propertyValueContainer;
    }

    private List<IFormula> getFormulas() {
        return getPropertyValueContainer().getPropertyValues(IFormula.class);
    }

    protected abstract String getSuperClassQualifiedClassName(T container);

    protected abstract void buildConstructor(JavaCodeFragmentBuilder constructorBuilder);

    protected abstract IIpsSrcFile getVirtualIpsSrcFile(T propertyContainer);

    /**
     * Returns <code>true</code> if there is at least one formula that has an entered expression.
     * Returns <code>false</code> if there is no formula or if every formula has no entered
     * expression.
     * 
     * @param container The product component or product component generation that may contain the
     *            formulas
     * @return <code>true</code> for at least one available formula
     */
    public boolean isContainingAvailableFormula(IPropertyValueContainer container) {
        for (IFormula formula : container.getPropertyValues(IFormula.class)) {
            if (!formula.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    protected IIpsObject findProductCmptType(IIpsProject ipsProject) {
        return getPropertyValueContainer().findProductCmptType(ipsProject);
    }

    /**
     * Returns the qualified name of the class where the formula or method is implemented.
     * 
     * @param container The product component or product component generation that may contain the
     *            formula or method
     */
    public String getImplementationClass(T container) {
        if (isContainingAvailableFormula(container)
                && GeneratorConfig.forIpsObject(container.getIpsObject()).getFormulaCompiling().isCompileToSubclass()) {
            return getQualifiedClassName(container);
        } else {
            return getSuperClassQualifiedClassName(container);
        }
    }

}
