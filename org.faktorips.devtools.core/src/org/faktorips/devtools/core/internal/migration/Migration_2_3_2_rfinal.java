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

package org.faktorips.devtools.core.internal.migration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Migration from version 2.3.2.rfinal to version 2.4.0.rc1.
 * 
 * @author Alexander Weickmann
 */
public class Migration_2_3_2_rfinal extends AbstractIpsProjectMigrationOperation {

    public Migration_2_3_2_rfinal(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "The handling of literal names in Faktor-IPS enumerations has changed."
                + " Attributes of enumeration types are no longer checked \"to be used as literal name\"."
                + " Instead, there will be a special literal name enumeration attribute. This special"
                + " enumeration attribute will be managed by Faktor-IPS automatically, so users should not"
                + " have to worry about it."
                + " From now on, each enumeration type that does define enumeration values directly in the model"
                + " while not being abstract will have such an enumeration literal name attribute. The migration"
                + " will create these attributes now where necessary while removing the old \"use as literal name\" flags.";
    }

    @Override
    public String getTargetVersion() {
        return "2.4.0.rc1"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

        IIpsProject ipsProject = getIpsProject();
        List<IIpsSrcFile> allIpsSrcFiles = new ArrayList<IIpsSrcFile>();
        ipsProject.collectAllIpsSrcFilesOfSrcFolderEntries(allIpsSrcFiles);

        // Go over all enumeration types.
        for (IIpsSrcFile currentIpsSrcFile : allIpsSrcFiles) {
            if (currentIpsSrcFile.getIpsObjectType().equals(IpsObjectType.ENUM_TYPE)) {
                IEnumType currentEnumType = (IEnumType)currentIpsSrcFile.getIpsObject();
                /*
                 * Perform the following operations if the current enumeration type needs an
                 * enumeration literal name attribute (that means it is not abstract and does
                 * contain values) or if the enumeration type contains any enumeration values (so
                 * enumeration values that are considered 'out of use' get their literal names,
                 * too).
                 */
                if (currentEnumType.needsToUseEnumLiteralNameAttribute() || currentEnumType.getEnumValues().size() > 0) {
                    /*
                     * It could be that the new literal name attributes already exist. This is the
                     * case if the user migrates a project that's version is less than 2.3.0.rfinal.
                     * The Migration2_2_to_2_3 will be started before in this case which already
                     * creates literal name attributes. So we have to check for this before creating
                     * the literal name attribute.
                     */
                    if (!(currentEnumType.containsEnumLiteralNameAttribute())) {
                        IEnumLiteralNameAttribute literalNameAttribute = currentEnumType.newEnumLiteralNameAttribute();
                        /*
                         * We now need to find out which enumeration attribute was marked as literal
                         * name. Because this functionality was removed from Faktor-IPS we try it
                         * the XML way. The literal name property was inherited however so we need
                         * to also search the supertype hierarchy. This won't work cross-project
                         * however.
                         */
                        String oldLiteralAttributeName = "";
                        List<IEnumType> searchedTypes = new ArrayList<IEnumType>(5);
                        searchedTypes.add(currentEnumType);
                        searchedTypes.addAll(currentEnumType.findAllSuperEnumTypes(ipsProject));
                        for (IEnumType currentSearchedEnumType : searchedTypes) {
                            InputStream is = currentSearchedEnumType.getIpsSrcFile().getContentFromEnclosingResource();
                            DocumentBuilder builder = IpsPlugin.getDefault().newDocumentBuilder();
                            Document doc;
                            try {
                                doc = builder.parse(is);
                            } catch (IOException e) {
                                throw new CoreException(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                                        "IO error while parsing input file.", e));
                            } catch (SAXException e) {
                                throw new CoreException(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                                        "SAX error while parsing input file.", e));
                            }
                            Element rootElement = doc.getDocumentElement();
                            NodeList children = rootElement.getChildNodes();
                            for (int i = 0; i < children.getLength(); i++) {
                                Node currentItem = children.item(i);
                                if (currentItem.getNodeName().equals(IEnumAttribute.XML_TAG)) {
                                    NamedNodeMap attributes = currentItem.getAttributes();
                                    Node literalNameNode = attributes.getNamedItem("literalName");
                                    // Theoretically not possible to be null.
                                    if (literalNameNode == null) {
                                        continue;
                                    }
                                    Boolean literalNameBoolean = Boolean.parseBoolean(literalNameNode.getTextContent());
                                    if (literalNameBoolean.booleanValue()) {
                                        Node nameNode = attributes.getNamedItem("name");
                                        // Theoretically not possible to be null.
                                        if (nameNode != null) {
                                            oldLiteralAttributeName = nameNode.getTextContent();
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        /*
                         * If we could obtain the name of the enumeration attribute that was used as
                         * literal name we can now correctly set the default value provider
                         * attribute. If we could not find it we set the default value provider to
                         * the name attribute. If even this fails there won't be a default provider
                         * which is not a tragedy however.
                         */
                        if (oldLiteralAttributeName.length() > 0) {
                            literalNameAttribute.setDefaultValueProviderAttribute(oldLiteralAttributeName);
                        } else {
                            IEnumAttribute nameAttribute = currentEnumType
                                    .findUsedAsNameInFaktorIpsUiAttribute(ipsProject);
                            if (nameAttribute != null) {
                                literalNameAttribute.setDefaultValueProviderAttribute(nameAttribute.getName());
                            }
                        }
                        /*
                         * Now go over all enumeration values that are stored in this enumeration
                         * type. For each enumeration value we need to set the value of the
                         * EnumAttributeValue referencing the EnumLiteralNameAttribute. The value is
                         * derived from the default provider. If none is available we do not set the
                         * value.
                         */
                        for (IEnumValue currentEnumValue : currentEnumType.getEnumValues()) {
                            IEnumAttribute defaultProviderAttribute = currentEnumType
                                    .getEnumAttributeIncludeSupertypeCopies(literalNameAttribute
                                            .getDefaultValueProviderAttribute());
                            if (defaultProviderAttribute != null) {
                                List<IEnumAttributeValue> enumAttributeValues = currentEnumValue
                                        .getEnumAttributeValues();
                                IEnumAttributeValue literalNameAttributeValue = enumAttributeValues.get(currentEnumType
                                        .getIndexOfEnumAttribute(literalNameAttribute));
                                IEnumAttributeValue defaultProviderattributeValue = enumAttributeValues
                                        .get(currentEnumType.getIndexOfEnumAttribute(defaultProviderAttribute));
                                literalNameAttributeValue.setValue(defaultProviderattributeValue
                                        .getValueAsLiteralName());
                            }
                        }
                    }
                }
                /*
                 * Mark each enumeration type as dirty so it will be saved an so the old
                 * 'literalName' properties will be removed.
                 */
                currentEnumType.getIpsSrcFile().markAsDirty();
            }
        }

        return new MessageList();
    }

}
