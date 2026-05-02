package id.web.gayuhma.survival;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Inheritance: extends GameObject
 * Interface: implements IDamageable
 * Encapsulation: semua field private, akses via getter/setter
 */
public class Player extends GameObject implements IDamageable {

    private final float speed = 200f;
    private Texture texture;
    private int hearts;
    private final int maxHearts = 3;

    // Invincibility frames: setelah kena hit, player kebal selama 1.5 detik
    // Mencegah kehilangan banyak hati sekaligus dari satu tabrakan
    private float invincibilityTimer = 0f;
    private final float invincibilityDuration = 1.5f;

    public Player(float startX, float startY) {
        super(startX, startY, 32, 32);
        this.hearts = maxHearts;
        this.texture = new Texture("images/character.png");
    }

    public void dispose() {
        texture.dispose();
    }

    // Method Overriding: implementasi update() dari GameObject
    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) x -= speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) x += speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) y -= speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) y += speed * deltaTime;

        // Hitung mundur timer kebal
        if (invincibilityTimer > 0) {
            invincibilityTimer -= deltaTime;
        }
    }

    // Batasi posisi player agar tidak keluar layar
    public void clampToScreen(float screenWidth, float screenHeight) {
        if (x < 0) x = 0;
        if (x > screenWidth - width) x = screenWidth - width;
        if (y < 0) y = 0;
        if (y > screenHeight - height) y = screenHeight - height;
    }

    public void draw(SpriteBatch batch) {
        // Saat kebal, kedipkan sprite player (gambar hanya setiap 0.1 detik ganjil)
        if (invincibilityTimer <= 0 || (int)(invincibilityTimer * 10) % 2 == 0) {
            batch.draw(texture, x, y, width, height);
        }
    }

    // Method Overriding: gambar 3 ikon hati di sudut kiri atas layar
    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        float heartSize = 10f;    // radius tiap bola hati
        float startX    = 10f;   // posisi X awal HUD
        float startY    = 460f;  // posisi Y (dekat atas layar 480)
        float gap       = 28f;   // jarak antar hati

        for (int i = 0; i < maxHearts; i++) {
            float cx = startX + i * gap + heartSize;

            if (i < hearts) {
                // Hati penuh — merah
                shapeRenderer.setColor(Color.RED);
            } else {
                // Hati kosong — abu-abu gelap
                shapeRenderer.setColor(Color.DARK_GRAY);
            }

            // Bentuk hati: 2 lingkaran (atas) + 1 segitiga (bawah)
            shapeRenderer.circle(cx - heartSize * 0.3f, startY + heartSize * 0.5f, heartSize * 0.55f);
            shapeRenderer.circle(cx + heartSize * 0.3f, startY + heartSize * 0.5f, heartSize * 0.55f);
            shapeRenderer.triangle(
                cx - heartSize,       startY + heartSize * 0.5f,
                cx + heartSize,       startY + heartSize * 0.5f,
                cx,                   startY - heartSize * 0.6f
            );
        }
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
    }
}
