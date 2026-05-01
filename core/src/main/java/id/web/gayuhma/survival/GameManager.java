package id.web.gayuhma.survival;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

/**
 * Kelas utama game loop. Bertugas mengatur timer, koordinasi objek,
 * deteksi tabrakan, dan memanggil draw() setiap frame.
 */
public class GameManager extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;

    private Player player;
    private Array<Enemy> enemies;
    private Array<Bullet> bullets;

    private final float screenWidth = 640f;
    private final float screenHeight = 480f;

    private long lastEnemySpawnTime;
    private long lastShootTime;
    private final float bulletSpeed = 400f;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        player = new Player(screenWidth / 2 - 16, screenHeight / 2 - 16);
        enemies = new Array<>();
        bullets = new Array<>();
        spawnEnemy();
    }

    private void spawnEnemy() {
        float x, y;
        if (MathUtils.randomBoolean()) {
            x = MathUtils.randomBoolean() ? 0 : screenWidth - 24;
            y = MathUtils.random(0, screenHeight - 24);
        } else {
            x = MathUtils.random(0, screenWidth - 24);
            y = MathUtils.randomBoolean() ? 0 : screenHeight - 24;
        }
        enemies.add(new Enemy(x, y));
        lastEnemySpawnTime = TimeUtils.nanoTime();
    }

    private void shootTowardsCursor() {
        Vector2 playerCenter = player.getCenter();
        float mouseX = Gdx.input.getX();
        // Y-axis input LibGDX dimulai dari atas, dibalik agar sesuai koordinat game
        float mouseY = screenHeight - Gdx.input.getY();

        Vector2 direction = new Vector2(mouseX - playerCenter.x, mouseY - playerCenter.y);
        if (!direction.isZero()) {
            direction.nor();
            bullets.add(new Bullet(playerCenter.x, playerCenter.y, direction, bulletSpeed));
            lastShootTime = TimeUtils.nanoTime();
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        float deltaTime = Gdx.graphics.getDeltaTime();
        updateGame(deltaTime);
        drawGame();
    }

    private void updateGame(float deltaTime) {
        player.update(deltaTime);
        player.clampToScreen(screenWidth, screenHeight);

        if (TimeUtils.nanoTime() - lastEnemySpawnTime > 1000000000L) spawnEnemy();
        if (TimeUtils.nanoTime() - lastShootTime > 1000000000L) shootTowardsCursor();

        Vector2 playerCenter = player.getCenter();
        float playerRadius = player.getWidth() / 2;

        // Update musuh & cek tabrakan dengan player
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            // Method Overloading: versi 2 dipilih karena ada argumen target
            enemy.update(deltaTime, playerCenter);

            if (playerCenter.dst(enemy.getCenter()) < (playerRadius + enemy.getRadius())) {
                player.takeDamage(1);
            }
        }

        // Update peluru & cek tabrakan dengan musuh
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(deltaTime);

            if (bullet.isOffScreen(screenWidth, screenHeight)) {
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
            player.reset(screenWidth / 2 - 16, screenHeight / 2 - 16);
            enemies.clear();
            bullets.clear();
        }
    }

    private void drawGame() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Polymorphism: .draw() dipanggil seragam, implementasinya dipilih otomatis oleh Java
        player.draw(shapeRenderer);
        for (Enemy enemy : enemies) enemy.draw(shapeRenderer);
        for (Bullet bullet : bullets) bullet.draw(shapeRenderer);

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
