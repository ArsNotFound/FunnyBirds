package ru.samsung.itschool.mdev.funnybirds;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import ru.samsung.itschool.mdev.funnybirds.game.GameView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GameView(this));
    }

}