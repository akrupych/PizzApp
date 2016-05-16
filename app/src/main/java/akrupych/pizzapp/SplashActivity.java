package akrupych.pizzapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.IBackground;
import org.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.color.Color;

import java.io.IOException;

public class SplashActivity extends SimpleBaseGameActivity {

    private static final long DURATION = 2000;
    private static final float CAMERA_WIDTH = 720;
    private static final float CAMERA_HEIGHT = 1280;
    private static final float CENTER_X = CAMERA_WIDTH / 2;
    private static final float CENTER_Y = CAMERA_HEIGHT / 2;

    private TextureRegion backgroundTextureRegion;
    private TextureRegion halfPizzaTextureRegion;
    private TextureRegion logoTextureRegion;

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, DURATION);
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(),
                new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT));
    }

    @Override
    protected void onCreateResources() throws IOException {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        // background
        BitmapTextureAtlas atlas = new BitmapTextureAtlas(getTextureManager(), 400, 400, TextureOptions.REPEATING_BILINEAR);
        BitmapTextureAtlasTextureRegionFactory.createFromAsset(atlas, this, "leather.jpg", 0, 0);
        atlas.load();
        backgroundTextureRegion = TextureRegionFactory.extractFromTexture(atlas, 0, 0, 400, 400);
        // half pizza
        atlas = new BitmapTextureAtlas(getTextureManager(), 600, 307, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        halfPizzaTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                atlas, this, "half_pizza.png", 0, 0);
        atlas.load();
        // logo
        atlas = new BitmapTextureAtlas(getTextureManager(), 600, 391, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        logoTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                atlas, this, "chelentano.png", 0, 0);
        atlas.load();
    }

    @Override
    protected Scene onCreateScene() {
        // setup scene
        Scene scene = new Scene();
        IBackground spriteBackground = new RepeatingSpriteBackground(
                CAMERA_WIDTH, CAMERA_HEIGHT, backgroundTextureRegion, getVertexBufferObjectManager());
        scene.setBackground(spriteBackground);
        // attach pizza
        Sprite halfPizzaSprite = new Sprite(CENTER_X, CENTER_Y, halfPizzaTextureRegion, getVertexBufferObjectManager());
        halfPizzaSprite.setY(halfPizzaSprite.getY() + halfPizzaSprite.getHeight() / 2);
        scene.attachChild(halfPizzaSprite);
        // attach logo
        Sprite logoSprite = new Sprite(CENTER_X, CENTER_Y, logoTextureRegion, getVertexBufferObjectManager());
        logoSprite.setY(logoSprite.getY() - logoSprite.getHeight() / 2);
        scene.attachChild(logoSprite);
        // done
        return scene;
    }
}
