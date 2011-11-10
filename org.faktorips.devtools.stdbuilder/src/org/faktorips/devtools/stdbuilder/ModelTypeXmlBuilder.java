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

package org.faktorips.devtools.stdbuilder;

import java.util.List;
import java.util.Locale;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IUnrestrictedValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProductCmptTypeAttribute;
import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAttribute;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A builder writing meta information from design time objects to XML files for later use at
 * runtime.
 * 
 * @author Daniel Hohenberger
 */
public class ModelTypeXmlBuilder extends AbstractXmlFileBuilder {

    private Document doc;

    public ModelTypeXmlBuilder(IpsObjectType type, DefaultBuilderSet builderSet) {
        super(type, builderSet);
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();

        IType type = (IType)ipsSrcFile.getIpsObject();
        Element modelTypeEl = createModelType(type);

        try {
            super.build(ipsSrcFile, XmlUtil.nodeToString(modelTypeEl, ipsSrcFile.getIpsProject().getXmlFileCharset()));
        } catch (TransformerException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    private Element createModelType(IType type) throws CoreException {
        Element modelTypeElement = doc.createElement(IModelType.XML_TAG);
        modelTypeElement.setAttribute(IModelElement.PROPERTY_NAME, type.getQualifiedName());
        modelTypeElement.setAttribute(IModelType.PROPERTY_CLASS, getStandardBuilderSet().getGenerator(type)
                .getQualifiedName(false));
        modelTypeElement.setAttribute(XmlUtil.XML_ATTRIBUTE_SPACE, XmlUtil.XML_ATTRIBUTE_SPACE_VALUE);
        IType supertype = type.findSupertype(getIpsProject());
        if (supertype != null) {
            modelTypeElement.setAttribute(IModelType.PROPERTY_SUPERTYPE, getStandardBuilderSet()
                    .getGenerator(supertype).getQualifiedName(false));
        } else {
            modelTypeElement.setAttribute(IModelType.PROPERTY_SUPERTYPE, null);
        }

        addLabels(type, modelTypeElement);
        addDescriptions(type, modelTypeElement);
        addExtensionProperties(type, modelTypeElement);
        addAssociations(type, modelTypeElement);
        addAttributes(type, modelTypeElement);

        return modelTypeElement;
    }

    private void addAssociations(IType model, Element modelType) throws CoreException {
        List<IAssociation> associations = model.getAssociations();
        if (associations.size() <= 0) {
            return;
        }
        Element modelTypeAssociations = doc.createElement(IModelTypeAssociation.XML_WRAPPER_TAG);
        modelType.appendChild(modelTypeAssociations);
        for (IAssociation association : associations) {
            if (association.isValid(model.getIpsProject())) {
                Element modelTypeAssociation = doc.createElement(IModelTypeAssociation.XML_TAG);
                modelTypeAssociations.appendChild(modelTypeAssociation);
                modelTypeAssociation.setAttribute(IModelElement.PROPERTY_NAME, association.getTargetRoleSingular());
                modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_NAME_PLURAL,
                        association.getTargetRolePlural());
                String targetName = association.getTarget();
                if (targetName != null && targetName.length() > 0) {
                    if (model instanceof IPolicyCmptType) {
                        modelTypeAssociation.setAttribute(
                                IModelTypeAssociation.PROPERTY_TARGET,
                                ((StandardBuilderSet)getBuilderSet()).getGenerator(
                                        getIpsProject().findPolicyCmptType(targetName)).getQualifiedName(false));
                    } else if (model instanceof IProductCmptType) {
                        modelTypeAssociation.setAttribute(
                                IModelTypeAssociation.PROPERTY_TARGET,
                                ((StandardBuilderSet)getBuilderSet()).getGenerator(
                                        getIpsProject().findProductCmptType(targetName)).getQualifiedName(false));
                    }
                } else {
                    modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_TARGET, null);
                }
                modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_MIN_CARDINALITY,
                        Integer.toString(association.getMinCardinality()));
                modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_MAX_CARDINALITY,
                        Integer.toString(association.getMaxCardinality()));
                modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_ASSOCIATION_TYPE,
                        getAssociationType(association));
                modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL_REQUIRED,
                        Boolean.toString(association.isTargetRolePluralRequired()));
                modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_DERIVED_UNION,
                        Boolean.toString(association.isDerivedUnion()));
                modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_SUBSET_OF_A_DERIVED_UNION,
                        Boolean.toString(association.isSubsetOfADerivedUnion()));
                try {
                    boolean productRelevant = true;
                    if (association instanceof IPolicyCmptTypeAssociation) {
                        IPolicyCmptTypeAssociation polCmptTypeAsso = (IPolicyCmptTypeAssociation)association;
                        productRelevant = polCmptTypeAsso.isConstrainedByProductStructure(getIpsProject())
                                && polCmptTypeAsso.isConfigured();
                    }
                    modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_PRODUCT_RELEVANT,
                            Boolean.toString(productRelevant));
                } catch (DOMException e) {
                    // don't bother
                } catch (CoreException e) {
                    // don't bother
                }

                if (association instanceof IPolicyCmptTypeAssociation) {
                    IPolicyCmptTypeAssociation pcTypeAsso = (IPolicyCmptTypeAssociation)association;
                    modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_INVERSE_ASSOCIATION,
                            pcTypeAsso.getInverseAssociation());
                }

                addDescriptions(association, modelTypeAssociation);
                addLabels(association, modelTypeAssociation);
                addExtensionProperties(association, modelTypeAssociation);
            }
        }
    }

    private void addAttributes(IType model, Element modelType) throws CoreException {
        List<? extends IAttribute> attributes = model.getAttributes();
        if (attributes.size() <= 0) {
            return;
        }
        Element modelTypeAttributes = doc.createElement(IModelTypeAttribute.XML_WRAPPER_TAG);
        modelType.appendChild(modelTypeAttributes);
        for (IAttribute attribute : attributes) {
            if (attribute.isValid(model.getIpsProject())) {
                Element modelTypeAttribute = doc.createElement(IModelTypeAttribute.XML_TAG);
                modelTypeAttributes.appendChild(modelTypeAttribute);
                modelTypeAttribute.setAttribute(IModelElement.PROPERTY_NAME, attribute.getName());
                if (model instanceof IPolicyCmptType) {
                    GenPolicyCmptTypeAttribute genPolicyCmptTypeAttribute = ((StandardBuilderSet)getBuilderSet())
                            .getGenerator((IPolicyCmptType)model).getGenerator((IPolicyCmptTypeAttribute)attribute);
                    if (genPolicyCmptTypeAttribute != null) {
                        modelTypeAttribute.setAttribute(IModelTypeAttribute.PROPERTY_DATATYPE,
                                genPolicyCmptTypeAttribute.getDatatype().getJavaClassName());
                    }
                } else if (model instanceof IProductCmptType) {
                    GenProductCmptTypeAttribute genAttribute = ((StandardBuilderSet)getBuilderSet()).getGenerator(
                            (IProductCmptType)model).getGenerator((IProductCmptTypeAttribute)attribute);
                    if (genAttribute != null) {
                        modelTypeAttribute.setAttribute(IModelTypeAttribute.PROPERTY_DATATYPE, genAttribute
                                .getDatatype().getJavaClassName());
                    }
                } else {
                    modelTypeAttribute.setAttribute(IModelTypeAttribute.PROPERTY_DATATYPE, null);
                }
                modelTypeAttribute
                        .setAttribute(IModelTypeAttribute.PROPERTY_VALUE_SET_TYPE, getValueSetType(attribute));
                modelTypeAttribute.setAttribute(IModelTypeAttribute.PROPERTY_ATTRIBUTE_TYPE,
                        getAttributeType(attribute));
                modelTypeAttribute.setAttribute(IModelTypeAttribute.PROPERTY_PRODUCT_RELEVANT, Boolean
                        .toString(attribute instanceof IPolicyCmptTypeAttribute ? ((IPolicyCmptTypeAttribute)attribute)
                                .isProductRelevant() : true));

                addDescriptions(attribute, modelTypeAttribute);
                addLabels(attribute, modelTypeAttribute);
                addExtensionProperties(attribute, modelTypeAttribute);
            }
        }
    }

    private void addLabels(ILabeledElement model, Element runtimeModelElement) {
        List<ILabel> labels = model.getLabels();
        if (labels.size() <= 0) {
            return;
        }
        Element runtimeLabels = doc.createElement(IModelElement.LABELS_XML_WRAPPER_TAG);
        runtimeModelElement.appendChild(runtimeLabels);
        for (ILabel label : labels) {
            Element runtimeLabel = doc.createElement(IModelElement.LABELS_XML_TAG);
            runtimeLabels.appendChild(runtimeLabel);
            Locale locale = label.getLocale();
            runtimeLabel.setAttribute(IModelElement.LABELS_PROPERTY_LOCALE, locale == null ? "" : locale.getLanguage());
            runtimeLabel.setAttribute(IModelElement.LABELS_PROPERTY_VALUE, label.getValue());
            if (model instanceof IAssociation) {
                runtimeLabel.setAttribute(IModelElement.LABELS_PROPERTY_PLURAL_VALUE, label.getPluralValue());
            }
        }
    }

    private void addDescriptions(IDescribedElement model, Element runtimeModelElement) {
        List<IDescription> descriptions = model.getDescriptions();
        if (descriptions.size() <= 0) {
            return;
        }
        Element runtimeDescriptions = doc.createElement(IModelElement.DESCRIPTIONS_XML_WRAPPER_TAG);
        runtimeModelElement.appendChild(runtimeDescriptions);
        for (IDescription description : descriptions) {
            Element runtimeDescription = doc.createElement(IModelElement.DESCRIPTIONS_XML_TAG);
            runtimeDescriptions.appendChild(runtimeDescription);
            Locale locale = description.getLocale();
            runtimeDescription.setAttribute(IModelElement.DESCRIPTIONS_PROPERTY_LOCALE,
                    locale == null ? "" : locale.getLanguage());
            runtimeDescription.setTextContent(description.getText());
        }
    }

    private void addExtensionProperties(IExtensionPropertyAccess element, Element modelElement) {
        IExtensionPropertyDefinition[] extensionPropertyDefinitions = getIpsProject().getIpsModel()
                .getExtensionPropertyDefinitions(element.getClass(), true);
        if (extensionPropertyDefinitions.length > 0) {
            Element extensionProperties = doc.createElement(IModelElement.EXTENSION_PROPERTIES_XML_WRAPPER_TAG);
            modelElement.appendChild(extensionProperties);
            for (IExtensionPropertyDefinition extensionPropertyDefinition : extensionPropertyDefinitions) {
                String propertyId = extensionPropertyDefinition.getPropertyId();
                if (element.isExtPropertyDefinitionAvailable(propertyId)) {
                    Element extensionProperty = doc.createElement(IModelElement.EXTENSION_PROPERTIES_XML_TAG);
                    extensionProperty.setAttribute(IModelElement.EXTENSION_PROPERTIES_PROPERTY_NULL,
                            Boolean.toString(element.getExtPropertyValue(propertyId) == null));
                    extensionProperty.setAttribute(IModelElement.EXTENSION_PROPERTIES_PROPERTY_ID, propertyId);
                    if (element.getExtPropertyValue(propertyId) != null) {
                        extensionPropertyDefinition.valueToXml(extensionProperty,
                                element.getExtPropertyValue(propertyId));
                    }
                    extensionProperties.appendChild(extensionProperty);
                }
            }
        }
    }

    private String getAssociationType(IAssociation association) {
        AssociationType associationType = association.getAssociationType();
        if (associationType.equals(AssociationType.ASSOCIATION)) {
            return "Association";
        }
        if (associationType.equals(AssociationType.COMPOSITION_DETAIL_TO_MASTER)) {
            return "CompositionToMaster";
        }
        if (associationType.equals(AssociationType.COMPOSITION_MASTER_TO_DETAIL)
                || associationType.equals(AssociationType.AGGREGATION)) {
            return "Composition";
        }
        return null;
    }

    private String getAttributeType(IAttribute attribute) {
        if (attribute instanceof IPolicyCmptTypeAttribute) {
            return ((IPolicyCmptTypeAttribute)attribute).getAttributeType().getId();
        }
        if (attribute instanceof IProductCmptTypeAttribute) {
            return AttributeType.CHANGEABLE.getId();
        }
        return null;
    }

    private String getValueSetType(IAttribute attribute) {
        IValueSet valueSet = null;
        if (attribute instanceof IPolicyCmptTypeAttribute) {
            valueSet = ((IPolicyCmptTypeAttribute)attribute).getValueSet();
        }
        if (attribute instanceof IProductCmptTypeAttribute) {
            valueSet = ((IProductCmptTypeAttribute)attribute).getValueSet();
        }
        if (valueSet instanceof IUnrestrictedValueSet) {
            return "AllValues";
        }
        if (valueSet instanceof IRangeValueSet) {
            return "Range";
        }
        if (valueSet instanceof IEnumValueSet) {
            return "Enum";
        }
        return null;
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    /**
     * Returns the path to the (generated) XML resource as used by the Class.getResourceAsStream()
     * Method.
     * 
     * @see Class#getResourceAsStream(java.lang.String)
     */
    public String getXmlResourcePath(IType type) throws CoreException {
        String packageInternal = getBuilderSet().getPackage(this, type.getIpsSrcFile());
        return packageInternal.replace('.', '/') + '/' + type.getName() + ".xml";
    }

    private StandardBuilderSet getStandardBuilderSet() {
        return (StandardBuilderSet)getBuilderSet();
    }

}
