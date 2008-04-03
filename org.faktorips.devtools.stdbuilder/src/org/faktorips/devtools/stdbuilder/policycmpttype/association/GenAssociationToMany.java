/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.BasePolicyCmptTypeBuilder;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class GenAssociationToMany extends GenAssociation {
    
    public GenAssociationToMany(IPolicyCmptTypeAssociation association, BasePolicyCmptTypeBuilder builder,
            LocalizedStringsSet stringsSet, boolean generateImplementation) throws CoreException {
        super(association, builder, stringsSet, generateImplementation);
    }
    
    /**
     * {@inheritDoc}
     */
    protected String computeFieldName() {
        return getJavaNamingConvention().getMemberVarName(association.getTargetRolePlural());
    }
    
    /**
     * {@inheritDoc}
     */
    public String getMethodNameAddOrSetObject() {
        return getMethodNameAddObject();
    }

    /**
     * Returns the name of the paramter for the method that tests if an object is references in a multi-value association,
     * e.g. objectToTest
     */
    public String getParamNameForContainsObject() {
        return getLocalizedText("PARAM_OBJECT_TO_TEST_NAME", association.getTargetRoleSingular());
    }

    /**
     * Code sample:
     * <pre>
     * public int getNumOfCoverages()
     * </pre>
     */
    protected void generateSignatureGetNumOfRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String methodName = getMethodNameGetNumOfRefObjects();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "int", methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the number of referenced objects,
     * e.g. getNumOfCoverages()
     */
    protected String getMethodNameGetNumOfRefObjects() {
        return getLocalizedText("METHOD_GET_NUM_OF_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }

    /**
     * Code sample:
     * <pre>
     * public boolean containsCoverage(ICoverage objectToTest)
     * </pre>
     */
    protected void generateSignatureContainsObject() throws CoreException {
        String methodName = getMethodNameContainsObject();
        String paramName = getParamNameForContainsObject();
        getMethodBuilder().signature(java.lang.reflect.Modifier.PUBLIC, "boolean", methodName, new String[]{paramName}, new String[]{targetInterfaceName});
    }
    
    /**
     * Returns the name of the method returning the number of referenced objects,
     * e.g. getNumOfCoverages()
     */
    protected String getMethodNameContainsObject() {
        return getLocalizedText("METHOD_CONTAINS_OBJECT_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * Code sample:
     * <pre>
     * public ICoverage[] getCoverages()
     * </pre>
     */
    public void generateSignatureGetAllRefObjects() throws CoreException {
        String methodName = getMethodNameGetAllRefObjects();
        String returnType = targetInterfaceName + "[]";
        getMethodBuilder().signature(java.lang.reflect.Modifier.PUBLIC, returnType, methodName, new String[]{}, new String[]{});
    }
    
    /**
     * Returns the name of the method returning the referenced objects,
     * e.g. getCoverages()
     */
    protected String getMethodNameGetAllRefObjects() {
        return getLocalizedText("METHOD_GET_ALL_REF_OBJECTS_NAME", StringUtils.capitalize(association.getTargetRolePlural()));
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index)
     * </pre>
     */
    protected void generateSignatureGetRefObjectAtIndex(JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        appendLocalizedJavaDoc("METHOD_GET_REF_OBJECT_BY_INDEX", association.getTargetRoleSingular(), methodBuilder);
        methodBuilder.signature(java.lang.reflect.Modifier.PUBLIC, targetInterfaceName, getMethodNameGetRefObjectAtIndex(), 
                    new String[]{"index"}, new String[]{Integer.TYPE.getName()});
    }
    
    /**
     * Returns the name of the method that returns a reference object at a specified index.
     */
    public String getMethodNameGetRefObjectAtIndex(){
        //TODO extend JavaNamingConvensions for association accessor an mutator methods 
        return "get" + association.getTargetRoleSingular();
    }

    /**
     * Code sample:
     * <pre>
     * public void addCoverage(ICoverage objectToAdd)
     * </pre>
     */
    public void generateSignatureAddObject() throws CoreException {
        String methodName = getMethodNameAddObject();
        String paramName = getParamNameForAddObject();
        getMethodBuilder().signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[]{paramName}, new String[]{targetInterfaceName});
    }
    
    /**
     * Returns the name of the method adding an object to a multi-value association,
     * e.g. getCoverage()
     */
    public String getMethodNameAddObject() {
        return getLocalizedText("METHOD_ADD_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method adding an object to a multi-value association,
     * e.g. objectToAdd
     */
    public String getParamNameForAddObject() {
        return getLocalizedText("PARAM_OBJECT_TO_ADD_NAME", association.getTargetRoleSingular());
    }
    
    /**
     * Code sample:
     * <pre>
     * public void removeCoverage(ICoverage objectToRemove)
     * </pre>
     */
    public void generateSignatureRemoveObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        
        String methodName = getMethodNameRemoveObject();
        String paramName = getParamNameForRemoveObject();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName, new String[]{paramName}, new String[]{targetInterfaceName});
    }
    
    /**
     * Returns the name of the method removing an object from a multi-value association,
     * e.g. removeCoverage()
     */
    public String getMethodNameRemoveObject() {
        return getLocalizedText("METHOD_REMOVE_OBJECT_NAME", association.getTargetRoleSingular());
    }

    /**
     * Returns the name of the paramter for the method removing an object from a multi-value association,
     * e.g. objectToRemove
     */
    public String getParamNameForRemoveObject() {
        return getLocalizedText("PARAM_OBJECT_TO_REMOVE_NAME", association.getTargetRoleSingular());
    }
    
    
}
