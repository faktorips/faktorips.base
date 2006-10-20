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

package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.AllValuesValueSet;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValueSet;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExcelFunctionsResolver;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 */
public class ConfigElement extends IpsObjectPart implements IConfigElement {

	final static String TAG_NAME = "ConfigElement"; //$NON-NLS-1$

	private ConfigElementType type = ConfigElementType.PRODUCT_ATTRIBUTE;

	private String pcTypeAttribute = ""; //$NON-NLS-1$

	private IValueSet valueSet;

	private String value = ""; //$NON-NLS-1$

    private List formulaTestCases = new ArrayList(0);
    
	private boolean deleted = false;

	public ConfigElement(ProductCmptGeneration parent, int id) {
		super(parent, id);
		valueSet = new AllValuesValueSet(this, getNextPartId());
	}

	/**
	 * Overridden.
	 */
	public IProductCmpt getProductCmpt() {
		return (IProductCmpt) getParent().getParent();
	}

	/**
	 * Overridden.
	 */
	public IProductCmptGeneration getProductCmptGeneration() {
		return (IProductCmptGeneration) getParent();
	}

	/**
	 * Overridden.
	 */
	public ConfigElementType getType() {
		return type;
	}

	/**
	 * Overridden.
	 */
	public void setType(ConfigElementType newType) {
		ConfigElementType oldType = type;
		type = newType;
		valueChanged(oldType, newType);

	}

	/**
	 * Overridden.
	 */
	public String getPcTypeAttribute() {
		return pcTypeAttribute;
	}

	/**
	 * Overridden.
	 */
	public void setPcTypeAttribute(String newName) {
		String oldName = pcTypeAttribute;
		pcTypeAttribute = newName;
		name = pcTypeAttribute;
		valueChanged(oldName, pcTypeAttribute);
	}

	/**
	 * Overridden.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Overridden.
	 */
	public void setValue(String newValue) {
		String oldValue = value;
		value = newValue;
		valueChanged(oldValue, value);
	}

	/**
	 * Overridden.
	 */
	public void delete() {
		((ProductCmptGeneration) getParent()).removeConfigElement(this);
		deleted = true;
        objectHasChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Overridden.
	 */
	public Image getImage() {
		return IpsPlugin.getDefault().getImage("AttributePublic.gif"); //$NON-NLS-1$
	}

	/**
	 * Overridden.
	 */
	public IAttribute findPcTypeAttribute() throws CoreException {
		IPolicyCmptType pcType = ((IProductCmpt) getIpsObject())
				.findPolicyCmptType();
		if (pcType == null) {
			return null;
		}
		IAttribute a = pcType.getAttribute(this.pcTypeAttribute);
		if (a != null) {
			return a;
		}
		ITypeHierarchy hierarchy = pcType.getSupertypeHierarchy();
		return hierarchy.findAttribute(pcType, pcTypeAttribute);
	}

	/**
	 * Overridden.
	 */
	public ExprCompiler getExprCompiler() throws CoreException {
		ExprCompiler compiler = new ExprCompiler();
		compiler.add(new ExcelFunctionsResolver(getIpsProject()
				.getExpressionLanguageFunctionsLanguage()));
		compiler.add(new TableFunctionsResolver(getIpsProject()));
		IIpsArtefactBuilderSet builderSet = getIpsProject()
				.getIpsArtefactBuilderSet();
		IAttribute a = findPcTypeAttribute();
		if (a == null) {
			return compiler;
		}
		IParameterIdentifierResolver resolver = builderSet
				.getFlParameterIdentifierResolver();
		if (resolver == null) {
			return compiler;
		}
		resolver.setIpsProject(getIpsProject());
		resolver.setParameters(a.getFormulaParameters());
		compiler.setIdentifierResolver(resolver);
		return compiler;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void validateThis(MessageList list) throws CoreException {
		super.validateThis(list);
		IAttribute attribute = findPcTypeAttribute();
		if (attribute == null) {
			String text = NLS.bind(Messages.ConfigElement_msgAttrNotDefined, pcTypeAttribute, getProductCmpt().getPolicyCmptType());
			list.add(new Message(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE, text, Message.ERROR, this, PROPERTY_VALUE));
		} else {
    		if (attribute.getAttributeType() == AttributeType.CHANGEABLE
    				|| attribute.getAttributeType() == AttributeType.CONSTANT) {
    			validateValue(attribute, list);
    		} else {
    			validateFormula(attribute, list);
    		}
        }

        // validate that formula tests are only allowed if the type of the config element is formula
        if (formulaTestCases.size() > 0 && type != ConfigElementType.FORMULA) {
            String text = NLS.bind(Messages.ConfigElement_msgFormulaTestCaseNotAllowedIfTypeOfConfigElemIs, type);
            list.add(new Message(IConfigElement.MSGCODE_WRONG_TYPE_FOR_FORMULA_TESTS, text, Message.ERROR, this,
                    PROPERTY_TYPE));
        }
	}

	private void validateFormula(IAttribute attribute, MessageList list)
			throws CoreException {
		if (StringUtils.isEmpty(value)) {
			list.add(new Message(MSGCODE_MISSING_FORMULA, Messages.ConfigElement_msgFormulaNotDefined, Message.ERROR, this, PROPERTY_VALUE));
			return;
		}
		ExprCompiler compiler = getExprCompiler();
		CompilationResult result = compiler.compile(value);
		if (!result.successfull()) {
			MessageList compilerMessageList = result.getMessages();
			for (int i = 0; i < compilerMessageList.getNoOfMessages(); i++) {
				Message msg = compilerMessageList.getMessage(i);
				list.add(new Message(msg.getCode(), msg.getText(), msg
						.getSeverity(), this, PROPERTY_VALUE));
			}
			return;
		}
		Datatype attributeDatatype = attribute.findDatatype();
		if (attributeDatatype == null) {
			String text = Messages.ConfigElement_msgDatatypeMissing;
			list.add(new Message(
					IConfigElement.MSGCODE_UNKNOWN_DATATYPE_FORMULA, text,
					Message.ERROR, this, PROPERTY_VALUE));
			return;
		}
		if (attributeDatatype.equals(result.getDatatype())) {
			return;
		}
		if (compiler.getConversionCodeGenerator().canConvert(
				result.getDatatype(), attributeDatatype)) {
			return;
		}
		String text = NLS.bind(Messages.ConfigElement_msgReturnTypeMissmatch, attributeDatatype.getName(), result.getDatatype().getName());
		list.add(new Message(IConfigElement.MSGCODE_WRONG_FORMULA_DATATYPE,
				text, Message.ERROR, this, PROPERTY_VALUE));
	}

	private void validateValue(IAttribute attribute, MessageList list)
			throws CoreException {
		
		ValueDatatype valueDatatype = attribute.findDatatype();
		if (valueDatatype == null) {
			if (!StringUtils.isEmpty(value)) {
				String text = Messages.ConfigElement_msgUndknownDatatype;
				list.add(new Message(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_VALUE, text, Message.WARNING, this,
						PROPERTY_VALUE));
			}
			return;
		}
		try {
			if (valueDatatype.validate().containsErrorMsg()) {
				String text = Messages.ConfigElement_msgInvalidDatatype;
				list.add(new Message(IConfigElement.MSGCODE_INVALID_DATATYPE, text, Message.ERROR, this,
						PROPERTY_VALUE));
				return;
			}
		} catch (Exception e) {
			throw new CoreException(new IpsStatus(e));
		}

        valueSet.validate(list);
        
		if (!valueDatatype.isParsable(value)) {
        	String valueInMsg = value;
        	if (value==null) {
        		valueInMsg = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        	} else if (value.equals("")){ //$NON-NLS-1$
        		valueInMsg = Messages.ConfigElement_msgValueIsEmptyString;
        	}
			String text = NLS.bind(Messages.ConfigElement_msgValueNotParsable, valueInMsg, valueDatatype.getName());
			list.add(new Message(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE, text, Message.ERROR, this,
					PROPERTY_VALUE));
            return;
		}
		
        IValueSet modelValueSet = attribute.getValueSet();
        if (modelValueSet.validate().containsErrorMsg()) {
            String text = Messages.ConfigElement_msgInvalidAttributeValueset;
            list.add(new Message(IConfigElement.MSGCODE_UNKNWON_VALUESET, text, Message.WARNING, this, PROPERTY_VALUE));
            return;
        }

        if (this.type != ConfigElementType.PRODUCT_ATTRIBUTE && !modelValueSet.containsValueSet(valueSet, list, valueSet, null)) {
            return;
        }

		if (StringUtils.isNotEmpty(value)) {
			// validate valuset containment. If the type of this element
			// is PRODUCT_ATTRIBUTE, we do not validate against the
			// valueset of this element but against the valueset of
			// the attribute this element is based on. This is because an
			// element
			// of type PRODUCT_ATTRIBUTE becomes an ALL_VALUES-valueset,
			// but the valueset can not be changed for this type of config
			// element.
			if (this.type != ConfigElementType.PRODUCT_ATTRIBUTE) {
				if (!valueSet.containsValue(value)) {
					list.add(new Message(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET, NLS.bind(
                            Messages.ConfigElement_msgValueNotInValueset, value), Message.ERROR, this, PROPERTY_VALUE));
				}
			} else if (!modelValueSet.containsValue(value)) {
				list.add(new Message(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET, NLS.bind(
                        Messages.ConfigElement_msgValueNotInValueset, value), Message.ERROR, this, PROPERTY_VALUE));
			}
		}
	}

	/**
	 * Overridden.
	 */
	public IValueSet getValueSet() {
		if (type == ConfigElementType.PRODUCT_ATTRIBUTE) {
			IAttribute attr = null;
			try {
				attr = findPcTypeAttribute();
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
			if (attr != null) {
				return attr.getValueSet();
			}
			else {
				return null;
			}
		}
		return valueSet;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValueSetType(ValueSetType type) {
		if (this.type == ConfigElementType.PRODUCT_ATTRIBUTE) {
			throw new UnsupportedOperationException(
					"ConfigElement of type PRODUCT_ATTRIBUTE does not support own value sets."); //$NON-NLS-1$
		}
		IValueSet oldset = valueSet;
		valueSet = type.newValueSet(this, getNextPartId());
		valueChanged(oldset, valueSet);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValueSetCopy(IValueSet source) {
		if (this.type == ConfigElementType.PRODUCT_ATTRIBUTE) {
			throw new UnsupportedOperationException(
					"ConfigElement of type PRODUCT_ATTRIBUTE does not support own value sets."); //$NON-NLS-1$
		}

		IValueSet oldset = valueSet;
		valueSet = source.copy(this, getNextPartId());
		valueChanged(oldset, valueSet);
	}
	
	/**
	 * Overridden.
	 */
	protected Element createElement(Document doc) {
		return doc.createElement(TAG_NAME);
	}

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        formulaTestCases = new ArrayList();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void reAddPart(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = (IValueSet)part;
            return;
        } else if (part instanceof IFormulaTestCase){
            formulaTestCases.add(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }
    
    /**
	 * Overridden.
	 */
	protected void initPropertiesFromXml(Element element, Integer id) {
		super.initPropertiesFromXml(element, id);
		type = ConfigElementType.getConfigElementType(element
				.getAttribute(PROPERTY_TYPE));
		
		value = ValueToXmlHelper.getValueFromElement(element, "Value"); //$NON-NLS-1$
		
		pcTypeAttribute = element.getAttribute(PROPERTY_PCTYPE_ATTRIBUTE);
		name = pcTypeAttribute;
	}

	/**
	 * Overridden.
	 */
	protected void propertiesToXml(Element element) {
		super.propertiesToXml(element);
		element.setAttribute(PROPERTY_TYPE, type.getId());
		element.setAttribute(PROPERTY_PCTYPE_ATTRIBUTE, pcTypeAttribute);
		ValueToXmlHelper.addValueToElement(value, element, "Value"); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public IIpsElement[] getChildren() {
        List childrenList = new ArrayList((valueSet!=null?1:0) + formulaTestCases.size());
        if (valueSet != null) {
            childrenList.add(valueSet);
		}
        childrenList.addAll(formulaTestCases);
        return (IIpsElement[]) childrenList.toArray(new IIpsElement[0]);
    }
	
	/**
	 * {@inheritDoc}
	 */
    protected IIpsObjectPart newPart(Element partEl, int id) {
        String xmlTagName = partEl.getNodeName();
    	if (ValueSet.XML_TAG.equals(xmlTagName)) {
    		valueSet = ValueSetType.newValueSet(partEl, this, id);
    		return valueSet;
    	} else if (FormulaTestCase.TAG_NAME.equals(xmlTagName)){
    	    return newFormulaTestInternal(id);
        } else if (PROPERTY_VALUE.equalsIgnoreCase(xmlTagName)){
            // ignore value nodes, will be parsed in the this#initPropertiesFromXml method
            return null;
        }
        throw new RuntimeException("Could not create part for tag name: " + xmlTagName); //$NON-NLS-1$
    }

	/**
	 * {@inheritDoc}
	 */
	public ValueDatatype getValueDatatype() {
		try {
			IAttribute attr = findPcTypeAttribute();
			if (attr == null){
				return null;
			}
			return attr.getValueDatatype();
		} catch (CoreException e) {
			IpsPlugin.log(e);
		}

		return null;
	}
    

    /**
     * {@inheritDoc}
     */
    public IFormulaTestCase newFormulaTestCase() {
        ArgumentCheck.isTrue(getType() == ConfigElementType.FORMULA);
        IFormulaTestCase f = newFormulaTestInternal(getNextPartId());
        objectHasChanged();
        return f;
    }
    
    /*
     * Creates a new formula test without updating the source file.
     */
    private IFormulaTestCase newFormulaTestInternal(int nextPartId) {
        IFormulaTestCase f = new FormulaTestCase(this, nextPartId);
        formulaTestCases.add(f);
        return f;
    }

    /**
     * {@inheritDoc}
     */
    public IFormulaTestCase getFormulaTestCase(String name) {
        for (Iterator it = formulaTestCases.iterator(); it.hasNext();) {
            IFormulaTestCase f = (IFormulaTestCase) it.next();
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IFormulaTestCase[] getFormulaTestCases() {
        return (IFormulaTestCase[]) formulaTestCases.toArray(new IFormulaTestCase[0]);
    }

    /**
     * {@inheritDoc}
     */
    public void removeFormulaTestCase(IFormulaTestCase formulaTest) {
        if (formulaTestCases.contains(formulaTest)){
            formulaTestCases.remove(formulaTest);
            objectHasChanged();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveFormulaTestCases(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(formulaTestCases);
        int[] newIdxs = mover.move(indexes, up);
        valueChanged(indexes, newIdxs);
        return newIdxs;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getIdentifierUsedInFormula() throws CoreException {
        List result = new ArrayList();
        if (!ConfigElementType.FORMULA.equals(type)){
            return new String[0];
        }
        
        IAttribute attribute = findPcTypeAttribute();
        if (attribute == null){
            return new String[0];
        }
        
        Parameter[] formulaParameters = attribute.getFormulaParameters();
        for (int i = 0; i < formulaParameters.length; i++) {
            Datatype datatype = getIpsProject().findDatatype(formulaParameters[i].getDatatype());
            if (datatype instanceof IPolicyCmptType) {
                // if the datatype specifies a policy cmpt type add all identifiets of all attributes
                IPolicyCmptType pcType = (IPolicyCmptType) datatype;
                IAttribute[] attributes = pcType.getAttributes();
                for (int j = 0; j < attributes.length; j++) {
                    result.add(formulaParameters[i].getName() + "." + attributes[j].getName()); //$NON-NLS-1$
                }
            } else {
                result.add(formulaParameters[i].getName());
            }
        }
        
        // return only identifier which are in the formula
        List cleanedResult = new ArrayList();
        for (Iterator iter = result.iterator(); iter.hasNext();) {
            String identifier = (String)iter.next();
            // check if the identifier is given in the formula, use reg exp to check if the
            // identifier is in the formula and no character is on the beginning or end of the
            // identifier
            String identifiedInValue = (" " + value + " ").replaceAll(".*[^a-zA-Z]" + identifier + "[^a-zA-Z].*", identifier); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            if (identifiedInValue.equals(identifier)) {
                cleanedResult.add(identifier);
            }
        }
        
        return (String[]) cleanedResult.toArray(new String[0]);
    }
}
