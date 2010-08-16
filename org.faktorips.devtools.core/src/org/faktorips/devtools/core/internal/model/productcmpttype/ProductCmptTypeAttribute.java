/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
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

    public ProductCmptTypeAttribute(IIpsObject parent, String id) {
        super(parent, id);
        valueSet = new UnrestrictedValueSet(this, getNextPartId());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    public boolean isOverwrite() {
        return false;
    }

    @Override
    public String getPropertyName() {
        return name;
    }

    @Override
    public ProdDefPropertyType getProdDefPropertyType() {
        return ProdDefPropertyType.VALUE;
    }

    @Override
    public boolean isDerived() {
        return false;
    }

    @Override
    public String getPropertyDatatype() {
        return getDatatype();
    }

    public ValueDatatype getValueDatatype() {
        try {
            return findDatatype(getIpsProject());
        } catch (CoreException e) {
            IpsPlugin.log(e);
            return null;
        }
    }

    @Override
    public IValueSet getValueSet() {
        return valueSet;
    }

    @Override
    public List<ValueSetType> getAllowedValueSetTypes(IIpsProject ipsProject) throws CoreException {
        return ipsProject.getValueSetTypes(findDatatype(ipsProject));
    }

    @Override
    public boolean isValueSetUpdateable() {
        return true;
    }

    @Override
    public void setValueSetType(ValueSetType newType) {
        ArgumentCheck.notNull(newType);
        if (newType == valueSet.getValueSetType()) {
            return;
        }
        valueSet = newType.newValueSet(this, getNextPartId());
        objectHasChanged();
    }

    @Override
    public IValueSet changeValueSetType(ValueSetType newType) {
        setValueSetType(newType);
        return valueSet;
    }

    @Override
    public void setValueSetCopy(IValueSet source) {
        IValueSet oldset = valueSet;
        valueSet = source.copy(this, getNextPartId());
        valueChanged(oldset, valueSet);
    }

    @Override
    public IIpsElement[] getChildren() {
        IIpsElement[] superChildren = super.getChildren();
        if (valueSet != null) {
            List<IIpsElement> children = new ArrayList<IIpsElement>(Arrays.asList(superChildren));
            children.add(valueSet);
            return children.toArray(new IIpsElement[children.size()]);
        }
        return superChildren;
    }

    @Override
    protected boolean addPart(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = (IValueSet)part;
            return true;
        }
        return super.addPart(part);
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        if (xmlTag.getNodeName().equals(ValueSet.XML_TAG)) {
            valueSet = ValueSetType.newValueSet(xmlTag, this, id);
            return valueSet;
        }
        return super.newPart(xmlTag, id);
    }

    @Override
    protected boolean removePart(IIpsObjectPart part) {
        if (part instanceof IValueSet) {
            valueSet = new UnrestrictedValueSet(this, getNextPartId());
            return true;
        }
        return super.removePart(part);
    }

}
