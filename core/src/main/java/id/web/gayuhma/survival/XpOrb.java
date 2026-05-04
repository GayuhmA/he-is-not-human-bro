package id.web.gayuhma.survival;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Inheritance: extends GameObject
 * Bola XP yang di-drop musuh saat mati. Diam di tempat dan bisa dikumpulkan player.
 */
public class XpOrb extends GameObject {

    private final float radius = 5f;
    private final int xpValue;
    private final float attractSpeed = 350f; // harus lebih cepat dari playerSpeed (200f) agar pasti mengejar

    public XpOrb(float centerX, float centerY, int xpValue) {
        // Simpan posisi berdasarkan titik tengah (radius = 5, jadi offset -5)
        super(centerX - 5, centerY - 5, 10, 10);
        this.xpValue = xpValue;
    }

    // Method Overriding: orb diam di tempat selama tidak dalam radius vakum
    @Override
    public void update(float deltaTime) { }

    // Method Overloading: versi update yang bergerak menuju target (player)
    // Dipanggil oleh GameManager saat orb masuk radius vakum
    public void update(float deltaTime, Vector2 target) {
        Vector2 orbCenter = new Vector2(x + radius, y + radius);
        Vector2 direction = new Vector2(target.x - orbCenter.x, target.y - orbCenter.y);
        direction.nor();
        x += direction.x * attractSpeed * deltaTime;
        y += direction.y * attractSpeed * deltaTime;
    }

    // Method Overriding: gambar lingkaran kecil dengan warna menyesuaikan nilai XP
    @Override
    public void draw(ShapeRenderer shapeRenderer) {
        if (xpValue >= 3) {
            shapeRenderer.setColor(Color.YELLOW); // High tier
        } else if (xpValue == 2) {
            shapeRenderer.setColor(Color.GREEN);  // Mid tier
        } else {
            shapeRenderer.setColor(new Color(0.2f, 0.9f, 1f, 1f)); // Base cyan (1 XP)
        }
        shapeRenderer.circle(x + radius, y + radius, radius);

        // Titik putih kecil di tengah sebagai highlight
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(x + radius, y + radius, radius * 0.35f);
    }

    // Cek apakah player berada cukup dekat untuk mengambil orb ini
    public boolean isCollectedBy(Vector2 playerCenter, float collectRadius) {
        Vector2 orbCenter = new Vector2(x + radius, y + radius);
        return orbCenter.dst(playerCenter) < collectRadius;
    }

    public int getXpValue() { return xpValue; }
}
