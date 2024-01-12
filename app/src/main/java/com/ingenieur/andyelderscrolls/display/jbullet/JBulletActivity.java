package com.ingenieur.andyelderscrolls.display.jbullet;

import android.content.Intent;
import android.os.Bundle;

import com.ingenieur.andyelderscrolls.ElderScrollsActivity;

import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;

import jogamp.newt.driver.android.NewtBaseFragmentActivity;
import scrollsexplorer.GameConfig;


public class JBulletActivity extends NewtBaseFragmentActivity {
    private JBulletTester jBulletTester;
    private GameConfig gameConfigToLoad;


    @Override
    public void onCreate(final Bundle state) {
        System.setProperty("j3d.cacheAutoComputeBounds", "true");
        System.setProperty("j3d.defaultReadCapability", "false");
        System.setProperty("j3d.defaultNodePickable", "false");
        System.setProperty("j3d.defaultNodeCollidable", "false");

        SimpleShaderAppearance.setVersionES300();

        super.onCreate(state);

        Intent intent = getIntent();
        String gameName = intent.getStringExtra(ElderScrollsActivity.SELECTED_GAME);
        gameConfigToLoad = ElderScrollsActivity.getGameConfig(gameName);

        jBulletTester = new JBulletTester(this, gameConfigToLoad);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
