/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.faktorips.devtools.core.IpsPlugin;

public class CurrencySymbolPainter implements PaintListener {

    private static final int PADDING_X = 2;
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
        Rectangle clipping = gc.getClipping();
        Point textExtent = gc.textExtent(symbol);
        int y = (clipping.height - textExtent.y) / 2;
        gc.drawText(symbol, clipping.x + PADDING_X, y);
        clipping.x += textExtent.x + (PADDING_X * 2);
        gc.setClipping(clipping);
    }

    public static String getCurrencySymbol(Currency currency) {
        return currency.getSymbol(IpsPlugin.getDefault().getIpsPreferences().getDatatypeFormattingLocale());
    }

    private Currency getCurrency() {
        return currencyHolder.getCurrency();
    }

}
