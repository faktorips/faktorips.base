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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.IoUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Migration_3_15_0Test extends AbstractIpsPluginTest {

    private String featureId = "anyFeatureId";

    private IIpsProject ipsProject;

    private Migration_3_15_0 migration_3_15_0;

    @Before
    public void setUpMigration() throws CoreException {
        ipsProject = newIpsProject();
        migration_3_15_0 = new Migration_3_15_0(ipsProject, featureId);
    }

    @Test
    public void testMigrate_AddChangingOverTimeFlagForProductCmptTypesToXml() throws Exception {
        Element documentElement = getTestDocument().getDocumentElement();
        assertFalse(documentElement.getAttribute("changingOverTime").contentEquals("true"));

        ProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductCmptTypeName");
        IIpsSrcFile ipsSrcFile = productCmptType.getIpsSrcFile();
        IFile file = ipsSrcFile.getCorrespondingFile();
        writeTestDocument(file);

        migration_3_15_0.migrate(new NullProgressMonitor());

        ipsSrcFile.save(true, null);

        InputStream contents = ipsSrcFile.getCorrespondingFile().getContents(true);
        Document newDocument = getDocumentBuilder().parse(contents);
        Element newDocumentElement = newDocument.getDocumentElement();
        assertTrue(newDocumentElement.getAttribute("changingOverTime").contentEquals("true"));
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
