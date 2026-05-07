package id.web.gayuhma.survival;

// import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Inheritance: extends GameObject
 * Interface: implements IDamageable
 * Method Overloading: dua versi update() — diam vs mengejar target
 */
public class Enemy extends GameObject implements IDamageable {

    private Animation<TextureRegion> animation;
    private float stateTime = 0f;
    private boolean facingRight = true;
    private float speed;
    private int hp;
    private int maxHp;

    // Sprite drawing size
    private float drawWidth = 48f;
    private float drawHeight = 48f;

    public Enemy(float x, float y, float speed, int hp, Animation<TextureRegion> animation) {
        super(x, y, 24, 24);
        this.speed = speed;
        this.hp = hp;
        this.maxHp = hp;
        this.animation = animation;
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

        // Determine facing direction
        if (direction.x > 0) facingRight = true;
        else if (direction.x < 0) facingRight = false;

        stateTime += deltaTime;
    }

    // Method Overriding: dikosongkan karena enemy digambar pakai SpriteBatch
    @Override
    public void draw(ShapeRenderer shapeRenderer) {
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);

        // Pusatkan sprite di atas hitbox
        float drawX = x + (width - drawWidth) / 2;
        float drawY = y + (height - drawHeight) / 2;

        boolean flipX = !facingRight;
        batch.draw(currentFrame.getTexture(),
                drawX, drawY,
                0, 0,
                drawWidth, drawHeight,
                1f, 1f,
                0f,
                currentFrame.getRegionX(), currentFrame.getRegionY(),
                currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
                flipX, false);
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
