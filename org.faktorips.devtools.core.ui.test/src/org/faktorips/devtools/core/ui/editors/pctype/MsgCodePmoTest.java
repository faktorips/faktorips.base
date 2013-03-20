/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.pctype;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.core.ui.editors.pctype.ValidationRuleEditingUI.MsgCodePMO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MsgCodePmoTest {

    private static final String DELIMITER = "."; //$NON-NLS-1$

    private String severity = "error";

    private String pcType = "pcType";

    private String nameOfRule = "name";

    private String generatedMsgCode = StringUtils.EMPTY;

    private MsgCodePMO msgCodePmo;

    @Mock
    private MessageSeverity msgSeverity;

    @Mock
    private IValidationRule rule;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private QualifiedNameType qualifiedNameType;

    @Mock
    private IpsModel model;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        when(rule.getIpsModel()).thenReturn(model);
        when(rule.getMessageSeverity()).thenReturn(msgSeverity);
        when(msgSeverity.getName()).thenReturn(severity);

        when(rule.getIpsObject()).thenReturn(ipsObject);
        when(ipsObject.getQualifiedNameType()).thenReturn(qualifiedNameType);
        when(qualifiedNameType.getUnqualifiedName()).thenReturn(pcType);

        when(rule.getName()).thenReturn(nameOfRule);
        msgCodePmo = new MsgCodePMO(rule);

        generatedMsgCode = severity + DELIMITER + pcType + DELIMITER + nameOfRule;

    }

    @Test
    public void updateMsgCodeEntry_Empty() throws Exception {
        when(rule.getMessageCode()).thenReturn(StringUtils.EMPTY);

        msgCodePmo.partHasChanged();

        verify(rule).setMessageCode(generatedMsgCode);
    }

    @Test
    public void updateMsgCodeEntry_SeverityAndNameEqual() throws Exception {
        when(rule.getMessageCode()).thenReturn(severity + DELIMITER + pcType + DELIMITER + "test");

        msgCodePmo.partHasChanged();

        verify(rule).setMessageCode(generatedMsgCode);
    }

    @Test
    public void updateMsgCodeEntry_NameAndRuleNameEqual() throws Exception {
        when(rule.getMessageCode()).thenReturn("test" + DELIMITER + pcType + DELIMITER + nameOfRule);

        msgCodePmo.partHasChanged();

        verify(rule).setMessageCode(generatedMsgCode);
    }

    @Test
    public void noUpdateMsgCodeEntry_LessThanTwoPoints() throws Exception {
        when(rule.getMessageCode()).thenReturn("test");

        msgCodePmo.partHasChanged();

        verify(rule, times(0)).setMessageCode(generatedMsgCode);
    }

    @Test
    public void noUpdateMsgCodeEntry_MoreThanTwoPoints() throws Exception {
        when(rule.getMessageCode()).thenReturn("test.test.test.test");

        msgCodePmo.partHasChanged();

        verify(rule, times(0)).setMessageCode(generatedMsgCode);
    }
}