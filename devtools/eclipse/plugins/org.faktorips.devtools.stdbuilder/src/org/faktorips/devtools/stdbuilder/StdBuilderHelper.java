/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.Signature;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.util.QNameUtil;

/**
 * 
 * @author Jan Ortmann
 */
public final class StdBuilderHelper {

    private StdBuilderHelper() {
        // prohibit instantiation
    }

    /**
     * Returns the JDT signature for a primitive integer parameter.
     */
    public static String intParam() {
        return Signature.SIG_INT;
    }

    /**
     * Returns the JDT signature for a void parameter.
     */
    public static String voidParam() {
        return Signature.SIG_VOID;
    }

    /**
     * Returns the JDT signature for a primitive boolean parameter.
     */
    public static String booleanParam() {
        return Signature.SIG_BOOLEAN;
    }

    /**
     * Returns the JDT signature for a primitive byte parameter.
     */
    public static String byteParam() {
        return Signature.SIG_BYTE;
    }

    /**
     * Returns the JDT signature for a primitive char parameter.
     */
    public static String charParam() {
        return Signature.SIG_CHAR;
    }

    /**
     * Returns the JDT signature for a primitive double parameter.
     */
    public static String doubleParam() {
        return Signature.SIG_DOUBLE;
    }

    /**
     * Returns the JDT signature for a primitive float parameter.
     */
    public static String floatParam() {
        return Signature.SIG_FLOAT;
    }

    /**
     * Returns the JDT signature for a primitive long parameter.
     */
    public static String longParam() {
        return Signature.SIG_LONG;
    }

    /**
     * Returns the JDT signature for a primitive short parameter.
     */
    public static String shortParam() {
        return Signature.SIG_SHORT;
    }

    /**
     * Returns the JDT signature for a {@link String} parameter.
     */
    public static String stringParam() {
        return Signature.createTypeSignature(String.class.getSimpleName(), false);
    }

    /**
     * Returns the JDT type signature for unresolved types.
     * <p>
     * Use this method to create type signatures for types that are found in the source code or via
     * import.
     */
    public static String unresolvedParam(String unqualifiedTypeName) {
        return Signature.createTypeSignature(unqualifiedTypeName, false);
    }

    /**
     * Shortcut for <code>unresolvedParam(clazz.getSimpleName())</code>.
     * 
     * @see #unresolvedParam(String)
     */
    public static String unresolvedParam(Class<?> clazz) {
        return unresolvedParam(clazz.getSimpleName());
    }

    /**
     * Returns the JDT type signature for resolved types.
     * <p>
     * Use this method to create type signatures for types that are not found in the source code or
     * via import (e.g. if a type is written fully qualified in the source code such as
     * <code>java.util.Calendar</code>).
     */
    public static String resolvedParam(String qualifiedTypeName) {
        return Signature.createTypeSignature(qualifiedTypeName, true);
    }

    /**
     * Returns the JDT type signature for the given {@link Datatype}.
     * <p>
     * The JDT type signature is a special representation of a datatype so it can be used to
     * identify {@link IJavaElement}s. For example, the datatype <code>int</code> in JDT is
     * represented as just <code>I</code>.
     * <p>
     * When dealing with Faktor-IPS datatypes it is also possible that the datatype should be
     * resolved to the published interface. In this case depending on the used naming convention the
     * name of the datatype is modified, e.g. from <code>Policy</code> to <code>IPolicy</code>.
     * 
     * @param datatype The {@link Datatype} to get the parameter type signature for
     * @param resolveToPublishedInterface Flag indicating whether the datatype should be resolved to
     *            the published interface type
     * 
     * @see Signature
     */
    public static String transformDatatypeToJdtTypeSignature(Datatype datatype,
            boolean resolveToPublishedInterface,
            StandardBuilderSet builderSet,
            IIpsProject ipsProject) {

        String qualifiedJavaClassName;
        qualifiedJavaClassName = transformDatatypeToJavaClassName(datatype.getQualifiedName(),
                resolveToPublishedInterface, builderSet, ipsProject);
        String unqualifiedJavaClassName = QNameUtil.getUnqualifiedName(qualifiedJavaClassName);
        return Signature.createTypeSignature(unqualifiedJavaClassName, false);
    }

    public static String transformDatatypeToJavaClassName(String qualifiedDatatypeName,
            boolean resolveToPublishedInterface,
            StandardBuilderSet builderSet,
            IIpsProject ipsProject) {
        Datatype datatype = ipsProject.findDatatype(qualifiedDatatypeName);
        if (datatype == null) {
            return "/* Can't find Java class for datatype " + qualifiedDatatypeName + " */";
        }
        if (datatype.isVoid()) {
            return "void";
        }
        return builderSet.getJavaClassName(datatype, resolveToPublishedInterface);
    }

    public static String[] transformParameterTypesToJavaClassNames(IParameter[] params,
            boolean resolveToPublishedInterface,
            StandardBuilderSet builderSet,
            IIpsProject ipsProject) {

        String[] javaClasses = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            javaClasses[i] = transformDatatypeToJavaClassName(params[i].getDatatype(), resolveToPublishedInterface,
                    builderSet, ipsProject);
        }
        return javaClasses;
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
