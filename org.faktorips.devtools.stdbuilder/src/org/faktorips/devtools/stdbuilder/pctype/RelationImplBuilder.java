
package org.faktorips.devtools.stdbuilder.pctype;

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
    
    private IPolicyCmptType getPcType(){
        return pcType;
    }
    
    protected JavaCodeFragment getContainerRelationGetAllMethodBody(IRelation container,
            String classname,
            IRelation[] subRelations) throws CoreException {
        /*
         * elements = super.getCoveragesRole(); 
         * for (int i = 0; i < elements.length; i++) { 
         *     result[i+count] = elements[i];
         * 	   count++; 
         * } 
         * elements = getPNCCoveragesRole(); 
         * for (int i = 0; i < elements.length; i++) { 
         *     result[i+count] = elements[i]; 
         *     count++;
         * } 
         * return (Coverage[]) list.toArray(new Coverage[list.size()]);
         */
        JavaCodeFragment code = new JavaCodeFragment();
        code.append("int num = 0;");
        if (StringUtils.isNotEmpty(getPcType().getSupertype()) && container.getPolicyCmptType()!= getPcType()) {
            // if the container relation is not defined in this type, it must be defined somewhere up in the
            // supertype hierarchy, so the Java type's supertype must also implement the method
            code.append("num += super.");
            code.append(getNumOfMethod(container));
            code.append("();");
        }
        for (int i = 0; i < subRelations.length; i++) {
            code.appendln();
            IRelation subrel = subRelations[i];
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
        if (StringUtils.isNotEmpty(getPcType().getSupertype()) && container.getPolicyCmptType()!= getPcType()) {
            // if the container relation is not defined in this type, it must be defined somewhere up in the
            // supertype hierarchy, so the Java type's supertype must also implement the method
            code.append("elements = super.");
            code.append(getGetAllMethod(container));
            code.append("();");
            code.append("for(int i=0;i<elements.length;i++) { result[i] = elements[i]; num++;}");
        }
        for (int i = 0; i < subRelations.length; i++) {
            IRelation subrel = subRelations[i];
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
    protected JavaCodeFragment getContainerRelationGetterMethodBody(IRelation container, IRelation[] subRelations)
            throws CoreException {
        /*
         * if(getNumOfGlasvertrag() > 0) { return getGlasvertrag(); } if(getNumOfHausratvertrag() > 0) { return
         * getHausratvertrag(); } return null;
         */
        JavaCodeFragment body = new JavaCodeFragment();
        if (StringUtils.isNotEmpty(getPcType().getSupertype()) && container.getPolicyCmptType()!= getPcType()) {
            // if the container relation is not defined in this type, it must be defined somewhere up in the
            // supertype hierarchy, so the Java type's supertype must also implement the method
            body.append("if(super.");
            body.append(getNumOfMethod(container));
            body.append("() > 0) ");
            body.appendOpenBracket();
            body.append("return super.");
            body.append(getGetterMethod(container));
            body.append("();");
            body.appendCloseBracket();
        }        
        for (int i = 0; i < subRelations.length; i++) {
            body.append("if(");
            IRelation subrel = subRelations[i];
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
            IRelation[] subRelations) throws CoreException{
        /*
         * int num = 0; num+= super.getSuperrelation(); num += getNumOfHausratvertrag(); i = i + getNumOfGlasvertrag();
         * return num;
         */
        JavaCodeFragment body = new JavaCodeFragment(); 
        body.append("int num = 0;");
        if (StringUtils.isNotEmpty(getPcType().getSupertype()) && containerRelation.getPolicyCmptType()!= getPcType()) {
            // if the container relation is not defined in this type, it must be defined somewhere up in the
            // supertype hierarchy, so the Java type's supertype must also implement the method
            body.append("num += super.");
            body.append(getNumOfMethod(containerRelation));
            body.append("();");
        }
        for (int i = 0; i < subRelations.length; i++) {
            body.appendln();
            IRelation subrel = subRelations[i];
            body.append("num += ");
            body.append(getNumOfMethod(subrel));
            body.append("();");
        }
        body.append("return num;");
        return body;
    }

}
