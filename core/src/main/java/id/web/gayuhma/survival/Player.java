package id.web.gayuhma.survival;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {
    public Rectangle bounds;
    public float speed = 200f;
    public int hp;
    public final int maxHp = 100;
    
    public Player(float startX, float startY) {
        bounds = new Rectangle(startX, startY, 32, 32);
        hp = maxHp;
    }
    
    public void update(float deltaTime, float screenWidth, float screenHeight) {
        // Logika Input dan Pergerakan
        if (Gdx.input.isKeyPressed(Input.Keys.A)) bounds.x -= speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) bounds.x += speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) bounds.y -= speed * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) bounds.y += speed * deltaTime;
        
        // Mencegah player keluar dari layar
        if (bounds.x < 0) bounds.x = 0;
        if (bounds.x > screenWidth - bounds.width) bounds.x = screenWidth - bounds.width;
        if (bounds.y < 0) bounds.y = 0;
        if (bounds.y > screenHeight - bounds.height) bounds.y = screenHeight - bounds.height;
    }
    
    public void draw(ShapeRenderer shapeRenderer) {
        // Menggambar kotak player (Biru)
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // Menggambar bar darah (Hijau)
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(bounds.x, bounds.y + 36, bounds.width * (Math.max(hp, 0) / (float) maxHp), 4);
    }
    
    // Bantuan untuk sistem tabrakan (mendapatkan titik tengah)
    public Vector2 getCenter() {
        return new Vector2(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    }
    
    // Bantuan radius untuk deteksi tabrakan
    public float getRadius() {
        return bounds.width / 2;
    }
    
    public void takeDamage(int amount) {
        hp -= amount;
    }
    
    public boolean isDead() {
        return hp <= 0;
    }
    
    public void reset(float startX, float startY) {
        hp = maxHp;
        bounds.x = startX;
        bounds.y = startY;
    }
}
