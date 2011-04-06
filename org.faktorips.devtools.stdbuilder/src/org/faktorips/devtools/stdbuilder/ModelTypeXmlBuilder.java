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

package org.faktorips.devtools.stdbuilder;

import java.util.List;
import java.util.Locale;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.internal.model.type.AssociationType;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
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
import org.faktorips.runtime.modeltype.IModelTypeLabel;
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

    public ModelTypeXmlBuilder(IpsObjectType type, IIpsArtefactBuilderSet builderSet, String kind) {
        super(type, builderSet, kind);
    }

    @Override
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        StandardBuilderSet stdBuilderSet = (StandardBuilderSet)getBuilderSet();
        IType type = (IType)ipsSrcFile.getIpsObject();
        doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        Element modelTypeEl = doc.createElement("ModelType");
        modelTypeEl.setAttribute("name", type.getQualifiedName());
        modelTypeEl.setAttribute("class", stdBuilderSet.getGenerator(type).getQualifiedName(false));
        modelTypeEl.setAttribute(XmlUtil.XML_ATTRIBUTE_SPACE, XmlUtil.XML_ATTRIBUTE_SPACE_VALUE);
        IType supertype = type.findSupertype(getIpsProject());
        if (supertype != null) {
            modelTypeEl.setAttribute("supertype", stdBuilderSet.getGenerator(supertype).getQualifiedName(false));
        } else {
            modelTypeEl.setAttribute("supertype", null);
        }
        addLabels(type, modelTypeEl);
        addExtensionProperties(modelTypeEl, type);
        addAssociations(type, modelTypeEl);
        addAttributes(type, modelTypeEl);

        try {
            super.build(ipsSrcFile, XmlUtil.nodeToString(modelTypeEl, ipsSrcFile.getIpsProject().getXmlFileCharset()));
        } catch (TransformerException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    private void addAssociations(IType model, Element modelType) throws CoreException {
        List<IAssociation> associations = model.getAssociations();
        if (associations.size() > 0) {
            Element modelTypeAssociations = doc.createElement("ModelTypeAssociations");
            modelType.appendChild(modelTypeAssociations);
            for (IAssociation association : associations) {
                if (association.isValid(model.getIpsProject())) {
                    Element modelTypeAssociation = doc.createElement("ModelTypeAssociation");
                    modelTypeAssociations.appendChild(modelTypeAssociation);
                    modelTypeAssociation.setAttribute("name", association.getTargetRoleSingular());
                    modelTypeAssociation.setAttribute("namePlural", association.getTargetRolePlural());
                    String targetName = association.getTarget();
                    if (targetName != null && targetName.length() > 0) {
                        if (model instanceof IPolicyCmptType) {
                            modelTypeAssociation.setAttribute(
                                    "target",
                                    ((StandardBuilderSet)getBuilderSet()).getGenerator(
                                            getIpsProject().findPolicyCmptType(targetName)).getQualifiedName(false));
                        } else if (model instanceof IProductCmptType) {
                            modelTypeAssociation.setAttribute(
                                    "target",
                                    ((StandardBuilderSet)getBuilderSet()).getGenerator(
                                            getIpsProject().findProductCmptType(targetName)).getQualifiedName(false));
                        }
                    } else {
                        modelTypeAssociation.setAttribute("target", null);
                    }
                    modelTypeAssociation.setAttribute("minCardinality",
                            Integer.toString(association.getMinCardinality()));
                    modelTypeAssociation.setAttribute("maxCardinality",
                            Integer.toString(association.getMaxCardinality()));
                    modelTypeAssociation.setAttribute("associationType", getAssociantionType(association));
                    modelTypeAssociation.setAttribute("isTargetRolePluralRequired",
                            Boolean.toString(association.isTargetRolePluralRequired()));
                    modelTypeAssociation.setAttribute("isDerivedUnion", Boolean.toString(association.isDerivedUnion()));
                    modelTypeAssociation.setAttribute("isSubsetOfADerivedUnion",
                            Boolean.toString(association.isSubsetOfADerivedUnion()));
                    try {
                        modelTypeAssociation
                                .setAttribute(
                                        "isProductRelevant",
                                        Boolean.toString(association instanceof IPolicyCmptTypeAssociation ? ((IPolicyCmptTypeAssociation)association)
                                                .isConstrainedByProductStructure(getIpsProject()) : true));
                    } catch (DOMException e) {
                        // don't bother
                    } catch (CoreException e) {
                        // don't bother
                    }

                    if (association instanceof IPolicyCmptTypeAssociation) {
                        IPolicyCmptTypeAssociation pcTypeAsso = (IPolicyCmptTypeAssociation)association;
                        modelTypeAssociation.setAttribute("inverseAssociation", pcTypeAsso.getInverseAssociation());
                    }

                    addLabels(association, modelTypeAssociation);
                    addExtensionProperties(modelTypeAssociation, association);
                }
            }
        }
    }

    private void addAttributes(IType model, Element modelType) throws CoreException {
        List<? extends IAttribute> attributes = model.getAttributes();
        if (attributes.size() > 0) {
            Element modelTypeAttributes = doc.createElement("ModelTypeAttributes");
            modelType.appendChild(modelTypeAttributes);
            for (IAttribute attribute : attributes) {
                if (attribute.isValid(model.getIpsProject())) {
                    Element modelTypeAttribute = doc.createElement("ModelTypeAttribute");
                    modelTypeAttributes.appendChild(modelTypeAttribute);
                    modelTypeAttribute.setAttribute("name", attribute.getName());
                    if (model instanceof IPolicyCmptType) {
                        GenPolicyCmptTypeAttribute genPolicyCmptTypeAttribute = ((StandardBuilderSet)getBuilderSet())
                                .getGenerator((IPolicyCmptType)model).getGenerator((IPolicyCmptTypeAttribute)attribute);
                        if (genPolicyCmptTypeAttribute != null) {
                            modelTypeAttribute.setAttribute("datatype", genPolicyCmptTypeAttribute.getDatatype()
                                    .getJavaClassName());
                        }
                    } else if (model instanceof IProductCmptType) {
                        GenProductCmptTypeAttribute genAttribute = ((StandardBuilderSet)getBuilderSet()).getGenerator(
                                (IProductCmptType)model).getGenerator((IProductCmptTypeAttribute)attribute);
                        if (genAttribute != null) {
                            modelTypeAttribute.setAttribute("datatype", genAttribute.getDatatype().getJavaClassName());
                        }
                    } else {
                        modelTypeAttribute.setAttribute("datatype", null);
                    }
                    modelTypeAttribute.setAttribute("valueSetType", getValueSetType(attribute));
                    modelTypeAttribute.setAttribute("attributeType", getAttributeType(attribute));
                    modelTypeAttribute
                            .setAttribute(
                                    "isProductRelevant",
                                    Boolean.toString(attribute instanceof IPolicyCmptTypeAttribute ? ((IPolicyCmptTypeAttribute)attribute)
                                            .isProductRelevant() : true));
                    addLabels(attribute, modelTypeAttribute);
                    addExtensionProperties(modelTypeAttribute, attribute);
                }
            }
        }
    }

    private void addLabels(ILabeledElement model, Element runtimeModelElement) {
        List<ILabel> labels = model.getLabels();
        if (labels.size() == 0) {
            return;
        }
        Element runtimeLabels = doc.createElement(IModelTypeLabel.XML_WRAPPER_TAG);
        runtimeModelElement.appendChild(runtimeLabels);
        for (ILabel label : labels) {
            Element runtimeLabel = doc.createElement(IModelTypeLabel.XML_TAG);
            runtimeLabels.appendChild(runtimeLabel);
            Locale locale = label.getLocale();
            runtimeLabel.setAttribute(IModelTypeLabel.PROPERTY_LOCALE, locale == null ? "" : locale.getLanguage());
            runtimeLabel.setAttribute(IModelTypeLabel.PROPERTY_VALUE, label.getValue());
            runtimeLabel.setAttribute(IModelTypeLabel.PROPERTY_PLURAL_VALUE, label.getPluralValue());
        }
    }

    private void addExtensionProperties(Element modelElement, IExtensionPropertyAccess element) {
        IExtensionPropertyDefinition[] extensionPropertyDefinitions = getIpsProject().getIpsModel()
                .getExtensionPropertyDefinitions(element.getClass(), true);
        Element extensionProperties = doc.createElement("ExtensionProperties");
        modelElement.appendChild(extensionProperties);
        for (IExtensionPropertyDefinition extensionPropertyDefinition : extensionPropertyDefinitions) {
            String propertyId = extensionPropertyDefinition.getPropertyId();
            if (extensionPropertyDefinition instanceof StringExtensionPropertyDefinition
                    && element.isExtPropertyDefinitionAvailable(propertyId)) {
                // TODO enable non-String extension properties
                Element extensionProperty = doc.createElement("Value");
                extensionProperty.setAttribute("isNull",
                        Boolean.toString(element.getExtPropertyValue(propertyId) == null));
                extensionProperty.setAttribute("id", propertyId);
                if (element.getExtPropertyValue(propertyId) != null) {
                    extensionProperty.appendChild(doc.createCDATASection(element.getExtPropertyValue(propertyId)
                            .toString()));
                }
                extensionProperties.appendChild(extensionProperty);
            }
        }
    }

    private String getAssociantionType(IAssociation association) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean buildsDerivedArtefacts() {
        return true;
    }

    /**
     * Returns the path to the (generated) xml resource as used by the Class.getResourceAsStream()
     * Method.
     * 
     * @see Class#getResourceAsStream(java.lang.String)
     */
    public String getXmlResourcePath(IType type) throws CoreException {
        String packageInternal = getBuilderSet().getPackage(DefaultBuilderSet.KIND_MODEL_TYPE, type.getIpsSrcFile());
        return packageInternal.replace('.', '/') + '/' + type.getName() + ".xml";
    }

}
