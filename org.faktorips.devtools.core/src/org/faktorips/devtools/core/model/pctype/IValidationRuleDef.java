package org.faktorips.devtools.core.model.pctype;

import org.faktorips.devtools.core.model.IIpsObjectPart;

/**
 *
 */
public interface IValidationRuleDef extends IIpsObjectPart {
    
    public final static String PROPERTY_BUSINESS_FUNCTIONS = "businessFunctions";
    public final static String PROPERTY_APPLIED_IN_ALL_FUNCTIONS = "appliedInAllBusinessFunctions";
    public final static String PROPERTY_MESSAGE_TEXT = "messageText";
    public final static String PROPERTY_MESSAGE_CODE = "messageCode";
    public final static String PROPERTY_MESSAGE_SEVERITY = "messageSeverity";
    
    /**
     * Sets the rules name.
     */
    public void setName(String newName);
    
    /**
     * Returns the message text that is presented to the user, if the rule determines
     * that a given policy is invalid.
     */
    public String getMessageText();
    
    /**
     * Sets the message text, that is presented to the user, if the rule determines
     * that a given policy is invalid.
     */
    public void setMessageText(String newText);
    
    /**
     * Returns the code of the message that is generated, if the rule determines
     * that a given policy is invalid.
     */
    public String getMessageCode();
    
    /**
     * Sets the code of the message that is generated, if the rule determines
     * that a given policy is invalid.
     */
    public void setMessageCode(String newCode);
    
    /**
     * Returns the message's severity.
     */
    public MessageSeverity getMessageSeverity();
    
    /**
     * Sets the message's severity.
     */
    public void setMessageSeverity(MessageSeverity newSeverity);
    
    /**
     * Returns the qualified name of the business functions in that the rule is applied.
     */
    public String[] getBusinessFunctions();
    
    /**
     * Returns the number of business functions in which the rule is applied. 
     */
    public int getNumOfBusinessFunctions();
    
    /**
     * Sets the qualified name of the business functions in that the rule is applied.
     */
    public void setBusinessFunctions(String[] functionNames);

    /**
     * Returns the business function name at the given index.
     * 
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    public String getBusinessFunction(int index);
    
    /**
     * Sets the business function at the specified index.
     * 
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    public void setBusinessFunctions(int index, String functionName);
    
    /**
     * Adds a new business function with the given name.
     */
    public void addBusinessFunction(String functionName);
    
    /**
     * Removes the business functions at the specified indices.
     */
    public void removeBusinessFunction(int index);
    
    /**
     * Returns true if the rule is applied in all business functions.
     */
    public boolean isAppliedInAllBusinessFunctions();
    
    /**
     * Sets if the rule should be applied in all business functions.
     */
    public void setAppliedInAllBusinessFunctions(boolean newValue);
    
    
}
