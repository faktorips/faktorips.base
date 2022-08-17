/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.messagesimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ui.wizards.messagesimport.MessagesImportPMO.ImportFormat;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.runtime.Message;
import org.junit.Before;
import org.junit.Test;

public class MessagesImportPMOTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;

    private IIpsProject ipsProject;

    private MessagesImportPMO pmo;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.addSupportedLanguage(Locale.ENGLISH);
        properties.addSupportedLanguage(Locale.GERMAN);
        properties.addSupportedLanguage(Locale.FRENCH);
        properties.setDefaultLanguage(Locale.FRENCH);
        ipsProject.setProperties(properties);

        ipsPackageFragmentRoot = newIpsPackageFragmentRoot(ipsProject, null, "root");

        pmo = new MessagesImportPMO();
    }

    @Test
    public void testDefaultValues() {

        assertEquals(';', pmo.getColumnDelimiter().charValue());
        assertEquals("1", pmo.getIdentifierColumnIndex());
        assertEquals("2", pmo.getTextColumnIndex());
    }

    @Test
    public void testUpdateSupportedLanguage() {
        assertNull(pmo.getSupportedLanguage());

        pmo.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);

        assertEquals(ipsProject.getReadOnlyProperties().getDefaultLanguage(), pmo.getSupportedLanguage());
    }

    @Test
    public void testTarget() {
        pmo.setIpsPackageFragmentRoot(null);
        assertEquals(new Message(MessagesImportPMO.MSG_INVALID_TARGET, Messages.MessagesImportPMO_EmptyTargetname,
                Message.ERROR), pmo.validate().getMessageByCode(MessagesImportPMO.MSG_INVALID_TARGET));

    }

    @Test
    public void testEmptyFilename() {
        pmo.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);
        pmo.setFilename("");
        assertEquals(new Message(MessagesImportPMO.MSG_EMPTY_FILE, Messages.MessagesImportPMO_EmptyFilename,
                Message.ERROR), pmo.validate().getMessageByCode(MessagesImportPMO.MSG_EMPTY_FILE));
    }

    @Test
    public void testNoExistFilename() {
        pmo.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);
        pmo.setFilename(
                "src/org/faktorips/devtools/stdbuilder/policycmpttype/validationrule/validation-test-messages.prope");
        assertEquals((new Message(MessagesImportPMO.MSG_NO_EXIST_FILE, Messages.MessagesImportPMO_FileDoesNotExist,
                Message.ERROR)), pmo.validate().getMessageByCode(MessagesImportPMO.MSG_NO_EXIST_FILE));
    }

    @Test
    public void testIsCsvFileFormat() {
        assertTrue(pmo.isCsvFileFormat());

        pmo.setFormat(ImportFormat.PROPERTIES);
        assertFalse(pmo.isCsvFileFormat());

        pmo.setFormat(ImportFormat.CSV);
        assertTrue(pmo.isCsvFileFormat());
    }

    @Test
    public void testNoColumnDelimiter() {
        pmo.setFormat(ImportFormat.CSV);
        pmo.setColumnDelimiter(null);
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_COLUMN_DELIMITER);

        pmo.setFormat(ImportFormat.PROPERTIES);
        pmo.setColumnDelimiter(null);
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_COLUMN_DELIMITER);
    }

    @Test
    public void testNoIdColumnIndex() {
        pmo.setFormat(ImportFormat.CSV);
        pmo.setIdentifierColumnIndex(null);
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_ID_COLUMN_INDEX);
        pmo.setIdentifierColumnIndex("x");
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_ID_COLUMN_INDEX);
        pmo.setTextColumnIndex("0");
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_ID_COLUMN_INDEX);
        pmo.setTextColumnIndex("-1");
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_ID_COLUMN_INDEX);

        pmo.setIdentifierColumnIndex("1");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_ID_COLUMN_INDEX);

        pmo.setFormat(ImportFormat.PROPERTIES);
        pmo.setIdentifierColumnIndex("y");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_ID_COLUMN_INDEX);

        pmo.setIdentifierColumnIndex("1");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_ID_COLUMN_INDEX);
    }

    @Test
    public void testNoTextColumnIndex() {
        pmo.setFormat(ImportFormat.CSV);
        pmo.setTextColumnIndex(null);
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_TEXT_COLUMN_INDEX);
        pmo.setTextColumnIndex("x");
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_TEXT_COLUMN_INDEX);
        pmo.setTextColumnIndex("0");
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_TEXT_COLUMN_INDEX);
        pmo.setTextColumnIndex("-1");
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_TEXT_COLUMN_INDEX);

        pmo.setTextColumnIndex("1");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_TEXT_COLUMN_INDEX);

        pmo.setFormat(ImportFormat.PROPERTIES);
        pmo.setTextColumnIndex("y");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_TEXT_COLUMN_INDEX);

        pmo.setTextColumnIndex("1");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_TEXT_COLUMN_INDEX);
    }

    private void assertNoMessageExists(MessagesImportPMO pmo, String msgCode) {
        assertNull(pmo.validate().getMessageByCode(msgCode));
    }

    private void assertMessageExists(MessagesImportPMO pmo, String msgCode) {
        assertNotNull(pmo.validate().getMessageByCode(msgCode));
    }
}
