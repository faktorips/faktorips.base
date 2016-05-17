package org.faktorips.devtools.stdbuilder.util;

/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.DatatypeUtil;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.runtime.IRuntimeRepository;

/**
 * Utility for handling {@link DatatypeHelper DatatypeHelpers}.
 */
public class DatatypeHelperUtil {

    protected DatatypeHelperUtil() {
        // prevents default constructor
    }

    /**
     * If the {@link DatatypeHelper} is an {@link EnumTypeDatatypeHelper}, it needs to generate
     * access to the {@link IRuntimeRepository} via the given expression, which regular datatype
     * helpers don't know about. For all other datatypes,
     * {@link DatatypeHelper#newInstanceFromExpression(String)} is called.
     */
    public static JavaCodeFragment getNewInstanceFromExpression(DatatypeHelper datatypeHelper,
            String expression,
            String repoExpression) {
        if (DatatypeUtil.isExtensibleEnumType(datatypeHelper.getDatatype())) {
            EnumTypeDatatypeHelper enumtypeHelper = (EnumTypeDatatypeHelper)datatypeHelper;
            try {
                return enumtypeHelper.getEnumTypeBuilder().getCallGetValueByIdentifierCodeFragment(
                        enumtypeHelper.getEnumType(), expression, new JavaCodeFragment(repoExpression));
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        } else {
            return datatypeHelper.newInstanceFromExpression(expression);
        }

    }
}
