/**
 * Augmented Reality Rubik Cube Solver
 * 
 * Author: Steven P. Punte (aka Android Steve)
 * Date:   Nov 1st 2014
 * 
 * Project Description:
 *   Android application developed on a commercial Smart Phone which, when run on a pair 
 *   of Smart Glasses, guides a user through the process of solving a Rubik Cube.
 *   
 * File Description:
 * 
 * License:
 * 
 *  GPL
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ar.rubik;

import java.io.Serializable;

import org.opencv.core.Core;
import org.opencv.core.Scalar;


public class Constants {
	
    public static final String TAG = "RubikSolver";
    
    public static final String TAG_CNTRL = "RubikCntrl";

    // Specifies where image comes from
	public enum ImageSourceModeEnum { NORMAL, SAVE_NEXT, PLAYBACK}
	
	// Specifies what to do with image
	public enum ImageProcessModeEnum { DIRECT, MONOCHROMATIC, GREYSCALE, BOXBLUR, CANNY, DILATION, CONTOUR, POLYGON, RHOMBUS, FACE_DETECT}
	
	// Specifies what annotation to add
	public enum AnnotationModeEnum { LAYOUT, RHOMBUS, FACE_METRICS, TIME, COLOR, CUBE_METRICS, NORMAL }
	
	// Constant Colors as Scalar objects for purpose of various text, line and annotation rendering.
	public final static Scalar ColorRed    = new Scalar(255.0, 0.0, 0.0);
	public final static Scalar ColorOrange = new Scalar(240.0, 120.0, 100.0);
	public final static Scalar ColorYellow = new Scalar(255.0, 255.0, 0.0);
	public final static Scalar ColorGreen  = new Scalar(0.0, 255.0, 0.0);
	public final static Scalar ColorBlue   = new Scalar(0.0, 0.0, 255.0);
	public final static Scalar ColorWhite  = new Scalar(255.0, 255.0, 255.0);
	public final static Scalar ColorGrey   = new Scalar(50.0, 50.0, 50.0);
	public final static Scalar ColorBlack  = new Scalar(0.0, 0.0, 0.0);
	
	// Constant Colors as Scalar objects to match Rubik tile colors.
	// This values below are calibrated for morning light.
	public final static Scalar RubikRed    = new Scalar(180.0,  20.0,  30.0);
	public final static Scalar RubikOrange = new Scalar(240.0,  80.0,   0.0);
	public final static Scalar RubikYellow = new Scalar(230.0, 230.0,  20.0);
	public final static Scalar RubikGreen  = new Scalar(  0.0, 140.0,  60.0);
	public final static Scalar RubikBlue   = new Scalar(  0.0,  60.0, 220.0);
	public final static Scalar RubikWhite  = new Scalar(225.0, 255.0, 255.0);
	
	public enum LogicalTileColorEnum { RED, ORANGE, YELLOW, GREEN, BLUE, WHITE };
	
	// Group together enum, color and character annotation.
	public static final class LogicalTile implements Serializable {
		private static final long serialVersionUID = 4739751093453679173L;
		
		// Color values selected to be close to that of cube.
		public LogicalTileColorEnum logicalTileColor;
		
		// Color values selected for display purposes.
		public Scalar color;
		
		// Character symbol of color
		public char character;
		
		public LogicalTile(LogicalTileColorEnum logicalTileColor, Scalar color, char character) {
			this.logicalTileColor = logicalTileColor;
			this.color = color;
			this.character = character;
		}
	}
	
	// Six Logical Tile types: =+= not yet filled out.
	public final static LogicalTile redLogicalTile = new LogicalTile(LogicalTileColorEnum.RED,     RubikRed,    'R');

	// Array of possible Rubik Tile Colors.
	public final static LogicalTile [] logicalTileColorArray = {
		redLogicalTile,
			new LogicalTile(LogicalTileColorEnum.ORANGE,  RubikOrange, 'O'),
			new LogicalTile(LogicalTileColorEnum.YELLOW,  RubikYellow, 'Y'),
			new LogicalTile(LogicalTileColorEnum.GREEN,   RubikGreen,  'G'),
			new LogicalTile(LogicalTileColorEnum.BLUE,    RubikBlue,   'B'),
			new LogicalTile(LogicalTileColorEnum.WHITE,   RubikWhite,  'W')
	};

    public final static int FontFace = Core.FONT_HERSHEY_PLAIN;
}
