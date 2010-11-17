/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.productrelease;

import java.util.Observable;
import java.util.Observer;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.productrelease.ObservableProgressMessages;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

public class CopyOfProductReleaseProcessorTest extends AbstractIpsPluginTest {

    private ProductReleaseProcessor productReleaseProcessor;
    private IIpsProject ipsProject;
    private MessageList messageList;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        ObservableProgressMessages observableMessages = new ObservableProgressMessages();
        productReleaseProcessor = new ProductReleaseProcessor(ipsProject, observableMessages);
        messageList = new MessageList();
        new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                messageList.add((Message)arg);
            }
        };
    }

    public void testStartReleaseBuilder() throws Exception {
        productReleaseProcessor.startReleaseBuilder("abc", null, null);
        assertTrue(messageList.containsErrorMsg());
        assertEquals(1, messageList.size());
        assertEquals(Messages.ReleaseAndDeploymentOperation_exception_noDeploymentExtension, messageList.getMessage(0));
    }

}
