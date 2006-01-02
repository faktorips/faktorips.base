package org.faktorips.devtools.core.model.extproperties;

import org.faktorips.util.XmlUtil;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

/**
 * Implementation of </code>IExtensionPropertyDefinition</code> for extension properties of type String.
 * 
 * @author Jan Ortmann
 */
public class StringExtensionPropertyDefinition extends ExtensionPropertyDefinition {

    /**
     * Empty constructor needed because of Eclipse extension point mechanism.
     */
    public StringExtensionPropertyDefinition() {
        super();
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.IExtensionPropertyDefinition#getValueFromXml(org.w3c.dom.Element)
     */
    public Object getValueFromXml(Element valueElement) {
    	CDATASection cdata = XmlUtil.getFirstCDataSection(valueElement);
    	if (cdata==null) {
    		return "";
    	}
    	return cdata.getData();
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.extproperties.ExtensionPropertyDefinition#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String s) {
        defaultValue = s;
    }

}
