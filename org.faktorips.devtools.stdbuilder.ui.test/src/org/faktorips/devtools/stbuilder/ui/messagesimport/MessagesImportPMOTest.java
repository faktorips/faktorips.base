/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stbuilder.ui.messagesimport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.stdbuilder.ui.messagesimport.Messages;
import org.faktorips.devtools.stdbuilder.ui.messagesimport.MessagesImportPMO;
import org.faktorips.util.message.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MessagesImportPMOTest {

    private MessagesImportPMO messageImportPMOFile;

    private MessagesImportPMO messageImportPMOEmpty;

    private MessagesImportPMO messageImportPMODirectory;

    private MessagesImportPMO messageImportPMONoExist;

    private MessagesImportPMO messageImportPMOTarget;

    private MessagesImportPMO messageImportPMONoSelectLocal;

    @Mock
    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;

    @Mock
    private ISupportedLanguage supportedLanguage;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testTarget() {
        messageImportPMOTarget = new MessagesImportPMO();
        messageImportPMOTarget.setIpsPackageFragmentRoot(null);
        assertEquals(new Message(MessagesImportPMO.MSG_INVALID_TARGET, Messages.MessagesImportPMO_EmptyTargetname,
                Message.ERROR), messageImportPMOTarget.validate()
                .getMessageByCode(MessagesImportPMO.MSG_INVALID_TARGET));

    }

    @Test
    public void testValidateMessageEmpty() {
        messageImportPMOFile = new MessagesImportPMO();
        messageImportPMOFile.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);
        messageImportPMOFile
                .setFileName("src/org/faktorips/devtools/stbuilder/ui/messagesimport/validation-test-messages.properties");
        messageImportPMOFile.setLocale(supportedLanguage);
        when(messageImportPMOFile.getLocale().getLanguageName()).thenReturn("de");

        assertEquals("", messageImportPMOFile.validate().getText());

    }

    @Test
    public void testEmptyFilename() {
        messageImportPMOEmpty = new MessagesImportPMO();
        messageImportPMOEmpty.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);
        messageImportPMOEmpty.setFileName("");
        assertEquals(new Message(MessagesImportPMO.MSG_EMPTY_FILE, Messages.MessagesImportPMO_EmptyFilename,
                Message.ERROR), messageImportPMOEmpty.validate().getMessageByCode(MessagesImportPMO.MSG_EMPTY_FILE));
    }

    @Test
    public void testNoDirectoryFilename() {
        messageImportPMODirectory = new MessagesImportPMO();
        messageImportPMODirectory.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);
        messageImportPMODirectory.setFileName("src/org/faktorips/");
        assertEquals((new Message(MessagesImportPMO.MSG_DIRECTORY_FILE, Messages.MessagesImportPMO_FilenameIsDirectory,
                Message.ERROR)),
                messageImportPMODirectory.validate().getMessageByCode(MessagesImportPMO.MSG_DIRECTORY_FILE));
    }

    @Test
    public void testNoExistFilename() {
        messageImportPMONoExist = new MessagesImportPMO();
        messageImportPMONoExist.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);
        messageImportPMONoExist
                .setFileName("src/org/faktorips/devtools/stdbuilder/policycmpttype/validationrule/validation-test-messages.prope");
        assertEquals((new Message(MessagesImportPMO.MSG_NO_EXIST_FILE, Messages.MessagesImportPMO_FileDoesNotExist,
                Message.ERROR)),
                messageImportPMONoExist.validate().getMessageByCode(MessagesImportPMO.MSG_NO_EXIST_FILE));
    }

    @Test
    public void testNoLocale() {
        messageImportPMONoSelectLocal = new MessagesImportPMO();
        messageImportPMONoSelectLocal.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);
        messageImportPMONoSelectLocal
                .setFileName("src/org/faktorips/devtools/stbuilder/ui/messagesimport/validation-test-messages.properties");
        messageImportPMONoSelectLocal.setLocale(null);

        assertEquals((new Message(MessagesImportPMO.MSG_NO_LOCALE, Messages.MessagesImportPMO_EmptyLocale,
                Message.ERROR)),
                messageImportPMONoSelectLocal.validate().getMessageByCode(MessagesImportPMO.MSG_NO_LOCALE));

    }

}
