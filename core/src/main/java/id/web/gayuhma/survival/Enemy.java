package id.web.gayuhma.survival;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Inheritance: extends GameObject
 * Interface: implements IDamageable
 * Method Overloading: dua versi update() — diam vs mengejar target
 */
public class Enemy extends GameObject implements IDamageable {

    private float speed;
    private int hp;
    private int maxHp;

    public Enemy(float x, float y, float speed, int hp) {
        super(x, y, 24, 24);
        this.speed = speed;
        this.hp = hp;
        this.maxHp = hp;
    }

    // Method Overloading versi 1: musuh diam (tidak punya target)
    @Override
    public void update(float deltaTime) {
        // tidak bergerak
    }

    // Method Overloading versi 2: musuh mengejar target — nama sama, parameter beda
    public void update(float deltaTime, Vector2 target) {
        Vector2 direction = new Vector2(target.x - getCenter().x, target.y - getCenter().y);
        direction.nor();
        x += direction.x * speed * deltaTime;
        y += direction.y * speed * deltaTime;
    }

    // Method Overriding: gambar kotak merah, warna di-set sebelum panggil super
    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.RED);
        super.draw(shapeRenderer);
    }

    // Implementasi kontrak IDamageable
    @Override
    public void takeDamage(int amount) { this.hp -= amount; }

    @Override
    public boolean isDead() { return hp <= 0; }

    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public float getRadius() { return width / 2; }
}
