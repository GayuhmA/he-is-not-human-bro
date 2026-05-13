package id.web.gayuhma.survival;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Inheritance: extends GameObject
 */
public class Bullet extends GameObject {

    private final Vector2 velocity;
    private final float radius = 4f;

    public Bullet(float startX, float startY, Vector2 direction, float speed) {
        super(startX - 4, startY - 4, 8, 8);
        this.velocity = new Vector2(direction.x * speed, direction.y * speed);
    }

    @Override
    public void update(float deltaTime) {
        x += velocity.x * deltaTime;
        y += velocity.y * deltaTime;
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.circle(x + radius, y + radius, radius);
    }

    public boolean isOffScreen(float screenWidth, float screenHeight) {
        return x < 0 || x > screenWidth || y < 0 || y > screenHeight;
    }

    public float getRadius() { return radius; }
}
