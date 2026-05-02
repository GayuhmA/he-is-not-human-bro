package id.web.gayuhma.survival;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Kelas utama game loop. Bertugas mengatur timer, koordinasi objek,
 * deteksi tabrakan, dan memanggil draw() setiap frame.
 */
public class GameManager extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;

    // Camera & Viewport untuk menangani resize layar dan fullscreen
    private OrthographicCamera camera;
    private FitViewport viewport;

    private Player player;
    private Array<Enemy> enemies;
    private Array<Bullet> bullets;

    // Ukuran dunia game (tetap, tidak berubah saat resize)
    private final float worldWidth = 640f;
    private final float worldHeight = 480f;

    private long lastEnemySpawnTime;
    private long lastShootTime;
    private final float bulletSpeed = 400f;

    @Override
    public void create() {
        // Setup kamera dan viewport
        camera = new OrthographicCamera();
        // FitViewport: menjaga rasio aspek worldWidth:worldHeight, menambah black bar jika perlu
        viewport = new FitViewport(worldWidth, worldHeight, camera);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        player = new Player(worldWidth / 2 - 16, worldHeight / 2 - 16);
        enemies = new Array<>();
        bullets = new Array<>();
        spawnEnemy();
    }

    // Dipanggil saat ukuran jendela berubah (termasuk saat masuk/keluar fullscreen)
    @Override
    public void resize(int width, int height) {
        // Update viewport agar FitViewport bisa menghitung ulang skala & black bar
        viewport.update(width, height, true);
    }

    private void spawnEnemy() {
        float x, y;
        if (MathUtils.randomBoolean()) {
            x = MathUtils.randomBoolean() ? 0 : worldWidth - 24;
            y = MathUtils.random(0, worldHeight - 24);
        } else {
            x = MathUtils.random(0, worldWidth - 24);
            y = MathUtils.randomBoolean() ? 0 : worldHeight - 24;
        }
        enemies.add(new Enemy(x, y));
        lastEnemySpawnTime = TimeUtils.nanoTime();
    }

    private void shootTowardsCursor() {
        Vector2 playerCenter = player.getCenter();

        // viewport.unproject() sudah otomatis memperhitungkan offset black bar dari FitViewport
        Vector2 mouseWorld = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        Vector2 direction = new Vector2(mouseWorld.x - playerCenter.x, mouseWorld.y - playerCenter.y);
        if (!direction.isZero()) {
            direction.nor();
            bullets.add(new Bullet(playerCenter.x, playerCenter.y, direction, bulletSpeed));
            lastShootTime = TimeUtils.nanoTime();
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        // Terapkan viewport (menggambar black bar jika perlu) lalu update kamera
        viewport.apply();
        camera.update();

        float deltaTime = Gdx.graphics.getDeltaTime();
        updateGame(deltaTime);
        drawGame();
    }

    private void updateGame(float deltaTime) {
        player.update(deltaTime);
        player.clampToScreen(worldWidth, worldHeight);

        if (TimeUtils.nanoTime() - lastEnemySpawnTime > 1000000000L) spawnEnemy();
        if (TimeUtils.nanoTime() - lastShootTime > 1000000000L) shootTowardsCursor();

        Vector2 playerCenter = player.getCenter();
        float playerRadius = player.getWidth() / 2;

        // Update musuh & cek tabrakan dengan player
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(deltaTime, playerCenter);

            if (playerCenter.dst(enemy.getCenter()) < (playerRadius + enemy.getRadius())) {
                // Kurangi 1 hati; Player mengurus i-frames agar tidak kena damage per-frame
                player.takeDamage(1);
            }
        }

        // Update peluru & cek tabrakan dengan musuh
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(deltaTime);

            if (bullet.isOffScreen(worldWidth, worldHeight)) {
                bullets.removeIndex(i);
                continue;
            }

            boolean hit = false;
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (bullet.getCenter().dst(enemy.getCenter()) < (bullet.getRadius() + enemy.getRadius())) {
                    enemy.takeDamage(10);
                    hit = true;
                    if (enemy.isDead()) enemies.removeIndex(j);
                    break;
                }
            }
            if (hit) bullets.removeIndex(i);
        }

        // Game over: reset semua
        if (player.isDead()) {
            player.reset(worldWidth / 2 - 16, worldHeight / 2 - 16);
            enemies.clear();
            bullets.clear();
        }
    }

    private void drawGame() {
        // Sambungkan ShapeRenderer dan SpriteBatch ke matriks proyeksi kamera
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        // 1. Gambar enemy dan bullet pakai ShapeRenderer
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy enemy : enemies) enemy.draw(shapeRenderer);
        for (Bullet bullet : bullets) bullet.draw(shapeRenderer);
        shapeRenderer.end();

        // 2. Gambar texture player pakai SpriteBatch
        batch.begin();
        player.draw(batch);
        batch.end();

        // 3. Gambar health bar player di atas texture
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        player.draw(shapeRenderer);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        player.dispose();
    }
}
