/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.type.Attribute;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of IAttribute.
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttribute extends Attribute implements IProductCmptTypeAttribute {

    final static String TAG_NAME = "Attribute"; //$NON-NLS-1$

    private IValueSet valueSet;

    public ProductCmptTypeAttribute(IIpsObject parent, int id) {
        super(parent, id);
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    public boolean isOverwrite() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsElement[] getChildren() {
        if (valueSet != null) {
            return new IIpsElement[] { valueSet };
        } else {
            return new IIpsElement[0];
        }
    }

    /**
     * {@inheritDoc} Implementation of IProdDefProperty.
     */
    public String getPropertyName() {
        return name;
    }

    /**
     * {@inheritDoc} Implementation of IProdDefProperty.
     */
    public ProdDefPropertyType getProdDefPropertyType() {
        return ProdDefPropertyType.VALUE;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDerived() {
        return false;
    }

    /**
     * {@inheritDoc} Implementation of IProdDefProperty.
     */
    public String getPropertyDatatype() {
        return getDatatype();
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
    @Override
    public IValueSet getValueSet() {
        return valueSet;
    }

    /**
     * {@inheritDoc}
     */
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException {
        return ipsProject.getValueSetTypes(findDatatype(ipsProject));
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
        if (newType == valueSet.getValueSetType()) {
            return;
        }
        valueSet = newType.newValueSet(this, getNextPartId());
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public IValueSet changeValueSetType(ValueSetType newType) {
        setValueSetType(newType);
        return valueSet;
    }

    /**
     * {@inheritDoc}
     */
    public void setValueSetCopy(IValueSet source) {
        IValueSet oldset = valueSet;
        valueSet = source.copy(this, getNextPartId());
        valueChanged(oldset, valueSet);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reAddPart(IIpsObjectPart part) {
        valueSet = (IValueSet)part;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    protected void reinitPartCollections() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removePart(IIpsObjectPart part) {
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("AttributePublic.gif"); //$NON-NLS-1$
    }

}
