/*******************************************************************************
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.worldgrower.gui;

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class TiledImageButton extends JButton {

	public TiledImageButton(String text, ImageInfoReader imageInfoReader) {
		super(text);
		setPaintOptions();
	}

	public TiledImageButton(String text, ImageIcon icon, ImageInfoReader imageInfoReader) {
		super(text, icon);
		setPaintOptions();
	}

	private void setPaintOptions() {
		setContentAreaFilled(false);
		setFocusPainted(false);
		setBorder(null);
	}

	@Override
	protected void paintComponent(Graphics g) {

		if (getModel().isRollover()) {
			setForeground(ColorPalette.DISABLED_FOREGROUND_COLOR);
		} else {
			setForeground(ColorPalette.FOREGROUND_COLOR);
		}
		
		super.paintComponent(g);
	}
}