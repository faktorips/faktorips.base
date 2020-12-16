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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.IoUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class Migration_3_13_0Test extends AbstractIpsPluginTest {

    private String featureId = "anyFeatureId";

    private IIpsProject ipsProject;

    private Migration_3_13_0 migration_3_13_0;

    @Before
    public void setUpMigration() throws CoreException {
        ipsProject = newIpsProject();
        migration_3_13_0 = new Migration_3_13_0(ipsProject, featureId);
    }

    @Test
    public void testMigrate_tableContents() throws Exception {
        Element documentElement = getTestDocument().getDocumentElement();
        assertNotNull(XmlUtil.getFirstElement(documentElement, "Generation"));
        assertNull(XmlUtil.getFirstElement(documentElement, "Rows"));

        TableStructure tableStructure = newTableStructure(ipsProject, "pack.my.MyTableContent");
        TableContents tableContents = newTableContents(tableStructure, "MyTableContent");
        IIpsSrcFile ipsSrcFile = tableContents.getIpsSrcFile();
        IFile file = ipsSrcFile.getCorrespondingFile();
        writeTestDocument(file);

        migration_3_13_0.migrate(new NullProgressMonitor());

        ipsSrcFile.save(true, null);

        ITableContents newTableContents = (ITableContents)ipsSrcFile.getIpsObject();
        ITableRows tableRows = newTableContents.getTableRows();
        assertNotNull(tableRows);
        assertEquals(2, tableRows.getRows().length);

        InputStream contents = ipsSrcFile.getCorrespondingFile().getContents(true);
        Document newDocument = getDocumentBuilder().parse(contents);
        Element newDocumentElement = newDocument.getDocumentElement();
        assertNotNull(XmlUtil.getFirstElement(newDocumentElement, "Rows"));
        assertNull(XmlUtil.getFirstElement(newDocumentElement, "Generation"));

    }

    private void writeTestDocument(IFile file) {
        ByteArrayInputStream bis = null;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            XmlUtil.writeXMLtoStream(bos, getTestDocument(), null, 2, ipsProject.getXmlFileCharset());
            bis = new ByteArrayInputStream(bos.toByteArray());
            file.setContents(bis, true, false, null);
        } catch (TransformerException e) {
            throw new CoreRuntimeException(new CoreException(new IpsStatus(e)));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        } finally {
            IoUtil.close(bis);
            IoUtil.close(bos);
        }
    }
}
