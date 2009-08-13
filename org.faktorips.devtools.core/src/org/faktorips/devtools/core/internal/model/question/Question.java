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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.BaseIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.question.IQuestion;
import org.faktorips.devtools.core.model.question.QuestionIpsObjectType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

/**
 * 
 * @author INSERT YOUR NAME
 */
public class Question extends BaseIpsObject implements IQuestion {

    private String question;

    /**
     * @param file
     */
    public Question(IIpsSrcFile file) {
        super(file);
    }

    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return QuestionIpsObjectType.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    public String getQuestionText() {
        return question;
    }

    /**
     * {@inheritDoc}
     */
    public void setQuestionText(String newText) {
        ArgumentCheck.notNull(newText);
        String oldText = question;
        question = newText;
        valueChanged(oldText, newText);
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        question = element.getAttribute(PROPERY_QUESTION_TEXT);
    }

    @Override
    protected void propertiesToXml(Element element) {
        element.setAttribute(PROPERY_QUESTION_TEXT, question);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (StringUtils.isEmpty(question)) {
            String text = "Must provide a question text!";
            list.add(new Message(IQuestion.MSG_CODE_QUESTION_TEXT_IS_EMPTY, text, Message.ERROR, this,
                    IQuestion.PROPERY_QUESTION_TEXT));
        }
    }

}
