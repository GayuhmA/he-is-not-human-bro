package id.web.gayuhma.survival;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
// import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Inheritance: extends GameObject
 * Interface: implements IDamageable
 * Encapsulation: semua field private, akses via getter/setter
 */
public class Player extends GameObject implements IDamageable {

    private final float speed = 200f;

    // Spritesheet & Animasi
    private Texture idleSheet;
    private Texture walkSheet;
    private Texture redHeartTex;
    private Texture noHeartTex;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> walkAnimation;
    private float stateTime = 0f;       
    private boolean isMoving = false;   
    private boolean facingRight = true; 

    // Sistem nyawa
    private int hearts;
    private final int maxHearts = 3;
    private float invincibilityTimer = 0f;
    private final float invincibilityDuration = 1.5f;

    // Sistem XP & Level
    private int xp = 0;
    private int level = 1;
    private int xpToNextLevel = 10;

    // Resolusi layar dinamis untuk HUD
    private float hudWorldHeight = 360f;

    public Player(float startX, float startY) {
        super(startX, startY, 64, 64);
        this.hearts = maxHearts;

        // Load spritesheet idle (256×32 px → 8 frame × 32×32 per frame)
        idleSheet = new Texture("images/character/idle/Cat_Idle-Sheet.png");
        TextureRegion[][] idleFrames = TextureRegion.split(idleSheet,
                idleSheet.getWidth() / 8, idleSheet.getHeight());
        idleAnimation = new Animation<>(0.15f, idleFrames[0]);
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Load spritesheet walk/run (320×32 px → 10 frame × 32×32 per frame)
        walkSheet = new Texture("images/character/walk/Cat_Run-Sheet.png");
        TextureRegion[][] walkFrames = TextureRegion.split(walkSheet,
                walkSheet.getWidth() / 10, walkSheet.getHeight());
        walkAnimation = new Animation<>(0.1f, walkFrames[0]);
        walkAnimation.setPlayMode(Animation.PlayMode.LOOP);

        redHeartTex = new Texture("images/gameplay/redheart.png");
        noHeartTex = new Texture("images/gameplay/noheart.png");
    }

    public void addXp(int amount) {
        xp += amount;
        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel;
            level++;
            xpToNextLevel = (int) (10 * Math.pow(level, 1.2)); 
        }
    }

    public float getXpProgress() { return (float) xp / xpToNextLevel; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getXpToNextLevel() { return xpToNextLevel; }

    public void dispose() {
        idleSheet.dispose();
        walkSheet.dispose();
        redHeartTex.dispose();
        noHeartTex.dispose();
    }

    // Method Overriding: implementasi update() dari GameObject
    @Override
    public void update(float deltaTime) {
        boolean moving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= speed * deltaTime;
            facingRight = false;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += speed * deltaTime;
            facingRight = true;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            y -= speed * deltaTime;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            y += speed * deltaTime;
            moving = true;
        }

        if (moving != isMoving) {
            stateTime = 0f;
        }
        isMoving = moving;

        stateTime += deltaTime;

        if (invincibilityTimer > 0) {
            invincibilityTimer -= deltaTime;
        }
    }


    public void drawHUD(SpriteBatch batch, float screenHeight) {
        float heartSize = 24f; 
        float gap       = 32f;
        float startX    = 10f;
        float startY    = screenHeight - 37f - (heartSize / 2);

        for (int i = 0; i < maxHearts; i++) {
            float cx = startX + i * gap;
            Texture tex = (i < hearts) ? redHeartTex : noHeartTex;
            batch.draw(tex, cx, startY, heartSize, heartSize);
        }
    }

    public void draw(SpriteBatch batch) {

        if (invincibilityTimer > 0 && (int)(invincibilityTimer * 10) % 2 != 0) {
            return;
        }

        TextureRegion currentFrame;
        if (isMoving) {
            currentFrame = walkAnimation.getKeyFrame(stateTime);
        } else {
            currentFrame = idleAnimation.getKeyFrame(stateTime);
        }

        if (!facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        batch.draw(currentFrame, x, y, width, height);
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {
    }

    // Implementasi kontrak IDamageable
    // Hanya kurangi hati jika tidak sedang dalam masa kebal
    @Override
    public void takeDamage(int amount) {
        if (invincibilityTimer <= 0) {
            hearts -= amount;
            invincibilityTimer = invincibilityDuration;
        }
    }

    @Override
    public boolean isDead() { return hearts <= 0; }

    public int getHearts() { return hearts; }
    public int getMaxHearts() { return maxHearts; }
    public boolean isInvincible() { return invincibilityTimer > 0; }

    public void reset(float startX, float startY) {
        setPosition(startX, startY);
        this.hearts = maxHearts;
        this.invincibilityTimer = 0f;
        this.stateTime = 0f;
        this.isMoving = false;
        this.xp = 0;
        this.level = 1;
        this.xpToNextLevel = 10;
    }
}
