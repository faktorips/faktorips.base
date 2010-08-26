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

package org.faktorips.devtools.core.internal.migration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Migration from Faktor-IPS Version 2.1.0.ms2 to 2.1.0
 * 
 * @author Peter Erzberger
 */
public class Migration_2_1_0_ms2 extends AbstractIpsProjectMigrationOperation {

    public Migration_2_1_0_ms2(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "The structure of the .ipsproject file has changed and must be converted. All other Faktor-IPS files remain unchanged."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "2.1.0.rfinal"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    private void migrateIpsProjectProperties(IProgressMonitor monitor) throws CoreException {
        IFile propertiesFile = getIpsProject().getIpsProjectPropertiesFile();
        InputStream is = null;
        try {
            is = propertiesFile.getContents();
            Document doc = XmlUtil.parseDocument(is);

            // Deleting the Tag <GeneratedSourcecode>
            // <GeneratedSourcecode docLanguage="en" changesInTimeNamingConvention="VAA"/>
            String changesInTimeNamingConventionValue = getIpsProject()
                    .getChangesInTimeNamingConventionForGeneratedCode().getId();
            String docLanguageValue = "en"; //$NON-NLS-1$
            NodeList GeneratedSourcecodeNl = doc.getElementsByTagName("GeneratedSourcecode"); //$NON-NLS-1$
            if (GeneratedSourcecodeNl.getLength() > 0) {
                Element generatedSourcecodeEl = (Element)GeneratedSourcecodeNl.item(0);
                changesInTimeNamingConventionValue = generatedSourcecodeEl
                        .getAttribute("changesInTimeNamingConvention"); //$NON-NLS-1$
                docLanguageValue = generatedSourcecodeEl.getAttribute("docLanguage"); //$NON-NLS-1$
                generatedSourcecodeEl.getParentNode().removeChild(generatedSourcecodeEl);
            }

            // Adding the attribute "changesInTimeNamingConvention" to the Tag <IpsProject>
            NodeList ipsProjectNl = doc.getElementsByTagName(IpsProjectProperties.TAG_NAME);
            if (ipsProjectNl.getLength() == 1) {
                Element projectEl = (Element)ipsProjectNl.item(0);
                projectEl.setAttribute("changesInTimeNamingConvention", changesInTimeNamingConventionValue); //$NON-NLS-1$
            }

            // Deleting the attribute "loggingFrameworkConnectorId" from the tag
            // <IpsArtefactBuilderSet>
            // <IpsArtefactBuilderSet id="org.faktorips.devtools.stdbuilder.ipsstdbuilderset"
            // loggingFrameworkConnectorId="de.qv.faktorips.kqvLoggingConnector">
            NodeList builderSetNl = doc.getElementsByTagName("IpsArtefactBuilderSet"); //$NON-NLS-1$
            String attributeValueLoggingFrameworkConnectorId = ""; //$NON-NLS-1$
            if (builderSetNl.getLength() == 1) {
                Element builderSetEl = (Element)builderSetNl.item(0);
                attributeValueLoggingFrameworkConnectorId = builderSetEl.getAttribute("loggingFrameworkConnectorId"); //$NON-NLS-1$
                builderSetEl.removeAttribute("loggingFrameworkConnectorId"); //$NON-NLS-1$

                // Deleting the sub element <Property> with the name "generateLoggingStatements"
                // from the tag <IpsArtefactBuilderSetConfig>
                // <IpsArtefactBuilderSetConfig>
                // <Property name="generateLoggingStatements" value="false"/>
                NodeList builderSetConfigNl = doc.getElementsByTagName("IpsArtefactBuilderSetConfig"); //$NON-NLS-1$
                boolean attributeValueLoggingEnabled = false;
                if (builderSetConfigNl.getLength() == 1) {
                    Element builderSetConfigEl = (Element)builderSetConfigNl.item(0);
                    NodeList propertiesNl = builderSetConfigEl.getElementsByTagName("Property"); //$NON-NLS-1$
                    for (int i = 0; i < propertiesNl.getLength(); i++) {
                        Element propertyEl = (Element)propertiesNl.item(i);
                        String propertyName = propertyEl.getAttribute("name"); //$NON-NLS-1$
                        if (propertyName.equals("generateLoggingStatements")) { //$NON-NLS-1$
                            String generateLoggingStatementsValue = propertyEl.getAttribute("value"); //$NON-NLS-1$
                            attributeValueLoggingEnabled = Boolean.valueOf(generateLoggingStatementsValue)
                                    .booleanValue();
                            builderSetConfigEl.removeChild(propertyEl);
                            break;
                        }
                    }
                    // Adding the sub element <Property> with the name "generatorLocale".
                    // <Property name="generatorLocale" value="en"/>
                    Element propertyGeneratorLocale = doc.createElement("Property"); //$NON-NLS-1$
                    propertyGeneratorLocale.setAttribute("name", "generatorLocale"); //$NON-NLS-1$//$NON-NLS-2$
                    propertyGeneratorLocale.setAttribute("value", docLanguageValue); //$NON-NLS-1$
                    builderSetConfigEl.appendChild(propertyGeneratorLocale);

                    // Adding the sub element <Property> with the name "loggingFrameworkConnector".
                    // <Property name="loggingFrameworkConnector" value="None"/>
                    Element propertyLoggingConnector = doc.createElement("Property"); //$NON-NLS-1$
                    if (StringUtils.isEmpty(attributeValueLoggingFrameworkConnectorId)) {
                        propertyLoggingConnector.setAttribute("name", "loggingFrameworkConnector"); //$NON-NLS-1$//$NON-NLS-2$
                        propertyLoggingConnector.setAttribute("value", "None"); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (attributeValueLoggingEnabled) {
                        propertyLoggingConnector.setAttribute("name", "loggingFrameworkConnector"); //$NON-NLS-1$ //$NON-NLS-2$
                        propertyLoggingConnector.setAttribute("value", attributeValueLoggingFrameworkConnectorId); //$NON-NLS-1$
                    } else {
                        propertyLoggingConnector.setAttribute("name", "loggingFrameworkConnector"); //$NON-NLS-1$ //$NON-NLS-2$
                        propertyLoggingConnector.setAttribute("value", "None"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    builderSetConfigEl.appendChild(propertyLoggingConnector);
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XmlUtil.writeXMLtoStream(bos, doc, null, 2, getIpsProject().getXmlFileCharset());
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            propertiesFile.setContents(bis, true, false, monitor);
        } catch (Exception e) {
            throw new CoreException(new IpsStatus(e));
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new CoreException(new IpsStatus(e));
                }
            }
        }
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException {
        migrateIpsProjectProperties(monitor);
        return new MessageList();
    }
}
