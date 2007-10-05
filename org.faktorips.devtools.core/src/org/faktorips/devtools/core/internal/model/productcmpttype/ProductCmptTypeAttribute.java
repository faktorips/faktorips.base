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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.AllValuesValueSet;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IValueDatatypeProvider;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttribute extends IpsObjectPart implements IProductCmptTypeAttribute, IValueDatatypeProvider {

    final static String TAG_NAME = "Attribute"; //$NON-NLS-1$

    private String datatype = "";
    private Modifier modifier = Modifier.PUBLISHED;
    
    private String defaultValue = null;
    private IValueSet valueSet;
    
    /**
     * @param parent
     * @param id
     */
    public ProductCmptTypeAttribute(IIpsObject parent, int id) {
        super(parent, id);
        name = "";
        valueSet = new AllValuesValueSet(this, getNextPartId());
    }

    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() {
        if (valueSet != null) {
            return new IIpsElement[] { valueSet };
        } else {
            return new IIpsElement[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        valueChanged(oldName, newName);
    }

    /**
     * {@inheritDoc}
     */
    public Modifier getModifier() {
        return modifier;
    }

    /**
     * {@inheritDoc}
     */
    public void setModifier(Modifier newModifer) {
        ArgumentCheck.notNull(newModifer);
        Modifier oldModifier = modifier;
        modifier = newModifer;
        valueChanged(oldModifier, newModifer);
    }

    /**
     * {@inheritDoc}
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String newDatatype) {
        String oldDatatype = datatype;
        datatype = newDatatype;
        valueChanged(oldDatatype, newDatatype);
    }
    
    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyName() {
        return name;
    }
    
    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public ProdDefPropertyType getProdDefPropertyType() {
        return ProdDefPropertyType.VALUE;
    }

    /**
     * {@inheritDoc}
     * Implementation of IProdDefProperty.
     */
    public String getPropertyDatatype() {
        return datatype;
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype findDatatype(IIpsProject project) throws CoreException {
        return project.findValueDatatype(datatype);
    }
    
    /**
     * {@inheritDoc}
     */
    // implementation of IValueDatatypeProvider
    public ValueDatatype getValueDatatype() {
        try {
            return findDatatype(getIpsProject());
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaultValue(String newValue) {
        String oldValue = defaultValue;
        defaultValue = newValue;
        valueChanged(oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public IValueSet getValueSet() {
        return valueSet;
    }
    

    /**
     * {@inheritDoc}
     */
    public boolean isValueSetUpdateable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void setValueSetType(ValueSetType newType) {
        ArgumentCheck.notNull(newType);
        if (newType==valueSet.getValueSetType()) {
            return;
        }
        valueSet = newType.newValueSet(this, getNextPartId());
        objectHasChanged();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        modifier = Modifier.getModifier(element.getAttribute(PROPERTY_MODIFIER));
        if (modifier==null) {
            modifier = Modifier.PUBLISHED;
        }
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        defaultValue = ValueToXmlHelper.getValueFromElement(element, "DefaultValue"); //$NON-NLS-1$    
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name); 
        element.setAttribute(PROPERTY_DATATYPE, datatype); 
        element.setAttribute(PROPERTY_MODIFIER, modifier.getId());
        ValueToXmlHelper.addValueToElement(defaultValue, element, "DefaultValue"); //$NON-NLS-1$
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
    protected void reAddPart(IIpsObjectPart part) {
        valueSet = (IValueSet)part;
    }

    /**
     * {@inheritDoc}
     */
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        if (xmlTag.getNodeName().equals(ValueSet.XML_TAG)) {
            valueSet = ValueSetType.newValueSet(xmlTag, this, id);
            return valueSet;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    protected void reinitPartCollections() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void removePart(IIpsObjectPart part) {
        valueSet = new AllValuesValueSet(this, getNextPartId());
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("AttributePublic.gif"); //$NON-NLS-1$
    }

}
