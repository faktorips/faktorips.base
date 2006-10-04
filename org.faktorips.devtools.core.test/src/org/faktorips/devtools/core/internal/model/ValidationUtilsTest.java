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

package org.faktorips.devtools.core.internal.model;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IValidationMsgCodes;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.util.message.MessageList;

public class ValidationUtilsTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
    }
    
    public void testCheckValue() throws Exception {
        MessageList ml = new MessageList();
        
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "policyCmptType");
        IAttribute attribute = policyCmptType.newAttribute();
        attribute.setName("attribute");
        ValidationUtils.checkValue("Integer", "1", attribute, IAttribute.PROPERTY_DEFAULT_VALUE, ml);
        assertNull(ml.getMessageByCode(IValidationMsgCodes.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
        
        ValidationUtils.checkValue("Integer", "x", attribute, IAttribute.PROPERTY_DEFAULT_VALUE, ml);
        assertNotNull(ml.getMessageByCode(IValidationMsgCodes.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        ValidationUtils.checkValue("x", "x", attribute, IAttribute.PROPERTY_DEFAULT_VALUE, ml);
        assertNotNull(ml.getMessageByCode(IValidationMsgCodes.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND));
    }
}
