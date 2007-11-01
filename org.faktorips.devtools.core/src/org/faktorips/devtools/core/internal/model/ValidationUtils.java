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
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IValidationMsgCodesForInvalidValues;
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
            IIpsObjectPartContainer part,
            String propertyName,
            String msgCode,
            MessageList list) throws CoreException {
        
    	if (!checkStringPropertyNotEmpty(objectName, propertyDisplayName,part, propertyName, msgCode, list)) {
    		return false;
    	}

        // due to better performance findIpsSrcFile is used instead of findIpsObject,
        // because only the existence of the ips object needs to be checked here, 
        // not the initialization (which is implicitly done by calling findIpsObject)
    	if (part.getIpsProject().findIpsSrcFile(type, objectName)==null) {
            String text = NLS.bind(Messages.ValidationUtils_msgObjectDoesNotExist, StringUtils.capitalise(propertyDisplayName), objectName);
            list.add(new Message(msgCode, text, Message.ERROR, part, propertyName));
            return false;
        }
        return true;
    }
    
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
    public final static IIpsObject checkIpsObjectReference2(
            String objectName,
            IpsObjectType type,
            String propertyDisplayName,
            IIpsObjectPartContainer part,
            String propertyName,
            String msgCode,
            MessageList list,
            IIpsProject ipsProject) throws CoreException {
        
        if (!checkStringPropertyNotEmpty(objectName, propertyDisplayName,part, propertyName, msgCode, list)) {
            return null;
        }
        IIpsSrcFile srcFile = ipsProject.findIpsSrcFile(type, objectName);
        if (srcFile==null) {
            String text = NLS.bind(Messages.ValidationUtils_msgObjectDoesNotExist, StringUtils.capitalise(propertyDisplayName), objectName);
            list.add(new Message(msgCode, text, Message.ERROR, part, propertyName));
            return null;
        }
        return srcFile.getIpsObject();
    }

    /**
     * Checks if the given name identifies a datatype.
     * If not, it adds an error message to the given message list.
     * If the datatype is found, it is validated and any messages generated
     * by the datatype validation are added to the given message list.
     * 
     * @param datatypeName  The datatype name to check.
     * @param mandatory     Is the reference mandatory. If yes, it is checked that
     *                      the reference is not an empty string. Otherwise an empty reference is valid.
     * @param voidAllowed   <code>true</code> to allow void as datatype, <code>false</code> to prohibit void.
     * @param part          The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName  The (technical) name of the property used if a message has to be created.
     * @param msgCode       The message code to use if a message has to be created.
     * @param list          The list of messages to add a new one.
     * @param ipsProject    The ips project which ips object path is used to search the datatype.
     * 
     * @return the datatype if no error was detected, otherwise null.
     */
    public final static Datatype checkDatatypeReference(
            String datatypeName,
            boolean voidAllowed,
            IIpsObjectPart part,
            String propertyName,
            String msgcode,
            MessageList list,
            IIpsProject ipsProject) throws CoreException {
        
    	if (!checkStringPropertyNotEmpty(datatypeName, "Datatype", part, propertyName, msgcode, list)) { //$NON-NLS-1$
    		return null;
    	}
        
        Datatype datatype = ipsProject.findDatatype(datatypeName);
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
     * Checks if a given value is an "instance" of the indicated value datatype. Adds a warning
     * to the given list, if the datatype either can't be found or is invalid. Adds an error
     * message if the value datatype is ok, but the value is not an instance of it.
     * 
     * @param valueDatatype the qualified value datatype name which will be used to validate the given value
     * @param value the value which will be validated with the given datatype
     * @param part The part the checked reference belongs to (used if a message has to be created).
     * @param propertyName The (technical) name of the property used if a message has to be created.
     * @param list The list of messages to add a new one.
     * 
     * @return <code>true</code> if the value is valid otherwise <code>false</code>.
     * 
     * @throws CoreException
     */
    public final static boolean checkValue (
            String valueDatatype,
            String value,
            IIpsObjectPart part,
            String propertyName,
            MessageList list) throws CoreException {

    	ValueDatatype datatype = part.getIpsProject().findValueDatatype(valueDatatype);
    	if (datatype==null) {
    		String text = Messages.ValidationUtils_VALUE_VALUEDATATYPE_NOT_FOUND;
			Message msg = new Message(IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND, 
					text, Message.WARNING, part, propertyName); //$NON-NLS-1$
			list.add(msg);
			return false;
    	}
    	try {
    		if (datatype.validate().containsErrorMsg()) {
        		String text = NLS.bind(Messages.ValidationUtils_VALUEDATATYPE_INVALID, datatype.getName()); //$NON-NLS-2$
    			Message msg = new Message(IValidationMsgCodesForInvalidValues.MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_IS_INVALID, text, Message.WARNING, part, propertyName); //$NON-NLS-1$
    			list.add(msg);
    			return false;
    		}
		} catch (Exception e) {
			throw new CoreException(new IpsStatus(e));
		}
    	
		if (!datatype.isParsable(value)) {
			String text = NLS.bind(Messages.ValidationUtils_NO_INSTANCE_OF_VALUEDATATYPE, value, datatype);
			Message msg = new Message(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE, text, Message.ERROR, part, propertyName); //$NON-NLS-1$
			list.add(msg);
            return false;
		}
        return true;
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
