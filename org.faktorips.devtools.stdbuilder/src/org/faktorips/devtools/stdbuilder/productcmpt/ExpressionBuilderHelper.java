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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IBaseMethod;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.JavaExprCompiler;
import org.faktorips.runtime.MessageList;

/**
 * A class that provides the
 * {@link ExpressionBuilderHelper#compileFormulaToJava(IExpression, IBaseMethod, MultiStatus)}
 * method for {@link IIpsArtefactBuilder}s handling {@link IExpression}s.
 * 
 * @author schwering
 */
public class ExpressionBuilderHelper {

    private ExpressionBuilderHelper() {
        // Utility class not to be instantiated
    }

    /**
     * Compiles the given formula to java code, logging errors to the given status object.
     * 
     * @param formula the {@link IExpression} to be compiled
     * @param formulaSignature the signature of the formula
     * @param buildStatus a {@link MultiStatus} that receives {@link IpsStatus} messages when
     *            compilation produces an error
     * @return the Java code compiled from the formula
     */
    public static JavaCodeFragment compileFormulaToJava(IExpression formula,
            IBaseMethod formulaSignature,
            MultiStatus buildStatus) {
        String expression = formula.getExpression();
        if (StringUtils.isEmpty(expression)) {
            JavaCodeFragment fragment = new JavaCodeFragment();
            fragment.append("null"); //$NON-NLS-1$
            return fragment;
        }
        try {
            IIpsProject ipsProject = formula.getIpsProject();
            JavaExprCompiler compiler = formula.newExprCompiler(ipsProject);
            CompilationResult<JavaCodeFragment> result = compiler.compile(expression);
            if (result.successfull()) {
                Datatype attributeDatatype = formulaSignature.findDatatype(ipsProject);
                if (result.getDatatype().equals(attributeDatatype)) {
                    return result.getCodeFragment();
                }
                ConversionCodeGenerator<JavaCodeFragment> conversion = compiler.getConversionCodeGenerator();
                JavaCodeFragment convertedFrag = conversion.getConversionCode(result.getDatatype(), attributeDatatype,
                        result.getCodeFragment());
                if (convertedFrag == null) {
                    return new JavaCodeFragment("// Unable to convert the expression \"" //$NON-NLS-1$
                            + result.getCodeFragment().getSourcecode() + "\" of datatype " + result.getDatatype() //$NON-NLS-1$
                            + " to the datatype " + attributeDatatype); //$NON-NLS-1$
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
        } catch (CoreRuntimeException e) {
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