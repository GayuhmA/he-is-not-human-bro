package id.web.gayuhma.survival;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    public Vector2 position;
    public Vector2 velocity;
    public float radius = 4f;
    
    public Bullet(float startX, float startY, Vector2 direction, float speed) {
        position = new Vector2(startX, startY);
        // Vektor arah (yang sudah dinormalisasi panjang 1) dikali dengan speed
        velocity = new Vector2(direction.x * speed, direction.y * speed);
    }
    
    public void update(float deltaTime) {
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;
    }
    
    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.circle(position.x, position.y, radius);
    }
    
    // Cek apakah peluru sudah terbang keluar dari area layar
    public boolean isOffScreen(float screenWidth, float screenHeight) {
        return position.x < 0 || position.x > screenWidth || 
               position.y < 0 || position.y > screenHeight;
    }
}
