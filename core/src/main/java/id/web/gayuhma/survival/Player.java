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
    private float stateTime = 0f;       // waktu berjalan untuk animasi
    private boolean isMoving = false;    // apakah sedang bergerak
    private boolean facingRight = true;  // arah hadap (untuk flip horizontal)

    // Sistem nyawa
    private int hearts;
    private final int maxHearts = 3;
    private float invincibilityTimer = 0f;
    private final float invincibilityDuration = 1.5f;

    // Sistem XP & Level
    private int xp = 0;
    private int level = 1;
    private int xpToNextLevel = 10; // Kebutuhan XP level 1 ke 2

    // Resolusi layar dinamis untuk HUD
    private float currentWorldHeight = 360f;

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

    // Tambah XP; naik level otomatis jika sudah cukup
    public void addXp(int amount) {
        xp += amount;
        while (xp >= xpToNextLevel) {
            xp -= xpToNextLevel;
            level++;
            // Formula baru: targetXP = baseXP * (level ^ 1.2)
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
        // Deteksi arah gerak
        boolean moving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= speed * deltaTime;
            facingRight = false; // hadap kiri
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += speed * deltaTime;
            facingRight = true;  // hadap kanan
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

        // Jika status gerak berubah (mulai/berhenti), reset waktu animasi
        if (moving != isMoving) {
            stateTime = 0f;
        }
        isMoving = moving;

        // Tambah waktu animasi
        stateTime += deltaTime;

        // Hitung mundur timer kebal
        if (invincibilityTimer > 0) {
            invincibilityTimer -= deltaTime;
        }
    }

    // Batasi posisi player agar tidak keluar layar
    public void clampToScreen(float screenWidth, float screenHeight) {
        this.currentWorldHeight = screenHeight; // Simpan untuk posisi HUD
        if (x < 0) x = 0;
        if (x > screenWidth - width) x = screenWidth - width;
        if (y < 0) y = 0;
        if (y > screenHeight - height) y = screenHeight - height;
    }

    public void draw(SpriteBatch batch) {
        // Gambar 3 ikon hati di HUD (menggunakan texture)
        float heartSize = 24f; // Ukuran asset gambar
        float gap       = 32f;
        float startX    = 10f;
        float startY    = currentWorldHeight - 37f - (heartSize / 2); // Center alignment

        for (int i = 0; i < maxHearts; i++) {
            float cx = startX + i * gap;
            Texture tex = (i < hearts) ? redHeartTex : noHeartTex;
            batch.draw(tex, cx, startY, heartSize, heartSize);
        }

        // Saat kebal, kedipkan sprite player (gambar hanya setiap 0.1 detik ganjil)
        if (invincibilityTimer > 0 && (int)(invincibilityTimer * 10) % 2 != 0) {
            return; // skip frame ini (efek kedip)
        }

        // Pilih animasi berdasarkan status gerak
        TextureRegion currentFrame;
        if (isMoving) {
            currentFrame = walkAnimation.getKeyFrame(stateTime);
        } else {
            currentFrame = idleAnimation.getKeyFrame(stateTime);
        }

        // Flip horizontal jika hadap kiri
        if (!facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        batch.draw(currentFrame, x, y, width, height);
    }

    // Method Overriding: dikosongkan karena HUD hati kini digambar pakai SpriteBatch
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
