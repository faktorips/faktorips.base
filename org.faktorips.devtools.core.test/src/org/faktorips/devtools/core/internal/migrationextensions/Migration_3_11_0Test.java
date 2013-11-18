/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
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
    public void testMigrate() throws Exception {
        TableStructure tableStructure = newTableStructure(ipsProject, "pack.my.MyTableContent");
        IIpsSrcFile ipsSrcFile = tableStructure.getIpsSrcFile();
        IFile file = ipsSrcFile.getCorrespondingFile();
        writeTestDocument(file);

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
