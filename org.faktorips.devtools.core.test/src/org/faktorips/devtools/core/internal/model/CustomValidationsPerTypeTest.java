/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptTypeAttribute;
import org.faktorips.devtools.core.internal.model.type.Attribute;
import org.faktorips.devtools.core.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class CustomValidationsPerTypeTest {

    @SuppressWarnings("rawtypes")
    private List<ICustomValidation> allValidations = new ArrayList<ICustomValidation>();
    private ValidationOnAttribute validationOnAttribute = new ValidationOnAttribute();
    private ValidationOnIAttribute validationOnIAttribute = new ValidationOnIAttribute();
    private ValidationOnPolicyCmptType validationOnPolicyCmptType = new ValidationOnPolicyCmptType();
    private ValidationOnIPolicyCmptType validationOnIPolicyCmptType = new ValidationOnIPolicyCmptType();

    @SuppressWarnings("rawtypes")
    @Before
    public void setUp() {
        allValidations = new ArrayList<ICustomValidation>();
        allValidations.add(validationOnAttribute);
        allValidations.add(validationOnIAttribute);
        allValidations.add(validationOnPolicyCmptType);
        allValidations.add(validationOnIPolicyCmptType);
    }

    @Test
    public void getCustomValidations_should_ReturnValidationsRegisteredForTheClassOrInterfaceItself() {
        CustomValidationsPerType validationsPerType = CustomValidationsPerType.createFromList(allValidations);
        Set<ICustomValidation<IAttribute>> validations = validationsPerType.getCustomValidations(IAttribute.class);
        assertEquals(1, validations.size());
        assertTrue(validations.contains(validationOnIAttribute));
    }

    @Test
    public void getCustomValidations_should_ReturnValidationsRegisteredForAnImplementedInterface() {
        CustomValidationsPerType validationsPerType = CustomValidationsPerType.createFromList(allValidations);
        Set<ICustomValidation<Attribute>> validations = validationsPerType.getCustomValidations(Attribute.class);
        assertTrue(validations.contains(validationOnIAttribute));
    }

    @Test
    public void getCustomValidations_should_ReturnValidationsRegisteredForAnyInterfaceOrClassInTheHierarchy() {
        CustomValidationsPerType validationsPerType = CustomValidationsPerType.createFromList(allValidations);
        Set<ICustomValidation<PolicyCmptTypeAttribute>> validations = validationsPerType
                .getCustomValidations(PolicyCmptTypeAttribute.class);
        assertEquals(4, validations.size());
        assertTrue(validations.contains(validationOnAttribute));
        assertTrue(validations.contains(validationOnIAttribute));
        assertTrue(validations.contains(validationOnPolicyCmptType));
        assertTrue(validations.contains(validationOnIPolicyCmptType));
    }

    class ValidationOnAttribute implements ICustomValidation<Attribute> {

        @Override
        public Class<Attribute> getExtendedClass() {
            return Attribute.class;
        }

        @Override
        public MessageList validate(Attribute objectToValidate, IIpsProject ipsProject) throws CoreException {
            return null;
        }

    }

    class ValidationOnIAttribute implements ICustomValidation<IAttribute> {

        @Override
        public Class<IAttribute> getExtendedClass() {
            return IAttribute.class;
        }

        @Override
        public MessageList validate(IAttribute objectToValidate, IIpsProject ipsProject) throws CoreException {
            return null;
        }

    }

    class ValidationOnPolicyCmptType implements ICustomValidation<PolicyCmptTypeAttribute> {

        @Override
        public Class<PolicyCmptTypeAttribute> getExtendedClass() {
            return PolicyCmptTypeAttribute.class;
        }

        @Override
        public MessageList validate(PolicyCmptTypeAttribute objectToValidate, IIpsProject ipsProject)
                throws CoreException {
            return null;
        }

    }

    class ValidationOnIPolicyCmptType implements ICustomValidation<IPolicyCmptTypeAttribute> {

        @Override
        public Class<IPolicyCmptTypeAttribute> getExtendedClass() {
            return IPolicyCmptTypeAttribute.class;
        }

        @Override
        public MessageList validate(IPolicyCmptTypeAttribute objectToValidate, IIpsProject ipsProject)
                throws CoreException {
            return null;
        }

    }

}
