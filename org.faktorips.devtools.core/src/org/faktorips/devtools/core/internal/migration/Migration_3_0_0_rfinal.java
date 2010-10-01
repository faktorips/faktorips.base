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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Migration from Faktor-IPS Version 3.0.0.rfinal to 3.0.1.rfinal
 * 
 * @author Joerg Ortmann
 */
public class Migration_3_0_0_rfinal extends AbstractIpsProjectMigrationOperation {

    public Migration_3_0_0_rfinal(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return "The content of the .ipsproject file has changed: new PersistenceOptions attributes. All other Faktor-IPS files remain unchanged."; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "3.0.1.rfinal"; //$NON-NLS-1$
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

            // Adding the attributes "maxTableColumnScale", "maxTableColumnPrecision",
            // "maxTableColumnSize" to the Tag <PersistenceOptions>
            NodeList ipsProjectNl = doc.getElementsByTagName(IPersistenceOptions.XML_TAG_NAME);
            if (ipsProjectNl.getLength() == 1) {
                Element projectEl = (Element)ipsProjectNl.item(0);
                projectEl.setAttribute(IPersistenceOptions.MAX_TABLE_COLUMN_SCALE, "255"); //$NON-NLS-1$
                projectEl.setAttribute(IPersistenceOptions.MAX_TABLE_COLUMN_PRECISION, "255"); //$NON-NLS-1$
                projectEl.setAttribute(IPersistenceOptions.MAX_TABLE_COLUMN_SIZE, "255"); //$NON-NLS-1$
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
