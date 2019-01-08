package com.lightning.fotc;

import java.awt.event.KeyEvent;

import com.lightning.jpipeworks.Engine;
import com.lightning.jpipeworks.Game;
import com.lightning.jpipeworks.things.BGMPlayer;
import com.lightning.jpipeworks.things.Sprite;
import com.lightning.jpipeworks.things.Sprite.SpriteAI;
import com.lightning.jpipeworks.things.Thing;

public class Main {
    public static class FoTCDemoGame extends Game {
        private BGMPlayer music;
        private int fc;
        
        public void loadState(Engine engine, GameState state) {
            // Ignore state for now
            Sprite mainPlayer;
            engine.things.add(mainPlayer = new Sprite(
                    "mainPlayer/r%04d.png",
                    new SpriteAI() {
                        public void runAI(Sprite mainPlayer) {
                            if(engine.keysDown[KeyEvent.VK_UP]) {
                                mainPlayer.frame--;
                                if(mainPlayer.frame < 0) mainPlayer.frame = 0;
                            }
                            if(engine.keysDown[KeyEvent.VK_DOWN]) {
                                mainPlayer.frame++;
                                if(mainPlayer.frame > 180) mainPlayer.frame = 180;
                            }
                            float xVelocity = (float) Math.sin(mainPlayer.frame  * Math.PI / 180) * 2;
                            float yVelocity = (float) Math.cos(mainPlayer.frame  * Math.PI / 180) * -2;
                            System.out.println("Velocity: (x = " + xVelocity + ", y = " + yVelocity + ")");
                            mainPlayer.x += xVelocity;
                            mainPlayer.y += yVelocity;
//                            if(mainPlayer.collision)
//                                System.out.println("HIT");
                            fc++;
//                            engine.things.add(0, new FoTCBullet(400, 400, (float) Math.cos(fc/60f)*2, (float) Math.sin(fc/60f)*2, engine));
                        }
                    }, 50, 50, 100, 100, engine));
            mainPlayer.frame = 90;
            mainPlayer.collideEnable = true;
            mainPlayer.collisionColor = 0xFF0000;
            engine.things.add(music = new BGMPlayer("music/lvl1.wav", engine));
            music.startMusic = true;
        }
    }
    
    public static class FoTCBullet extends Thing {
        private float x, y, slopeX, slopeY;
        private Engine engine;
        
        public FoTCBullet(float x, float y, float slopeX, float slopeY, Engine engine) {
            this.x = x;
            this.y = y;
            this.slopeX = slopeX;
            this.slopeY = slopeY;
            this.engine = engine;
        }
        
        public void update() {
            x += slopeX;
            y += slopeY;
        }

        public void render() {
            engine.plotPixel((int)x, (int)y, 255, 0, 0);
        }
    }
    
    public static void main(String[] args) {
        FoTCDemoGame game = new FoTCDemoGame();
        Engine engine = new Engine(game);
        engine.start();
        System.exit(0);
    }
}
