package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRuleDef;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 */
public class ValidationRuleDef extends IpsObjectPart implements IValidationRuleDef {

    final static String TAG_NAME = "ValidationRuleDef";

    private String msgText = "";
    private String msgCode = "";
    private MessageSeverity msgSeverity = MessageSeverity.ERROR;
    
    // the qualified name of the business functions this rule is used in 
    private ArrayList functions = new ArrayList(0);
    
    private boolean applyInAll;
    
    /**
     * Creates a new validation rule definition.
     * 
     * @param pcType The type the rule belongs to.
     * @param id The rule's unique id within the type.
     */    
    public ValidationRuleDef(IPolicyCmptType pcType, int id) {
        super(pcType, id);
    }

    /**
     * Constructor for testing purposes.
     */
    public ValidationRuleDef() {
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
     */
    public void delete() {
        ((PolicyCmptType)getIpsObject()).removeRule(this);
        updateSrcFile();
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#setName(java.lang.String)
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, newName);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("ValidationRuleDef.gif");
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#getBusinessFunctions()
     */
    public String[] getBusinessFunctions() {
        return (String[])functions.toArray(new String[functions.size()]);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#setBusinessFunctions(java.lang.String[])
     */
    public void setBusinessFunctions(String[] functionNames) {
        functions.clear();
        for (int i=0; i<functionNames.length; i++) {
            functions.add(functionNames[i]);
        }
        updateSrcFile();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#getNumOfBusinessFunctions()
     */
    public int getNumOfBusinessFunctions() {
        return functions.size();
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#addBusinessFunction(java.lang.String)
     */
    public void addBusinessFunction(String functionName) {
        ArgumentCheck.notNull(functionName);
        functions.add(functionName);
        updateSrcFile();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#removeBusinessFunctions(int[])
     */
    public void removeBusinessFunction(int index) {
        functions.remove(index);
        updateSrcFile();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#getBusinessFunction(int)
     */
    public String getBusinessFunction(int index) {
        return (String)functions.get(index);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#setBusinessFunctions(int, java.lang.String)
     */
    public void setBusinessFunctions(int index, String functionName) {
        ArgumentCheck.notNull(functionName);
        String oldName = getBusinessFunction(index);
        functions.set(index, functionName);
        valueChanged(oldName, functionName);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#isAppliedInAllBusinessFunctions()
     */
    public boolean isAppliedInAllBusinessFunctions() {
        return applyInAll;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#setAppliedInAllBusinessFunctions(boolean)
     */
    public void setAppliedInAllBusinessFunctions(boolean newValue) {
        boolean oldValue = applyInAll;
        applyInAll = newValue;
        valueChanged(oldValue, newValue);
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPart#validate(org.faktorips.util.message.MessageList)
     */
    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        ValidationUtils.checkStringPropertyNotEmpty(name, "name", this, PROPERTY_NAME, list);
        IIpsProject project = getIpsProject();
        for (Iterator it=functions.iterator(); it.hasNext();) {
            String function = (String)it.next();
            if (StringUtils.isNotEmpty(function)) {
                if (project.findIpsObject(IpsObjectType.BUSINESS_FUNCTION, function)==null) {
                    String text = function + " does not exists.";
                    list.add(new Message("", text, Message.ERROR, function, "name"));
                } else {
                    if (isAppliedInAllBusinessFunctions()) {
                        String text = "The rule is applied in all business functions, this information is ignored.";
                        list.add(new Message("", text, Message.WARNING, function, "name"));
                    }
                }
            }
        }
        
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#getMessageText()
     */
    public String getMessageText() {
        return msgText;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#setMessageText(java.lang.String)
     */
    public void setMessageText(String newText) {
        String oldText = msgText;
        msgText = newText;
        valueChanged(oldText, msgText);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#getMessageCode()
     */
    public String getMessageCode() {
        return msgCode;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#setMessageCode(java.lang.String)
     */
    public void setMessageCode(String newCode) {
        String oldCode = msgCode;
        msgCode = newCode;
        valueChanged(oldCode, msgCode);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#getMessageSeverity()
     */
    public MessageSeverity getMessageSeverity() {
        return msgSeverity;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.IValidationRuleDef#setMessageSeverity(org.faktorips.devtools.core.model.pctype.MessageSeverity)
     */
    public void setMessageSeverity(MessageSeverity newSeverity) {
        MessageSeverity oldSeverity = msgSeverity;
        msgSeverity = newSeverity;
        valueChanged(oldSeverity, msgSeverity);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#createElement(org.w3c.dom.Document)
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element) {
        super.initPropertiesFromXml(element);
        name = element.getAttribute(PROPERTY_NAME);
        applyInAll = Boolean.valueOf(element.getAttribute(PROPERTY_APPLIED_IN_ALL_FUNCTIONS)).booleanValue();
        msgCode = element.getAttribute(PROPERTY_MESSAGE_CODE);
        msgText = element.getAttribute(PROPERTY_MESSAGE_TEXT);
        msgSeverity = MessageSeverity.getMessageSeverity(element.getAttribute(PROPERTY_MESSAGE_SEVERITY));        
        
        NodeList nl = element.getChildNodes();
        functions.clear();
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i) instanceof Element) {
                Element fctElement = (Element)nl.item(i);
                if (fctElement.getNodeName().equals("BusinessFunction")) {
                    functions.add(fctElement.getAttribute("name"));
                }
            }
        }
        functions.trimToSize();
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#propertiesToXml(org.w3c.dom.Element)
     */
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_NAME, name);
        newElement.setAttribute(PROPERTY_APPLIED_IN_ALL_FUNCTIONS, "" + applyInAll);
        newElement.setAttribute(PROPERTY_MESSAGE_CODE, msgCode);
        newElement.setAttribute(PROPERTY_MESSAGE_TEXT, msgText);
        newElement.setAttribute(PROPERTY_MESSAGE_SEVERITY, msgSeverity.getId());
        Document doc = newElement.getOwnerDocument();
        for (int i=0; i<functions.size(); i++) {
            Element fctElement = doc.createElement("BusinessFunction");
            fctElement.setAttribute("name", (String)functions.get(i));
            newElement.appendChild(fctElement);
        }
    }
    
}
