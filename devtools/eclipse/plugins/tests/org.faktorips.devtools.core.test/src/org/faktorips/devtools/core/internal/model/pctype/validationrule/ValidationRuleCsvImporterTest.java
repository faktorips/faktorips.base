/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype.validationrule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.validationrule.ValidationRuleCsvImporter.CsvTableBean;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleCsvImporterTest {

    @Mock
    private InputStream contents;

    private Locale locale = Locale.GERMAN;

    @Mock
    private IIpsPackageFragmentRoot root;

    MultiStatus multipleMessages = new MultiStatus(IpsPlugin.PLUGIN_ID, 0, "", null);

    private ValidationRuleCsvImporter validationRuleCsvImporter;

    @Before
    public void setUpImporter() {
        validationRuleCsvImporter = new ValidationRuleCsvImporter(contents, root, locale);
    }

    @Test
    public void testIndexTableEntries_simpleIndex() throws Exception {
        List<CsvTableBean> list = new ArrayList<>();
        list.add(new CsvTableBean("1", "a"));
        list.add(new CsvTableBean("2", "b"));
        list.add(new CsvTableBean("3", "a"));

        Map<String, String> indexTableEntries = validationRuleCsvImporter.indexTableEntries(list, multipleMessages);

        assertEquals("a", indexTableEntries.get("1"));
        assertEquals("b", indexTableEntries.get("2"));
        assertEquals("a", indexTableEntries.get("3"));
    }

    @Test
    public void testIndexTableEntries_emptyList() throws Exception {
        List<CsvTableBean> list = new ArrayList<>();

        Map<String, String> indexTableEntries = validationRuleCsvImporter.indexTableEntries(list, multipleMessages);

        assertTrue(indexTableEntries.isEmpty());
    }

    @Test
    public void testIndexTableEntries_emptyDuplicateKey() throws Exception {
        List<CsvTableBean> list = new ArrayList<>();
        list.add(new CsvTableBean("1", "a"));
        list.add(new CsvTableBean("1", "b"));

        Map<String, String> indexTableEntries = validationRuleCsvImporter.indexTableEntries(list, multipleMessages);

        assertEquals("b", indexTableEntries.get("1"));
        assertEquals(1, multipleMessages.getChildren().length);
    }

    @Test
    public void testIllegalColumnIndex() {
        validationRuleCsvImporter.setKeyAndValueColumn(0, -1);

        IStatus status = validationRuleCsvImporter.loadContent();
        assertTrue(status instanceof IpsStatus);
        IpsStatus ipsStatus = (IpsStatus)status;
        assertTrue(ipsStatus.getException() instanceof ArrayIndexOutOfBoundsException);
    }

}
