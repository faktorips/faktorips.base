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

package org.faktorips.runtime.modeltype.internal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.internal.StringUtils;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelTypeAssociation extends AbstractModelElement implements IModelTypeAssociation {

    private final Map<Locale, String> pluralLabelsByLocale = new HashMap<Locale, String>();

    private ModelType modelType;
    private AssociationType associationType = AssociationType.Association;
    private int minCardinality = 0;
    private int maxCardinality = Integer.MAX_VALUE;
    private String namePlural = null;
    private String targetJavaClassName = null;
    private boolean isProductRelevant = false;
    private boolean isDerivedUnion = false;
    private boolean isSubsetOfADerivedUnion = false;
    private Boolean isTargetRolePluralRequired = false;
    private String inverseAssociation;

    public ModelTypeAssociation(ModelType modelType) {
        super(modelType.getRepository());
        this.modelType = modelType;
    }

    public String getLabelForPlural(Locale locale) {
        String label = pluralLabelsByLocale.get(locale);
        return StringUtils.isEmpty(label) ? getNamePlural() : label;
    }

    public IModelType getModelType() {
        return modelType;
    }

    public AssociationType getAssociationType() {
        return associationType;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public String getNamePlural() {
        return namePlural;
    }

    public IModelType getTarget() throws ClassNotFoundException {
        if (targetJavaClassName != null && targetJavaClassName.length() > 0) {
            Class<?> targetClass = loadClass(targetJavaClassName);
            return getRepository().getModelType(targetClass);
        }
        return null;
    }

    public boolean isProductRelevant() {
        return isProductRelevant;
    }

    @Override
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        super.initFromXml(parser);

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals(IModelTypeAssociation.PROPERTY_NAME_PLURAL)) {
                namePlural = parser.getAttributeValue(i);
                if (namePlural.length() == 0) {
                    namePlural = null;
                }
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_TARGET)) {
                targetJavaClassName = parser.getAttributeValue(i);
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_MIN_CARDINALITY)) {
                minCardinality = Integer.parseInt(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_MAX_CARDINALITY)) {
                maxCardinality = Integer.parseInt(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_ASSOCIATION_TYPE)) {
                associationType = AssociationType.valueOf(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_PRODUCT_RELEVANT)) {
                isProductRelevant = Boolean.valueOf(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_DERIVED_UNION)) {
                isDerivedUnion = Boolean.valueOf(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_SUBSET_OF_A_DERIVED_UNION)) {
                isSubsetOfADerivedUnion = Boolean.valueOf(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_TARGET_ROLE_PLURAL_REQUIRED)) {
                isTargetRolePluralRequired = Boolean.valueOf(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals(PROPERTY_INVERSE_ASSOCIATION)) {
                inverseAssociation = parser.getAttributeValue(i);
            }
        }

        parser.next();
        initLabelsFromXml(parser);

        initExtPropertiesFromXml(parser);
    }

    @Override
    protected void initPluralLabel(Locale locale, String pluralValue) {
        pluralLabelsByLocale.put(locale, pluralValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getUsedName());
        sb.append(": ");
        sb.append(targetJavaClassName);
        sb.append('(');
        sb.append(associationType);
        sb.append(' ');
        if (isDerivedUnion) {
            sb.append(", Derived Union ");
        }
        if (isSubsetOfADerivedUnion) {
            sb.append(", Subset of a Derived Union ");
        }
        sb.append(minCardinality);
        sb.append("..");
        sb.append(maxCardinality == Integer.MAX_VALUE ? "*" : maxCardinality);
        if (isProductRelevant) {
            sb.append(", ");
            sb.append("isProductRelevant");
        }
        sb.append(')');
        return sb.toString();
    }

    public String getUsedName() {
        return isTargetRolePluralRequired ? getNamePlural() : getName();
    }

    public boolean isDerivedUnion() {
        return isDerivedUnion;
    }

    public boolean isSubsetOfADerivedUnion() {
        return isSubsetOfADerivedUnion;
    }

    public String getInverseAssociation() {
        return inverseAssociation;
    }

}
