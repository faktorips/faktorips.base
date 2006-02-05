package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.IpsObject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;

/**
 * 
 * @author Jan Ortmann
 */
public class StdBuilderHelper {

    public final static String[] transformParameterTypesToJavaClassNames(
            Parameter[] params,
            IIpsProject ipsProject,
            PolicyCmptImplClassBuilder policyCmptImplBuilder) throws CoreException {
        
        String[] javaClasses = new String[params.length];
        for (int i=0; i<params.length; i++) {
            Datatype paramDatatype = ipsProject.findDatatype(params[i].getDatatype());
            if (paramDatatype instanceof PolicyCmptType) {
                javaClasses[i] = policyCmptImplBuilder.getQualifiedClassName((IpsObject)paramDatatype);
            } else {
                javaClasses[i] = paramDatatype.getJavaClassName();
            }
        }
        return javaClasses;
    }

    private StdBuilderHelper() {
    }

}
