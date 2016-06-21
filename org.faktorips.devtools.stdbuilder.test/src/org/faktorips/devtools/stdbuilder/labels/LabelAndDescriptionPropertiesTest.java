/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.labels;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.labels.LabelAndDescriptionProperties.MessageKey;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.junit.Test;

public class LabelAndDescriptionPropertiesTest extends AbstractIpsPluginTest {

    private IPolicyCmptType pcType;
    private IPolicyCmptTypeAttribute attribute;
    private IPolicyCmptTypeAssociation association;
    private TableStructure tableStructure;
    private LabelAndDescriptionProperties labelAndDescriptionProperties;
    private PolicyCmptType target;

    private void createObjectsAndMessages() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        pcType = newPolicyCmptType(ipsProject, "my.PcType");
        target = newPolicyCmptType(ipsProject, "other.PcType");
        attribute = pcType.newPolicyCmptTypeAttribute("attrName");
        association = pcType.newPolicyCmptTypeAssociation();
        association.setTarget(target.getQualifiedName());
        association.setTargetRoleSingular("target");
        tableStructure = newTableStructure(ipsProject, "my.deep.table.Structure");

        labelAndDescriptionProperties = new LabelAndDescriptionProperties(false);
    }

    @Test
    public void testDeleteAllMessagesFor() throws Exception {
        createObjectsAndMessages();
        labelAndDescriptionProperties.put(attribute, DocumentationType.LABEL, "Hello World");
        labelAndDescriptionProperties.put(attribute, DocumentationType.DESCRIPTION, "Lorem Ipsum\ndolor sit amet");
        labelAndDescriptionProperties.put(association, DocumentationType.LABEL, "Hello World");
        labelAndDescriptionProperties.put(tableStructure, DocumentationType.LABEL, "TabTab");

        labelAndDescriptionProperties.deleteAllMessagesFor(pcType.getQualifiedNameType());

        Collection<MessageKey> keysForPcType = labelAndDescriptionProperties.getKeysForIpsObject(pcType
                .getQualifiedNameType());
        assertThat(keysForPcType.size(), is(0));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(tableStructure, DocumentationType.LABEL)
                .getKey()), is(equalTo("TabTab")));
    }

    @Test
    public void testPutAndGet() throws Exception {
        createObjectsAndMessages();
        IPolicyCmptTypeAttribute attribute2 = pcType.newPolicyCmptTypeAttribute("foobar");
        IPolicyCmptTypeAssociation association2 = pcType.newPolicyCmptTypeAssociation();
        association2.setTarget(target.getQualifiedName());
        association2.setTargetRoleSingular("foobar2");

        labelAndDescriptionProperties.put(attribute, DocumentationType.LABEL, "Hello World");
        labelAndDescriptionProperties.put(attribute, DocumentationType.DESCRIPTION, "Lorem Ipsum\ndolor sit amet");
        labelAndDescriptionProperties.put(attribute2, DocumentationType.LABEL, "Foo");
        labelAndDescriptionProperties.put(association, DocumentationType.LABEL, "Hello World");
        labelAndDescriptionProperties.put(association2, DocumentationType.LABEL, "Bar");
        labelAndDescriptionProperties.put(tableStructure, DocumentationType.LABEL, "TabTab");

        Collection<MessageKey> keysForPcType = labelAndDescriptionProperties.getKeysForIpsObject(pcType
                .getQualifiedNameType());
        assertThat(keysForPcType.size(), is(equalTo(5)));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationType.LABEL).getKey()),
                is(equalTo("Hello World")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationType.DESCRIPTION)
                .getKey()), is(equalTo("Lorem Ipsum\ndolor sit amet")));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(attribute2, DocumentationType.LABEL).getKey()),
                is(equalTo("Foo")));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(association, DocumentationType.LABEL).getKey()),
                is(equalTo("Hello World")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(association2, DocumentationType.LABEL)
                .getKey()), is(equalTo("Bar")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(tableStructure, DocumentationType.LABEL)
                .getKey()), is(equalTo("TabTab")));
    }

    @Test
    public void testPutAndGet_emptyMessage() throws Exception {
        createObjectsAndMessages();
        labelAndDescriptionProperties = new LabelAndDescriptionProperties(false);
        IPolicyCmptTypeAttribute attribute2 = pcType.newPolicyCmptTypeAttribute("foobar");
        IPolicyCmptTypeAssociation association2 = pcType.newPolicyCmptTypeAssociation();
        association2.setTarget(target.getQualifiedName());
        association2.setTargetRoleSingular("foobar2");

        labelAndDescriptionProperties.put(attribute, DocumentationType.LABEL, "Hello World");
        labelAndDescriptionProperties.put(attribute, DocumentationType.DESCRIPTION, "Lorem Ipsum\ndolor sit amet");
        labelAndDescriptionProperties.put(attribute2, DocumentationType.LABEL, "Foo");
        labelAndDescriptionProperties.put(association, DocumentationType.LABEL, "Hello World");
        labelAndDescriptionProperties.put(association2, DocumentationType.LABEL, "Bar");
        labelAndDescriptionProperties.put(tableStructure, DocumentationType.LABEL, "TabTab");

        Collection<MessageKey> keysForPcType = labelAndDescriptionProperties.getKeysForIpsObject(pcType
                .getQualifiedNameType());
        assertThat(keysForPcType.size(), is(equalTo(5)));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationType.LABEL).getKey()),
                is(equalTo("Hello World")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationType.DESCRIPTION)
                .getKey()), is(equalTo("Lorem Ipsum\ndolor sit amet")));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(attribute2, DocumentationType.LABEL).getKey()),
                is(equalTo("Foo")));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(association, DocumentationType.LABEL).getKey()),
                is(equalTo("Hello World")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(association2, DocumentationType.LABEL)
                .getKey()), is(equalTo("Bar")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(tableStructure, DocumentationType.LABEL)
                .getKey()), is(equalTo("TabTab")));
    }

    @Test
    public void testPutAndGet_emptyMessageWithDefaultLanguage() throws Exception {
        createObjectsAndMessages();
        labelAndDescriptionProperties = new LabelAndDescriptionProperties(true);
        labelAndDescriptionProperties.put(attribute, DocumentationType.LABEL, "");
        labelAndDescriptionProperties.put(attribute, DocumentationType.DESCRIPTION, "");

        Collection<MessageKey> keysForPcType = labelAndDescriptionProperties.getKeysForIpsObject(pcType
                .getQualifiedNameType());
        assertThat(keysForPcType.size(), is(equalTo(2)));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationType.LABEL).getKey()),
                is(equalTo("")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationType.DESCRIPTION)
                .getKey()), is(equalTo("")));
    }

    @Test
    public void testRemove() throws Exception {
        createObjectsAndMessages();
        labelAndDescriptionProperties.put(attribute, DocumentationType.LABEL, "Hello World");
        labelAndDescriptionProperties.put(attribute, DocumentationType.DESCRIPTION, "Lorem Ipsum\ndolor sit amet");

        labelAndDescriptionProperties.remove(new MessageKey(attribute, DocumentationType.LABEL));

        Collection<MessageKey> keysForPcType = labelAndDescriptionProperties.getKeysForIpsObject(pcType
                .getQualifiedNameType());
        assertThat(keysForPcType.size(), is(equalTo(1)));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationType.DESCRIPTION)
                .getKey()), is(equalTo("Lorem Ipsum\ndolor sit amet")));
    }

}
