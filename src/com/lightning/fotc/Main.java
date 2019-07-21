package com.lightning.fotc;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.lightning.jpipeworks.Engine;
import com.lightning.jpipeworks.Game;
import com.lightning.jpipeworks.PipeworksInternalGame;
import com.lightning.jpipeworks.things.BGMPlayer;
import com.lightning.jpipeworks.things.Camera;
import com.lightning.jpipeworks.things.SFXPlayer;
import com.lightning.jpipeworks.things.Sprite;
import com.lightning.jpipeworks.things.Sprite.SpriteAI;
import com.lightning.jpipeworks.things.Thing;

public class Main {
    protected static FotCStarfieldBG bg;
    protected static FotCShip mainPlayer;
    protected static Camera camera;
    
    public static class FotCGame extends Game {
        private BGMPlayer music;
        private SFXPlayer laser;
        private SFXPlayer hit;
        private int fc;
        private boolean spaceDown = false;
        private int framesBeforeFire = 200;
        private FotCShip enemy1;
        private FotCExplosion[] explosions = new FotCExplosion[16];
        private FotCExplosion mainPlayerExplosion;
        private FotCExplosion enemy1Explosion;
        
        public void loadState(Engine engine, GameState state) {
            if(state instanceof PrimaryGameState) {
                switch((PrimaryGameState) state) {
                case MAIN_GAME:
                    engine.things.add(camera = new FotCCamera());
                    engine.things.add(bg = new FotCStarfieldBG(engine));
                    engine.things.add(mainPlayer = new FotCShip(
                            "mainPlayer", true, // right
                            new SpriteAI() {
                                private float speed = 1;
                                private boolean wasHit = false;
                                private int timer = 100;
                                
                                public void runAI(Sprite _mainPlayer) {
                                    FotCShip mainPlayer = (FotCShip) _mainPlayer;
                                    if(engine.keysDown[KeyEvent.VK_UP]) {
                                        mainPlayer.frame--;
                                        if(mainPlayer.frame < 0) mainPlayer.frame = 0;
                                    }
                                    if(engine.keysDown[KeyEvent.VK_DOWN]) {
                                        mainPlayer.frame++;
                                        if(mainPlayer.frame > 180) mainPlayer.frame = 180;
                                    }
                                    if(engine.keysDown[KeyEvent.VK_LEFT]) {
                                        speed -= 0.01f;
                                        if(speed < -0.5f) speed = -0.5f;
                                    }
                                    if(engine.keysDown[KeyEvent.VK_RIGHT]) {
                                        speed += 0.01f;
                                        if(speed > 1.5f) speed = 1.5f;
                                    }
                                    float xVelocity = (float) Math.sin((float) mainPlayer.frame / 180 * Math.PI) * 20;
                                    float yVelocity = (float) Math.cos((float) mainPlayer.frame / 180 * Math.PI) * -20;
                                    mainPlayer.x += xVelocity*speed;
                                    mainPlayer.y += yVelocity*speed;
                                    if(engine.keysDown[KeyEvent.VK_SPACE] && !spaceDown) {
                                        engine.things.add(0, new FotCSelfBullet(mainPlayer.x, mainPlayer.y, xVelocity*(speed+0.5f), yVelocity*(speed+0.5f), engine));
                                        laser.play();
                                    }
                                    spaceDown = engine.keysDown[KeyEvent.VK_SPACE];
                                    if(mainPlayer.y > 576) { mainPlayer.y = 576; mainPlayer.frame = 90; }
                                    if(mainPlayer.y < 0) { mainPlayer.y = 0; mainPlayer.frame = 90; }
                                    if(mainPlayer.collision) {
                                        if(!wasHit) {
                                            for(int i = 0; i < explosions.length; i++) {
                                                if(!explosions[i].enable) {
                                                    explosions[i].enable = true;
                                                    explosions[i].anchor = mainPlayer;
                                                    explosions[i].offsetX = (int) (Math.random() * 10 - 5);
                                                    explosions[i].offsetY = (int) (Math.random() * 10 - 5);
                                                    break;
                                                }
                                            }
                                            hit.play();
                                            mainPlayer.health--;
                                            wasHit = true;
                                            if(mainPlayer.health < 0) {
                                                mainPlayer.enable = false;
                                                mainPlayerExplosion.enable = true;
                                            }
                                        }
                                    } else wasHit = false;
                                    fc++;
                                    if(!enemy1.enable) {
                                        timer--;
                                        if(timer < 0) {
                                            System.exit(0);
                                        }
                                    }
                                }
                            }, 50, 50, 100, 100, engine));
                    mainPlayer.frame = 90;
                    mainPlayer.collideEnable = true;
                    mainPlayer.collisionColor = 0x00FFFF;
                    engine.things.add(new FotCHealthBar(mainPlayer, engine));
                    engine.things.add(mainPlayerExplosion = new FotCExplosion(engine));
                    mainPlayerExplosion.anchor = mainPlayer;
                    mainPlayerExplosion.width = 100;
                    mainPlayerExplosion.height = 100;
                    engine.things.add(enemy1 = new FotCShip(
                            "enemy1", false, // left-facing
                            new SpriteAI() {
                                private float speed = 1;
                                private boolean wasHit = false;
                                private int timer = 100;
                                
                                public void runAI(Sprite _enemy1) {
                                    FotCShip enemy1 = (FotCShip) _enemy1;
                                    double targetFrame = (90 - Math.toDegrees(Math.atan2(enemy1.y - (mainPlayer.enable ? mainPlayer.y : 400), enemy1.x - (mainPlayer.enable ? mainPlayer.x : 400))));
                                    if(enemy1.frame < targetFrame) enemy1.frame++;
                                    else if(enemy1.frame > targetFrame) enemy1.frame--;
                                    double targetVelocity = (824-enemy1.x+mainPlayer.x)/Math.sin(Math.toRadians(enemy1.frame));
                                    targetVelocity /= 20;
                                    if(speed < targetVelocity && fc % 2 == 0) speed += 0.01f;
                                    else if(speed > targetVelocity && fc % 2 == 0) speed -= 0.01f;
                                    if(speed < -0.5f) speed = -0.5f;
                                    else if(speed > 1.5f) speed = 1.5f;
                                    float xVelocity = (float) Math.sin((float) enemy1.frame / 180 * Math.PI) * 20;
                                    float yVelocity = (float) Math.cos((float) enemy1.frame / 180 * Math.PI) * -20;
                                    if(engine.keysDown[KeyEvent.VK_SPACE]) {
                                        framesBeforeFire = Math.min(60, framesBeforeFire);
                                        if(framesBeforeFire < 60) framesBeforeFire /= 1.1;
                                    }
                                    framesBeforeFire--;
                                    if(framesBeforeFire <= 0 && (fc % 30 == 0)) {
                                        framesBeforeFire = 90;
                                        engine.things.add(0, new FotCEnemyBullet(enemy1.x, enemy1.y, -xVelocity*(speed+0.5f), yVelocity*(speed+0.5f), engine));
                                        laser.play();
                                    }
                                    enemy1.x += xVelocity*speed;
                                    enemy1.y += yVelocity*speed;
                                    if(enemy1.y > 576) { enemy1.y = 576; enemy1.frame = 90; }
                                    if(enemy1.y < 0) { enemy1.y = 0; enemy1.frame = 90; }
                                    if(enemy1.collision) {
                                        if(!wasHit) {
                                            for(int i = 0; i < explosions.length; i++) {
                                                if(!explosions[i].enable) {
                                                    explosions[i].enable = true;
                                                    explosions[i].anchor = enemy1;
                                                    explosions[i].offsetX = (int) (Math.random() * 20 - 10);
                                                    explosions[i].offsetY = (int) (Math.random() * 20 - 10);
                                                    break;
                                                }
                                            }
                                            hit.play();
                                            enemy1.health--;
                                            wasHit = true;
                                            if(enemy1.health < 0) {
                                                enemy1.enable = false;
                                                enemy1Explosion.enable = true;
                                            }
                                        }
                                    } else wasHit = false;
                                    if(!mainPlayer.enable) {
                                        timer--;
                                        if(timer < 0) {
                                            System.exit(0);
                                        }
                                    }
                                }
                            }, engine.getWidth()-150, engine.getHeight()/2, 100, 100, engine));
                    enemy1.frame = 90;
                    enemy1.collideEnable = true;
                    enemy1.collisionColor = 0x00FF00;
                    engine.things.add(new FotCHealthBar(enemy1, engine));
                    engine.things.add(enemy1Explosion = new FotCExplosion(engine));
                    enemy1Explosion.anchor = enemy1;
                    enemy1Explosion.width = 100;
                    enemy1Explosion.height = 100;
                    for(int i = 0; i < explosions.length; i++) {
                        engine.things.add(explosions[i] = new FotCExplosion(engine));
                    }
                    engine.things.add(music = new BGMPlayer("music/lvl1.wav", engine));
                    engine.things.add(laser = new SFXPlayer("sfx/laser.wav", engine));
                    engine.things.add(hit = new SFXPlayer("sfx/hit.wav", engine));
                    break;
                default:
                    // Do nothing
                    break;
                }
            }
        }
        
        public void doneLoading(Engine engine, GameState state) {
            if(state instanceof PrimaryGameState) {
                switch((PrimaryGameState) state) {
                case MAIN_GAME:
                    mainPlayer.enable = true;
                    enemy1.enable = true;
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
    
    public static class FotCCamera extends Camera {
        public void update() {
            offsetX = (int)-mainPlayer.x+50;
            offsetY = 0;
        }
    }
    
    public static class FotCShip extends Sprite {
        public int health = 10;
        public final int maxHealth = 10;
        
        public FotCShip(String shipname, boolean direction, SpriteAI ai, int x, int y, int width, int height, Engine engine) {
            super(shipname + "/" + (direction ? 'r' : 'l') + "%04d.png", ai, x, y, width, height, engine);
        }
    }
    
    public static class FotCHealthBar extends Thing {
        private float x, y;
        private Engine engine;
        private FotCShip anchor;
        
        public FotCHealthBar(FotCShip anchor, Engine engine) {
            this.anchor = anchor;
            this.engine = engine;
        }
        
        public void update() {
            x = anchor.x + camera.offsetX;
            y = anchor.y + 25 + camera.offsetY;
        }

        public void render() {
            for(float i = -12.5f; i <= 12.5; i++) {
                float curX = x+i;
                float curY = y-1;
                int rgb = (i+12.5)/25 > (float) anchor.health/anchor.maxHealth ? 0xff0000 : 0x00ff00;
                engine.plotPixel((int) curX, (int) curY, rgb);
                engine.plotPixel((int) curX, (int) curY+1, rgb);
                engine.plotPixel((int) curX, (int) curY+2, rgb);
            }
        }
    }
    
    public static class FotCStarfieldBG extends Thing {
    	public float x;
    	private float[] starX = new float[MAX_STARS];
    	private float[] starY = new float[MAX_STARS];
    	private float[] starZ = new float[MAX_STARS];
    	private Engine engine;
    	
    	public static final int MAX_STARS = 2000;
    	
    	public FotCStarfieldBG(Engine engine) {
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
    			starX[i] -= (x-camera.offsetX)/1024;
                if(starX[i] < -20) {
                    starX[i] += 40;
                } else if(starX[i] > 20) {
                    starX[i] -= 40;
                }
    		}
    		x = camera.offsetX;
    	}
    	
    	public void render() {
    		for(int i = 0; i < starX.length; i++) {
    			int x = (int) ((starX[i]/starZ[i]+1)*512);
    			int y = (int) ((starY[i]/starZ[i]+1)*512);

    			engine.plotPixel(x, y, 255, 255, 255);
    			engine.plotPixel(x, y+1, 255, 255, 255);
    			engine.plotPixel(x+1, y, 255, 255, 255);
    			engine.plotPixel(x+1, y+1, 255, 255, 255);
    		}
    	}
    }
    
    public static class FotCEnemyBullet extends Thing {
        private float x, y, slopeX, slopeY;
        private Engine engine;
        
        public FotCEnemyBullet(float x, float y, float slopeX, float slopeY, Engine engine) {
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
                engine.plotPixel((int)(x-(slopeX)/20*i)+camera.offsetX, (int)(y-slopeY/20*i), 0, 255, 255);
        }
    }
    
    public static class FotCSelfBullet extends Thing {
        private float x, y, slopeX, slopeY;
        private Engine engine;
        
        public FotCSelfBullet(float x, float y, float slopeX, float slopeY, Engine engine) {
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
                engine.plotPixel((int)(x-(slopeX)/20*i)+camera.offsetX, (int)(y-slopeY/20*i), 0, 255, 0);
        }
    }
    
    public static class FotCExplosion extends Sprite implements SpriteAI {
        public Sprite anchor;
        public int offsetX;
        public int offsetY;
        
        public FotCExplosion(Engine engine) {
            super("explosion/%02d.png", null, 0, 0, 16, 16, engine);
            ai = this; // Can't pass into Sprite constructor because Java weirdness
        }

        public void runAI(Sprite sprite) {
            frame++;
            if(frame >= 16) {
                frame = 0;
                enable = false;
            }
            x = anchor.x + offsetX;
            y = anchor.y + offsetY;
        }
    }
    
    public static void main(String[] args) {
        PipeworksInternalGame.doIntro = false;
        FotCGame game = new FotCGame();
        System.out.println();
        Engine engine = new Engine(game);
        engine.start();
        System.exit(0);
    }
}
