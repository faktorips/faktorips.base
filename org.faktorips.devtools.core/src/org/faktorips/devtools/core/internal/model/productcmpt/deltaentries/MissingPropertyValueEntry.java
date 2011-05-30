/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class MissingPropertyValueEntry extends AbstractDeltaEntryForProperty {

    private final IProductCmptProperty property;

    private final IPropertyValueContainer propertyValueContainer;

    private ValueWithoutPropertyEntry predecessor;

    public MissingPropertyValueEntry(IPropertyValueContainer propertyValueContainer, IProductCmptProperty property) {
        super(null);
        this.propertyValueContainer = propertyValueContainer;
        this.property = property;
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return property.getProductCmptPropertyType();
    }

    @Override
    public String getPropertyName() {
        return property.getPropertyName();
    }

    @Override
    public String getDescription() {
        String description = IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(property);
        if (hasPredecessorValue()) {
            IPropertyValueContainer predecessorContainer = predecessor.getPropertyValue().getPropertyValueContainer();
            String name = predecessorContainer.getName();
            if (predecessorContainer instanceof IProductCmptGeneration) {
                name = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                        .getGenerationConceptNameSingular()
                        + " " + name; //$NON-NLS-1$
            }
            description += NLS.bind(Messages.MissingPropertyValueEntry_valueTransferedInformation, name);
        }
        return description;
    }

    @Override
    public DeltaType getDeltaType() {
        return DeltaType.MISSING_PROPERTY_VALUE;
    }

    /**
     * @param predecessor The predecessor to set.
     */
    public void setPredecessor(ValueWithoutPropertyEntry predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * @return Returns the predecessor.
     */
    public AbstractDeltaEntryForProperty getPredecessor() {
        return predecessor;
    }

    @Override
    public void fix() {
        IPropertyValue newPropertyValue = propertyValueContainer.newPropertyValue(property);
        if (hasPredecessorValue()) {
            // if there was a predecessor value we copy the whole value
            IPropertyValue predecessorValue = predecessor.getPropertyValue();
            Element xml = predecessorValue.toXml(IpsPlugin.getDefault().getDocumentBuilder().newDocument());
            newPropertyValue.initFromXml(xml);
        }
    }

    private boolean hasPredecessorValue() {
        return predecessor != null && predecessor.getPropertyValue() != null;
    }

}
