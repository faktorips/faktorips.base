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

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.stdbuilder.ui.messagesimport.Messages;
import org.faktorips.devtools.stdbuilder.ui.messagesimport.MessagesImportPMO;
import org.faktorips.util.message.Message;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MessagesImportPMOTest {

    private MessagesImportPMO messageImportPMOEmpty;

    private MessagesImportPMO messageImportPMONoExist;

    private MessagesImportPMO messageImportPMOTarget;

    @Mock
    private IIpsPackageFragmentRoot ipsPackageFragmentRoot;

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
    public void testEmptyFilename() {
        messageImportPMOEmpty = new MessagesImportPMO();
        messageImportPMOEmpty.setIpsPackageFragmentRoot(ipsPackageFragmentRoot);
        messageImportPMOEmpty.setFileName("");
        assertEquals(new Message(MessagesImportPMO.MSG_EMPTY_FILE, Messages.MessagesImportPMO_EmptyFilename,
                Message.ERROR), messageImportPMOEmpty.validate().getMessageByCode(MessagesImportPMO.MSG_EMPTY_FILE));
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

}
