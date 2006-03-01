
package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

public abstract class RelationImplBuilder {
    
    private IPolicyCmptType pcType;
    
    protected abstract String getNumOfMethod(IRelation rel) throws CoreException;
    protected abstract String getGetterMethod(IRelation rel) throws CoreException;
    protected abstract String getGetAllMethod(IRelation rel) throws CoreException;
    protected abstract String getField(IRelation rel) throws CoreException;
    
    protected abstract boolean is1ToMany(IRelation rel) throws CoreException;

    public RelationImplBuilder(IPolicyCmptType pcType) {
        this.pcType = pcType; 
    }
    
    protected JavaCodeFragment getContainerRelationGetAllMethodBody(IRelation container,
            String classname,
            List subRelations) throws CoreException {
        /*
         * elements = getPNCCoveragesRole(); 
         * for (int i = 0; i < elements.length; i++) { 
         *     result[i+count] = elements[i]; 
         *     count++;
         * } 
         * return (Coverage[]) list.toArray(new Coverage[list.size()]);
         */
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("int num = 0;");
        for (int i = 0; i < subRelations.size(); i++) {
            code.appendln();
            IRelation subrel = (IRelation)subRelations.get(i);
            code.append("num += ");
            code.append(getNumOfMethod(subrel));
            code.append("();");
        }        
        code.appendClassName(classname);
        code.append("[] result = new ");       
        code.appendClassName(classname);
        code.append("[num];");       
        code.appendClassName(classname);   
        code.append("[] elements;");
        code.append("num = 0;");
        for (int i = 0; i < subRelations.size(); i++) {
            IRelation subrel = (IRelation)subRelations.get(i);
            if (is1ToMany(subrel)) {
                code.append("elements = ");
                code.append(getGetAllMethod(subrel));
                code.append("();");
                code.append("for(int i=0;i<elements.length;i++) { result[num] = elements[i]; num++;}");
            } else {
                code.append("if(");
                code.append(getNumOfMethod(subrel));
                code.append("() > 0) {");
                code.append("result[num] = ");
                code.append(getGetterMethod(subrel));
                code.append("(); num++;}");    
            }
        }
        code.append("return result;");
        return code;
    }
    
    /**
     * @param container
     * @param target
     * @param subRelations
     * @param body
     * @throws CoreException
     */
    protected JavaCodeFragment getContainerRelationGetterMethodBody(IRelation container, List subRelations)
            throws CoreException {
        /*
         * if(getNumOfGlasvertrag() > 0) { return getGlasvertrag(); } if(getNumOfHausratvertrag() > 0) { return
         * getHausratvertrag(); } return null;
         */
        JavaCodeFragment body = new JavaCodeFragment();
        for (int i = 0; i < subRelations.size(); i++) {
            body.append("if(");
            IRelation subrel = (IRelation)subRelations.get(i);
            if (subrel.is1ToMany()) {
                throw new CoreException(new IpsStatus("Subrelation \"zu n\" unzulaessig, falls Superrelation \"zu 1\""));
            }
            body.append(getNumOfMethod(subrel));
            body.append("() > 0)");
            body.appendOpenBracket();
            body.append("return ");
            body.append(getGetterMethod(subrel));
            body.append("();");
            body.appendCloseBracket();
        }
        body.append("return null;");
        return body;
    }
    
    protected JavaCodeFragment getContainerRelationGetNumOfMethodBody(IRelation containerRelation, 
            List subRelations) throws CoreException{
        /*
         * int num = 0; 
         * num += getNumOfHausratvertrag(); 
         * num += getNumOfGlasvertrag();
         * return num;
         */
        JavaCodeFragment body = new JavaCodeFragment(); 
        body.append("int num = 0;");
        for (int i = 0; i < subRelations.size(); i++) {
            body.appendln();
            IRelation subrel = (IRelation)subRelations.get(i);
            body.append("num += ");
            body.append(getNumOfMethod(subrel));
            body.append("();");
        }
        body.append("return num;");
        return body;
    }

}
