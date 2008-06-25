/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.runtime.modeltype.internal;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ModelTypeAssociation extends AbstractModelElement implements IModelTypeAssociation {

    private AssociationType associationType = AssociationType.Association;
    private int minCardinality = 0;
    private int maxCardinality = Integer.MAX_VALUE;
    private String namePlural = null;
    private String targetName = null;
    private boolean isProductRelevant = false;

    public ModelTypeAssociation(IRuntimeRepository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     */
    public AssociationType getAssociationType() {
        return associationType;
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxCardinality() {
        return maxCardinality;
    }

    /**
     * {@inheritDoc}
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /**
     * {@inheritDoc}
     */
    public String getNamePlural() {
        return namePlural;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public IModelType getTarget() throws ClassNotFoundException {
        if (targetName != null && targetName.length() > 0) {
            return getRepository().getModelType(
                    (Class<? extends IConfigurableModelObject>)this.getClass().getClassLoader().loadClass(targetName));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isProductRelevant() {
        return isProductRelevant;
    }

    /**
     * {@inheritDoc}
     */
    public void initFromXml(XMLStreamReader parser) throws XMLStreamException {
        super.initFromXml(parser);
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeLocalName(i).equals("namePlural")) {
                this.namePlural = parser.getAttributeValue(i);
                if (this.namePlural.length() == 0) {
                    this.namePlural = null;
                }
            } else if (parser.getAttributeLocalName(i).equals("target")) {
                this.targetName = parser.getAttributeValue(i);
            } else if (parser.getAttributeLocalName(i).equals("minCardinality")) {
                this.minCardinality = Integer.parseInt(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals("maxCardinality")) {
                this.maxCardinality = Integer.parseInt(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals("associationType")) {
                this.associationType = AssociationType.valueOf(parser.getAttributeValue(i));
            } else if (parser.getAttributeLocalName(i).equals("isProductRelevant")) {
                this.isProductRelevant = Boolean.valueOf(parser.getAttributeValue(i));
            }
        }
        initExtPropertiesFromXml(parser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(": ");
        sb.append(targetName);
        sb.append('(');
        sb.append(associationType);
        sb.append(' ');
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

}
