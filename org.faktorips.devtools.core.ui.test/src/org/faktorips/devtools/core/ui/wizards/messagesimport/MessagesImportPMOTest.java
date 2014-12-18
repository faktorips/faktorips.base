/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.util.message.Message;
import org.junit.Before;
import org.junit.Test;

public class MessagesImportPMOTest extends AbstractIpsPluginTest {

    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;

    private IIpsProject ipsProject;

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
    }

    @Test
    public void testDefaultValues() {
        MessagesImportPMO pmo = new MessagesImportPMO();

        assertEquals(";", pmo.getColumnDelimiter());
        assertEquals("1", pmo.getIdentifierColumnIndex());
        assertEquals("2", pmo.getTextColumnIndex());
    }

    @Test
    public void testUpdateSupportedLanguage() {
        MessagesImportPMO pmo = new MessagesImportPMO();

        assertNull(pmo.getSupportedLanguage());

        pmo.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);

        assertEquals(ipsProject.getReadOnlyProperties().getDefaultLanguage(), pmo.getSupportedLanguage());
    }

    @Test
    public void testTarget() {
        MessagesImportPMO messageImportPMOTarget = new MessagesImportPMO();
        messageImportPMOTarget.setIpsPackageFragmentRoot(null);
        assertEquals(new Message(MessagesImportPMO.MSG_INVALID_TARGET, Messages.MessagesImportPMO_EmptyTargetname,
                Message.ERROR), messageImportPMOTarget.validate()
                .getMessageByCode(MessagesImportPMO.MSG_INVALID_TARGET));

    }

    @Test
    public void testEmptyFilename() {
        MessagesImportPMO messageImportPMOEmpty = new MessagesImportPMO();
        messageImportPMOEmpty.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);
        messageImportPMOEmpty.setFileName("");
        assertEquals(new Message(MessagesImportPMO.MSG_EMPTY_FILE, Messages.MessagesImportPMO_EmptyFilename,
                Message.ERROR), messageImportPMOEmpty.validate().getMessageByCode(MessagesImportPMO.MSG_EMPTY_FILE));
    }

    @Test
    public void testNoExistFilename() {
        MessagesImportPMO messageImportPMONoExist = new MessagesImportPMO();
        messageImportPMONoExist.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);
        messageImportPMONoExist
                .setFileName("src/org/faktorips/devtools/stdbuilder/policycmpttype/validationrule/validation-test-messages.prope");
        assertEquals((new Message(MessagesImportPMO.MSG_NO_EXIST_FILE, Messages.MessagesImportPMO_FileDoesNotExist,
                Message.ERROR)),
                messageImportPMONoExist.validate().getMessageByCode(MessagesImportPMO.MSG_NO_EXIST_FILE));
    }

    @Test
    public void testIsCsvFileFormat() {
        MessagesImportPMO pmo = new MessagesImportPMO();
        assertTrue(pmo.isCsvFileFormat());

        pmo.setFormat(MessagesImportPMO.FORMAT_PROPERTIES_FILE);
        assertFalse(pmo.isCsvFileFormat());

        pmo.setFormat(MessagesImportPMO.FORMAT_CSV_FILE);
        assertTrue(pmo.isCsvFileFormat());
    }

    @Test
    public void testNoColumnDelimiter() {
        MessagesImportPMO pmo = new MessagesImportPMO();

        pmo.setFormat(MessagesImportPMO.FORMAT_CSV_FILE);
        pmo.setColumnDelimiter(null);
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_COLUMN_DELIMITER);

        pmo.setColumnDelimiter("");
        assertMessageExists(pmo, MessagesImportPMO.MSG_NO_COLUMN_DELIMITER);

        pmo.setColumnDelimiter("\t");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_COLUMN_DELIMITER);

        pmo.setFormat(MessagesImportPMO.FORMAT_PROPERTIES_FILE);
        pmo.setColumnDelimiter(null);
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_COLUMN_DELIMITER);

        pmo.setColumnDelimiter("");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_COLUMN_DELIMITER);

        pmo.setColumnDelimiter("\t");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_COLUMN_DELIMITER);
    }

    @Test
    public void testNoIdColumnIndex() {
        MessagesImportPMO pmo = new MessagesImportPMO();

        pmo.setFormat(MessagesImportPMO.FORMAT_CSV_FILE);
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

        pmo.setFormat(MessagesImportPMO.FORMAT_PROPERTIES_FILE);
        pmo.setIdentifierColumnIndex("y");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_ID_COLUMN_INDEX);

        pmo.setIdentifierColumnIndex("1");
        assertNoMessageExists(pmo, MessagesImportPMO.MSG_NO_ID_COLUMN_INDEX);
    }

    @Test
    public void testNoTextColumnIndex() {
        MessagesImportPMO pmo = new MessagesImportPMO();

        pmo.setFormat(MessagesImportPMO.FORMAT_CSV_FILE);
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

        pmo.setFormat(MessagesImportPMO.FORMAT_PROPERTIES_FILE);
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
