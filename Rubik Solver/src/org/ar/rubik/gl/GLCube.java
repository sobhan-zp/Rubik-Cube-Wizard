/**
 * Augmented Reality Rubik Cube Wizard
 * 
 * Author: Steven P. Punte (aka Android Steve : android.steve@cl-sw.com)
 * Date:   April 25th 2015
 * 
 * Project Description:
 *   Android application developed on a commercial Smart Phone which, when run on a pair 
 *   of Smart Glasses, guides a user through the process of solving a Rubik Cube.
 *   
 * File Description:
 *   Renders a six sided cube in Object Coordinates centered at the origin with
 *   edge length of 2.0 units.
 *   
 * To Do List:
 *   0)  How to get colors from global Constants
 *   1)  Color can be assigned per face name.
 *   2)  Only the center tile of a face is colored.
 *   3)  Orientation is set able (i.e., xxxxxxxxxx)
 *   4)  Or should we consider Rubik Cube Animator
 * 
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
package org.ar.rubik.gl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.ar.rubik.Constants;
import org.ar.rubik.Constants.FaceNameEnum;
import org.ar.rubik.RubikFace;
import org.ar.rubik.StateModel;

import android.opengl.GLES20;

/**
 * A three-dimensional cube use as a drawn object in OpenGL ES 2.0.
 */
public class GLCube {
    
    public enum Transparency { OPAQUE, TRANSLUCENT, TRANSPARENT, WIREFRAME };
    
    // Buffer for vertex-array
    private FloatBuffer vertexBuffer;

    // Used to obtain center tile color information
    private StateModel stateModel; 
    
    // number of cube faces: of course it is 6!
    private static final int NUM_FACES = 6;

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 3;
    
    // number of bytes in a float
    private static final int BYTES_PER_FLOAT = 4;

    // number of total bytes in vertex stride: 12 in this case.
    private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * BYTES_PER_FLOAT;
    
    // A completely transparent OpenGL color
    private static float [] transparentBlack = { 0f, 0f, 0f, 0f};
    
    // A grey translucent OpenGL color
    private static float [] translusentGrey = { 0.5f, 0.5f, 0.5f, 0.5f};

    // An opaque white OpenGL color
    private static float [] opaqueWhite = { 1.0f, 1.0f, 1.0f, 1.0f};
    
    private static float[] vertices = {  // Vertices of the 6 faces
            // FRONT
           -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
           -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            1.0f,  1.0f,  1.0f,  // 3. right-top-front
            // BACK
            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
           -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
           -1.0f,  1.0f, -1.0f,  // 5. left-top-back
            // LEFT
           -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
           -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front 
           -1.0f,  1.0f, -1.0f,  // 5. left-top-back
           -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            // RIGHT
            1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
            1.0f,  1.0f,  1.0f,  // 3. right-top-front
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            // UP
           -1.0f,  1.0f,  1.0f,  // 2. left-top-front
            1.0f,  1.0f,  1.0f,  // 3. right-top-front
           -1.0f,  1.0f, -1.0f,  // 5. left-top-back
            1.0f,  1.0f, -1.0f,  // 7. right-top-back
            // DOWN
           -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
            1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
           -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
            1.0f, -1.0f,  1.0f   // 1. right-bottom-front
    };

    
    

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     * @param stateModel 
     * @param programID2 
     */
    public GLCube(StateModel stateModel) {
        
        this.stateModel = stateModel;
        
        // Setup vertex-array buffer. Vertices in float. A float has 4 bytes
        // This reserves memory that GPU has direct access to (correct?).
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        vertexBuffer.put(vertices);         // Copy data into buffer
        vertexBuffer.position(0);           // Rewind
    }

    
    
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     * @param programID 
     */
    public void draw(float[] mvpMatrix, Transparency transparencyMode, int programID) {
        
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);   // =+= ?? Why ??
        
        // Add program to OpenGL environment
        GLES20.glUseProgram(programID);

        // get handle to vertex shader's vPosition member
        int vertexArrayID = GLES20.glGetAttribLocation(programID, "vPosition");

        // Enable a handle to the cube vertices
        GLES20.glEnableVertexAttribArray(vertexArrayID);

        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(
                vertexArrayID, 
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                VERTEX_STRIDE,
                vertexBuffer);
        
        // get handle to fragment shader's vColor member
        int colorID = GLES20.glGetUniformLocation(programID, "vColor");

        // get handle to shape's transformation matrix
        int mvpMatrixID = GLES20.glGetUniformLocation(programID, "uMVPMatrix");
        GLUtil.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mvpMatrixID, 1, false, mvpMatrix, 0);
        GLUtil.checkGlError("glUniformMatrix4fv");
        
        // Render all the faces
        for (int faceIndex = 0; faceIndex < NUM_FACES; faceIndex++) {
            
        	// Specify color
            switch (transparencyMode) {

            case TRANSPARENT:
                GLES20.glUniform4fv(colorID, 1, transparentBlack, 0);
                break;

            case OPAQUE:

                // Get Face
                RubikFace rubikFace = null;
                switch(faceIndex) {
                case 0: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.FRONT); break;
                case 1: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.BACK);  break;
                case 2: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.LEFT);  break;
                case 3: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.RIGHT); break;
                case 4: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.UP);    break;
                case 5: rubikFace = stateModel.nameRubikFaceMap.get( FaceNameEnum.DOWN);  break;
                }

                // Color in GL format
                float [] colorGL = (rubikFace != null && rubikFace.observedTileArray != null && rubikFace.observedTileArray[1][1] != null) ? 
                		rubikFace.observedTileArray[1][1].glColor : Constants.ColorTileEnum.GREY.glColor;

                // Render
                GLES20.glUniform4fv(colorID, 1, colorGL, 0);
                break;

            case TRANSLUCENT:
                GLES20.glUniform4fv(colorID, 1, translusentGrey, 0);
                break;
                
            case WIREFRAME:
                GLES20.glUniform4fv(colorID, 1, opaqueWhite, 0);
                break;
            }

            // Draw 
            switch (transparencyMode) {

            case WIREFRAME:
            	
            	GLES20.glLineWidth(10.0f);
            	
            	// =+= This is not as exected: need's its own vertex set.
            	// Draw Lines
            	GLES20.glDrawArrays(
            			GLES20.GL_LINE_LOOP,
            			faceIndex*BYTES_PER_FLOAT, 
            			BYTES_PER_FLOAT);

            	break;

            case  OPAQUE:
            case TRANSLUCENT:
            case TRANSPARENT: 

            	// Draw Triangles
            	GLES20.glDrawArrays(
            			GLES20.GL_TRIANGLE_STRIP, 
            			faceIndex*BYTES_PER_FLOAT, 
            			BYTES_PER_FLOAT);

            	break;
            }
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(vertexArrayID);
        
        GLES20.glDisable(GLES20.GL_CULL_FACE);
    }
}