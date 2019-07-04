package org.faktorips.devtools.core.internal.migrationextensions;

/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

import static org.faktorips.abstracttest.matcher.Matchers.isEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.tablecontents.TableContents;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.util.StringUtil;
import org.faktorips.util.message.MessageList;
import org.junit.Test;

/**
 * These tests are located in the stdbuilder project because they need a full build with active
 * builder plugin
 * 
 */
public class Migration_3_21_0Test extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private Migration_3_21_0 migration;
    private IFolder outputFolder;

    private void setUpMigration() throws CoreException {
        ipsProject = newIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setBuilderSetId("org.faktorips.devtools.stdbuilder.ipsstdbuilderset");
        outputFolder = ipsProject.getProject().getFolder("extension/org/faktorips/sample/model/internal/my");
        ipsProject.setProperties(properties);
        migration = (Migration_3_21_0)new Migration_3_21_0.Factory().createIpsProjectMigrationOpertation(ipsProject,
                "irrelevant");
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
        List<String> elementsWithId = findUnwantedIds(fileContent);
        assertThat("unwanted ids after:" + elementsWithId, elementsWithId.size(), is(0));
    }

    private List<String> findUnwantedIds(String fileContent) {
        Pattern pattern = Pattern.compile("(\\w+) id=");
        Matcher matcher = pattern.matcher(fileContent);
        List<String> elementsWithId = new ArrayList<String>();
        while (matcher.find()) {
            String elementWithId = matcher.group(1);
            if (!elementWithId.equals("Description")) {
                elementsWithId.add(elementWithId);
            }
        }
        return elementsWithId;
    }

    private IFile writeTestContents(String testFileName) throws CoreException {
        IFile file = outputFolder.getFile(testFileName + ".xml");
        InputStream resourceAsStream = this.getClass().getClassLoader()
                .getResourceAsStream(getXmlResourceName(testFileName));
        file.setContents(resourceAsStream, true, true, null);
        return file;
    }

}
