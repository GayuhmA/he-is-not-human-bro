package id.web.gayuhma.survival;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Abstract Class (Pilar OOP: Abstract Class)
 * Kelas dasar semua objek game. Tidak bisa diinstansiasi langsung.
 * Kelas anak WAJIB mengimplementasikan update().
 */
public abstract class GameObject {

    // protected agar bisa diakses kelas anak, tapi tidak dari luar (Encapsulation)
    protected float x, y;
    protected float width, height;

    public GameObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Method Overloading: setPosition versi 1 (x, y terpisah)
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // Method Overloading: setPosition versi 2 (Vector2) — nama sama, parameter beda
    public void setPosition(Vector2 pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    public Vector2 getCenter() {
        return new Vector2(x + width / 2, y + height / 2);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    // Abstract method — kelas anak wajib @Override ini (Method Overriding)
    public abstract void update(float deltaTime);

    // Draw default: kotak. Kelas anak bisa @Override untuk tampilan berbeda.
    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(x, y, width, height);
    }
}
