/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.migrationextensions;

import static org.faktorips.abstracttest.matcher.Matchers.isEmpty;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.enums.EnumContent;
import org.faktorips.devtools.core.internal.model.enums.EnumType;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

public class Migration_3_20_0Test extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private Migration_3_20_0 migration;
    private IFolder outputFolder;

    private void setUpMigration() throws CoreException {
        ipsProject = newIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setBuilderSetId(StandardBuilderSet.ID);
        outputFolder = ipsProject.getProject().getFolder("extension/org/faktorips/sample/model/internal/my");
        ipsProject.setProperties(properties);
        migration = (Migration_3_20_0)new Migration_3_20_0.Factory().createIpsProjectMigrationOperation(ipsProject,
                "irrelevant");
    }

    @Test
    public void testMigrate_rewritesProductCmptWithoutUUID() throws Exception {
        setUpMigration();
        String testFileName = "Product 2017-03";
        ProductCmptType productCmptType = newProductCmptType(ipsProject, "my.PcType");
        productCmptType.getIpsSrcFile().save(true, null);
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "my." + testFileName);
        productCmpt.newGeneration(new GregorianCalendar(2017, Calendar.MARCH, 1));
        productCmpt.getIpsSrcFile().save(true, null);
        fullBuild();
        IFile file = writeTestContents(testFileName);

        MessageList messageList = migration.migrate(new NullProgressMonitor());
        productCmpt.getIpsSrcFile().save(true, null);
        fullBuild();

        assertThat(messageList, isEmpty());

        String fileContent = StringUtil.readFromInputStream(file.getContents(), "UTF-8");
        assertThat(fileContent, is(not(containsString(" id="))));
    }

    @Test
    public void testMigrate_rewritesTableContentWithoutUUID() throws Exception {
        setUpMigration();
        String testFileName = "TableContent";
        TableStructure tableStructure = newTableStructure(ipsProject, "my.TableStructure");
        IColumn column = tableStructure.newColumn();
        column.setDatatype(Datatype.INTEGER.getQualifiedName());
        column.setName("i");
        tableStructure.getIpsSrcFile().save(true, null);
        TableContents tableContents = newTableContents(tableStructure, "my." + testFileName);
        tableContents.getIpsSrcFile().save(true, null);
        fullBuild();
        IFile file = writeTestContents(testFileName);

        MessageList messageList = migration.migrate(new NullProgressMonitor());
        tableContents.getIpsSrcFile().save(true, null);
        fullBuild();

        assertThat(messageList, isEmpty());

        String fileContent = StringUtil.readFromInputStream(file.getContents(), "UTF-8");
        assertThat(fileContent, is(not(containsString(" id="))));
    }

    @Test
    public void testMigrate_rewritesEnumContentWithoutUUID() throws Exception {
        setUpMigration();
        String testFileName = "EnumContent";
        EnumType enumType = newEnumType(ipsProject, "my.EnumType");
        IEnumAttribute attribute = enumType.newEnumAttribute();
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setName("i");
        enumType.getIpsSrcFile().save(true, null);
        EnumContent enumContents = newEnumContent(enumType, "my." + testFileName);
        enumContents.getIpsSrcFile().save(true, null);
        fullBuild();
        IFile file = writeTestContents(testFileName);

        MessageList messageList = migration.migrate(new NullProgressMonitor());
        enumContents.getIpsSrcFile().save(true, null);
        fullBuild();

        assertThat(messageList, isEmpty());

        String fileContent = StringUtil.readFromInputStream(file.getContents(), "UTF-8");
        assertThat(fileContent, is(not(containsString(" id="))));
    }

    private IFile writeTestContents(String testFileName) throws CoreException {
        IFile file = outputFolder.getFile(testFileName + ".xml");
        InputStream resourceAsStream = this.getClass().getClassLoader()
                .getResourceAsStream(getXmlResourceName(testFileName));
        file.setContents(resourceAsStream, true, true, null);
        return file;
    }

}
