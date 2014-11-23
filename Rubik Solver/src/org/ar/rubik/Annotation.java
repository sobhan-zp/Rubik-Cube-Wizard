/**
 * 
 */
package org.ar.rubik;

import java.util.List;

import org.ar.rubik.Constants.ConstantTile;
import org.ar.rubik.Constants.ConstantTileColorEnum;
import org.ar.rubik.RubikFace.FaceRecognitionStatusEnum;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.util.Log;

/**
 * @author android.steve@testlens.com
 *
 */
public class Annotation {

	private StateModel stateModel;
	private StateMachine stateMachine;
	
	/**
	 * @param stateModel
	 * @param stateMachine 
	 */
    public Annotation(StateModel stateModel, StateMachine stateMachine) {
	    this.stateModel = stateModel;
	    this.stateMachine = stateMachine;
    }
    
    
	/**
	 * Add Annotation
	 * This typically will consume the right third of the landscape orientation image.
	 * @param image
	 * @return
	 */
	public Mat renderAnnotation(Mat image) {
		
		renderFaceOverlayAnnotation(image, false);
		
		switch(MenuAndParams.annotationMode) {
		
		case LAYOUT:
			renderFlatCubeLayoutRepresentations(image);
			break;
			
		case RHOMBUS:
	    	renderRhombusRecognitionMetrics(image, stateModel.activeRubikFace.rhombusList);
			break;

		case FACE_METRICS:
			renderRubikFaceMetrics(image, stateModel.activeRubikFace);
			break;
			
		case CUBE_METRICS:
			renderCubeMetrics(image);
			break;

		case TIME:
			stateModel.activeRubikFace.profiler.renderTimeConsumptionMetrics(image, stateModel);
			break;
			
		case COLOR:
			renderFaceColorMetrics(image, stateModel.activeRubikFace);
			break;
			
		case NORMAL:
			
			// hack so that user instructions are all visible
			Core.rectangle(image, new Point(0, 60), new Point(350, 720), Constants.ColorBlack, -1);

//			Core.rectangle(image, new Point(0, 0), new Point(350, 720), Constants.ColorBlack, -1);
//			annotationGlRenderer.setRenderState(true);
//			annotationGlRenderer.setCubeOrienation(RubikCube.active);
			break;
		}
		
		
		// Render Text User Instructions on top part of screen.
		renderUserInstructions(image);
		
		return image;
	}

   

	/**
	 * Render Unfolded Cube Layout Representations
	 * 
	 * Render both the non-transformed unfolded Rubik Cube layout (this is as observed
	 * from Face Recognizer directly), and the transformed unfolded Rubik Cube layout
	 * (this is rotationally correct with respect unfolded layout definition, and is what the 
	 * cube logical and what the cube logic solver is expecting.
	 * 
	 * 
	 * @param image
	 */
    private void renderFlatCubeLayoutRepresentations(Mat image) {
    	
    	Core.rectangle(image, new Point(0, 0), new Point(450, 720), Constants.ColorBlack, -1);
	    
		final int tSize = 35;  // Tile Size in pixels
		
		// Faces are orientated as per Face Observation (and N, M axis)
		renderFlatFaceRepresentation(image, stateModel.upRubikFace,     3 * tSize, 0 * tSize + 70, tSize, true);
		renderFlatFaceRepresentation(image, stateModel.leftRubikFace,   0 * tSize, 3 * tSize + 70, tSize, true);
		renderFlatFaceRepresentation(image, stateModel.frontRubikFace,  3 * tSize, 3 * tSize + 70, tSize, true);
		renderFlatFaceRepresentation(image, stateModel.rightRubikFace,  6 * tSize, 3 * tSize + 70, tSize, true);
		renderFlatFaceRepresentation(image, stateModel.backRubikFace,   9 * tSize, 3 * tSize + 70, tSize, true);
		renderFlatFaceRepresentation(image, stateModel.downRubikFace,   3 * tSize, 6 * tSize + 70, tSize, true);
		
		// Faces are transformed (rotate) as per Unfolded Layout representation convention.
		// Faces are orientated as per Face Observation (and N, M axis)
		renderFlatFaceRepresentation(image, stateModel.upRubikFace,     3 * tSize, 0 * tSize + 70 + 350, tSize, false);
		renderFlatFaceRepresentation(image, stateModel.leftRubikFace,   0 * tSize, 3 * tSize + 70 + 350, tSize, false);
		renderFlatFaceRepresentation(image, stateModel.frontRubikFace,  3 * tSize, 3 * tSize + 70 + 350, tSize, false);
		renderFlatFaceRepresentation(image, stateModel.rightRubikFace,  6 * tSize, 3 * tSize + 70 + 350, tSize, false);
		renderFlatFaceRepresentation(image, stateModel.backRubikFace,   9 * tSize, 3 * tSize + 70 + 350, tSize, false);
		renderFlatFaceRepresentation(image, stateModel.downRubikFace,   3 * tSize, 6 * tSize + 70 + 350, tSize, false);
    }

    
	/**
	 * Render Logical Face Cube Layout Representations
	 * 
	 * Render the Rubik Face at the specified location.  
	 * 
     * @param image
     * @param rubikFace
     * @param x
     * @param y
     * @param tSize
     * @param observed  If true, use observed tile array, otherwise use transformed tile array.
     */
    private void renderFlatFaceRepresentation(Mat image, RubikFace rubikFace, int x, int y, int tSize, boolean observed) {
		
		if(rubikFace == null) {
			Core.rectangle(image, new Point( x, y), new Point( x + 3*tSize, y + 3*tSize), Constants.ColorGrey, -1);
		}

		else if(rubikFace.faceRecognitionStatus != FaceRecognitionStatusEnum.SOLVED) {
			Core.rectangle(image, new Point( x, y), new Point( x + 3*tSize, y + 3*tSize), Constants.ColorGrey, -1);
		}
		else

			for(int n=0; n<3; n++) {
				for(int m=0; m<3; m++) {
					
					// Choose observed rotation or transformed rotation.
					ConstantTile tile = observed == true ?
							                  rubikFace.observedTileArray[n][m] :
							                  rubikFace.transformedTileArray[n][m];

			        // Render tile
					if(tile != null)
						Core.rectangle(image, new Point( x + tSize * n, y + tSize * m), new Point( x + tSize * (n + 1), y + tSize * (m + 1)), tile.color, -1);
					else
						Core.rectangle(image, new Point( x + tSize * n, y + tSize * m), new Point( x + tSize * (n + 1), y + tSize * (m + 1)), Constants.ColorGrey, -1);
				}
			}
    }
    
    
    

	/**
	 * Render Face Overlay Annotation
	 * 
	 * 
	 * @param image
	 */
    private void renderFaceOverlayAnnotation(Mat img, boolean accepted) {
    	
    	RubikFace face = stateModel.activeRubikFace;
    	
		Scalar color = Constants.ColorBlack;
		switch(face.faceRecognitionStatus) {
		case UNKNOWN:
		case INSUFFICIENT:
		case INVALID_MATH:
			color = Constants.ColorRed;
			break;
		case BAD_METRICS:
		case INCOMPLETE:
		case INADEQUATE:
		case BLOCKED:
		case UNSTABLE:
			color = Constants.ColorOrange;
			break;
		case SOLVED:
			color = accepted ? Constants.ColorGreen : Constants.ColorYellow;
			break;
		}
		
		// Adjust drawing grid to start at edge of cube and not center of a tile.
		double x = face.lmsResult.origin.x - (face.alphaLatticLength * Math.cos(face.alphaAngle) + face.betaLatticLength * Math.cos(face.betaAngle) ) / 2;
		double y = face.lmsResult.origin.y - (face.alphaLatticLength * Math.sin(face.alphaAngle) + face.betaLatticLength * Math.sin(face.betaAngle) ) / 2;

		for(int n=0; n<4; n++) {
			Core.line(
					img,
					new Point(
							x + n * face.alphaLatticLength * Math.cos(face.alphaAngle),
							y + n * face.alphaLatticLength * Math.sin(face.alphaAngle) ), 
					new Point(
							x + (face.betaLatticLength * 3 * Math.cos(face.betaAngle)) + (n * face.alphaLatticLength * Math.cos(face.alphaAngle) ),
							y + (face.betaLatticLength * 3 * Math.sin(face.betaAngle)) + (n * face.alphaLatticLength * Math.sin(face.alphaAngle) ) ), 
					color, 
					3);
		}
		
		for(int m=0; m<4; m++) {
			Core.line(
					img,
					new Point(
							x + m * face.betaLatticLength * Math.cos(face.betaAngle),
							y + m * face.betaLatticLength * Math.sin(face.betaAngle) ), 
					new Point(
							x + (face.alphaLatticLength * 3 * Math.cos(face.alphaAngle)) + (m * face.betaLatticLength * Math.cos(face.betaAngle) ),
							y + (face.alphaLatticLength * 3 * Math.sin(face.alphaAngle)) + (m * face.betaLatticLength * Math.sin(face.betaAngle) ) ), 
					color, 
					3);
		}
		
//		// Draw a circule at the Rhombus reported center of each tile.
//		for(int n=0; n<3; n++) {
//			for(int m=0; m<3; m++) {
//				Rhombus rhombus = faceRhombusArray[n][m];
//				if(rhombus != null)
//					Core.circle(img, rhombus.center, 5, Constants.ColorBlue, 3);
//			}
//		}
//		
//		// Draw the error vector from center of tile to actual location of Rhombus.
//		for(int n=0; n<3; n++) {
//			for(int m=0; m<3; m++) {
//				Rhombus rhombus = faceRhombusArray[n][m];
//				if(rhombus != null) {
//					
//					Point tileCenter = getTileCenterInPixels(n, m);				
//					Core.line(img, tileCenter, rhombus.center, Constants.ColorRed, 3);
//					Core.circle(img, tileCenter, 5, Constants.ColorBlue, 1);
//				}
//			}
//		}
		
		// Draw reported Logical Tile Color Characters in center of each tile.
		if(face.faceRecognitionStatus == FaceRecognitionStatusEnum.SOLVED)
			for(int n=0; n<3; n++) {
				for(int m=0; m<3; m++) {

					// Draw tile character in UV plane
					Point tileCenterInPixels = face.getTileCenterInPixels(n, m);
					tileCenterInPixels.x -= 10.0;
					tileCenterInPixels.y += 10.0;
					String text = Character.toString(face.observedTileArray[n][m].character);
					Core.putText(img, text, tileCenterInPixels, Constants.FontFace, 3, Constants.ColorBlack, 3);
				}
			}
		
		// Also draw recognized Rhombi for clarity.
		if(face.faceRecognitionStatus != FaceRecognitionStatusEnum.SOLVED)
			for(Rhombus rhombus : face.rhombusList)
				rhombus.draw(img, Constants.ColorGreen);
	}

	

	/**
	 * Render Rhombus Recognition Metrics
	 * 
	 * @param image
	 * @param rhombusList
	 */
    private void renderRhombusRecognitionMetrics(Mat image, List<Rhombus> rhombusList) {
    	
//		RubikFace.drawFlatFaceRepresentation(image, RubikCube.active, 50, 50, 50);

    	Core.rectangle(image, new Point(0, 0), new Point(450, 720), Constants.ColorBlack, -1);
    	
		int totalNumber = 0;
		int totalNumberValid = 0;
		
		int totalNumberUnknow = 0;
		int totalNumberNot4Points = 0;
		int totalNumberNotConvex = 0;
		int totalNumberBadArea = 0;
		int totalNumberClockwise = 0;
		int totalNumberOutlier = 0;

		// Loop over Rhombus list and total status types.
		for(Rhombus rhombus : rhombusList)  {

			switch(rhombus.status) {
			case NOT_PROCESSED:
				totalNumberUnknow++;
				break;
			case NOT_4_POINTS:
				totalNumberNot4Points++;
				break;
			case NOT_CONVEX:
				totalNumberNotConvex++;
				break;
			case AREA:
				totalNumberBadArea++;
				break;
			case CLOCKWISE:
				totalNumberClockwise++;
				break;
			case OUTLIER:
				totalNumberOutlier++;
				break;
			case VALID:
				totalNumberValid++;
				break;
			default:
				break;
			}
			totalNumber++;
		}
		
		Core.putText(image, "Num Unknown: " + totalNumberUnknow,          new Point(50, 300), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, "Num Not 4 Points: " + totalNumberNot4Points, new Point(50, 350), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, "Num Not Convex: " + totalNumberNotConvex,    new Point(50, 400), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, "Num Bad Area: " + totalNumberBadArea,        new Point(50, 450), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, "Num Clockwise: " + totalNumberClockwise,     new Point(50, 500), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, "Num Outlier: " + totalNumberOutlier,         new Point(50, 550), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, "Num Valid: " + totalNumberValid,             new Point(50, 600), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, "Total Num: " + totalNumber,                  new Point(50, 650), Constants.FontFace, 2, Constants.ColorWhite, 2);
    }


	/**
	 * Diagnostic Text Rendering of Rubik Face Metrics
	 * 
	 * @param image
	 * @param activeRubikFace
	 */
    private void renderRubikFaceMetrics(Mat image, RubikFace activeRubikFace) {

    	Core.rectangle(image, new Point(0, 0), new Point(450, 720), Constants.ColorBlack, -1);
    	
    	if(activeRubikFace == null)
    		return;
		
    	RubikFace face = activeRubikFace;
    	renderFlatFaceRepresentation(image, face, 50, 50, 50, true);

		Core.putText(image, "Status = " + face.faceRecognitionStatus,                              new Point(50, 300), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, String.format("AlphaA = %4.1f", face.alphaAngle * 180.0 / Math.PI),    new Point(50, 350), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, String.format("BetaA  = %4.1f", face.betaAngle  * 180.0 / Math.PI),    new Point(50, 400), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, String.format("AlphaL = %4.0f", face.alphaLatticLength),               new Point(50, 450), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, String.format("Beta L = %4.0f", face.betaLatticLength),                new Point(50, 500), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, String.format("Gamma  = %4.2f", face.gammaRatio),                      new Point(50, 550), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, String.format("Sigma  = %5.0f", face.lmsResult.sigma),                 new Point(50, 600), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, String.format("Moves  = %d",    face.numRhombusMoves),                 new Point(50, 650), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, String.format("#Rohmbi= %d",    face.rhombusList.size()),              new Point(50, 700), Constants.FontFace, 2, Constants.ColorWhite, 2);

    }
    
    
    
	/**
	 * Render Face Color Metrics
	 * 
	 * Render a 2D representation of observed tile colors vs.  pre-defined constant rubik tile colors. 
	 * Also, right side 1D representation of measured and adjusted luminous.  See ...... for 
	 * existing luminous correction.
	 * 
	 * @param image
	 * @param face
	 */
    private void renderFaceColorMetrics(Mat image, RubikFace face) {
    	
    	Core.rectangle(image, new Point(0, 0), new Point(570, 720), Constants.ColorBlack, -1);
    	
		if(face.faceRecognitionStatus != FaceRecognitionStatusEnum.SOLVED)
			return;

		// Draw simple grid
		Core.rectangle(image, new Point(-256 + 256, -256 + 400), new Point(256 + 256, 256 + 400), Constants.ColorWhite);
		Core.line(image, new Point(0 + 256, -256 + 400), new Point(0 + 256, 256 + 400), Constants.ColorWhite);		
		Core.line(image, new Point(-256 + 256, 0 + 400), new Point(256 + 256, 0 + 400), Constants.ColorWhite);
		Core.putText(image, String.format("Luminosity Offset = %4.0f", face.luminousOffset), new Point(0, -256 + 400 - 60), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, String.format("Color Error Before Corr = %4.0f", face.colorErrorBeforeCorrection), new Point(0, -256 + 400 - 30), Constants.FontFace, 2, Constants.ColorWhite, 2);
		Core.putText(image, String.format("Color Error After Corr = %4.0f", face.colorErrorAfterCorrection), new Point(0, -256 + 400), Constants.FontFace, 2, Constants.ColorWhite, 2);

		for(int n=0; n<3; n++) {
			for(int m=0; m<3; m++) {

				double [] measuredTileColor = face.measuredColorArray[n][m];
//				Log.e(Constants.TAG, "RGB: " + logicalTileArray[n][m].character + "=" + actualTileColor[0] + "," + actualTileColor[1] + "," + actualTileColor[2] + " x=" + x + " y=" + y );
				double[] measuredTileColorYUV   = Util.getYUVfromRGB(measuredTileColor);

//				if(measuredTileColor == null)
//					return;

//				Log.e(Constants.TAG, "Lum: " + logicalTileArray[n][m].character + "=" + acutalTileYUV[0]);

				
				double luminousScaled     = measuredTileColorYUV[0] * 2 - 256;
				double uChromananceScaled = measuredTileColorYUV[1] * 2;
				double vChromananceScaled = measuredTileColorYUV[2] * 2;

				String text = Character.toString(face.observedTileArray[n][m].character);
				
				// Draw tile character in UV plane
				Core.putText(image, text, new Point(uChromananceScaled + 256, vChromananceScaled + 400), Constants.FontFace, 3, face.observedTileArray[n][m].color, 3);
				
				// Draw tile characters on right side for Y axis
				Core.putText(image, text, new Point(512 - 40, luminousScaled + 400 + face.luminousOffset  + MenuAndParams.luminousOffsetParam.value), Constants.FontFace, 3, face.observedTileArray[n][m].color, 3);
				Core.putText(image, text, new Point(512 + 20, luminousScaled + 400), Constants.FontFace, 3, face.observedTileArray[n][m].color, 3);
//				Log.e(Constants.TAG, "Lum: " + logicalTileArray[n][m].character + "=" + luminousScaled);
			}
		}

		Scalar rubikRed    = Constants.constantTileColorArray[ConstantTileColorEnum.RED.ordinal()].color;
		Scalar rubikOrange = Constants.constantTileColorArray[ConstantTileColorEnum.ORANGE.ordinal()].color;
		Scalar rubikYellow = Constants.constantTileColorArray[ConstantTileColorEnum.YELLOW.ordinal()].color;
		Scalar rubikGreen  = Constants.constantTileColorArray[ConstantTileColorEnum.GREEN.ordinal()].color;
		Scalar rubikBlue   = Constants.constantTileColorArray[ConstantTileColorEnum.BLUE.ordinal()].color;
		Scalar rubikWhite  = Constants.constantTileColorArray[ConstantTileColorEnum.WHITE.ordinal()].color;

		
		// Render Color Calibration in UV plane as dots
		Core.circle(image, new Point(2*Util.getYUVfromRGB(rubikRed.val)[1] +    256, 2*Util.getYUVfromRGB(rubikRed.val)[2] + 400), 10, rubikRed, -1);
		Core.circle(image, new Point(2*Util.getYUVfromRGB(rubikOrange.val)[1] + 256, 2*Util.getYUVfromRGB(rubikOrange.val)[2] + 400), 10, rubikOrange, -1);
		Core.circle(image, new Point(2*Util.getYUVfromRGB(rubikYellow.val)[1] + 256, 2*Util.getYUVfromRGB(rubikYellow.val)[2] + 400), 10, rubikYellow, -1);
		Core.circle(image, new Point(2*Util.getYUVfromRGB(rubikGreen.val)[1] +  256, 2*Util.getYUVfromRGB(rubikGreen.val)[2] + 400), 10, rubikGreen, -1);
		Core.circle(image, new Point(2*Util.getYUVfromRGB(rubikBlue.val)[1] +   256, 2*Util.getYUVfromRGB(rubikBlue.val)[2] + 400), 10, rubikBlue, -1);
		Core.circle(image, new Point(2*Util.getYUVfromRGB(rubikWhite.val)[1] +  256, 2*Util.getYUVfromRGB(rubikWhite.val)[2] + 400), 10, rubikWhite, -1);

		// Render Color Calibration on right side Y axis as dots
		Core.line(image, new Point(502, -256 + 2*Util.getYUVfromRGB(rubikRed.val)[0] + 400),    new Point(522, -256 + 2*Util.getYUVfromRGB(rubikRed.val)[0] + 400), rubikRed, 3);
		Core.line(image, new Point(502, -256 + 2*Util.getYUVfromRGB(rubikOrange.val)[0] + 400), new Point(522, -256 + 2*Util.getYUVfromRGB(rubikOrange.val)[0] + 400), rubikOrange, 3);
		Core.line(image, new Point(502, -256 + 2*Util.getYUVfromRGB(rubikGreen.val)[0] + 400),  new Point(522, -256 + 2*Util.getYUVfromRGB(rubikGreen.val)[0] + 400), rubikGreen, 3);
		Core.line(image, new Point(502, -256 + 2*Util.getYUVfromRGB(rubikYellow.val)[0] + 400), new Point(522, -256 + 2*Util.getYUVfromRGB(rubikYellow.val)[0] + 400), rubikYellow, 3);
		Core.line(image, new Point(502, -256 + 2*Util.getYUVfromRGB(rubikBlue.val)[0] + 400),   new Point(522, -256 + 2*Util.getYUVfromRGB(rubikBlue.val)[0] + 400), rubikBlue, 3);
		Core.line(image, new Point(502, -256 + 2*Util.getYUVfromRGB(rubikWhite.val)[0] + 400),  new Point(522, -256 + 2*Util.getYUVfromRGB(rubikWhite.val)[0] + 400), rubikWhite, 3); 
    }
    
    
	/**
	 * Render Cube Diagnostic Metrics
	 * 
	 * Count and display how many colors of each tile were found over the entire cube.
	 * Also output the total tile count of each color.
	 * 
	 * @param image
	 */
	public void renderCubeMetrics(Mat image) {
		
		Core.rectangle(image, new Point(0, 0), new Point(450, 720), Constants.ColorBlack, -1);
		
		// Render Face Types and their center tile color
		int pos = 1;
		for(RubikFace rubikFace : stateModel.nameRubikFaceMap.values()) {
			Core.putText(image, String.format("%s:    %s", rubikFace.faceNameEnum, rubikFace.observedTileArray[1][1].constantTileColor),    new Point(50, 100 + 50*pos++), Constants.FontFace, 2, Constants.ColorWhite, 2);
		}
		
    	// Count how many tile colors entire cube has as a first check.
    	int [] numColorTilesArray = new int[] {0, 0, 0, 0, 0, 0};
		for(RubikFace rubikFace : stateModel.nameRubikFaceMap.values() ) {
			for(int n=0; n<3; n++) {
				for(int m=0; m<3; m++) {
					numColorTilesArray[ rubikFace.observedTileArray[n][m].constantTileColor.ordinal() ]++;
				}
			}	
		}
		
		// Render total tile count of each tile color.
		for(ConstantTileColorEnum constantTileColor : Constants.ConstantTileColorEnum.values()) {
			int count = numColorTilesArray[constantTileColor.ordinal()];
			Core.putText(image, String.format("%s:  %d", constantTileColor, count ),  new Point(50, 100 + 50*pos++), Constants.FontFace, 2, Constants.ColorWhite, 2);
		}
	}
	
	
  	/**
   	 * Render User Instructions
   	 * 
   	 * @param image
   	 */
   	public void renderUserInstructions(Mat image) {

   		// Create black area for text
   		if(MenuAndParams.userTextDisplay == true)
   			Core.rectangle(image, new Point(0, 0), new Point(1270, 60), Constants.ColorBlack, -1);

//   		pilotGLRenderer.setCubeOrienation(rubikFace);

   		switch(stateModel.appState) {

   		case START:
   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "Show Me The Rubik Cube", new Point(0, 60), Constants.FontFace, 5, Constants.ColorWhite, 5);
//   			pilotGLRenderer.setRenderArrow(false);
   			break;

   		case GOT_IT:
   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "OK, Got It", new Point(0, 60), Constants.FontFace, 5, Constants.ColorWhite, 5);
//   			pilotGLRenderer.setRenderArrow(false);
//   			pilotGLRenderer.setRenderCube(MenuAndParams.cubeOverlayDisplay);
   			break;

   		case ROTATE:
   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "Please Rotate: " + stateModel.getNumObservedFaces(), new Point(0, 60), Constants.FontFace, 5, Constants.ColorWhite, 5);
//   			if(  stateModel.getNumValidFaces() % 2 == 0)
//   				pilotGLRenderer.showFullCubeRotateArrow(FaceType.LEFT_TOP);
//   			else
//   				pilotGLRenderer.showFullCubeRotateArrow(FaceType.FRONT_TOP);
   			break;

   		case SEARCHING:
   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "Searching for Another Face", new Point(0, 60), Constants.FontFace, 5, Constants.ColorWhite, 5);
//   			pilotGLRenderer.setRenderArrow(false);
   			break;

   		case COMPLETE:
   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "Cube is Complete and has Good Colors", new Point(0, 60), Constants.FontFace, 4, Constants.ColorWhite, 4);
   			break;

   		case WAITING:
   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "Waiting - Preload Next: " + stateMachine.pruneTableLoaderCount, new Point(0, 60), Constants.FontFace, 5, Constants.ColorWhite, 5);
   			break;

   		case BAD_COLORS:
//   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "Cube is Complete but has Bad Colors", new Point(0, 60), Constants.FontFace, 4, Constants.ColorWhite, 4);
   			break;

   		case VERIFIED:
   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "Cube is Complete and Verified", new Point(0, 60), Constants.FontFace, 4, Constants.ColorWhite, 4);
   			break;

   		case INCORRECT:
//   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "Cube is Complete but Incorrect: " + stateModel.verificationResults, new Point(0, 60), Constants.FontFace, 4, Constants.ColorWhite, 4);
   			break;

   		case SOLVED:
   			if(MenuAndParams.userTextDisplay == true) {
   				Core.putText(image, "SOLUTION: ", new Point(0, 60), Constants.FontFace, 4, Constants.ColorWhite, 4);
   				Core.rectangle(image, new Point(0, 60), new Point(1270, 120), Constants.ColorBlack, -1);
   				Core.putText(image, "" + stateModel.solutionResults, new Point(0, 120), Constants.FontFace, 2, Constants.ColorWhite, 2);
   			}
   			break;

   		case DO_MOVE:
   			String moveNumonic = stateModel.solutionResultsArray[stateModel.solutionResultIndex];
   			Log.d(Constants.TAG, "Move:" + moveNumonic + ":");
   			StringBuffer moveDescription = new StringBuffer("Rotate ");
   			switch(moveNumonic.charAt(0)) {
   			case 'U': moveDescription.append("Top Face"); break;
   			case 'D': moveDescription.append("Down Face"); break;
   			case 'L': moveDescription.append("Left Face"); break;
   			case 'R': moveDescription.append("Right Face"); break;
   			case 'F': moveDescription.append("Front Face"); break;
   			case 'B': moveDescription.append("Back Face"); break;
   			}
   			if(moveNumonic.length() == 1)
   				moveDescription.append(" Clockwise");
   			else if(moveNumonic.charAt(1) == '2')
   				moveDescription.append(" 180 Degrees");
   			else if(moveNumonic.charAt(1) == '\'')
   				moveDescription.append(" Counter Clockwise");
   			else
   				moveDescription.append("?");

   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, moveDescription.toString(), new Point(0, 60), Constants.FontFace, 4, Constants.ColorWhite, 4);

//
//   			// Args to be passed to renderer.
//   			Rotation rotation = null;
//   			FaceType faceType = null;
//   			Scalar color = null;
//   			
//   			if(moveNumonic.length() == 1) 
//   				rotation = Rotation.CLOCKWISE;
//   			else if(moveNumonic.charAt(1) == '2') 
//   				rotation = Rotation.ONE_HUNDRED_EIGHTY;
//   			else if(moveNumonic.charAt(1) == '\'') 
//   				rotation = Rotation.COUNTER_CLOCKWISE;
//   			else
//   				throw new java.lang.Error("Unknow rotation amount");
//
//   			// Obtain details of arrow to be rendered.
//   			switch(moveNumonic.charAt(0)) {
//   			case 'U': 
//   				faceType = FaceType.UP;
//   				color = Constants.RubikWhite;
//   				break;
//   			case 'D': 
//   				faceType = FaceType.DOWN;  
//   				color = Constants.RubikYellow;
//   				break;
//   			case 'L': 
//   				faceType = FaceType.LEFT;
//   				color = Constants.RubikGreen;
//   				break;
//   			case 'R': 
//   				faceType = FaceType.RIGHT; 
//   				color = Constants.RubikBlue;
//   				break;
//   			case 'F': 
//   				faceType = FaceType.FRONT;
//   				color = Constants.RubikRed;
//   				break;
//   			case 'B':
//   				faceType = FaceType.BACK;
//   				color = Constants.RubikOrange;
//   				break;
//   			}
//   			pilotGLRenderer.setRenderCube(true && MenuAndParams.cubeOverlayDisplay);
//   			pilotGLRenderer.showCubeEdgeRotationArrow(
//   					rotation,
//   					faceType, 
//   					color);
   			break;

   		case WAITING_FOR_MOVE_COMPLETE:
//   			pilotGLRenderer.setRenderArrow(false);
   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "Waiting for move to be completed", new Point(0, 60), Constants.FontFace, 4, Constants.ColorWhite, 4);
   			break;

   		case DONE:
//   			pilotGLRenderer.setRenderArrow(false);
   			break;

   		default:
   			if(MenuAndParams.userTextDisplay == true)
   				Core.putText(image, "Oops", new Point(0, 60), Constants.FontFace, 5, Constants.ColorWhite, 5);
   			break;
   		}
   		
   		// User indicator that tables have been computed.
   		Core.line(image, new Point(0, 0), new Point(1270, 0), stateMachine.pruneTableLoaderCount < 12 ? Constants.ColorRed : Constants.ColorGreen, 4);
   	}

}