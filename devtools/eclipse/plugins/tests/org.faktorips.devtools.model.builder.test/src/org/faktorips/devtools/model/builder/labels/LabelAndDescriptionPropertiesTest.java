/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.labels;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.builder.labels.LabelAndDescriptionProperties.MessageKey;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.model.type.DocumentationKind;
import org.junit.Test;

public class LabelAndDescriptionPropertiesTest extends AbstractIpsPluginTest {

    private IPolicyCmptType pcType;
    private IPolicyCmptTypeAttribute attribute;
    private IPolicyCmptTypeAssociation association;
    private ITableStructure tableStructure;
    private LabelAndDescriptionProperties labelAndDescriptionProperties;
    private IPolicyCmptType target;

    private void createObjectsAndMessages() {
        IIpsProject ipsProject = newIpsProject();
        pcType = newPolicyCmptType(ipsProject, "my.PcType");
        target = newPolicyCmptType(ipsProject, "other.PcType");
        attribute = pcType.newPolicyCmptTypeAttribute("attrName");
        association = pcType.newPolicyCmptTypeAssociation();
        association.setTarget(target.getQualifiedName());
        association.setTargetRoleSingular("target");
        tableStructure = newTableStructure(ipsProject, "my.deep.table.Structure");

        labelAndDescriptionProperties = new LabelAndDescriptionProperties(false, System.lineSeparator());
    }

    @Test
    public void testDeleteAllMessagesFor() throws Exception {
        createObjectsAndMessages();
        labelAndDescriptionProperties.put(attribute, DocumentationKind.LABEL, "Hello World");
        labelAndDescriptionProperties.put(attribute, DocumentationKind.DESCRIPTION, "Lorem Ipsum\ndolor sit amet");
        labelAndDescriptionProperties.put(association, DocumentationKind.LABEL, "Hello World");
        labelAndDescriptionProperties.put(tableStructure, DocumentationKind.LABEL, "TabTab");

        labelAndDescriptionProperties.deleteAllMessagesFor(pcType.getQualifiedNameType());

        Collection<MessageKey> keysForPcType = labelAndDescriptionProperties.getKeysForIpsObject(pcType
                .getQualifiedNameType());
        assertThat(keysForPcType.size(), is(0));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(tableStructure, DocumentationKind.LABEL)
                .getKey()), is(equalTo("TabTab")));
    }

    @Test
    public void testPutAndGet() throws Exception {
        createObjectsAndMessages();
        IPolicyCmptTypeAttribute attribute2 = pcType.newPolicyCmptTypeAttribute("foobar");
        IPolicyCmptTypeAssociation association2 = pcType.newPolicyCmptTypeAssociation();
        association2.setTarget(target.getQualifiedName());
        association2.setTargetRoleSingular("foobar2");

        labelAndDescriptionProperties.put(attribute, DocumentationKind.LABEL, "Hello World");
        labelAndDescriptionProperties.put(attribute, DocumentationKind.DESCRIPTION, "Lorem Ipsum\ndolor sit amet");
        labelAndDescriptionProperties.put(attribute2, DocumentationKind.LABEL, "Foo");
        labelAndDescriptionProperties.put(association, DocumentationKind.LABEL, "Hello World");
        labelAndDescriptionProperties.put(association2, DocumentationKind.LABEL, "Bar");
        labelAndDescriptionProperties.put(tableStructure, DocumentationKind.LABEL, "TabTab");

        Collection<MessageKey> keysForPcType = labelAndDescriptionProperties.getKeysForIpsObject(pcType
                .getQualifiedNameType());
        assertThat(keysForPcType.size(), is(equalTo(5)));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationKind.LABEL).getKey()),
                is(equalTo("Hello World")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationKind.DESCRIPTION)
                .getKey()), is(equalTo("Lorem Ipsum\ndolor sit amet")));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(attribute2, DocumentationKind.LABEL).getKey()),
                is(equalTo("Foo")));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(association, DocumentationKind.LABEL).getKey()),
                is(equalTo("Hello World")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(association2, DocumentationKind.LABEL)
                .getKey()), is(equalTo("Bar")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(tableStructure, DocumentationKind.LABEL)
                .getKey()), is(equalTo("TabTab")));
    }

    @Test
    public void testPutAndGet_emptyMessage() throws Exception {
        createObjectsAndMessages();
        labelAndDescriptionProperties = new LabelAndDescriptionProperties(false, System.lineSeparator());
        IPolicyCmptTypeAttribute attribute2 = pcType.newPolicyCmptTypeAttribute("foobar");
        IPolicyCmptTypeAssociation association2 = pcType.newPolicyCmptTypeAssociation();
        association2.setTarget(target.getQualifiedName());
        association2.setTargetRoleSingular("foobar2");

        labelAndDescriptionProperties.put(attribute, DocumentationKind.LABEL, "Hello World");
        labelAndDescriptionProperties.put(attribute, DocumentationKind.DESCRIPTION, "Lorem Ipsum\ndolor sit amet");
        labelAndDescriptionProperties.put(attribute2, DocumentationKind.LABEL, "Foo");
        labelAndDescriptionProperties.put(association, DocumentationKind.LABEL, "Hello World");
        labelAndDescriptionProperties.put(association2, DocumentationKind.LABEL, "Bar");
        labelAndDescriptionProperties.put(tableStructure, DocumentationKind.LABEL, "TabTab");

        Collection<MessageKey> keysForPcType = labelAndDescriptionProperties.getKeysForIpsObject(pcType
                .getQualifiedNameType());
        assertThat(keysForPcType.size(), is(equalTo(5)));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationKind.LABEL).getKey()),
                is(equalTo("Hello World")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationKind.DESCRIPTION)
                .getKey()), is(equalTo("Lorem Ipsum\ndolor sit amet")));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(attribute2, DocumentationKind.LABEL).getKey()),
                is(equalTo("Foo")));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(association, DocumentationKind.LABEL).getKey()),
                is(equalTo("Hello World")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(association2, DocumentationKind.LABEL)
                .getKey()), is(equalTo("Bar")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(tableStructure, DocumentationKind.LABEL)
                .getKey()), is(equalTo("TabTab")));
    }

    @Test
    public void testPutAndGet_emptyMessageWithDefaultLanguage() throws Exception {
        createObjectsAndMessages();
        labelAndDescriptionProperties = new LabelAndDescriptionProperties(true, System.lineSeparator());
        labelAndDescriptionProperties.put(attribute, DocumentationKind.LABEL, "");
        labelAndDescriptionProperties.put(attribute, DocumentationKind.DESCRIPTION, "");

        Collection<MessageKey> keysForPcType = labelAndDescriptionProperties.getKeysForIpsObject(pcType
                .getQualifiedNameType());
        assertThat(keysForPcType.size(), is(equalTo(2)));
        assertThat(
                labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationKind.LABEL).getKey()),
                is(equalTo("")));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationKind.DESCRIPTION)
                .getKey()), is(equalTo("")));
    }

    @Test
    public void testRemove() throws Exception {
        createObjectsAndMessages();
        labelAndDescriptionProperties.put(attribute, DocumentationKind.LABEL, "Hello World");
        labelAndDescriptionProperties.put(attribute, DocumentationKind.DESCRIPTION, "Lorem Ipsum\ndolor sit amet");

        labelAndDescriptionProperties.remove(new MessageKey(attribute, DocumentationKind.LABEL));

        Collection<MessageKey> keysForPcType = labelAndDescriptionProperties.getKeysForIpsObject(pcType
                .getQualifiedNameType());
        assertThat(keysForPcType.size(), is(equalTo(1)));
        assertThat(labelAndDescriptionProperties.getMessage(new MessageKey(attribute, DocumentationKind.DESCRIPTION)
                .getKey()), is(equalTo("Lorem Ipsum\ndolor sit amet")));
    }

}
