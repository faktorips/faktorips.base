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

package org.faktorips.devtools.core.model.question;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.question.Question;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;

/**
 * The ips object type for a business function. A reference to this class is made in an extension of
 * the extension-point <code>org.faktorips.devtools.core.ipsobjecttype</code>
 * 
 * @author Peter Erzberger
 */
public class QuestionIpsObjectType extends IpsObjectType {

    public final static String ID = "org.faktorips.devtools.model.question.QuestionIpsObjectType"; //$NON-NLS-1$

    public QuestionIpsObjectType() {
        super(ID, "Question", //xml element name $NON-NLS-1$   //$NON-NLS-1$
                "Question", // display name
                "Questions", // display name plural
                "ipsquestion", false, false, //$NON-NLS-1$
                "Question.gif", "QuestionDisabled.gif"); //$NON-NLS-1$
    }

    /**
     * Returns the unique instance of this class.
     */
    public final static QuestionIpsObjectType getInstance() {
        return (QuestionIpsObjectType)IpsPlugin.getDefault().getIpsModel().getIpsObjectType(ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsObject newObject(IIpsSrcFile file) {
        return new Question(file);
    }

}
