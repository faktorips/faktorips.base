/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.rule;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.ui.editors.pctype.rule.ValidationRuleEditingUI.MsgCodePMO;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.pctype.MessageSeverity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MsgCodePmoTest {

    private static final String DELIMITER = "."; //$NON-NLS-1$

    private String severity = MessageSeverity.ERROR.getId();

    private String pcType = "pcType";

    private String nameOfRule = "name";

    private String generatedMsgCode = StringUtils.EMPTY;

    private MsgCodePMO msgCodePmo;

    private MessageSeverity msgSeverity = MessageSeverity.ERROR;

    @Mock
    private IValidationRule rule;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private QualifiedNameType qualifiedNameType;

    @Mock
    private IIpsModel model;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        when(rule.getIpsModel()).thenReturn(model);
        when(rule.getMessageSeverity()).thenReturn(msgSeverity);

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
