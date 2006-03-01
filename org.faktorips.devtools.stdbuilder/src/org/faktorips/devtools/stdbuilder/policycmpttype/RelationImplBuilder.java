
package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

public abstract class RelationImplBuilder {
    
    protected abstract String getNumOfMethod(IRelation rel) throws CoreException;
    protected abstract String getGetterMethod(IRelation rel) throws CoreException;
    protected abstract String getGetAllMethod(IRelation rel) throws CoreException;
    protected abstract String getField(IRelation rel) throws CoreException;
    
    protected abstract boolean is1ToMany(IRelation rel) throws CoreException;

    public RelationImplBuilder(IPolicyCmptType pcType) {
    }
    
}
