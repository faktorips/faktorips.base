/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.bf;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.bf.messages"; //$NON-NLS-1$
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
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
