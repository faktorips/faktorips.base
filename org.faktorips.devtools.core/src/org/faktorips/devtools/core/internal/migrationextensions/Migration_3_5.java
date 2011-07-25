/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.migrationextensions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.internal.model.InternationalStringXmlHelper;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsSrcFileMemento;
import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFileMemento;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Migration to version 3.5.
 * <p>
 * Migrates the validation rule XML for localized message texts.
 * 
 * @author dirmeier
 */
public class Migration_3_5 extends DefaultMigration {

    private static final String OLD_XML_ATTR_MESSAGE_TEXT = "messageText"; //$NON-NLS-1$

    public Migration_3_5(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        if (IpsObjectType.POLICY_CMPT_TYPE.equals(srcFile.getIpsObjectType())) {
            IFile file = srcFile.getCorrespondingFile();
            try {
                Element element = getElement(file);
                boolean wasMigrated = migrateXml(element);
                if (wasMigrated) {
                    IIpsSrcFileMemento memento = new IpsSrcFileMemento(srcFile, element, true);
                    srcFile.setMemento(memento);
                    srcFile.markAsDirty();
                }
            } catch (SAXException e) {
                throw new CoreException(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                        "Error while parsing xml of " + file.getFullPath(), e)); //$NON-NLS-1$
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID,
                        "Error while reading file " + file.getFullPath(), e)); //$NON-NLS-1$
            }
        }
    }

    Element getElement(IFile file) throws CoreException, SAXException, IOException {
        InputStream inputStream = file.getContents();
        DocumentBuilder builder = IpsPlugin.getDefault().getDocumentBuilder();
        Document doc = builder.parse(inputStream);
        Element element = doc.getDocumentElement();
        return element;
    }

    boolean migrateXml(Element element) {
        boolean migratedAnyElement = false;
        if (element.getNodeName().equals(IpsObjectType.POLICY_CMPT_TYPE.getXmlElementName())) {
            NodeList rulesNodeList = element.getElementsByTagName(ValidationRule.TAG_NAME);
            for (int i = 0; i < rulesNodeList.getLength(); i++) {
                Element ruleNode = (Element)rulesNodeList.item(i);
                migratedAnyElement |= migrateRuleNode(ruleNode);
            }
        }
        return migratedAnyElement;

    }

    private boolean migrateRuleNode(Element ruleNode) {
        Locale locale = getIpsProject().getProperties().getDefaultLanguage().getLocale();
        String oldMsgText = ruleNode.getAttribute(OLD_XML_ATTR_MESSAGE_TEXT);
        IInternationalString internationalString = new InternationalString();
        internationalString.add(new LocalizedString(locale, oldMsgText));
        InternationalStringXmlHelper.toXml(internationalString, ruleNode, ValidationRule.XML_TAG_MSG_TXT);
        ruleNode.removeAttribute(OLD_XML_ATTR_MESSAGE_TEXT);
        return true;
    }

    @Override
    public String getTargetVersion() {
        return "3.5.0"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return "Migrate the xml of validation rules in policy component types. The xml has changed to support localized message texts"; //$NON-NLS-1$
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {

        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_5(ipsProject, featureId);
        }

    }

}
