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

import java.net.PasswordAuthentication;

import org.faktorips.devtools.core.productrelease.ITargetSystem;

/**
 * Default implementation for {@link ITargetSystem} only provides the name of the target system
 * 
 * @author dirmeier
 */
public class DefaultTargetSystem implements ITargetSystem {

    private final String name;
    private PasswordAuthentication passwordAuthentication;

    public DefaultTargetSystem(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isValidAuthentication() {
        return true;
    }

    @Override
    public void setPasswordAuthentication(PasswordAuthentication passwordAuthentication) {
        this.passwordAuthentication = passwordAuthentication;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return passwordAuthentication;
    }

}
