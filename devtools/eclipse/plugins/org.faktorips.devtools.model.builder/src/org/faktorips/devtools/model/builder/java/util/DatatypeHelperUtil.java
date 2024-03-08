/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.util;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.util.DatatypeUtil;
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
            return enumtypeHelper.getCallGetValueByIdentifierCodeFragment(expression, new JavaCodeFragment(
                    repoExpression));
        } else {
            return datatypeHelper.newInstanceFromExpression(expression);
        }
    }

    /**
     * This method is supposed to be used for the generation of methods which deal with the range or
     * enum value set for a datatype. Since for primitive datatypes the range and enum value set
     * classes of the non primitive wrapper types are used. Therefore this method checks if the
     * provided DatatypeHelper is based on a primitive datatype. If so the according wrapper
     * datatype is retrieved from the IpsProject and returned. If the datatype is not primitive the
     * provided datatype will be returned.
     */
    public static DatatypeHelper getDatatypeHelperForValueSet(IIpsProject project, DatatypeHelper helper) {
        if (helper.getDatatype().isPrimitive()) {
            return project.getDatatypeHelper(((ValueDatatype)helper.getDatatype()).getWrapperType());
        }
        return helper;
    }
}
