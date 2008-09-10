/**
 * 
 */
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

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Some bugs fixed."; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetVersion() {
        return "2.1.0.rfinal"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return false;
    }

    private void migrateIpsProjectProperties(IProgressMonitor monitor) throws CoreException{
        IFile propertiesFile = getIpsProject().getIpsProjectPropertiesFile();
        InputStream is = null;
        try {
            is = propertiesFile.getContents();
            Document doc = XmlUtil.getDocument(is);
            
            //Deleting the Tag <GeneratedSourcecode>  
//          <GeneratedSourcecode docLanguage="en" changesInTimeNamingConvention="VAA"/>
            String changesInTimeNamingConventionValue = getIpsProject().getChangesInTimeNamingConventionForGeneratedCode().getId();
            String docLanguageValue = "en";
            NodeList GeneratedSourcecodeNl = doc.getElementsByTagName("GeneratedSourcecode");
            if(GeneratedSourcecodeNl.getLength() > 0){
                Element generatedSourcecodeEl = (Element)GeneratedSourcecodeNl.item(0);
                changesInTimeNamingConventionValue = generatedSourcecodeEl.getAttribute("changesInTimeNamingConvention");
                docLanguageValue = generatedSourcecodeEl.getAttribute("docLanguage");
                generatedSourcecodeEl.getParentNode().removeChild(generatedSourcecodeEl);
            }
            
            //Adding the attribute "changesInTimeNamingConvention" to the Tag <IpsProject>
            NodeList ipsProjectNl = doc.getElementsByTagName(IpsProjectProperties.TAG_NAME);
            if(ipsProjectNl.getLength() == 1){
                Element projectEl = (Element)ipsProjectNl.item(0);
                projectEl.setAttribute("changesInTimeNamingConvention", changesInTimeNamingConventionValue);
            }

            //Deleting the attribute "loggingFrameworkConnectorId" from the tag <IpsArtefactBuilderSet> 
//          <IpsArtefactBuilderSet id="org.faktorips.devtools.stdbuilder.ipsstdbuilderset" loggingFrameworkConnectorId="de.qv.faktorips.kqvLoggingConnector">
            NodeList builderSetNl = doc.getElementsByTagName("IpsArtefactBuilderSet");
            String attributeValueLoggingFrameworkConnectorId = ""; 
            if(builderSetNl.getLength() == 1){
                Element builderSetEl = (Element)builderSetNl.item(0);
                attributeValueLoggingFrameworkConnectorId = builderSetEl.getAttribute("loggingFrameworkConnectorId");
                builderSetEl.removeAttribute("loggingFrameworkConnectorId");
                
                //Deleting the sub element <Property> with the name "generateLoggingStatements" from the tag <IpsArtefactBuilderSetConfig>
//              <IpsArtefactBuilderSetConfig>
//                  <Property name="generateLoggingStatements" value="false"/>
                NodeList builderSetConfigNl = doc.getElementsByTagName("IpsArtefactBuilderSetConfig");
                boolean attributeValueLoggingEnabled = false;
                if(builderSetConfigNl.getLength() == 1){
                    Element builderSetConfigEl = (Element)builderSetConfigNl.item(0);
                    NodeList propertiesNl = builderSetConfigEl.getElementsByTagName("Property");
                    for (int i = 0; i < propertiesNl.getLength(); i++) {
                        Element propertyEl = (Element)propertiesNl.item(i);
                        String propertyName = propertyEl.getAttribute("name");
                        if(propertyName.equals("generateLoggingStatements")){
                            String generateLoggingStatementsValue = propertyEl.getAttribute("value");
                            attributeValueLoggingEnabled = Boolean.valueOf(generateLoggingStatementsValue).booleanValue();
                            builderSetConfigEl.removeChild(propertyEl);
                            break;
                        }
                    }
                    //Adding the sub element <Property> with the name "generatorLocale".
//              <Property name="generatorLocale" value="en"/>
                    Element propertyGeneratorLocale = doc.createElement("Property");
                    propertyGeneratorLocale.setAttribute("name", "generatorLocale");
                    propertyGeneratorLocale.setAttribute("value", docLanguageValue);
                    builderSetConfigEl.appendChild(propertyGeneratorLocale);
                    
                    //Adding the sub element <Property> with the name "loggingFrameworkConnector".
//              <Property name="loggingFrameworkConnector" value="None"/>
                    Element propertyLoggingConnector = doc.createElement("Property");
                    if(StringUtils.isEmpty(attributeValueLoggingFrameworkConnectorId)){
                        propertyLoggingConnector.setAttribute("name", "loggingFrameworkConnector");
                        propertyLoggingConnector.setAttribute("value", "None");
                    } else if(attributeValueLoggingEnabled){
                        propertyLoggingConnector.setAttribute("name", "loggingFrameworkConnector");
                        propertyLoggingConnector.setAttribute("value", attributeValueLoggingFrameworkConnectorId);
                    } else {
                        propertyLoggingConnector.setAttribute("name", "loggingFrameworkConnector");
                        propertyLoggingConnector.setAttribute("value", "None");
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
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    throw new CoreException(new IpsStatus(e));
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException {
        migrateIpsProjectProperties(monitor);
        return new MessageList();
    }
}