/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere. Alle Rechte vorbehalten. Dieses Programm und
 * alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, etc.) dürfen nur unter
 * den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung
 * Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann. Mitwirkende: Faktor Zehn GmbH -
 * initial API and implementation
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenConstantAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenDerivedAttribute;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class GenPolicyCmptType extends GenType{


    private Map generatorsByPart = new HashMap();
    private List genAttributes = new ArrayList();
    private List genAssociations = new ArrayList();
    private List genValidationRules = new ArrayList();
    private List genMethods = new ArrayList();

    /**
     * @param policyCmptType
     * @param builder
     * @throws CoreException
     */
    public GenPolicyCmptType(IPolicyCmptType policyCmptType, StandardBuilderSet builderSet, LocalizedStringsSet stringsSet) throws CoreException {
        super(policyCmptType, builderSet, stringsSet);
        ArgumentCheck.notNull(policyCmptType, this);
        ArgumentCheck.notNull(builderSet, this);

        createGeneratorsForAttributes();
        createGeneratorsForMethods();
        createGeneratorsForValidationRules();
    }

    public IPolicyCmptType getPolicyCmptType(){
        return (IPolicyCmptType) getType();
    }
    
    private void createGeneratorsForMethods() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenAttribute.class);
        IMethod[] methods = getPolicyCmptType().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].isValid()) {
                GenMethod generator = new GenMethod(this, methods[i], stringsSet);
                if (generator != null) {
                    genMethods.add(generator);
                    generatorsByPart.put(methods[i], generator);
                }
            }
        }
    }

    private void createGeneratorsForValidationRules() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenValidationRule.class);
        IValidationRule[] validationRules = getPolicyCmptType().getRules();
        for (int i = 0; i < validationRules.length; i++) {
            if (validationRules[i].isValid()) {
                GenValidationRule generator = new GenValidationRule(this, validationRules[i], stringsSet);
                if (generator != null) {
                    genValidationRules.add(generator);
                    generatorsByPart.put(validationRules[i], generator);
                }
            }
        }
    }

    private void createGeneratorsForAttributes() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenAttribute.class);
        IPolicyCmptTypeAttribute[] attrs = getPolicyCmptType().getPolicyCmptTypeAttributes();
        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i].isValid()) {
                GenAttribute generator = createGenerator(attrs[i], stringsSet);
                if (generator != null) {
                    genAttributes.add(generator);
                    generatorsByPart.put(attrs[i], generator);
                }
            }
        }
    }

    public Iterator getGenAttributes(){
        return genAttributes.iterator();
    }
    
    private GenAttribute createGenerator(IPolicyCmptTypeAttribute a, LocalizedStringsSet stringsSet)
            throws CoreException {
        if (a.isDerived()) {
            return new GenDerivedAttribute(this, a, stringsSet);
        }
        if (a.isChangeable()) {
            return new GenChangeableAttribute(this, a, stringsSet);
        }
        return new GenConstantAttribute(this, a, stringsSet);
    }

    public GenMethod getGenerator(IMethod a) {
        return (GenMethod)generatorsByPart.get(a);
    }

    public GenAttribute getGenerator(IPolicyCmptTypeAttribute a) throws CoreException {
        GenAttribute generator = (GenAttribute)generatorsByPart.get(a);
        if(generator != null){
            return generator;
        }
        //if the attributes policy component type is not this type but one in the super type hierarchy of this type 
        if(!a.getPolicyCmptType().equals(getPolicyCmptType())){
            GenPolicyCmptType superTypeGenerator = getBuilderSet().getGenerator(a.getPolicyCmptType());
            return superTypeGenerator.getGenerator(a);
            
        }
        return null;
    }

    public GenValidationRule getGenerator(IValidationRule a) {
        return (GenValidationRule)generatorsByPart.get(a);
    }

    /**
     * Returns the unqualified name for Java class generated by this builder for the given ips
     * source file.
     * 
     * @param ipsSrcFile the ips source file
     * @return the qualified class name
     * @throws CoreException is delegated from calls to other methods
     */
    public String getUnqualifiedClassName(boolean forInterface) throws CoreException {
        if(forInterface){
            return getBuilderSet().getJavaNamingConvention().getPublishedInterfaceName(getPolicyCmptType().getName());
            
        }
        return StringUtil.getFilenameWithoutExtension(getPolicyCmptType().getName());
    }

}
