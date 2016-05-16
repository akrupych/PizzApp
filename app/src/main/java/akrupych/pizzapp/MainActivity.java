package akrupych.pizzapp;

import android.graphics.Typeface;
import android.widget.Toast;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.RotationAtModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.primitive.Gradient;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.StrokeFont;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseElasticInOut;

import java.io.IOException;

public class MainActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener, IPinchZoomDetectorListener {

    private static final int CAMERA_WIDTH = 720;
    private static final int CAMERA_HEIGHT = 1280;
    private static final int CENTER_X = CAMERA_WIDTH / 2;
    private static final int CENTER_Y = CAMERA_HEIGHT / 2;

    private static final int[] PIZZA_SIZES = {30, 40, 50};
    private static final int DEFAULT_PIZZA_SIZE = 40;
    private static final float[] PIZZA_SCALE_FACTORS = {0.75f, 1f, 1.25f};
    private static final int PIZZA_SIZE_TO_PIXELS_SCALE = 17;

    private int currentPizzaSize = DEFAULT_PIZZA_SIZE;

    private ITextureRegion tableclothTextureRegion;
    private ITextureRegion pizzaTextureRegion;
    private ITextureRegion circleTextureRegion;
    private ITextureRegion okButtonTextureRegion;
    private ITextureRegion okButtonPressedTextureRegion;

    private PinchZoomDetector pinchZoomDetector;
    private float mPinchZoomStartedCameraZoomFactor;
    private StrokeFont font;
    private Text diameter;

    private Sprite[] pizzaSizeCircles = new Sprite[PIZZA_SIZES.length];
    private Sprite pizzaSprite;
    private Music music;
    private Sound pizzaSound;

    @Override
    public EngineOptions onCreateEngineOptions() {
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
                new FillResolutionPolicy(), new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT));
        engineOptions.getAudioOptions().setNeedsMusic(true);
        engineOptions.getAudioOptions().setNeedsSound(true);
        return engineOptions;
    }

    @Override
    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(getTextureManager(), 454, 449, TextureOptions.BILINEAR);
        BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "pizza.png", 0, 0);
        textureAtlas.load();
        pizzaTextureRegion = TextureRegionFactory.extractFromTexture(
                textureAtlas, 0, 0, textureAtlas.getWidth(), textureAtlas.getHeight());

        textureAtlas = new BitmapTextureAtlas(getTextureManager(), 256, 256, TextureOptions.REPEATING_BILINEAR);
        BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "tablecloth.jpg", 0, 0);
        textureAtlas.load();
        tableclothTextureRegion = TextureRegionFactory.extractFromTexture(
                textureAtlas, 0, 0, textureAtlas.getWidth(), textureAtlas.getHeight());

        textureAtlas = new BitmapTextureAtlas(getTextureManager(), 360, 360, TextureOptions.BILINEAR);
        BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "green_circle.png", 0, 0);
        textureAtlas.load();
        circleTextureRegion = TextureRegionFactory.extractFromTexture(
                textureAtlas, 0, 0, textureAtlas.getWidth(), textureAtlas.getHeight());

        textureAtlas = new BitmapTextureAtlas(getTextureManager(), 540, 256, TextureOptions.BILINEAR);
        BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "ok_button.png", 0, 0);
        textureAtlas.load();
        okButtonTextureRegion = TextureRegionFactory.extractFromTexture(textureAtlas, 0, 0, 270, 256);
        okButtonPressedTextureRegion = TextureRegionFactory.extractFromTexture(textureAtlas, 270, 0, 270, 256);

        font = FontFactory.createStroke(this.getFontManager(), this.getTextureManager(), 256, 256,
                TextureOptions.BILINEAR, Typeface.DEFAULT_BOLD, 48,
                Color.GREEN_ARGB_PACKED_INT, 1, Color.RED_ARGB_PACKED_INT);
        font.load();

        MusicFactory.setAssetBasePath("mfx/");
        try {
            music = MusicFactory.createMusicFromAsset(getEngine().getMusicManager(), this, "vivaldi_spring.mp3");
            music.setLooping(true);
        } catch (final IOException e) {
            Debug.e(e);
        }

        SoundFactory.setAssetBasePath("mfx/");
        try {
            pizzaSound = SoundFactory.createSoundFromAsset(getEngine().getSoundManager(), this, "jump.wav");
        } catch (final IOException e) {
            Debug.e(e);
        }
    }

    @Override
    public Scene onCreateScene() {

        mEngine.registerUpdateHandler(new FPSLogger());

        Scene scene = new Scene();
        scene.setOnAreaTouchTraversalFrontToBack();

        pinchZoomDetector = new PinchZoomDetector(this);
        scene.setOnSceneTouchListener(this);
        scene.setTouchAreaBindingOnActionDownEnabled(true);

        scene.setBackground(new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT,
                tableclothTextureRegion, getVertexBufferObjectManager()));

        for (int i = 0; i < pizzaSizeCircles.length; i++) {
            pizzaSizeCircles[i] = new Sprite(CENTER_X, CENTER_Y,
                    PIZZA_SIZES[i] * PIZZA_SIZE_TO_PIXELS_SCALE,
                    PIZZA_SIZES[i] * PIZZA_SIZE_TO_PIXELS_SCALE,
                    circleTextureRegion, getVertexBufferObjectManager());
        }

        pizzaSprite = new Sprite(CENTER_X, CENTER_Y,
                currentPizzaSize * PIZZA_SIZE_TO_PIXELS_SCALE,
                currentPizzaSize * PIZZA_SIZE_TO_PIXELS_SCALE,
                pizzaTextureRegion, getVertexBufferObjectManager());
        pizzaSprite.registerEntityModifier(new LoopEntityModifier(
                new RotationAtModifier(30, 0, 360, 0.5f, 0.5f)));
        scene.attachChild(pizzaSprite);

        Sprite okButtonSprite = new ButtonSprite(CAMERA_WIDTH - 100, 100,
                okButtonTextureRegion, okButtonPressedTextureRegion,
                getVertexBufferObjectManager(), new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "То ся зробе", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        okButtonSprite.setSize(150, 150);
        scene.attachChild(okButtonSprite);
        scene.registerTouchArea(okButtonSprite);

        HUD hud = new HUD();
        getEngine().getCamera().setHUD(hud);

        Gradient topGradient = new Gradient(CAMERA_WIDTH / 2, CAMERA_HEIGHT - 50, CAMERA_WIDTH, 150, getVertexBufferObjectManager());
        topGradient.setGradient(0.5f, 0.5f, 0, 0, 0, 0, 1, 0, 0, -1);
        hud.attachChild(topGradient);

        Gradient bottomGradient = new Gradient(CAMERA_WIDTH / 2, 50, CAMERA_WIDTH, 150, getVertexBufferObjectManager());
        bottomGradient.setGradient(0.5f, 0.5f, 0, 0, 0, 0, 1, 0, 0, 1);
        hud.attachChild(bottomGradient);

        Text title = new Text(CAMERA_WIDTH / 2, CAMERA_HEIGHT - 50, font, "Розмір має значення", getVertexBufferObjectManager());
        hud.attachChild(title);

        diameter = new Text(CAMERA_WIDTH / 2, 50, font, "Ø 40см", getVertexBufferObjectManager());
        updatePizzaSizeText();
        hud.attachChild(diameter);

        return scene;
    }

    @Override
    public synchronized void onResumeGame() {
        super.onResumeGame();
        music.play();
    }

    @Override
    public synchronized void onPauseGame() {
        super.onPauseGame();
        music.pause();
    }

    @Override
    public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
        mPinchZoomStartedCameraZoomFactor = pizzaSprite.getScaleX();
        for (Sprite pizzaSizeCircle : pizzaSizeCircles) {
            getEngine().getCamera().getHUD().attachChild(pizzaSizeCircle);
        }
    }

    @Override
    public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
        pizzaSprite.setScale(mPinchZoomStartedCameraZoomFactor * pZoomFactor);
    }

    @Override
    public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector,
                                    final TouchEvent pTouchEvent, float pZoomFactor) {
        float newScaleFactor = mPinchZoomStartedCameraZoomFactor * pZoomFactor;
        float minDelta = Float.MAX_VALUE;
        int resolvedIndex = 0;
        for (int i = 0; i < PIZZA_SCALE_FACTORS.length; i++) {
            float delta = Math.abs(newScaleFactor - PIZZA_SCALE_FACTORS[i]);
            if (delta < minDelta) {
                minDelta = delta;
                resolvedIndex = i;
            }
        }
        currentPizzaSize = PIZZA_SIZES[resolvedIndex];
        updatePizzaSizeText();
        pizzaSprite.registerEntityModifier(new ScaleModifier(0.5f,
                pizzaSprite.getScaleX(), PIZZA_SCALE_FACTORS[resolvedIndex],
                new IEntityModifier.IEntityModifierListener() {
                    @Override
                    public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                        // do nothing
                    }
                    @Override
                    public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
                        pizzaSound.play();
                    }
                },
                EaseElasticInOut.getInstance()));
        for (Sprite pizzaSizeCircle : pizzaSizeCircles) {
            getEngine().getCamera().getHUD().detachChild(pizzaSizeCircle);
        }
    }


    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
        pinchZoomDetector.onTouchEvent(pSceneTouchEvent);
        return true;
    }

    private void updatePizzaSizeText() {
        diameter.setText("Ø " + currentPizzaSize + " см");
    }
}