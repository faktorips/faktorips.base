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

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ValidationRuleConfig extends AtomicIpsObjectPart implements IValidationRuleConfig {

    public static final String TAG_NAME = "ValidationRuleDef";

    private boolean isActive = false;

    private String ruleName;

    public ValidationRuleConfig(ProductCmptGeneration parent, String id) {
        super(parent, id);
    }

    public ValidationRuleConfig(ProductCmptGeneration parent, String id, String ruleName) {
        super(parent, id);
        this.ruleName = ruleName;
    }

    @Override
    public boolean getValidationRuleActive() {
        return isActive;
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    public IValidationRule findValidationRule(IIpsProject ipsProject) throws CoreException {
        IProductCmptGeneration generation = (IProductCmptGeneration)getParent();
        IProductCmpt component = (IProductCmpt)generation.getParent();
        IPolicyCmptType pcType = component.findPolicyCmptType(ipsProject);
        IValidationRule rule = pcType.getValidationRule(ruleName);
        if (rule == null) {
            throw new CoreException(new IpsStatus("No rule found for name \"" + ruleName + "\""));
        }
        return rule;
    }

}
