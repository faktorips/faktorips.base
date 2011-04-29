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

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.Currency;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;

public class CurrencySymbolPainter implements PaintListener {

    private final ICurrencyHolder currencyHolder;

    public CurrencySymbolPainter(ICurrencyHolder currencyHolder) {
        this.currencyHolder = currencyHolder;
    }

    @Override
    public void paintControl(PaintEvent e) {
        GC gc = e.gc;
        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        Color color = colorRegistry.get(JFacePreferences.QUALIFIER_COLOR);
        if (color != null && !color.isDisposed()) {
            gc.setForeground(color);
        }
        String symbol = getCurrencySymbol(getCurrency());
        int y = (((Control)e.getSource()).getSize().y - gc.textExtent(symbol).y) / 2;
        gc.drawText(symbol, 2, y);
    }

    public static String getCurrencySymbol(Currency currency) {
        return currency.getSymbol(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
    }

    private Currency getCurrency() {
        return currencyHolder.getCurrency();
    }

}
