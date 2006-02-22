package org.faktorips.devtools.core.internal.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
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
     * @return true if the reference is valid, otherwise false.
     * 
     * @deprecated use the method with the additional msgCode parameter.
     */
    public final static boolean checkIpsObjectReference(
            String objectName,
            IpsObjectType type,
            boolean mandatory,
            String propertyDisplayName,
            IIpsObjectPart part,
            String propertyName,
            MessageList list) throws CoreException {

    	return checkIpsObjectReference(objectName, type, mandatory, propertyDisplayName, part, propertyName, "", list);
    }

    /**
     * Tests if the given qualified name identifies a policy component type.
     * If not, it adds an error message to the given message list.
     * 
     * @param objectName the qualified type name to check.
     * @param mandatory Is the reference mandatory. If yes, it is checked that
     * the reference is not an empty string. Otherwise an empty reference is valid.
     * 
     * @return true if the reference is valid, otherwise false.
     */
    // TODO document params
    public final static boolean checkIpsObjectReference(
            String objectName,
            IpsObjectType type,
            boolean mandatory,
            String propertyDisplayName,
            IIpsObjectPart part,
            String propertyName,
            String msgCode,
            MessageList list) throws CoreException {
        
        if (mandatory) {
            if (!checkStringPropertyNotEmpty(objectName, propertyDisplayName,part, propertyName, list)) {
                return false;
            }
        }
        if (part.getIpsProject().findIpsObject(type, objectName)==null) {
            String text = StringUtils.capitalise(propertyDisplayName) + " " + objectName + " does not exists.";
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
     * 
     * @return the datatype if no error was detected, otherwise null.
     */
    public final static Datatype checkDatatypeReference(
            String datatypeName,
            boolean mandatory,
            boolean voidAllowed,
            IIpsObjectPart part,
            String propertyName,
            MessageList list) throws CoreException {
        
        if (mandatory) {
            if (!checkStringPropertyNotEmpty(datatypeName, "Datatype", part, propertyName, list)) {
                return null;
            }
        }
        Datatype datatype = part.getIpsProject().findDatatype(datatypeName);
        if (datatype==null) {
            String text = "Datatype " + datatypeName + " does not exists.";
            list.add(new Message("", text, Message.ERROR, part, propertyName));
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
            String text = "Datatype void is not allowed.";
            list.add(new Message("", text, Message.ERROR, part, propertyName));
        }
        return datatype;
        
    }
    
    /**
     * Tests if the given property value is not empty.
     * If it is empty, it adds an error message to the given message list.
     * 
     * @return true if the string is not empty, false if the string is empty.
     * 
     * @deprecated use the method with the additional msgCode parameter.
     */
    public final static boolean checkStringPropertyNotEmpty(
            String propertyValue, 
            String propertyDisplayName,
            Object object,
            String propertyName,
            MessageList list)
    {
    	return checkStringPropertyNotEmpty(propertyValue, propertyDisplayName, object, propertyName, "", list);
    }


    /**
     * Tests if the given property value is not empty.
     * If it is empty, it adds an error message to the given message list.
     * 
     * @return true if the string is not empty, false if the string is empty.
     */
    // TODO document params
    public final static boolean checkStringPropertyNotEmpty(
            String propertyValue, 
            String propertyDisplayName,
            Object object,
            String propertyName,
            String msgCode,
            MessageList list)
    {
        if (StringUtils.isEmpty(propertyValue)) {
            String text = StringUtils.capitalise(propertyDisplayName) + " is missing.";
            list.add(new Message("", text, Message.ERROR, object, propertyName));
            return false;
        }
        return true;
    }
    
    public final static Image getSeverityImage(int messageSeverity) {
        String imageName;
        switch (messageSeverity) {
        	case Message.ERROR: {
        	    imageName = "size8/ErrorMessage.gif";
        	    break;
        	}
        	case Message.WARNING: {
        	    imageName = "size8/WarningMessage.gif";
        	    break;
        	}
        	case Message.INFO: {
        	    imageName = "size8/InfoMessage.gif";
        	    break;
        	}
        	default:
        	    imageName = "size8/NullMessage.gif";
        }
        return IpsPlugin.getDefault().getImage(imageName);
    }

    private ValidationUtils() {
        
    }

}
