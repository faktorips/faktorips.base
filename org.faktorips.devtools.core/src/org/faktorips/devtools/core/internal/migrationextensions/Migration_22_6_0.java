/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.migrationextensions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.jar.Manifest;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.builder.settings.ValueSetMethods;
import org.faktorips.devtools.model.internal.ipsproject.IpsBundleManifest;
import org.faktorips.devtools.model.internal.pctype.ValidationRule;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.util.EclipseIOUtil;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.model.versionmanager.options.IpsEnumMigrationOption;
import org.faktorips.devtools.model.versionmanager.options.IpsMigrationOption;
import org.faktorips.runtime.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Migration_22_6_0 extends MarkAsDirtyMigration {

    private static final String VERSION_22_6_0 = "22.6.0"; //$NON-NLS-1$
    private static final String MIGRATION_OPTION_UNIFY_VALUE_SET = "valueSetMethods"; //$NON-NLS-1$

    private final IpsMigrationOption<ValueSetMethods> valueSetMethodsOption = new IpsEnumMigrationOption<>(
            MIGRATION_OPTION_UNIFY_VALUE_SET,
            Messages.Migration_22_6_0_unifyValueSet,
            ValueSetMethods.ByValueSetType,
            ValueSetMethods.class);

    public Migration_22_6_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate,
                featureId,
                getAffectedIpsObjectTypes(projectToMigrate),
                VERSION_22_6_0,
                Messages.Migration_22_6_0_description);
    }

    private static Set<IpsObjectType> getAffectedIpsObjectTypes(IIpsProject ipsProject) {
        if (ipsProject.getProperties().isValidateIpsSchema()) {
            return Set.of(IIpsModel.get().getIpsObjectTypes());
        }
        return Set.of(IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.POLICY_CMPT_TYPE);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws InvocationTargetException {
        IIpsProject ipsProject = getIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();

        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel()
                .getIpsArtefactBuilderSetInfo(properties.getBuilderSetId());
        IIpsArtefactBuilderSetConfigModel builderSetConfig = properties.getBuilderSetConfig();

        ValueSetMethods selectedValue = valueSetMethodsOption.getSelectedValue();
        builderSetConfig.setPropertyValue(MIGRATION_OPTION_UNIFY_VALUE_SET,
                selectedValue != null ? selectedValue.toString() : null,
                Messages.Migration_Option_Unify_Value_Set_Description);

        MigrationUtil.updateAllIpsArtefactBuilderSetDescriptions(builderSetInfo, builderSetConfig);

        ipsProject.setProperties(properties);
        updateManifest();
        return super.migrate(monitor);
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) {
        if (srcFile.getIpsObjectType().equals(IpsObjectType.POLICY_CMPT_TYPE)) {
            try {
                boolean changed = false;
                InputStream is = srcFile.getContentFromEnclosingResource();
                Document document = XmlUtil.getDefaultDocumentBuilder().parse(is);
                Element doc = document.getDocumentElement();
                NodeList childNodes = doc.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node item = childNodes.item(i);
                    if (ValidationRule.TAG_NAME.equals(item.getNodeName())) {
                        changed = true;
                        Element validationRuleDef = (Element)item;
                        validationRuleDef.removeAttribute("appliedForAllBusinessFunctions"); //$NON-NLS-1$
                        NodeList childNodes2 = validationRuleDef.getChildNodes();
                        for (int j = 0; j < childNodes2.getLength(); j++) {
                            Node child = childNodes2.item(j);
                            if ("BusinessFunction".equals(child.getNodeName())) { //$NON-NLS-1$
                                item.removeChild(child);
                            }
                        }
                    }
                }
                if (changed) {
                    writeToFile(srcFile, doc);
                }
            } catch (SAXException | IOException e) {
                throw new IpsException(new IpsStatus(e));
            }
        }
        super.migrate(srcFile);
    }

    @Override
    protected boolean migrate(AFile file) {
        if ("ipsbf".equals(file.getExtension()) || "ipsbusinessfunction".equals(file.getExtension())) { //$NON-NLS-1$ //$NON-NLS-2$
            file.delete(null);
            return true;
        }
        return false;
    }

    private void writeToFile(IIpsSrcFile srcFile, Element doc) {
        XmlUtil.resetValidatingDocumentBuilders();
        try {
            if (getIpsProject().getReadOnlyProperties().isValidateIpsSchema()) {
                String xmlNamespace = XmlUtil.XML_IPS_DEFAULT_NAMESPACE;
                doc.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, xmlNamespace);
                doc.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                        "xsi:schemaLocation", //$NON-NLS-1$
                        xmlNamespace + " " //$NON-NLS-1$
                                + XmlUtil.getSchemaLocation(srcFile.getIpsObjectType()));
            }
            String xmlFileCharset = srcFile.getIpsProject().getXmlFileCharset();
            String nodeToString = XmlUtil.nodeToString(doc, xmlFileCharset,
                    srcFile.getIpsProject().getReadOnlyProperties().isEscapeNonStandardBlanks());
            EclipseIOUtil.writeToFile(srcFile.getCorrespondingFile().unwrap(),
                    new ByteArrayInputStream(nodeToString.getBytes(xmlFileCharset)), true, true,
                    new NullProgressMonitor());
        } catch (TransformerException | UnsupportedEncodingException e) {
            throw new IpsException(new IpsStatus(e));
        }

    }

    private void updateManifest() {
        IIpsProject ipsProject = getIpsProject();
        AFile manifestFile = ipsProject.getProject().getFile(IpsBundleManifest.MANIFEST_NAME);
        if (manifestFile.exists()) {
            try {
                Manifest manifest = new Manifest(manifestFile.getContents());
                IpsBundleManifest ipsBundleManifest = new IpsBundleManifest(manifest);
                ipsBundleManifest.writeBuilderSettings(ipsProject);
            } catch (IOException e) {
                throw new IpsException(new IpsStatus("Can't read " + manifestFile, e)); //$NON-NLS-1$
            }

        }
    }

    @Override
    public Collection<IpsMigrationOption<?>> getOptions() {
        return Collections.singleton(valueSetMethodsOption);
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_22_6_0(ipsProject, featureId);
        }
    }
}
