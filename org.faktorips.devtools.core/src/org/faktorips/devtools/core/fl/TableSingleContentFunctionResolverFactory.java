/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.fl;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.internal.fl.TableSingleContentFunctionsResolver;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.fl.FunctionResolver;

public class TableSingleContentFunctionResolverFactory extends
        AbstractProjectRelatedFunctionResolverFactory<JavaCodeFragment> {

    @Override
    public FunctionResolver<JavaCodeFragment> newFunctionResolver(IIpsProject ipsProject, Locale locale) {
        return new TableSingleContentFunctionsResolver(ipsProject);
    }

}
