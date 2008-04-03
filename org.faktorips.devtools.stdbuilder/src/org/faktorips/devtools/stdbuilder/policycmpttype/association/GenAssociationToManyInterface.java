/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Generator for the published interface aspects of associations. 
 * 
 * @author Jan Ortmann
 */
public class GenAssociationToManyInterface extends GenAssociationToMany {

    public GenAssociationToManyInterface(IPolicyCmptTypeAssociation association, PolicyCmptInterfaceBuilder builder,
            LocalizedStringsSet stringsSet) throws CoreException {
        
        super(association, builder, stringsSet, false);
    }

    /**
     * {@inheritDoc}
     */
    public void generateMethods(JavaCodeFragmentBuilder builder) throws CoreException {
        generateMethodGetNumOfRefObjects(builder);
        generateMethodContainsObject(builder);
        generateMethodGetAllRefObjects(builder);
        if (!association.isDerivedUnion()) {
            generateMethodAddObject(builder);
            generateMethodRemoveObject(builder);
            generateNewChildMethodsIfApplicable(builder);
            generateMethodGetRefObjectAtIndex(builder);
        }
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public int getNumOfCoverages();
     * </pre>
     */
    protected void generateMethodGetNumOfRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_NUM_OF", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureGetNumOfRefObjects(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public boolean containsCoverage(ICoverage objectToTest);
     * </pre>
     */
    protected void generateMethodContainsObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_CONTAINS_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureContainsObject();
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public ICoverage[] getCoverages();
     * </pre>
     */
    protected void generateMethodGetAllRefObjects(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_GET_ALL_REF_OBJECTS", association.getTargetRolePlural(), methodsBuilder);
        generateSignatureGetAllRefObjects();
        methodsBuilder.appendln(";");
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public IMotorCoverage getMotorCoverage(int index);
     * </pre>
     */
    protected void generateMethodGetRefObjectAtIndex(JavaCodeFragmentBuilder methodBuilder) throws CoreException{
        generateSignatureGetRefObjectAtIndex(methodBuilder);
        methodBuilder.append(';');
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void addCoverage(ICoverage objectToAdd);
     * </pre>
     */
    protected void generateMethodAddObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        appendLocalizedJavaDoc("METHOD_ADD_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureAddObject();
        methodsBuilder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void removeCoverage(ICoverage objectToRemove);
     * </pre>
     */
    protected void generateMethodRemoveObject(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        appendLocalizedJavaDoc("METHOD_REMOVE_OBJECT", association.getTargetRoleSingular(), methodsBuilder);
        generateSignatureRemoveObject(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    

}
