package id.web.gayuhma.survival;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public Rectangle bounds;
    public float speed = 100f;
    public int hp = 20;
    
    public Enemy(float x, float y) {
        bounds = new Rectangle(x, y, 24, 24);
    }
    
    public void update(float deltaTime, Vector2 playerCenter) {
        // Mekanisme Kejar (Chasing Mechanic)
        Vector2 enemyCenter = getCenter();
        Vector2 direction = new Vector2(playerCenter.x - enemyCenter.x, playerCenter.y - enemyCenter.y);
        direction.nor(); // Normalisasi vektor agar panjangnya 1
        
        bounds.x += direction.x * speed * deltaTime;
        bounds.y += direction.y * speed * deltaTime;
    }
    
    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    // Mengambil titik tengah untuk keperluan perhitungan jarak tabrakan
    public Vector2 getCenter() {
        return new Vector2(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    }
    
    // Mengambil nilai radius
    public float getRadius() {
        return bounds.width / 2;
    }
    
    public void takeDamage(int amount) {
        hp -= amount;
    }
    
    public boolean isDead() {
        return hp <= 0;
    }
}
