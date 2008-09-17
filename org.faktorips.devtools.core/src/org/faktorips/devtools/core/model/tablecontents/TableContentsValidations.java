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

package org.faktorips.devtools.core.model.tablecontents;

import org.faktorips.devtools.core.internal.model.tablecontents.Messages;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.ObjectProperty;

/**
 * A class that contains validations of the model class table contents which are also used in the
 * creation wizard where the model object doesn't exist at the point of validation.
 * 
 * @author Peter Erzberger
 */
public class TableContentsValidations {

    /**
     * Validates if the name of the table structure and the table contents are not the same when the table structure is 
     * of the type enumeration.
     * 
     * @param tableStructure the table structure the table contents relates to
     * @param contentsUnqualifiedName the unqualified name of the table contents
     * @param thisTableContents the table contents object. Can be <code>null</code>
     * 
     * @return a message object if the validation fails if not <code>null</code> will be returned
     */
    public static Message validateNameOfStructureAndContentsNotTheSameWhenEnum(ITableStructure tableStructure, String contentsUnqualifiedName, ITableContents thisTableContents){
        if (!tableStructure.getIpsProject().getIpsArtefactBuilderSet().isTableBasedEnumValidationRequired()) {
        	return null;
        }
        
    	if(tableStructure.isModelEnumType() && tableStructure.getName().equals(contentsUnqualifiedName)){
            return new Message(ITableContents.MSGCODE_NAME_OF_STRUCTURE_AND_CONTENTS_NOT_THE_SAME_WHEN_ENUM, 
                    Messages.TableContents_msgNameStructureAndContentsNotSameWhenEnum, Message.ERROR, 
                    thisTableContents != null ? new ObjectProperty[]{new ObjectProperty(thisTableContents, null)} : new ObjectProperty[0], 
                            ITableContents.PROPERTY_NAME);
        }
        return null;
    }
    

}
