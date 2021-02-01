/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.formula.groovy;

import java.util.Map;

import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;

/**
 * This is a {@link IFormulaEvaluatorFactory} creating {@link GroovyFormulaEvaluator}s.
 * 
 * @author dirmeier
 */
public class GroovyFormulaEvaluatorFactory implements IFormulaEvaluatorFactory {

    @Override
    public IFormulaEvaluator createFormulaEvaluator(Object object, Map<String, String> nameToCompiledExpressionMap) {
        return new GroovyFormulaEvaluator(object, nameToCompiledExpressionMap);
    }

}
