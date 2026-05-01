package id.web.gayuhma.survival;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Inheritance: extends GameObject
 * Interface: implements IDamageable
 * Encapsulation: semua field private, akses via getter/setter
 */
public class Player extends GameObject implements IDamageable {

    private final float speed = 200f;
    private int hp;
    private final int maxHp = 100;

    public Player(float startX, float startY) {
        super(startX, startY, 32, 32);
        this.hp = maxHp;
    }

    // Method Overriding: implementasi update() dari GameObject
    @Override
    public void update(float deltaTime) {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) x -= speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) x += speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) y -= speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) y += speed * deltaTime;
    }

    // Batasi posisi player agar tidak keluar layar
    public void clampToScreen(float screenWidth, float screenHeight) {
        if (x < 0) x = 0;
        if (x > screenWidth - width) x = screenWidth - width;
        if (y < 0) y = 0;
        if (y > screenHeight - height) y = screenHeight - height;
    }

    // Method Overriding: gambar kotak biru + health bar hijau di atas player
    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(x, y, width, height);

        // Latar bar HP (abu-abu)
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y + 36, width, 4);

        // Bar HP proporsional
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(x, y + 36, width * (Math.max(hp, 0) / (float) maxHp), 4);
    }

    // Implementasi kontrak IDamageable
    @Override
    public void takeDamage(int amount) { this.hp -= amount; }

    @Override
    public boolean isDead() { return hp <= 0; }

    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }

    public void reset(float startX, float startY) {
        setPosition(startX, startY); // memanfaatkan setPosition() warisan dari GameObject
        this.hp = maxHp;
    }
}
