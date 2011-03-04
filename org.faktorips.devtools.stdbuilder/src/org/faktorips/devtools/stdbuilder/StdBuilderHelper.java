/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

/**
 * 
 * @author Jan Ortmann
 */
public class StdBuilderHelper {

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
    public final static String transformDatatypeToJdtTypeSignature(Datatype datatype,
            boolean resolveToPublishedInterface) {

        if (resolveToPublishedInterface) {
            // StdBuilderHelper. TODO AW
        }
        return Signature.createTypeSignature(datatype.getName(), false);
    }

    public final static String transformDatatypeToJavaClassName(String datatypeName,
            boolean resolveToPublishedInterface,
            StandardBuilderSet builderSet,
            IIpsProject ipsProject) throws CoreException {

        Datatype datatype = ipsProject.findDatatype(datatypeName);
        if (datatype.isVoid()) {
            return "void";
        }
        if (datatype instanceof ValueDatatype) {
            DatatypeHelper helper = ipsProject.findDatatypeHelper(datatypeName);
            if (helper != null) {
                return helper.getJavaClassName();
            }
            throw new RuntimeException("Can't get datatype helper for datatype " + datatypeName);
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
        throw new RuntimeException("Can't get Java class name for datatype " + datatypeName);
    }

    public final static String[] transformParameterTypesToJavaClassNames(IParameter[] params,
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
    public final static DatatypeHelper getDatatypeHelperForValueSet(IIpsProject project, DatatypeHelper helper) {
        if (helper.getDatatype().isPrimitive()) {
            return project.getDatatypeHelper((((ValueDatatype)helper.getDatatype()).getWrapperType()));
        }
        return helper;
    }

    private StdBuilderHelper() {
        // prohibit instantiation
    }

}
