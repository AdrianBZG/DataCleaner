/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.widgets;

import java.awt.Color;

import javax.swing.AbstractButton;
import javax.swing.plaf.ButtonUI;

import org.datacleaner.util.WidgetUtils;

import com.jgoodies.looks.plastic.PlasticButtonUI;

/***
 * A {@link ButtonUI} for dark buttons in the DataCleaner user interface.
 */
public class DarkButtonUI extends PlasticButtonUI {

    private static final DarkButtonUI INSTANCE = new DarkButtonUI();

    public static DarkButtonUI get() {
        return INSTANCE;
    }

    private DarkButtonUI() {
    }

    @Override
    protected boolean is3D(AbstractButton b) {
        return false;
    }

    @Override
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setBackground(WidgetUtils.BG_COLOR_DARKEST);
        b.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        b.setFocusPainted(false);
    }

    @Override
    protected Color getSelectColor() {
        return WidgetUtils.BG_COLOR_LESS_DARK;
    }

}
