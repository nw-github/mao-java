package com.dog.ui;

import com.badlogic.gdx.Game;

public class Application extends Game {
    @Override
    public void create() {
        setScreen(new Scene(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        getScreen().dispose();
    }
}
