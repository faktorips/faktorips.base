/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenImplClassBuilder;

/**
 * 
 * @author Jan Ortmann
 */
public class StdBuilderHelper {

    public final static String transformDatatypeToJavaClassName(
            String datatypeName, 
            boolean resolveToPublishedInterface,
            IIpsProject ipsProject,
            PolicyCmptImplClassBuilder policyCmptImplBuilder,
            ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder) throws CoreException {
        
        Datatype datatype = ipsProject.findDatatype(datatypeName);
        if (datatype.isVoid()) {
            return "void";
        }
        if (datatype instanceof ValueDatatype) {
            DatatypeHelper helper = ipsProject.findDatatypeHelper(datatypeName);
            if (helper!=null) {
                return helper.getJavaClassName();
            }
            throw new RuntimeException("Can't get datatype helper for datatype " + datatypeName);
        }
        if (datatype instanceof PolicyCmptType) {
            if (resolveToPublishedInterface) {
                return policyCmptImplBuilder.getInterfaceBuilder().getQualifiedClassName((IIpsObject)datatype);
            }
            return policyCmptImplBuilder.getQualifiedClassName((IpsObject)datatype);
        } else if(datatype instanceof ProductCmptType){
            if (resolveToPublishedInterface) {
                return productCmptGenImplClassBuilder.getInterfaceBuilder().getQualifiedClassName((IpsObject)datatype);
            }
            return productCmptGenImplClassBuilder.getQualifiedClassName((IpsObject)datatype);
        }
        throw new RuntimeException("Can't get Java class name for datatype " + datatypeName);
    }
    
    public final static String[] transformParameterTypesToJavaClassNames(
            IParameter[] params,
            boolean resolveToPublishedInterface,
            IIpsProject ipsProject,
            PolicyCmptImplClassBuilder policyCmptImplBuilder,
            ProductCmptGenImplClassBuilder productCmptGenImplClassBuilder) throws CoreException {
        
        String[] javaClasses = new String[params.length];
        for (int i=0; i<params.length; i++) {
            javaClasses[i] = transformDatatypeToJavaClassName(params[i].getDatatype(), resolveToPublishedInterface, ipsProject, policyCmptImplBuilder, productCmptGenImplClassBuilder);
        }
        return javaClasses;
    }
    
    /**
     * This method is supposed to be used for the generation of methods which deal with the range or enum value set for a datatype.
     * Since for primitive datatypes the range and enum value set classes of the non primitive wrapper types are used. Therefor
     * this method checks if the provided DatatypeHelper is based on a primitive datatype. If so the according wrapper datatype is retrieved
     * from the IpsProject and returned. If the datatype is not primitive the provided datatype will be returned.  
     */
    public final static DatatypeHelper getDatatypeHelperForValueSet(IIpsProject project, DatatypeHelper helper){
        if(helper.getDatatype().isPrimitive()){
            return project.getDatatypeHelper((((ValueDatatype)helper.getDatatype()).getWrapperType()));
        }
        return helper;
    }

    
    private StdBuilderHelper() {
    }

}
