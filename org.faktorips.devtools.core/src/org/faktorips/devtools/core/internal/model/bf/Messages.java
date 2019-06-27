/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.bf;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.bf.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ActionBFE_bfDoesNotExist;
    public static String ActionBFE_bfMustBeSpecified;
    public static String ActionBFE_bfName;

    public static String BFElement_nameNotAllowed;
    public static String BFElement_nameNotSpecified;
    public static String BFElement_nameNotValid;

    public static String BusinessFunction_duplicateNames;
    public static String BusinessFunction_elementNotConnectedWithEnd;
    public static String BusinessFunction_elementNotConnectedWithStart;
    public static String BusinessFunction_elementOnlyOnce;
    public static String BusinessFunction_endMissing;
    public static String BusinessFunction_startMissing;

    public static String ControlFlow_duplicateControlFlowValue;
    public static String ControlFlow_valueMustBeSpecified;
    public static String ControlFlow_valueNotValid;

    public static String DecisionBFE_datatypeDoesNotExist;
    public static String DecisionBFE_DatatypeMustBeNotPrimitive;
    public static String DecisionBFE_datatypeNotSpecified;

    public static String MethodCallBFE_methodDoesNotExistOnParameter;
    public static String MethodCallBFE_methodMustBeSpecified;
    public static String MethodCallBFE_methodName;
    public static String MethodCallBFE_parameterMissing;
    public static String MethodCallBFE_parameterNotSpecified;
    public static String MethodCallBFE_parameterNoType;

    public static String ParameterBFE_datatypeNotSpecified;
    public static String ParameterBFE_duplicateParameter;
    public static String ParameterBFE_parameterOrDatatypeMissing;

}
