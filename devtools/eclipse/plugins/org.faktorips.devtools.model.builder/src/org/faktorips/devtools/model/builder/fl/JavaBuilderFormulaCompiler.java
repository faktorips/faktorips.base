/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.builder.fl;

import java.util.List;

import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.model.builder.IJavaBuilderSet;
import org.faktorips.devtools.model.builder.Messages;
import org.faktorips.devtools.model.builder.xmodel.GeneratorConfig;
import org.faktorips.devtools.model.internal.productcmpt.IFormulaCompiler;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.runtime.internal.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An {@link IFormulaCompiler} that compiles Faktor-IPS formulas to Java code, runnable via the
 * {@code GroovyFormulaEvaluator}.
 */
public class JavaBuilderFormulaCompiler implements IFormulaCompiler {

    @Override
    public void compileFormulas(IPropertyValueContainer propertyValueContainer, Document document, Element node) {
        IIpsArtefactBuilderSet ipsArtefactBuilderSet = propertyValueContainer.getIpsProject()
                .getIpsArtefactBuilderSet();
        if (!(ipsArtefactBuilderSet instanceof IJavaBuilderSet)) {
            return;
        }
        ExpressionXMLBuilderHelper expressionXMLBuilderHelper = new ExpressionXMLBuilderHelper(
                (IJavaBuilderSet)ipsArtefactBuilderSet);
        MultiStatus buildStatus = new MultiStatus(IpsModelActivator.PLUGIN_ID, 0, Messages.IpsBuilder_msgBuildResults,
                null);

        if (GeneratorConfig.forIpsObject(propertyValueContainer.getIpsObject()).getFormulaCompiling()
                .isCompileToXml()) {
            List<IFormula> formulas = propertyValueContainer.getPropertyValues(IFormula.class);
            List<Element> formulaElements = XmlUtil.getElements(node, IFormula.TAG_NAME);
            expressionXMLBuilderHelper.addCompiledFormulaExpressions(document, formulas, formulaElements, buildStatus);
        }
    }
}
