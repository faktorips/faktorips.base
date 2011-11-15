/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.fl.CompilationResult;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.message.MessageList;

/**
 * A class that provides the
 * {@link ExpressionBuilderHelper#compileFormulaToJava(IExpression, IProductCmptTypeMethod, boolean, MultiStatus)}
 * method for {@link IIpsArtefactBuilder}s handling {@link IExpression}s.
 * 
 * @author schwering
 */
public class ExpressionBuilderHelper {

    /**
     * Compiles the given formula to java code, logging errors to the given status object.
     * 
     * @param formula the {@link IExpression} to be compiled
     * @param formulaSignature the signature of the formula
     * @param formulaTest if {@code true} the formula will be compiled for usage inside a formula
     *            test, in formula tests all type parameters will be replaced by their value, which
     *            is defined inside the formula test.
     * @param buildStatus a {@link MultiStatus} that receives {@link IpsStatus} messages when
     *            compilation produces an error
     * @return the Java code compiled from the formula
     */
    public static JavaCodeFragment compileFormulaToJava(IExpression formula,
            IProductCmptTypeMethod formulaSignature,
            boolean formulaTest,
            MultiStatus buildStatus) {
        String expression = formula.getExpression();
        if (StringUtils.isEmpty(expression)) {
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.append("null"); //$NON-NLS-1$
            return fragment;
        }
        try {
            IIpsProject ipsProject = formula.getIpsProject();
            ExtendedExprCompiler compiler = formula.newExprCompiler(ipsProject, formulaTest);
            compiler.setRuntimeRepositoryExpression(new JavaCodeFragment(MethodNames.GET_THIS_REPOSITORY + "()"));
            CompilationResult result = compiler.compile(expression);
            if (result.successfull()) {
                Datatype attributeDatatype = formulaSignature.findDatatype(ipsProject);
                if (result.getDatatype().equals(attributeDatatype)) {
                    return result.getCodeFragment();
                }
                ConversionCodeGenerator conversion = compiler.getConversionCodeGenerator();
                JavaCodeFragment convertedFrag = conversion.getConversionCode(result.getDatatype(), attributeDatatype,
                        result.getCodeFragment());
                if (convertedFrag == null) {
                    return new JavaCodeFragment("// Unable to convert the expression \"" + //$NON-NLS-1$
                            result.getCodeFragment().getSourcecode() + "\" of datatype " + result.getDatatype() + //$NON-NLS-1$
                            " to the datatype " + attributeDatatype); //$NON-NLS-1$
                }
                return convertedFrag;
            }
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.appendln("// The expression compiler reported the following errors while compiling the formula:"); //$NON-NLS-1$
            fragment.append("// "); //$NON-NLS-1$
            fragment.appendln(expression);
            MessageList messages = result.getMessages();
            for (int i = 0; i < messages.size(); i++) {
                fragment.append("// "); //$NON-NLS-1$
                fragment.append(messages.getMessage(i).getText());
            }
            return fragment;
        } catch (CoreException e) {
            buildStatus.add(new IpsStatus("Error compiling formula " + formula.getExpression() //$NON-NLS-1$
                    + " of config element " + formula + ".", e)); //$NON-NLS-1$ //$NON-NLS-2$
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.appendln("// An exception occurred while compiling the following formula:"); //$NON-NLS-1$
            fragment.append("// "); //$NON-NLS-1$
            fragment.appendln(expression);
            fragment.append("// See the error log for details."); //$NON-NLS-1$
            return fragment;
        }
    }
}