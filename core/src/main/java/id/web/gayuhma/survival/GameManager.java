package id.web.gayuhma.survival;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

public class GameManager extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    
    // Objek-objek Game
    private Player player;
    private Array<Enemy> enemies;
    private Array<Bullet> bullets;
    
    // Resolusi
    private final float screenWidth = 640f;
    private final float screenHeight = 480f;
    
    // Timer & Konfigurasi
    private long lastEnemySpawnTime;
    private long lastShootTime;
    private float bulletSpeed = 400f;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        
        // 1. Instansiasi Player dari file Player.java
        float playerStartX = screenWidth / 2 - 16;
        float playerStartY = screenHeight / 2 - 16;
        player = new Player(playerStartX, playerStartY);
        
        // 2. Persiapkan koleksi musuh dan peluru
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

    private void shootAutoAim() {
        if (enemies.isEmpty()) return;
        
        Vector2 playerCenter = player.getCenter();
        Enemy closestEnemy = null;
        float minDistance = Float.MAX_VALUE;
        
        // Cari musuh terdekat
        for (Enemy enemy : enemies) {
            float distance = playerCenter.dst(enemy.getCenter());
            if (distance < minDistance) {
                minDistance = distance;
                closestEnemy = enemy;
            }
        }
        
        // Tembak musuh tersebut
        if (closestEnemy != null) {
            Vector2 direction = new Vector2(
                closestEnemy.getCenter().x - playerCenter.x, 
                closestEnemy.getCenter().y - playerCenter.y
            );
            direction.nor();
            
            bullets.add(new Bullet(playerCenter.x, playerCenter.y, direction, bulletSpeed));
            lastShootTime = TimeUtils.nanoTime();
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Pemisahan Logika: Hitung Matematikanya -> Gambar ke Layar
        updateGame(deltaTime);
        drawGame();
    }
    
    private void updateGame(float deltaTime) {
        // 1. Update pergerakan Player
        player.update(deltaTime, screenWidth, screenHeight);
        
        // 2. Timer Spawn Musuh
        if (TimeUtils.nanoTime() - lastEnemySpawnTime > 1000000000L) {
            spawnEnemy();
        }
        
        // 3. Timer Tembakan Otomatis
        if (TimeUtils.nanoTime() - lastShootTime > 1000000000L) {
            shootAutoAim();
        }
        
        Vector2 playerCenter = player.getCenter();
        float playerRadius = player.getRadius();
        
        // 4. Update Musuh (Bergerak & Tabrak Player)
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            
            // Panggil method update milik enemy
            enemy.update(deltaTime, playerCenter);
            
            // Cek tabrakan dengan Player
            if (playerCenter.dst(enemy.getCenter()) < (playerRadius + enemy.getRadius())) {
                player.takeDamage(1); // Kurangi HP jika bersentuhan
            }
        }
        
        // 5. Update Peluru (Maju & Tabrak Musuh)
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            
            // Panggil method update gerak peluru
            bullet.update(deltaTime);
            
            // Jika keluar layar, hapus peluru
            if (bullet.isOffScreen(screenWidth, screenHeight)) {
                bullets.removeIndex(i);
                continue;
            }
            
            // Cek tabrakan dengan setiap musuh
            boolean hit = false;
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (bullet.position.dst(enemy.getCenter()) < (bullet.radius + enemy.getRadius())) {
                    // Terkena tembakan
                    enemy.takeDamage(10);
                    hit = true;
                    
                    if (enemy.isDead()) {
                        enemies.removeIndex(j);
                    }
                    break; // Keluar dari loop musuh
                }
            }
            
            // Hapus peluru jika mengenai target
            if (hit) {
                bullets.removeIndex(i);
            }
        }
        
        // 6. Reset Game jika Player Mati
        if (player.isDead()) {
            player.reset(screenWidth / 2 - 16, screenHeight / 2 - 16);
            enemies.clear();
            bullets.clear();
        }
    }
    
    private void drawGame() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Class GameManager hanya bertugas "menyuruh" masing-masing objek menggambar dirinya sendiri
        player.draw(shapeRenderer);
        
        for (Enemy enemy : enemies) {
            enemy.draw(shapeRenderer);
        }
        
        for (Bullet bullet : bullets) {
            bullet.draw(shapeRenderer);
        }
        
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
