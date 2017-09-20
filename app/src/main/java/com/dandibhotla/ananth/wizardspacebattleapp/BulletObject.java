package com.dandibhotla.ananth.wizardspacebattleapp;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;


/**
 * Created by Ananth on 7/23/2017.
 */

public class BulletObject {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // The matrix must be included as a modifier of gl_Position.
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final FloatBuffer vertexBuffer,vertexBuffer2;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    public static final String COLOR_RED = "red";
    public static final String COLOR_BLUE = "blue";
    public static final String FACE_LEFT = "left";
    public static final String FACE_RIGHT = "right";

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    /*static float squareCoords[] = {
            -0.1f, 0.1f, 0.0f,   // top left
            -0.1f, -0.1f, 0.0f,   // bottom left
            0.1f, -0.1f, 0.0f,   // bottom right
            0.1f, 0.1f, 0.0f}; // top right*/
    static float leftCoords[] = {
            -0.15f, 0.01f, 0.0f,   // top left
            -0.15f, -0.01f, 0.0f,   // bottom left
            -0.1f, -0.01f, 0.0f,   // bottom right
            -0.1f, 0.01f, 0.0f}; // top right
    static float rightCoords[] = {
            0.1f, 0.01f, 0.0f,   // top left
            0.1f, -0.01f, 0.0f,   // bottom left
            0.15f, -0.01f, 0.0f,   // bottom right
            0.15f, 0.01f, 0.0f}; // top right
    private final short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

   private final float colorP1[] = {0f, 0f, 1f, 1f};
   private final float colorP2[] = {1f, 0f, 0f, 1f};

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public BulletObject() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                leftCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(leftCoords);
        vertexBuffer.position(0);


        ByteBuffer bb2 = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                rightCoords.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        vertexBuffer2 = bb2.asFloatBuffer();
        vertexBuffer2.put(rightCoords);
        vertexBuffer2.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);// create OpenGL program executables

    }


    public void draw(ArrayList<Bullet> bullets) {
        for (Bullet b : bullets) {
            // Add program to OpenGL environment
            GLES20.glUseProgram(mProgram);

            // get handle to vertex shader's vPosition member
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Prepare the triangle coordinate data
            if (b.getDirection().equals(Player.LEFT_FACING)) {
                GLES20.glVertexAttribPointer(
                        mPositionHandle, COORDS_PER_VERTEX,
                        GLES20.GL_FLOAT, false,
                        vertexStride, vertexBuffer);
            } else {
                GLES20.glVertexAttribPointer(
                        mPositionHandle, COORDS_PER_VERTEX,
                        GLES20.GL_FLOAT, false,
                        vertexStride, vertexBuffer2);
            }

            // get handle to fragment shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
            GLES20.glDisable(GLES20.GL_CULL_FACE);

// No depth testing
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);

// Enable blending
            //GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
            // Set colorWhite for drawing the triangle
          //  Log.v("colorWhite",b.getColor());
            if (b.getColor().equals(Player.PLAYER_TWO)) {
                GLES20.glUniform4fv(mColorHandle, 1, Player.colorP2, 0);
            } else {
                GLES20.glUniform4fv(mColorHandle, 1, Player.colorP1, 0);
            }
            // get handle to shape's transformation matrix
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            MyGLRenderer.checkGlError("glGetUniformLocation");

            // Apply the projection and view transformation
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, b.getMvpMatrix(), 0);
            MyGLRenderer.checkGlError("glUniformMatrix4fv");

            // Draw the square
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES, drawOrder.length,
                    GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set colorWhite for drawing the triangle

            GLES20.glUniform4fv(mColorHandle, 1, colorP2, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}