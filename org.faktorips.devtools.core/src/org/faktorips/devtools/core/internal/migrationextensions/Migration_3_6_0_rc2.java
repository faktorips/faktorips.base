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

package org.faktorips.devtools.core.internal.migrationextensions;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Migration to version 3.6.0.rc2
 * <ul>
 * <li>The OptionalConstraints section in the .ipsproject settings file is renamed to
 * AdditionalSettings.</li>
 * </ul>
 * 
 * @author Daniel Schwering
 */
public class Migration_3_6_0_rc2 extends AbstractIpsProjectMigrationOperation {

    public Migration_3_6_0_rc2(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "The OptionalConstraints section in the .ipsproject settings file is renamed to AdditionalSettings."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "3.6.0.rc2"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {
        MessageList msgResultList = new MessageList();

        IpsProject ipsProject = (IpsProject)getIpsProject();
        IFile file = ipsProject.getIpsProjectPropertiesFile();
        IpsProjectProperties data = new IpsProjectProperties();
        data.setCreatedFromParsableFileContents(false);
        if (!file.exists()) {
            return msgResultList;
        }
        Document doc;
        InputStream is;
        try {
            is = file.getContents(true);
        } catch (CoreException e1) {
            IpsPlugin.log(new IpsStatus("Error reading project file contents " //$NON-NLS-1$
                    + file, e1));
            return msgResultList;
        }
        try {
            doc = IpsPlugin.getDefault().getDocumentBuilder().parse(is);
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error parsing project file " + file, e));//$NON-NLS-1$
            return msgResultList;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                IpsPlugin.log(new IpsStatus("Error closing input stream after reading project file " //$NON-NLS-1$
                        + file, e));
                return msgResultList;
            }
        }

        Element documentElement = doc.getDocumentElement();
        Element optionalConstraintsEl = XmlUtil.getFirstElement(documentElement, "OptionalConstraints"); //$NON-NLS-1$
        if (optionalConstraintsEl == null) {
            return msgResultList;
        }
        documentElement.removeChild(optionalConstraintsEl);
        Element additionalSettingsEl = doc.createElement("AdditionalSettings"); //$NON-NLS-1$
        documentElement.appendChild(additionalSettingsEl);

        NodeList nl = optionalConstraintsEl.getElementsByTagName("Constraint"); //$NON-NLS-1$
        int length = nl.getLength();
        for (int i = 0; i < length; ++i) {
            Element constraint = (Element)nl.item(i);
            Element setting = doc.createElement("Setting"); //$NON-NLS-1$
            setting.setAttribute("name", constraint.getAttribute("name")); //$NON-NLS-1$//$NON-NLS-2$
            setting.setAttribute("enable", constraint.getAttribute("enable")); //$NON-NLS-1$//$NON-NLS-2$
            additionalSettingsEl.appendChild(setting);
        }

        try {
            data = IpsProjectProperties.createFromXml(ipsProject, documentElement);
            data.setCreatedFromParsableFileContents(true);
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error creating properties from xml, file:  " //$NON-NLS-1$
                    + file, e));
            data.setCreatedFromParsableFileContents(false);
        }
        data.setLastPersistentModificationTimestamp(new Long(file.getModificationStamp()));
        ipsProject.setProperties(data);
        return msgResultList;
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {

            return new Migration_3_6_0_rc2(ipsProject, featureId);
        }
    }
}
