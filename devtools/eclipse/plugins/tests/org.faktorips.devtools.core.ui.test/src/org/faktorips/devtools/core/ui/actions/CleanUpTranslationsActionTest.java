/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.enums.EnumContent;
import org.faktorips.devtools.model.internal.enums.EnumType;
import org.faktorips.devtools.model.internal.tablecontents.TableContents;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.values.LocalizedString;
import org.junit.Test;

public class CleanUpTranslationsActionTest extends AbstractIpsPluginTest {

    @Test
    public void testRun_CleansUpTableContents() throws IOException, InvocationTargetException, InterruptedException {
        IIpsProject ipsProject = newIpsProject(List.of(Locale.GERMAN, Locale.ENGLISH));
        TableStructure tableStructure = newTableStructure(ipsProject, "my.TableStructure");
        IColumn column = tableStructure.newColumn();
        column.setName("A");
        column.setDatatype(Datatype.STRING.getName());
        tableStructure.getIpsSrcFile().save(null);
        TableContents tableContents = newTableContents(tableStructure, "my.TableContents");
        tableContents.newColumn("", "A");
        tableContents.getTableRows().newRow(tableStructure, Optional.empty(), List.of("1"));
        tableContents.setDescriptionText(Locale.GERMAN, "Deutsche Beschreibung");
        tableContents.setDescriptionText(Locale.ENGLISH, "English description");
        tableContents.getIpsSrcFile().save(null);
        setProjectProperty(ipsProject, p -> p.removeSupportedLanguage(Locale.ENGLISH));

        var cleanUpTranslations = new CleanUpTranslationsAction.CleanUpTranslationsRunnableWithProgress(
                List.of(ipsProject));

        cleanUpTranslations.run(new NullProgressMonitor());

        assertThat(tableContents.getDescriptions().size(), is(1));
        assertThat(tableContents.getDescriptions().get(0).getLocale(), is(Locale.GERMAN));
        assertThat(tableContents.getDescriptionText(Locale.GERMAN), is("Deutsche Beschreibung"));
        InputStream inputStream = tableContents.getIpsSrcFile().getContentFromEnclosingResource();
        String xml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(xml, not(containsString("Description locale=\"en\"")));
        assertThat(xml, not(containsString("English description")));
    }

    @Test
    public void testRun_CleansUpEnumValues() throws IOException, InvocationTargetException, InterruptedException {
        IIpsProject ipsProject = newIpsProject(List.of(Locale.GERMAN, Locale.ENGLISH));
        EnumType enumType = newEnumType(ipsProject, "my.EnumType");
        IEnumAttribute idAttribute = enumType.newEnumAttribute();
        idAttribute.setDatatype(Datatype.STRING.getName());
        idAttribute.setName("id");
        idAttribute.setIdentifier(true);
        IEnumAttribute nameAttribute = enumType.newEnumAttribute();
        nameAttribute.setDatatype(Datatype.STRING.getName());
        nameAttribute.setMultilingual(true);
        nameAttribute.setName("name");
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        var literalNameAttribute = enumType.newEnumLiteralNameAttribute();
        literalNameAttribute.setDefaultValueProviderAttribute("name");
        enumType.setExtensible(true);
        enumType.setEnumContentName("my.EnumContent");
        enumType.getIpsSrcFile().save(null);
        EnumContent enumContent = newEnumContent(enumType, "my.EnumContent");
        IEnumValue enumValue = enumContent.newEnumValue();
        enumValue.setEnumAttributeValue(idAttribute, new StringValue("1"));
        var internationalStringValue = new InternationalStringValue();
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, "Eins"));
        internationalStringValue.getContent().add(new LocalizedString(Locale.ENGLISH, "One"));
        enumValue.setEnumAttributeValue(nameAttribute, internationalStringValue);
        enumContent.setDescriptionText(Locale.GERMAN, "Deutsche Beschreibung");
        enumContent.setDescriptionText(Locale.ENGLISH, "English description");

        setProjectProperty(ipsProject, p -> p.removeSupportedLanguage(Locale.ENGLISH));

        var cleanUpTranslations = new CleanUpTranslationsAction.CleanUpTranslationsRunnableWithProgress(
                List.of(ipsProject));

        cleanUpTranslations.run(new NullProgressMonitor());

        assertThat(enumContent.getDescriptions().size(), is(1));
        assertThat(enumContent.getDescriptions().get(0).getLocale(), is(Locale.GERMAN));
        assertThat(enumContent.getDescriptionText(Locale.GERMAN), is("Deutsche Beschreibung"));
        assertThat(internationalStringValue.getContent().get(Locale.GERMAN).getValue(), is("Eins"));
        assertThat(internationalStringValue.getContent().hasValueFor(Locale.ENGLISH), is(false));
        InputStream inputStream = enumContent.getIpsSrcFile().getContentFromEnclosingResource();
        String xml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(xml, not(containsString("Description locale=\"en\"")));
        assertThat(xml, not(containsString("English description")));
        assertThat(xml, not(containsString("LocalizedString locale=\"en\"")));
        assertThat(xml, not(containsString("One")));
    }

}
