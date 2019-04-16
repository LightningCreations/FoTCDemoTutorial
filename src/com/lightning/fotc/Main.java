package com.lightning.fotc;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.lightning.jpipeworks.Engine;
import com.lightning.jpipeworks.Game;
import com.lightning.jpipeworks.things.BGMPlayer;
import com.lightning.jpipeworks.things.SFXPlayer;
import com.lightning.jpipeworks.things.Sprite;
import com.lightning.jpipeworks.things.Sprite.SpriteAI;
import com.lightning.jpipeworks.things.Thing;

public class Main {
    public static class FoTCDemoGame extends Game {
        private BGMPlayer music;
        private SFXPlayer sfx;
        private int fc;
        private FoTCDemoBG bg;
        private boolean spaceDown = false;
        private Sprite mainPlayer;
        
        @Override
        public void loadState(Engine engine, GameState state) {
            if(state instanceof PrimaryGameState) {
                switch((PrimaryGameState) state) {
                case MAIN_GAME:
                    engine.things.add(bg = new FoTCDemoBG(engine));
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
                                    float xVelocity = (float) Math.sin((float) mainPlayer.frame / 180 * Math.PI) * 20;
                                    float yVelocity = (float) Math.cos((float) mainPlayer.frame / 180 * Math.PI) * -20;
                                    bg.x += xVelocity;
                                    mainPlayer.y += yVelocity;
                                    if(engine.keysDown[KeyEvent.VK_SPACE] && !spaceDown) {
                                        engine.things.add(new FoTCSelfBullet(50, mainPlayer.y, xVelocity, yVelocity*2, engine));
                                        sfx.play();
                                    }
                                    spaceDown = engine.keysDown[KeyEvent.VK_SPACE];
                                    if(mainPlayer.y > 576) { mainPlayer.y = 576; mainPlayer.frame = 90; }
                                    if(mainPlayer.y < 0) { mainPlayer.y = 0; mainPlayer.frame = 90; }
                                    if(mainPlayer.collision)
                                        System.out.println("HIT");
                                    fc++;
                                }
                            }, 50, 50, 100, 100, engine));
                    mainPlayer.frame = 90;
                    mainPlayer.collideEnable = true;
                    mainPlayer.collisionColor = 0xFF0000;
                    engine.things.add(music = new BGMPlayer("music/lvl1.wav", engine));
                    engine.things.add(sfx = new SFXPlayer("sfx/laser.wav", engine));
                    break;
                default:
                    // Do nothing
                    break;
                }
            }
        }
        
        @Override
        public void doneLoading(Engine engine, GameState state) {
            if(state instanceof PrimaryGameState) {
                switch((PrimaryGameState) state) {
                case MAIN_GAME:
                    mainPlayer.enable = true;
                    music.startMusic = true;
                    break;
                case MAIN_MENU:
                    engine.loadState(this, PrimaryGameState.MAIN_GAME);
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    public static class FoTCDemoBG extends Thing {
    	public float x;
    	private float[] starX = new float[MAX_STARS];
    	private float[] starY = new float[MAX_STARS];
    	private float[] starZ = new float[MAX_STARS];
    	private Engine engine;
    	
    	public static final int MAX_STARS = 2000;
    	
    	public FoTCDemoBG(Engine engine) {
    		this.engine = engine;
    		resources = new ArrayList<>();
    		for(int i = 0; i < MAX_STARS; i++) {
    			starX[i] = (float) Math.random()*40-20;
    			starY[i] = (float) Math.random()*40*9/16f-20*9/16f;
    			starZ[i] = (float) Math.random()*18+2;
    		}
    	}
    	
    	public void update() {
    		for(int i = 0; i < starX.length; i++) {
    			starX[i] -= x/1024;
    		}
    		x = 0;
    	}
    	
    	public void render() {
    		for(int i = 0; i < starX.length; i++) {
    			int x = (int) ((starX[i]/starZ[i]+1)*512);
    			int y = (int) ((starY[i]/starZ[i]+1)*512);
    			if(starX[i] < -20) {
        			starX[i] += 40;
			}

    			engine.plotPixel(x, y, 255, 255, 255);
    			engine.plotPixel(x, y+1, 255, 255, 255);
    			engine.plotPixel(x+1, y, 255, 255, 255);
    			engine.plotPixel(x+1, y+1, 255, 255, 255);
    		}
    	}
    }
    
    public static class FoTCEnemyBullet extends Thing {
        private float x, y, slopeX, slopeY;
        private Engine engine;
        
        public FoTCEnemyBullet(float x, float y, float slopeX, float slopeY, Engine engine) {
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
            for(int i = 0; i < 8; i++)
                engine.plotPixel((int)(x-slopeX/20*i), (int)(y-slopeY/20*i), 255, 0, 0);
        }
    }
    
    public static class FoTCSelfBullet extends Thing {
        private float x, y, slopeX, slopeY;
        private Engine engine;
        
        public FoTCSelfBullet(float x, float y, float slopeX, float slopeY, Engine engine) {
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
            for(int i = 0; i < 8; i++)
                engine.plotPixel((int)(x-slopeX/20*i), (int)(y-slopeY/20*i), 0, 255, 0);
        }
    }
    
    public static void main(String[] args) {
        FoTCDemoGame game = new FoTCDemoGame();
        System.out.println();
        Engine engine = new Engine(game);
        engine.start();
        System.exit(0);
    }
}
