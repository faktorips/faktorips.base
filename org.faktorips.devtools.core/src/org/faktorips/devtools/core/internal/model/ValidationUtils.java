/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;


/**
 * A collection of helper methods for validating model objects.
 * 
 * @author Jan Ortmann
 */
public class ValidationUtils {
    
    /**
     * Tests if the given qualified name identifies a policy component type.
     * If not, it adds an error message to the given message list.
     * 
     * @param objectName the qualified type name to check.
     * @param mandatory Is the reference mandatory. If yes, it is checked that
     * the reference is not an empty string. Otherwise an empty reference is valid.
     * 
     * @param objectName The (qualified) name of the object to check.
     * @param type The type the object to check is of.
     * @param propertyDisplayName The name used to display the value to the user.
     * @param part The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param msgCode The message code to use if a message has to be created.
     * @param list The list of messages to add a new one.
     * 
     * @return true if the reference is valid, otherwise false.
     */
    public final static boolean checkIpsObjectReference(
            String objectName,
            IpsObjectType type,
            String propertyDisplayName,
            IIpsObjectPart part,
            String propertyName,
            String msgCode,
            MessageList list) throws CoreException {
        
    	if (!checkStringPropertyNotEmpty(objectName, propertyDisplayName,part, propertyName, msgCode, list)) {
    		return false;
    	}

    	if (part.getIpsProject().findIpsObject(type, objectName)==null) {
            String text = NLS.bind(Messages.ValidationUtils_msgObjectDoesNotExist, StringUtils.capitalise(propertyDisplayName), objectName);
            list.add(new Message(msgCode, text, Message.ERROR, part, propertyName));
            return false;
        }
        return true;
    }
    
    /**
     * Checks if the given name identifies a datatype.
     * If not, it adds an error message to the given message list.
     * If the datatype is found, it is validated and any messages generated
     * by the datatype validation are added to the given message list.
     * 
     * @param datatypeName the datatype name to check.
     * @param mandatory Is the reference mandatory. If yes, it is checked that
     * the reference is not an empty string. Otherwise an empty reference is valid.
     * @param voidAllowed <code>true</code> to allow void as datatype, <code>false</code>
     * to prohibit void.
     * @param part The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param msgCode The message code to use if a message has to be created.
     * @param list The list of messages to add a new one.
     * 
     * @return the datatype if no error was detected, otherwise null.
     */
    public final static Datatype checkDatatypeReference(
            String datatypeName,
            boolean voidAllowed,
            IIpsObjectPart part,
            String propertyName,
            String msgcode,
            MessageList list) throws CoreException {
        
    	if (!checkStringPropertyNotEmpty(datatypeName, "Datatype", part, propertyName, msgcode, list)) { //$NON-NLS-1$
    		return null;
    	}
        
        Datatype datatype = part.getIpsProject().findDatatype(datatypeName);
        if (datatype==null) {
            String text = NLS.bind(Messages.ValidationUtils_msgDatatypeDoesNotExist, datatypeName); 
            list.add(new Message("", text, Message.ERROR, part, propertyName)); //$NON-NLS-1$
            return null;
        }
        try {
            if (datatype instanceof ValueDatatype) {
                list.add(datatype.validate(), new ObjectProperty(part, propertyName), true);
            }
        } catch (CoreException e) {
            throw e;
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }
        if (datatype.isVoid() && !voidAllowed) {
            String text = Messages.ValidationUtils_msgVoidNotAllowed;
            list.add(new Message("", text, Message.ERROR, part, propertyName)); //$NON-NLS-1$
        }
        return datatype;
        
    }
    
    /**
     * Checks if the given name identifies a datatype.
     * If not, it adds an error message to the given message list.
     * If the datatype is found, it is validated and any messages generated
     * by the datatype validation are added to the given message list.
     * 
     * @param datatypeName the datatype name to check.
     * @param mandatory Is the reference mandatory. If yes, it is checked that
     * the reference is not an empty string. Otherwise an empty reference is valid.
     * @param voidAllowed <code>true</code> to allow void as datatype, <code>false</code>
     * to prohibit void.
     * @param part The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param msgCode The message code to use if a message has to be created.
     * @param list The list of messages to add a new one.
     * 
     * @return the datatype if no error was detected, otherwise null.
     */
    public final static ValueDatatype checkValueDatatypeReference(
            String datatypeName,
            boolean voidAllowed,
            IIpsObjectPart part,
            String propertyName,
            String msgcode,
            MessageList list) throws CoreException {
    	
    	if (!checkStringPropertyNotEmpty(datatypeName, "Datatype", part, propertyName, msgcode, list)) { //$NON-NLS-1$
    		return null;
    	}
        ValueDatatype datatype = part.getIpsProject().findValueDatatype(datatypeName);
        if (datatype==null) {
            String text = NLS.bind(Messages.ValidationUtils_msgDatatypeDoesNotExist, datatypeName); 
            list.add(new Message("", text, Message.ERROR, part, propertyName)); //$NON-NLS-1$
            return null;
        }
        try {
            list.add(datatype.validate(), new ObjectProperty(part, propertyName), true);
        } catch (CoreException e) {
            throw e;
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        }
        if (datatype.isVoid() && !voidAllowed) {
            String text = Messages.ValidationUtils_msgVoidNotAllowed;
            list.add(new Message("", text, Message.ERROR, part, propertyName)); //$NON-NLS-1$
        }
        return datatype;
    }
        
    /**
     * Tests if the given property value is not empty.
     * If it is empty, it adds an error message to the given message list.
     * 
     * @param propertyValue The value to check.
     * @param propertyDisplayName The name used to display the value to the user.
     * @param object The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param msgCode The message code to use if a message has to be created.
     * @param list The list of messages to add a new one.
     * 
     * @return true if the string is not empty, false if the string is empty.
     */
    public final static boolean checkStringPropertyNotEmpty(
            String propertyValue, 
            String propertyDisplayName,
            Object object,
            String propertyName,
            String msgCode,
            MessageList list)
    {
        if (StringUtils.isEmpty(propertyValue)) {
            String text = NLS.bind(Messages.ValidationUtils_msgPropertyMissing, StringUtils.capitalise(propertyDisplayName));
            list.add(new Message(msgCode, text, Message.ERROR, object, propertyName));
            return false;
        }
        return true;
    }
    
    /**
     * Returns an image to represent the given message severity 
     * 
     * @param messageSeverity
     */
    public final static Image getSeverityImage(int messageSeverity) {
        String imageName;
        switch (messageSeverity) {
        	case Message.ERROR: {
        	    imageName = "size8/ErrorMessage.gif"; //$NON-NLS-1$
        	    break;
        	}
        	case Message.WARNING: {
        	    imageName = "size8/WarningMessage.gif"; //$NON-NLS-1$
        	    break;
        	}
        	case Message.INFO: {
        	    imageName = "size8/InfoMessage.gif"; //$NON-NLS-1$
        	    break;
        	}
        	default:
        	    imageName = "size8/NullMessage.gif"; //$NON-NLS-1$
        }
        return IpsPlugin.getDefault().getImage(imageName);
    }

    private ValidationUtils() {
        
    }

}
