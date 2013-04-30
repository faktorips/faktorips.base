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

package org.faktorips.devtools.formulalibrary.builder.xpand.model;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.devtools.formulalibrary.model.IFormulaLibrary;
import org.faktorips.devtools.stdbuilder.productcmpt.ExpressionBuilderHelper;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.MethodParameter;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XParameter;

/**
 * Represents the methods in the {@link IFormulaLibrary}
 * 
 * @author frank
 */
public class XFormulaMethod extends AbstractGeneratorModelNode {

    public XFormulaMethod(IBaseMethod method, GeneratorModelContext context, ModelService modelService) {
        super(method, context, modelService);
    }

    @Override
    public IBaseMethod getIpsObjectPartContainer() {
        return (IBaseMethod)super.getIpsObjectPartContainer();
    }

    /**
     * Returns the method
     */
    public IBaseMethod getMethod() {
        return getIpsObjectPartContainer();
    }

    /**
     * Returns the JavaModifier
     */
    public String getJavaModifier() {
        int javaModifier = Modifier.PUBLIC | Modifier.STATIC;
        return Modifier.toString(javaModifier);
    }

    /**
     * Returns the name of the method
     */
    public String getMethodName() {
        return getMethod().getName();
    }

    /**
     * Returns the classname of the datatype
     */
    public String getJavaClassName() {
        Datatype datatype = getDatatype();
        return getJavaClassName(datatype);
    }

    protected String getJavaClassName(Datatype datatype) {
        return getJavaClassName(datatype, true, isGeneratePublishedInterfaces());
    }

    /**
     * Returns the parameters
     */
    public List<MethodParameter> getMethodParameters() {
        List<MethodParameter> result = new ArrayList<MethodParameter>();
        for (XFormulaParameter param : getFormulaParameters()) {
            result.add(new MethodParameter(param.getJavaClassName(), param.getName()));
        }
        return result;
    }

    /**
     * Returns the return-datatype
     */
    public Datatype getDatatype() {
        try {
            return getMethod().findDatatype(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns <code>true</code> if the Returntype is void. <code>False</code> otherwise
     */
    public boolean isReturnVoid() {
        return getDatatype().isVoid();
    }

    private IExpression getExpression() {
        return ((IFormulaFunction)getMethod().getParent()).getExpression();
    }

    /**
     * Returns the compiled formula expression
     */
    public String getCompiledExpression() {
        IExpression formula = getExpression();
        IBaseMethod method = getMethod();
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        MultiStatus buildStatus = new MultiStatus(IpsPlugin.PLUGIN_ID, 0, "Build", null); //$NON-NLS-1$
        JavaCodeFragment compileFormulaToJava = ExpressionBuilderHelper.compileFormulaToJava(formula, method,
                buildStatus);
        builder.append(compileFormulaToJava.getSourcecode());
        addImport(compileFormulaToJava.getImportDeclaration());
        return builder.toString();
    }

    /**
     * Returns the parameters
     */
    public Set<XFormulaParameter> getFormulaParameters() {
        if (isCached(XParameter.class)) {
            return getCachedObjects(XFormulaParameter.class);
        } else {
            Set<XFormulaParameter> nodesForParts = initNodesForParts(Arrays.asList(getMethod().getParameters()),
                    XFormulaParameter.class);
            putToCache(nodesForParts);
            return nodesForParts;
        }
    }

    /**
     * Returns the Formula Expression as it is entered by the user
     */
    public String getFormulaExpression() {
        IExpression formula = getExpression();
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.appendQuoted(StringEscapeUtils.escapeJava(formula.getExpression()));
        return builder.toString();
    }
}
