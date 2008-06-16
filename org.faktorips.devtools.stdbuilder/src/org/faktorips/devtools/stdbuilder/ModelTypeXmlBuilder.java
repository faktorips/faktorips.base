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

package org.faktorips.devtools.stdbuilder;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.extproperties.StringExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyAccess;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.valueset.IAllValuesValueSet;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ModelTypeXmlBuilder extends AbstractXmlFileBuilder {

    private Document doc;

    public ModelTypeXmlBuilder(IpsObjectType type, IIpsArtefactBuilderSet builderSet, String kind) {
        super(type, builderSet, kind);
    }

    public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
        IType model = (IType)ipsSrcFile.getIpsObject();
        doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
        Element modelType = doc.createElement("ModelType");
        modelType.setAttribute("name", model.getQualifiedName());
        if (model instanceof IPolicyCmptType) {
            modelType.setAttribute("class", ((StandardBuilderSet)getBuilderSet()).getGenerator((IPolicyCmptType)model)
                    .getQualifiedName(false));
        } else if (model instanceof IProductCmptType) {
            modelType.setAttribute("class", ((StandardBuilderSet)getBuilderSet()).getGenerator((IProductCmptType)model)
                    .getQualifiedName(false));
        }
        String superTypeName = model.getSupertype();

        if(superTypeName!=null&&superTypeName.length()>0){
        if (model instanceof IPolicyCmptType) {            
            modelType.setAttribute("supertype", ((StandardBuilderSet)getBuilderSet()).getGenerator(getIpsProject().findPolicyCmptType(superTypeName))
                    .getQualifiedName(false));
        } else if (model instanceof IProductCmptType) {
            modelType.setAttribute("supertype", ((StandardBuilderSet)getBuilderSet()).getGenerator(getIpsProject().findProductCmptType(superTypeName))
                    .getQualifiedName(false));
        }
        }else{
            modelType.setAttribute("supertype", null);
        }
        addExtensionProperties(modelType, model);
        addAssociations(model, modelType);
        addAttributes(model, modelType);

        try {
            super.build(ipsSrcFile, XmlUtil.nodeToString(modelType, ipsSrcFile.getIpsProject().getXmlFileCharset()));
        } catch (TransformerException e) {
            throw new CoreException(new IpsStatus(e));
        }
    }

    private void addAssociations(IType model, Element modelType) {
        IAssociation[] associations = model.getAssociations();
        if (associations.length > 0) {
            Element modelTypeAssociations = doc.createElement("ModelTypeAssociations");
            modelType.appendChild(modelTypeAssociations);
            for (int i = 0; i < associations.length; i++) {
                IAssociation association = associations[i];
                Element modelTypeAssociation = doc.createElement("ModelTypeAssociation");
                modelTypeAssociations.appendChild(modelTypeAssociation);
                modelTypeAssociation.setAttribute("name", association.getTargetRoleSingular());
                modelTypeAssociation.setAttribute("namePlural", association.getTargetRolePlural());
                modelTypeAssociation.setAttribute("target", association.getTarget());
                modelTypeAssociation.setAttribute("minCardinality", Integer.toString(association.getMinCardinality()));
                modelTypeAssociation.setAttribute("maxCardinality", Integer.toString(association.getMaxCardinality()));
                modelTypeAssociation.setAttribute("associationType", getAssociantionType(association));
                try {
                    modelTypeAssociation
                            .setAttribute(
                                    "isProductRelevant",
                                    Boolean
                                            .toString(association instanceof IPolicyCmptTypeAssociation ? ((IPolicyCmptTypeAssociation)association)
                                                    .isConstrainedByProductStructure(getIpsProject())
                                                    : true));
                } catch (DOMException e) {
                    // don't bother
                } catch (CoreException e) {
                    // don't bother
                }
                addExtensionProperties(modelTypeAssociation, association);
            }
        }
    }

    private void addAttributes(IType model, Element modelType) {
        IAttribute[] attributes = model.getAttributes();
        if (attributes.length > 0) {
            Element modelTypeAttributes = doc.createElement("ModelTypeAttributes");
            modelType.appendChild(modelTypeAttributes);
            for (int i = 0; i < attributes.length; i++) {
                IAttribute attribute = attributes[i];
                Element modelTypeAttribute = doc.createElement("ModelTypeAttribute");
                modelTypeAttributes.appendChild(modelTypeAttribute);
                modelTypeAttribute.setAttribute("name", attribute.getName());
                modelTypeAttribute.setAttribute("datatype", attribute.getDatatype());
                modelTypeAttribute.setAttribute("valueSetType", getValueSetType(attribute));
                modelTypeAttribute.setAttribute("attributeType", getAttributeType(attribute));
                modelTypeAttribute.setAttribute("isProductRelevant", Boolean
                        .toString(attribute instanceof IPolicyCmptTypeAttribute ? ((IPolicyCmptTypeAttribute)attribute)
                                .isProductRelevant() : true));
                addExtensionProperties(modelTypeAttribute, attribute);
            }
        }
    }

    private void addExtensionProperties(Element modelElement, IExtensionPropertyAccess element) {
        IExtensionPropertyDefinition[] extensionPropertyDefinitions = this.getIpsProject().getIpsModel()
                .getExtensionPropertyDefinitions(element.getClass(), true);
        Element extensionProperties = doc.createElement("ExtensionProperties");
        modelElement.appendChild(extensionProperties);
        for (int i = 0; i < extensionPropertyDefinitions.length; i++) {
            IExtensionPropertyDefinition extensionPropertyDefinition = extensionPropertyDefinitions[i];
            String propertyId = extensionPropertyDefinition.getPropertyId();
            if (extensionPropertyDefinition instanceof StringExtensionPropertyDefinition && element
                    .isExtPropertyDefinitionAvailable(propertyId)) {
                // TODO enable non-String extension properties
                Element extensionProperty = doc.createElement("ExtensionProperty");
                extensionProperty.setAttribute("isNull", Boolean.toString(element.getExtPropertyValue(propertyId)==null));
                extensionProperty.setAttribute("id", propertyId);
                if (element.getExtPropertyValue(propertyId)!=null) {
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
        if (associationType.equals(AssociationType.COMPOSITION_MASTER_TO_DETAIL)) {
            return "Composition";
        }
        return null;
    }

    private String getAttributeType(IAttribute attribute) {
        if (attribute instanceof IPolicyCmptTypeAttribute) {
            return ((IPolicyCmptTypeAttribute)attribute).getAttributeType().getId();
        }
        // TODO wie mit IProductCmptTypeAttribute umgehen?
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
        if (valueSet instanceof IAllValuesValueSet) {
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

}