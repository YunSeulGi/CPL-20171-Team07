package org.andresoviedo.app.model3D.view;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.andresoviedo.app.model3D.entities.Camera;
import org.andresoviedo.app.model3D.model.Object3D;
import org.andresoviedo.app.model3D.model.Object3DBuilder;
import org.andresoviedo.app.model3D.model.Object3DData;
import org.andresoviedo.app.model3D.model.Object3DImpl;
import org.andresoviedo.app.model3D.services.SceneLoader;
import org.andresoviedo.app.model3D.util.GLUtil;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.Toast;

public class ModelRenderer implements GLSurfaceView.Renderer {

	private final static String TAG = ModelRenderer.class.getName();

	private ModelSurfaceView main;
	private int width;
	private int height;
	private Camera camera;
	private float near = 1f;
	private float far = 10f;

	private Object3DBuilder drawer;
	private Map<Object3DData, Object3DData> wireframes = new HashMap<Object3DData, Object3DData>();
	private Map<byte[], Integer> textures = new HashMap<byte[], Integer>();
	private Map<Object3DData, Object3DData> boundingBoxes = new HashMap<Object3DData, Object3DData>();
	private Map<Object3DData, Object3DData> normals = new HashMap<Object3DData, Object3DData>();

	private final float[] modelProjectionMatrix = new float[16];
	private final float[] modelViewMatrix = new float[16];
	private final float[] mvpMatrix = new float[16];

	private final float[] lightPosInEyeSpace = new float[4];

	public ModelRenderer(ModelSurfaceView modelSurfaceView) {
		this.main = modelSurfaceView;
	}

	public float getNear() {
		return near;
	}

	public float getFar() {
		return far;
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		float[] backgroundColor = main.getModelActivity().getBackgroundColor();
		GLES20.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		camera = new Camera();

		drawer = new Object3DBuilder();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		this.width = width;
		this.height = height;
		GLES20.glViewport(0, 0, width, height);
		Matrix.setLookAtM(modelViewMatrix, 0, camera.xPos, camera.yPos, camera.zPos, camera.xView, camera.yView,
				camera.zView, camera.xUp, camera.yUp, camera.zUp);
		float ratio = (float) width / height;
		Log.d(TAG, "projection: [" + -ratio + "," + ratio + ",-1,1]-near/far[1,10]");
		Matrix.frustumM(modelProjectionMatrix, 0, -ratio, ratio, -1, 1, getNear(), getFar());
		Matrix.multiplyMM(mvpMatrix, 0, modelProjectionMatrix, 0, modelViewMatrix, 0);
	}

	@Override
	public void onDrawFrame(GL10 unused) {

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		if (camera.hasChanged()) {
			Matrix.setLookAtM(modelViewMatrix, 0, camera.xPos, camera.yPos, camera.zPos, camera.xView, camera.yView,
					camera.zView, camera.xUp, camera.yUp, camera.zUp);
			Matrix.multiplyMM(mvpMatrix, 0, modelProjectionMatrix, 0, modelViewMatrix, 0);
			camera.setChanged(false);
		}
		SceneLoader scene = main.getModelActivity().getScene();
		if (scene == null) {
			return;
		}
		camera.setScene(scene);
		scene.onDrawFrame();
		if (scene.isDrawLighting()) {
			Object3DImpl lightBulbDrawer = (Object3DImpl) drawer.getPointDrawer();
			float[] lightModelViewMatrix = lightBulbDrawer.getMvMatrix(lightBulbDrawer.getMMatrix(scene.getLightBulb()),modelViewMatrix);
			Matrix.multiplyMV(lightPosInEyeSpace, 0, lightModelViewMatrix, 0, scene.getLightBulb().getPosition(), 0);
			lightBulbDrawer.draw(scene.getLightBulb(), modelProjectionMatrix, modelViewMatrix, -1, lightPosInEyeSpace);
		}

		List<Object3DData> objects = scene.getObjects();
		for (int i=0; i<objects.size(); i++) {
			try {
				Object3DData objData = objects.get(i);
				boolean changed = objData.isChanged();

				Object3D drawerObject = drawer.getDrawer(objData, scene.isDrawTextures(), scene.isDrawLighting());

				Integer textureId = textures.get(objData.getTextureData());
				if (textureId == null && objData.getTextureData() != null) {
					ByteArrayInputStream textureIs = new ByteArrayInputStream(objData.getTextureData());
					textureId = GLUtil.loadTexture(textureIs);
					textureIs.close();
					textures.put(objData.getTextureData(), textureId);
				}

				if (scene.isDrawWireframe() && objData.getDrawMode() != GLES20.GL_POINTS
						&& objData.getDrawMode() != GLES20.GL_LINES && objData.getDrawMode() != GLES20.GL_LINE_STRIP
						&& objData.getDrawMode() != GLES20.GL_LINE_LOOP) {
					try{
						Object3DData wireframe = wireframes.get(objData);
						if (wireframe == null || changed) {
							Log.i("ModelRenderer","Generating wireframe model...");
//							wireframe = Object3DBuilder.buildWireframe4(objData);
//							wireframe.centerAndScale(5.0f);
							wireframe = Object3DBuilder.buildWireframe(objData);
							wireframes.put(objData, wireframe);
						}
						drawerObject.draw(wireframe,modelProjectionMatrix,modelViewMatrix,wireframe.getDrawMode(),
								wireframe.getDrawSize(),textureId != null? textureId:-1, lightPosInEyeSpace);
					}catch(Error e){
						Log.e("ModelRenderer",e.getMessage(),e);
					}
				} else if (scene.isDrawPoints() || (objData.getFaces() != null && !objData.getFaces().loaded())){
					drawerObject.draw(objData, modelProjectionMatrix, modelViewMatrix
							,GLES20.GL_POINTS, objData.getDrawSize(),
							textureId != null ? textureId : -1, lightPosInEyeSpace);
				} else {
					drawerObject.draw(objData, modelProjectionMatrix, modelViewMatrix,
							textureId != null ? textureId : -1, lightPosInEyeSpace);
				}

				if (scene.isDrawBoundingBox() || scene.getSelectedObject() == objData) {
					Object3DData boundingBoxData = boundingBoxes.get(objData);
					if (boundingBoxData == null || changed) {
						boundingBoxData = Object3DBuilder.buildBoundingBox(objData);
						boundingBoxes.put(objData, boundingBoxData);
					}
					Object3D boundingBoxDrawer = drawer.getBoundingBoxDrawer();
					boundingBoxDrawer.draw(boundingBoxData, modelProjectionMatrix, modelViewMatrix, -1, null);
				}

				if (scene.isDrawNormals()) {
					Object3DData normalData = normals.get(objData);
					if (normalData == null || changed) {
						normalData = Object3DBuilder.buildFaceNormals(objData);
						if (normalData != null) {
							normals.put(objData, normalData);
						}
					}
					if (normalData != null) {
						Object3D normalsDrawer = drawer.getFaceNormalsDrawer();
						normalsDrawer.draw(normalData, modelProjectionMatrix, modelViewMatrix, -1, null);
					}
				}
			} catch (IOException ex) {
				Toast.makeText(main.getModelActivity().getApplicationContext(),
						"There was a problem creating 3D object", Toast.LENGTH_LONG).show();
			}
		}
	}

	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public float[] getModelProjectionMatrix() {
		return modelProjectionMatrix;
	}
	public float[] getModelViewMatrix() {
		return modelViewMatrix;
	}
	public Camera getCamera() {
		return camera;
	}
}