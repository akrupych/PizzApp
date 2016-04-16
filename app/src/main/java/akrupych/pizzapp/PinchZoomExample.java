package akrupych.pizzapp;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.RotationAtModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;

public class PinchZoomExample extends SimpleBaseGameActivity implements IOnSceneTouchListener, IPinchZoomDetectorListener {

    private static final int CAMERA_WIDTH = 720;
    private static final int CAMERA_HEIGHT = 1280;
    private static final int CENTER_X = CAMERA_WIDTH / 2;
    private static final int CENTER_Y = CAMERA_HEIGHT / 2;

    private ZoomCamera zoomCamera;

    private ITextureRegion tableclothTextureRegion;
    private ITextureRegion pizzaTextureRegion;
    private ITextureRegion circleTextureRegion;

    private PinchZoomDetector pinchZoomDetector;
    private float mPinchZoomStartedCameraZoomFactor;

    @Override
    public EngineOptions onCreateEngineOptions() {
        zoomCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
                new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), zoomCamera);
    }

    @Override
    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(getTextureManager(), 454, 449, TextureOptions.BILINEAR);
        BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "pizza.png", 0, 0);
        textureAtlas.load();
        pizzaTextureRegion = TextureRegionFactory.extractFromTexture(
                textureAtlas, 0, 0, textureAtlas.getWidth(), textureAtlas.getHeight());

//        textureAtlas = new BitmapTextureAtlas(getTextureManager(), 626, 372, TextureOptions.BILINEAR);
//        BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "tablecloth.jpg", 0, 0);
//        textureAtlas.load();
//        tableclothTextureRegion = TextureRegionFactory.extractFromTexture(
//                textureAtlas, 0, 0, textureAtlas.getWidth(), textureAtlas.getHeight());

        textureAtlas = new BitmapTextureAtlas(getTextureManager(), 256, 256, TextureOptions.REPEATING_BILINEAR);
        BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "tablecloth3.jpg", 0, 0);
        textureAtlas.load();
        tableclothTextureRegion = TextureRegionFactory.extractFromTexture(
                textureAtlas, 0, 0, textureAtlas.getWidth(), textureAtlas.getHeight());

        textureAtlas = new BitmapTextureAtlas(getTextureManager(), 200, 200, TextureOptions.BILINEAR);
        BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "circle.png", 0, 0);
        textureAtlas.load();
        circleTextureRegion = TextureRegionFactory.extractFromTexture(
                textureAtlas, 0, 0, textureAtlas.getWidth(), textureAtlas.getHeight());
    }

    @Override
    public Scene onCreateScene() {
        mEngine.registerUpdateHandler(new FPSLogger());

        Scene scene = new Scene();
        scene.setOnAreaTouchTraversalFrontToBack();

//        scene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
        scene.setBackground(new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT,
                tableclothTextureRegion, getVertexBufferObjectManager()));

//        Rectangle shadow = new Rectangle(CENTER_X, CENTER_Y, CAMERA_WIDTH, CAMERA_HEIGHT, getVertexBufferObjectManager());
//        shadow.setColor(0.5f, 0.5f, 0.5f, 0.3f);
//        scene.attachChild(shadow);

        pinchZoomDetector = new PinchZoomDetector(this);

        scene.setOnSceneTouchListener(this);
        scene.setTouchAreaBindingOnActionDownEnabled(true);

//        Sprite circle1 = new Sprite(CENTER_X, CENTER_Y, 660, 660,
//                circleTextureRegion, getVertexBufferObjectManager());
//        scene.attachChild(circle1);

        Sprite sprite = new Sprite(CENTER_X, CENTER_Y, 300, 300,
                pizzaTextureRegion, getVertexBufferObjectManager());
        scene.attachChild(sprite);
        sprite.registerEntityModifier(new LoopEntityModifier(
                new RotationAtModifier(30, 0, 360, 0.5f, 0.5f)));

        return scene;
    }

    @Override
    public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
        mPinchZoomStartedCameraZoomFactor = zoomCamera.getZoomFactor();
    }

    @Override
    public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
        zoomCamera.setZoomFactor(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
    }

    @Override
    public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector,
                                    final TouchEvent pTouchEvent, float pZoomFactor) {
        float newScaleFactor = mPinchZoomStartedCameraZoomFactor * pZoomFactor;
        if (newScaleFactor > 2) {
            newScaleFactor = 2;
        } else if (newScaleFactor < 1) {
            newScaleFactor = 1;
        }
        zoomCamera.setZoomFactor(newScaleFactor);
    }


    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        pinchZoomDetector.onTouchEvent(pSceneTouchEvent);
        return true;
    }
}