/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.Signature;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * 
 * @author Jan Ortmann
 */
public final class StdBuilderHelper {

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
     * Shortcut for <tt>unresolvedParam(class_.getSimpleName())</tt>.
     * 
     * @see #unresolvedParam(String)
     */
    public static String unresolvedParam(Class<?> class_) {
        return unresolvedParam(class_.getSimpleName());
    }

    /**
     * Returns the JDT type signature for resolved types.
     * <p>
     * Use this method to create type signatures for types that are not found in the source code or
     * via import (e.g. if a type is written fully qualified in the source code such as
     * <tt>java.util.Calendar</tt>).
     */
    public static String resolvedParam(String qualifiedTypeName) {
        return Signature.createTypeSignature(qualifiedTypeName, true);
    }

    /**
     * Returns the JDT type signature for the given {@link Datatype}.
     * <p>
     * The JDT type signature is a special representation of a datatype so it can be used to
     * identify {@link IJavaElement}s. For example, the datatype <tt>int</tt> in JDT is represented
     * as just <tt>I</tt>.
     * <p>
     * When dealing with Faktor-IPS datatypes it is also possible that the datatype should be
     * resolved to the published interface. In this case depending on the used naming convention the
     * name of the datatype is modified, e.g. from <tt>Policy</tt> to <tt>IPolicy</tt>.
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
        try {
            qualifiedJavaClassName = transformDatatypeToJavaClassName(datatype.getQualifiedName(),
                    resolveToPublishedInterface, builderSet, ipsProject);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        String unqualifiedJavaClassName = QNameUtil.getUnqualifiedName(qualifiedJavaClassName);
        return Signature.createTypeSignature(unqualifiedJavaClassName, false);
    }

    public static String transformDatatypeToJavaClassName(String qualifiedDatatypeName,
            boolean resolveToPublishedInterface,
            StandardBuilderSet builderSet,
            IIpsProject ipsProject) throws CoreException {

        Datatype datatype = ipsProject.findDatatype(qualifiedDatatypeName);
        if (datatype.isVoid()) {
            return "void";
        }
        if (datatype instanceof ValueDatatype) {
            DatatypeHelper helper = ipsProject.findDatatypeHelper(qualifiedDatatypeName);
            if (helper != null) {
                return helper.getJavaClassName();
            }
            throw new RuntimeException("Can't get datatype helper for datatype " + qualifiedDatatypeName);
        }
        if (datatype instanceof PolicyCmptType) {
            if (resolveToPublishedInterface) {
                return builderSet.getGenerator((IPolicyCmptType)datatype).getQualifiedName(true);
            }
            return builderSet.getGenerator((IPolicyCmptType)datatype).getQualifiedName(false);
        } else if (datatype instanceof ProductCmptType) {
            if (resolveToPublishedInterface) {
                return builderSet.getGenerator((IProductCmptType)datatype).getQualifiedClassNameForProductCmptTypeGen(
                        true);
            }
            return builderSet.getGenerator((IProductCmptType)datatype)
                    .getQualifiedClassNameForProductCmptTypeGen(false);
        }
        throw new RuntimeException("Can't get Java class name for datatype " + qualifiedDatatypeName);
    }

    public static String[] transformParameterTypesToJavaClassNames(IParameter[] params,
            boolean resolveToPublishedInterface,
            StandardBuilderSet builderSet,
            IIpsProject ipsProject) throws CoreException {

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
            return project.getDatatypeHelper((((ValueDatatype)helper.getDatatype()).getWrapperType()));
        }
        return helper;
    }

    private StdBuilderHelper() {
        // prohibit instantiation
    }

}
