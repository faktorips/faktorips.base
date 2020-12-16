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
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.IoUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;

@RunWith(MockitoJUnitRunner.class)
public class Migration_3_11_0Test extends AbstractIpsPluginTest {

    private String featureId = "anyFeatureId";

    private IIpsProject ipsProject;

    private Migration_3_11_0 migration_3_11_0;

    @Before
    public void setUpMigration() throws CoreException {
        ipsProject = newIpsProject();
        migration_3_11_0 = new Migration_3_11_0(ipsProject, featureId);
    }

    @Test
    public void testMigrate_tableStructure() throws Exception {
        TableStructure tableStructure = newTableStructure(ipsProject, "pack.my.MyTableContent");
        IIpsSrcFile ipsSrcFile = tableStructure.getIpsSrcFile();
        IFile file = ipsSrcFile.getCorrespondingFile();
        writeTestDocument(file, getTableStructureTestResource());

        migration_3_11_0.migrate(new NullProgressMonitor());

        ipsSrcFile.save(true, null);

        ITableStructure newTableStructure = (ITableStructure)ipsSrcFile.getIpsObject();
        assertEquals(2, newTableStructure.getIndices().size());
        assertEquals(2, newTableStructure.getUniqueKeys().length);
        IIndex index1 = newTableStructure.getIndex("item1, item2");
        assertEquals("4", index1.getId());
        assertTrue(index1.isUniqueKey());
        IIndex index2 = newTableStructure.getIndex("anotherKey");
        assertEquals("6", index2.getId());
        assertTrue(index2.isUniqueKey());
    }

    private String getTableStructureTestResource() {
        return getClass().getName().replaceAll("\\.", "/") + "_tableStructure.xml";
    }

    @Test
    public void testMigrate_enumType() throws Exception {
        EnumType enumType = newEnumType(ipsProject, "pack.my.MyEnumType");
        IIpsSrcFile ipsSrcFile = enumType.getIpsSrcFile();
        IFile file = ipsSrcFile.getCorrespondingFile();
        writeTestDocument(file, getEnumTypeTestResource());

        migration_3_11_0.migrate(new NullProgressMonitor());

        ipsSrcFile.save(true, null);

        IEnumType newEnumType = (IEnumType)ipsSrcFile.getIpsObject();
        assertTrue(newEnumType.isExtensible());
    }

    private String getEnumTypeTestResource() {
        return getClass().getName().replaceAll("\\.", "/") + "_enumType.xml";
    }

    private void writeTestDocument(IFile file, String resourceName) {
        ByteArrayInputStream bis = null;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            XmlUtil.writeXMLtoStream(bos, getTestDocument(resourceName), null, 2, ipsProject.getXmlFileCharset());
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

    @Override
    public Document getTestDocument(String resourceName) {
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream(resourceName);
            if (is == null) {
                throw new RuntimeException("Can't find resource " + resourceName);
            }
            return getDocumentBuilder().parse(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IoUtil.close(is);
        }
    }

}
