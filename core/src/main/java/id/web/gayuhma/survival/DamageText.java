package id.web.gayuhma.survival;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;

/**
 * Kelas untuk efek visual angka damage yang melayang saat musuh tertembak.
 */
public class DamageText {
    private float x, y;
    private int damage;
    private float lifetime;
    private final float maxLifetime = 0.6f;
    private float velocityY = 40f;
    private float velocityX;

    public DamageText(float x, float y, int damage) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.lifetime = 0f;
        this.velocityX = (float) (Math.random() - 0.5) * 30f;
    }

    public void update(float deltaTime) {
        lifetime += deltaTime;
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        velocityY -= 80f * deltaTime;
    }

    public boolean isFinished() {
        return lifetime >= maxLifetime;
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        float progress = lifetime / maxLifetime;

        float scale;
        if (progress < 0.2f) {
            scale = Interpolation.circleOut.apply(0.3f, 0.7f, progress / 0.2f);
        } else if (progress < 0.6f) {
            scale = 0.7f;
        } else {
            scale = Interpolation.circleIn.apply(0.7f, 0.0f, (progress - 0.6f) / 0.4f);
        }

        if (scale < 0.01f) scale = 0.01f;

        float alpha = 1f;
        if (progress > 0.5f) {
            alpha = 1f - ((progress - 0.5f) / 0.5f);
        }

        font.getData().setScale(scale);
        font.setColor(1f, 1f, 1f, alpha);
        
        font.draw(batch, String.valueOf(damage), x, y);

        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }
}
