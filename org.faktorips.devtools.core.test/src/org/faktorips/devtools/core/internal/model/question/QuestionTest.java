/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.question;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.question.IQuestion;
import org.faktorips.devtools.core.model.question.QuestionIpsObjectType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class QuestionTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private Question question;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        question = (Question)newIpsObject(ipsProject, QuestionIpsObjectType.getInstance(), "myquestions.Question1");
    }

    public void testInitFromXml() {
        Element element = getTestDocument().getDocumentElement();
        question.initFromXml(element);
        assertEquals("Why?", question.getQuestionText());
    }

    public void testToXml() throws CoreException {
        question.setQuestionText("Why??");
        Element el = question.toXml(newDocument());

        Question question2 = (Question)newIpsObject(ipsProject, QuestionIpsObjectType.getInstance(),
                "myquestions.Question2");
        question2.initFromXml(el);
        assertEquals("Why??", question2.getQuestionText());
    }

    public void testQuestionTextMustNotBeEmpty() throws CoreException {
        question.setQuestionText("");
        MessageList result = question.validate(ipsProject);
        assertNotNull(result.getMessageByCode(IQuestion.MSG_CODE_QUESTION_TEXT_IS_EMPTY));

        question.setQuestionText("Why?");
        result = question.validate(ipsProject);
        assertNull(result.getMessageByCode(IQuestion.MSG_CODE_QUESTION_TEXT_IS_EMPTY));

    }
}
