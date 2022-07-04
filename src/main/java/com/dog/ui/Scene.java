package com.dog.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Scene implements Screen {
    private final Application  mGame;
    private OrthographicCamera mCamera;
    private SpriteBatch        mBatch;

    public Scene(Application game) {
        mGame = game;
        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false, 800, 480);

        mBatch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        mCamera.update();

        mBatch.setProjectionMatrix(mCamera.combined);
        mBatch.begin();


        mBatch.end();
    }

    @Override
    public void resize(int width, int height) { }

    @Override
    public void show() { }

    @Override
    public void hide() { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void dispose() {
        mBatch.dispose();
    }
    
}
