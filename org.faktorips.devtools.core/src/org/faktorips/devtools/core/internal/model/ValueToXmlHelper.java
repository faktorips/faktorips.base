package org.faktorips.devtools.core.internal.model;

import org.faktorips.util.XmlUtil;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueToXmlHelper {

    /**
     * Adds the value to the given xml element. Takes care of proper null handling.
     * By value we mean a value of a datatype, e.g. 42EUR is a value of the datatype money.
     * 
     * @param value the string representation of the value
     * @param el the xml element.
     * @param tagName the tag name for the element that stored the value 
     */
	public final static void addValueToElement(String value, Element el, String tagName) {
		Element valueEl = el.getOwnerDocument().createElement(tagName);
		el.appendChild(valueEl);
        valueEl.setAttribute("isNull", value==null?"true":"false");     //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (value!=null) {
            valueEl.appendChild(el.getOwnerDocument().createCDATASection(value));
        }
	}
	
	/**
	 * Returns the string representation of the value stored in the child element
	 * of the given element with the indicated name. Returns <code>null</code> if 
	 * the value is null or no such child element exists.
	 * 
	 * @param el The xml element that is the parent of the element storing the value.
	 * @param tagName The name of the child 
	 */
	public final static String getValueFromElement(Element el, String tagName) {
		Element valueEl = XmlUtil.getFirstElement(el, tagName);
		if (valueEl==null) {
			return null;
		}
		if (Boolean.valueOf(valueEl.getAttribute("isNull")).equals(Boolean.TRUE)) { //$NON-NLS-1$
			return null;
		}
		CDATASection cdata = XmlUtil.getFirstCDataSection(valueEl);
		
		// if no cdata-section was found, the value stored was an empty string. In this
		// case, the cdata-section get lost during transformation of the xml-document
		// to a string.
		String result = ""; //$NON-NLS-1$
		if (cdata != null) {
			result = cdata.getData();
		}
		return result;
	}
	
	private ValueToXmlHelper() {
	}

}
