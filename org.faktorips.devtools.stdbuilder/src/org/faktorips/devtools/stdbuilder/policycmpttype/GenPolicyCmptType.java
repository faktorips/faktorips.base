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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.builder.DefaultJavaGeneratorForIpsPart;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

public class GenPolicyCmptType {

    private IPolicyCmptType policyCmptType;
    private DefaultJavaSourceFileBuilder builder;
    
    private Map generatorsByPart = new HashMap();
    private List genAttributes = new ArrayList();
    private List genAssociations = new ArrayList();
    private List genValidationRules = new ArrayList();
    private List genMethods =new ArrayList();

    /**
     * @param policyCmptType
     * @param builder
     */
    public GenPolicyCmptType(IPolicyCmptType policyCmptType, DefaultJavaSourceFileBuilder builder) {
        super();
        ArgumentCheck.notNull(policyCmptType, this);
        ArgumentCheck.notNull(builder, this);
        this.policyCmptType = policyCmptType;
        this.builder = builder;
    }

    private void createGeneratorsForMethods() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenAttribute.class);
        IPolicyCmptType type = policyCmptType;
        IMethod[] methods = type.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].isValid()) {
                GenMethod generator = new GenMethod(methods[i], builder, stringsSet);
                if (generator!=null) {
                    genMethods.add(generator);
                    generatorsByPart.put(methods[i], generator);
                }
            }
        }
    }

    private void createGeneratorsForValidationRules() throws CoreException {
        LocalizedStringsSet stringsSet = new LocalizedStringsSet(GenValidationRule.class);
        IPolicyCmptType type = policyCmptType;
        IValidationRule[] validationRules = type.getRules();
        for (int i = 0; i < validationRules.length; i++) {
            if (validationRules[i].isValid()) {
                GenValidationRule generator = new GenValidationRule(validationRules[i], builder,  stringsSet);
                if (generator!=null) {
                    genValidationRules.add(generator);
                    generatorsByPart.put(validationRules[i], generator);
                }
            }
        }
    }

    public GenMethod getGenerator(IMethod a) {
        return (GenMethod)generatorsByPart.get(a);
    }

    public GenMethod getGenerator(IValidationRule a) {
        return (GenMethod)generatorsByPart.get(a);
    }

    public DefaultJavaGeneratorForIpsPart getGenerator(IIpsObjectPartContainer part) {
        return (DefaultJavaGeneratorForIpsPart)generatorsByPart.get(part);
    }

}
