package com.dog.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Application extends Game {
    private OrthographicCamera mCamera;
    private SpriteBatch        mBatch;

    @Override
    public void create() {
        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false, 800, 480);

        mBatch = new SpriteBatch();

    }

    @Override
    public void render() {
        super.render();
        
        ScreenUtils.clear(0, 0, 0.2f, 1);

        mCamera.update();

        mBatch.setProjectionMatrix(mCamera.combined);
        mBatch.begin();




        mBatch.end();
    }

    @Override
    public void dispose() {
        mBatch.dispose();
    }
}
