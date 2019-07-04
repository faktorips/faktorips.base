/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
 * IpsProject-Migration to version 3.6.2
 * <ul>
 * <li>For Setting nodes in AdditionalSettings an attribute "value" is introduced that contains a
 * string. The former boolean attribute "enabled" is removed. Its (boolean) value is persisted as a
 * string representation (e.g. "true" or "false") in the "value" attribute.</li>
 * </ul>
 * 
 * @author Stefan Widmaier
 */
public class Migration_3_6_2 extends AbstractIpsProjectMigrationOperation {

    public Migration_3_6_2(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "Cleanup the AdditionalSettings section in the .ipsproject."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "3.6.2"; //$NON-NLS-1$
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
        IpsProjectProperties data = new IpsProjectProperties(ipsProject);
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
        // CSOFF: IllegalCatch
        try {
            doc = IpsPlugin.getDefault().getDocumentBuilder().parse(is);
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error parsing project file " + file, e)); //$NON-NLS-1$
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
        // CSON: IllegalCatch
        Element documentElement = doc.getDocumentElement();
        Element optionalConstraintsEl = XmlUtil.getFirstElement(documentElement, "AdditionalSettings"); //$NON-NLS-1$
        if (optionalConstraintsEl == null) {
            return msgResultList;
        }

        NodeList nl = optionalConstraintsEl.getElementsByTagName("Setting"); //$NON-NLS-1$
        int length = nl.getLength();
        for (int i = 0; i < length; ++i) {
            Element setting = (Element)nl.item(i);
            if (!setting.hasAttribute("value")) { //$NON-NLS-1$
                setting.setAttribute("value", setting.getAttribute("enable")); //$NON-NLS-1$//$NON-NLS-2$
            }
            setting.removeAttribute("enable"); //$NON-NLS-1$
        }
        // CSOFF: IllegalCatch
        try {
            data = IpsProjectProperties.createFromXml(ipsProject, documentElement);
            data.setCreatedFromParsableFileContents(true);
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Error creating properties from xml, file:  " //$NON-NLS-1$
                    + file, e));
            data.setCreatedFromParsableFileContents(false);
        }
        // CSON: IllegalCatch
        data.setLastPersistentModificationTimestamp(file.getModificationStamp());
        ipsProject.setProperties(data);
        return msgResultList;
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {
        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {

            return new Migration_3_6_2(ipsProject, featureId);
        }
    }
}
