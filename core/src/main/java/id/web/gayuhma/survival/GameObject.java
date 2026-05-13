package id.web.gayuhma.survival;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Abstract Class (Pilar OOP: Abstract Class)
 * Kelas dasar semua objek game. Tidak bisa diinstansiasi langsung.
 * Kelas anak WAJIB mengimplementasikan update().
 */
public abstract class GameObject {

    protected float x, y;
    protected float width, height;

    public GameObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

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

    public abstract void update(float deltaTime);

    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.rect(x, y, width, height);
    }
}
