/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
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
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.devtools.stdbuilder.xpand.model.XType;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAssociation;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
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

    public ModelTypeXmlBuilder(IpsObjectType type, StandardBuilderSet builderSet) {
        super(type, builderSet);
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
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
        XType xType = getXType(type);
        modelTypeElement.setAttribute(IModelType.PROPERTY_CLASS, xType.getQualifiedName(BuilderAspect.IMPLEMENTATION));
        modelTypeElement.setAttribute(XmlUtil.XML_ATTRIBUTE_SPACE, XmlUtil.XML_ATTRIBUTE_SPACE_VALUE);
        IType supertype = type.findSupertype(getIpsProject());
        if (supertype != null) {
            XType xSupertype = getXType(supertype);
            modelTypeElement.setAttribute(IModelType.PROPERTY_SUPERTYPE,
                    xSupertype.getQualifiedName(BuilderAspect.IMPLEMENTATION));
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

    private XType getXType(IType type) {
        XType xType;
        if (type instanceof IPolicyCmptType) {
            xType = getStandardBuilderSet().getModelNode(type, XPolicyCmptClass.class);
        } else if (type instanceof IProductCmptType) {
            xType = getStandardBuilderSet().getModelNode(type, XProductCmptClass.class);
        } else {
            throw new RuntimeException("Illegal type: " + type.getQualifiedNameType());
        }
        return xType;
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
                        XPolicyAssociation xAssociation = getStandardBuilderSet().getModelNode(association,
                                XPolicyAssociation.class);
                        modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_TARGET,
                                xAssociation.getTargetQualifiedClassName());
                    } else if (model instanceof IProductCmptType) {
                        XProductAssociation xAssociation = getStandardBuilderSet().getModelNode(association,
                                XProductAssociation.class);
                        modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_TARGET,
                                xAssociation.getTargetQualifiedClassName());
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

                addProductRelevant(association, modelTypeAssociation);
                addInverseAssociation(association, modelTypeAssociation);
                addMatchingAssociation(association, modelTypeAssociation);
                addDescriptions(association, modelTypeAssociation);
                addLabels(association, modelTypeAssociation);
                addExtensionProperties(association, modelTypeAssociation);
            }
        }
    }

    private void addProductRelevant(IAssociation association, Element modelTypeAssociation) {
        try {
            boolean productRelevant = true;
            if (association instanceof IPolicyCmptTypeAssociation) {
                IPolicyCmptTypeAssociation polCmptTypeAsso = (IPolicyCmptTypeAssociation)association;
                productRelevant = polCmptTypeAsso.isConstrainedByProductStructure(getIpsProject())
                        && polCmptTypeAsso.isConfigurable();
            }
            modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_PRODUCT_RELEVANT,
                    Boolean.toString(productRelevant));
        } catch (DOMException e) {
            // don't bother
            return;
        } catch (CoreException e) {
            // don't bother
            return;
        }
    }

    private void addInverseAssociation(IAssociation association, Element modelTypeAssociation) {
        if (association instanceof IPolicyCmptTypeAssociation) {
            IPolicyCmptTypeAssociation pcTypeAsso = (IPolicyCmptTypeAssociation)association;
            modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_INVERSE_ASSOCIATION,
                    pcTypeAsso.getInverseAssociation());

        }
    }

    /* private */void addMatchingAssociation(IAssociation association, Element modelTypeAssociation)
            throws CoreException {
        IAssociation matchingAssociation = association.findMatchingAssociation();
        if (matchingAssociation != null) {
            modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_NAME,
                    matchingAssociation.getName());
            modelTypeAssociation.setAttribute(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE,
                    matchingAssociation.getIpsObject().getQualifiedName());
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
                    XPolicyAttribute xAttribute = getBuilderSet().getModelNode(attribute, XPolicyAttribute.class);
                    modelTypeAttribute.setAttribute(IModelTypeAttribute.PROPERTY_DATATYPE,
                            xAttribute.getQualifiedJavaClassName());
                } else if (model instanceof IProductCmptType) {
                    XProductAttribute xAttribute = getBuilderSet().getModelNode(attribute, XProductAttribute.class);
                    modelTypeAttribute.setAttribute(IModelTypeAttribute.PROPERTY_DATATYPE,
                            xAttribute.getQualifiedJavaClassName());
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
            addExtensionProperties(label, runtimeLabel);
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
            addExtensionProperties(description, runtimeDescription);
        }
    }

    private void addExtensionProperties(IExtensionPropertyAccess element, Element modelElement) {
        Collection<IExtensionPropertyDefinition> extensionPropertyDefinitions = element
                .getExtensionPropertyDefinitions();
        if (!extensionPropertyDefinitions.isEmpty()) {
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
        if (valueSet == null) {
            return null;
        } else if (valueSet.isUnrestricted()) {
            return "AllValues";
        } else if (valueSet.isRange()) {
            return "Range";
        } else if (valueSet.isEnum()) {
            return "Enum";
        } else {
            return null;
        }
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    private StandardBuilderSet getStandardBuilderSet() {
        return getBuilderSet();
    }

}
