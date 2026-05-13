package id.web.gayuhma.survival;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * Kelas utama game loop. Bertugas mengatur timer, koordinasi objek,
 * deteksi tabrakan, dan memanggil draw() setiap frame.
 */
public class GameManager extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    public enum GameState {
        TITLE_SCREEN, TRANSITION_TO_MENU, MAIN_MENU, LOADING, PLAYING, PAUSED, GAME_OVER
    }
    private GameState state = GameState.TITLE_SCREEN;

    private Texture titleTexture;
    private Texture pressStartTexture;
    private Texture catDecoTexture;
    private Texture playButtonTexture;
    private Texture creditsButtonTexture;
    private Texture pauseBtnTex;
    private Texture resumeBtnTex;
    private Texture quitBtnTex;
    private float menuTimer = 0f;
    private float transitionTimer = 0f;
    private float loadingTimer = 0f;
    private float gameOverTimer = 0f;
    private boolean isGameOverFadingOut = false;
    private float gameOverFadeOutTimer = 0f;

    private Cursor customCursor;

    // Handles resizing and fullscreen scaling
    private OrthographicCamera camera;
    private ExtendViewport viewport;
    
    private OrthographicCamera hudCamera;
    private ExtendViewport hudViewport;

    private Player player;
    private Array<Enemy> enemies;
    private Array<Bullet> bullets;
    private Array<XpOrb> xpOrbs;
    private Array<DamageText> damageTexts;

    // Enemy Spritesheets
    private Texture batSheet;
    private Texture eyeSheet;
    private Animation<TextureRegion> batAnimation;
    private Animation<TextureRegion> eyeAnimation;

    private Texture mapTexture;

    private float worldWidth = 640f;
    private float worldHeight = 360f;

    // Orb vacuum radius
    private final float orbVacuumRadius = 80f;
    private final float orbCollectRadius = 16f;

    private long lastEnemySpawnTime;
    private long lastShootTime;
    private final float bulletSpeed = 400f;

    // Time and difficulty scaling
    private float survivalTime = 0f;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(worldWidth, worldHeight, camera);

        hudCamera = new OrthographicCamera();
        hudViewport = new ExtendViewport(worldWidth, worldHeight, hudCamera);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font/Pixelify_Sans/static/PixelifySans-Regular.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 20;
        parameter.color = Color.WHITE;
        parameter.borderColor = Color.DARK_GRAY;
        parameter.borderWidth = 1.5f;
        font = generator.generateFont(parameter);
        generator.dispose();

        player = new Player(worldWidth / 2 - 32, worldHeight / 2 - 32);
        enemies = new Array<>();
        bullets = new Array<>();
        xpOrbs = new Array<>();
        damageTexts = new Array<>();

        batSheet = new Texture("images/enemy/Flight_Bat.png");
        TextureRegion[][] batFrames = TextureRegion.split(batSheet, batSheet.getWidth() / 11, batSheet.getHeight());
        batAnimation = new Animation<>(0.1f, batFrames[0]);
        batAnimation.setPlayMode(Animation.PlayMode.LOOP);

        eyeSheet = new Texture("images/enemy/Flying_Eye.png");
        TextureRegion[][] eyeFrames = TextureRegion.split(eyeSheet, eyeSheet.getWidth() / 8, eyeSheet.getHeight());
        eyeAnimation = new Animation<>(0.1f, eyeFrames[0]);
        eyeAnimation.setPlayMode(Animation.PlayMode.LOOP);

        mapTexture = new Texture("images/gameplay/map.png");
        mapTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        spawnEnemy(100f, 20);

        titleTexture = new Texture("images/menu/Game-Title.png");
        pressStartTexture = new Texture("images/menu/Press_to_start-Button.png");
        catDecoTexture = new Texture("images/menu/Cat-Decoration.png");
        playButtonTexture = new Texture("images/menu/Play-Button.png");
        creditsButtonTexture = new Texture("images/menu/Credits-Button.png");
        pauseBtnTex = new Texture("images/menu/Pause1.png");
        resumeBtnTex = new Texture("images/menu/resume.png");
        quitBtnTex = new Texture("images/menu/quit.png");

        Pixmap originalPm = new Pixmap(Gdx.files.internal("images/gameplay/crosshair.png"));
        Pixmap cursorPm = new Pixmap(64, 64, originalPm.getFormat());

        cursorPm.drawPixmap(originalPm, 8, 8);

        customCursor = Gdx.graphics.newCursor(cursorPm, 32, 32);
        Gdx.graphics.setCursor(customCursor);

        originalPm.dispose();
        cursorPm.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);

        worldWidth = viewport.getWorldWidth();
        worldHeight = viewport.getWorldHeight();
    }

    private void spawnEnemy(float speed, int hp) {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float halfW = viewport.getWorldWidth() / 2f;
        float halfH = viewport.getWorldHeight() / 2f;

        float x, y;
        if (MathUtils.randomBoolean()) {
            x = MathUtils.randomBoolean() ? camX - halfW - 24 : camX + halfW + 24;
            y = MathUtils.random(camY - halfH, camY + halfH);
        } else {
            x = MathUtils.random(camX - halfW, camX + halfW);
            y = MathUtils.randomBoolean() ? camY - halfH - 24 : camY + halfH + 24;
        }

        // Determine which enemy type based on phase
        int currentMinute = (int) (survivalTime / 60);
        Animation<TextureRegion> selectedAnimation;

        if (currentMinute == 0) {
            // Phase 1
            selectedAnimation = batAnimation;
        } else if (currentMinute == 1) {
            // Phase 2
            selectedAnimation = MathUtils.randomBoolean() ? batAnimation : eyeAnimation;
        } else {
            // Phase 3
            selectedAnimation = eyeAnimation;
        }

        enemies.add(new Enemy(x, y, speed, hp, selectedAnimation));
        lastEnemySpawnTime = TimeUtils.nanoTime();
    }

    private void shootTowardsCursor() {
        Vector2 playerCenter = player.getCenter();

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

        viewport.apply();
        camera.update();
        hudViewport.apply();
        hudCamera.update();

        float deltaTime = Gdx.graphics.getDeltaTime();

        if (state == GameState.TITLE_SCREEN || state == GameState.TRANSITION_TO_MENU || state == GameState.MAIN_MENU) {
            updateMenu(deltaTime);
        } else if (state == GameState.LOADING) {
            updateLoading(deltaTime);
        } else if (state == GameState.PLAYING) {
            updateGame(deltaTime);
        } else if (state == GameState.GAME_OVER) {
            updateGameOver(deltaTime);
        } else if (state == GameState.PAUSED) {
            updatePaused();
        }

        // Execute draw calls separately to prevent state transition blinks
        if (state == GameState.TITLE_SCREEN || state == GameState.TRANSITION_TO_MENU || state == GameState.MAIN_MENU) {
            drawMenu();
        } else if (state == GameState.LOADING) {
            drawLoading();
        } else if (state == GameState.PLAYING) {
            drawGame();
        } else if (state == GameState.GAME_OVER) {
            drawGameOver();
        } else if (state == GameState.PAUSED) {
            drawPaused();
        }
    }

    private void updateMenu(float deltaTime) {
        menuTimer += deltaTime;

        if (state == GameState.TITLE_SCREEN) {
            // Transition on any input
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
                state = GameState.TRANSITION_TO_MENU;
                transitionTimer = 0f;
            }
        } else if (state == GameState.TRANSITION_TO_MENU) {
            transitionTimer += deltaTime;
            if (transitionTimer >= 1.0f) {
                state = GameState.MAIN_MENU;
            }
        } else if (state == GameState.MAIN_MENU) {
            // Handle Play button click
            if (Gdx.input.justTouched()) {
                Vector2 touch = hudViewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

                float btnW = 200f;
                float btnH = btnW * (129f / 409f);
                float playX = (worldWidth - btnW) / 2; 
                float playY = 130f;

                if (touch.x >= playX && touch.x <= playX + btnW && touch.y >= playY && touch.y <= playY + btnH) {
                    state = GameState.LOADING;
                    loadingTimer = 0f;
                }
            }
        }
    }

    private void drawMenu() {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Draw animated title
        float titleW = 400f;
        float titleH = titleW * (362f / 1077f);

        float titleSlide = 1f;
        if (menuTimer < 0.66f) { 
            titleSlide = Math.min(1f, menuTimer * 1.5f); 
        }
        float startTitleY = worldHeight;
        float targetTitleY = worldHeight - titleH - 20;
        float titleY = Interpolation.swingOut.apply(startTitleY, targetTitleY, titleSlide);

        batch.draw(titleTexture, (worldWidth - titleW) / 2, titleY, titleW, titleH);

        if (state == GameState.TITLE_SCREEN) {
            // Blinking "Press To Start"
            if ((int)(menuTimer * 2) % 2 == 0) {
                float startW = 250f;
                float startH = startW * (65f / 734f);
                batch.draw(pressStartTexture, (worldWidth - startW) / 2, 80, startW, startH);
            }
        } else if (state == GameState.TRANSITION_TO_MENU) {
            // Fade out "Press To Start"
            float fadeOutAlpha = Math.max(0f, 1f - transitionTimer * 2f);
            if (fadeOutAlpha > 0) {
                batch.setColor(1f, 1f, 1f, fadeOutAlpha);
                float startW = 250f;
                float startH = startW * (65f / 734f);
                batch.draw(pressStartTexture, (worldWidth - startW) / 2, 80, startW, startH);
            }

            // Slide in menu items
            float slideProgress = Math.min(1f, Math.max(0f, transitionTimer * 2f - 1f));
            if (slideProgress > 0) {
                drawMainMenuItems(slideProgress, slideProgress);
            }
            batch.setColor(Color.WHITE); // Reset batch color
        } else if (state == GameState.MAIN_MENU) {
            drawMainMenuItems(1f, 1f); 
        }

        batch.end();
    }

    private void drawMainMenuItems(float progress, float alpha) {
        batch.setColor(1f, 1f, 1f, alpha);

        // Draw cat decoration
        float catW = 350f; 
        float catH = catW * (782f / 756f);
        float startCatX = worldWidth;
        float targetCatX = worldWidth - catW + 50;
        float catX = Interpolation.exp10Out.apply(startCatX, targetCatX, progress);
        batch.draw(catDecoTexture, catX, -10, catW, catH);

        // Draw menu buttons
        float btnW = 200f;
        float btnH = btnW * (129f / 409f);

        float targetPlayY = 130f;
        float targetCreditsY = targetPlayY - btnH - 10f;

        float startPlayY = targetPlayY - 200f;
        float startCreditsY = targetCreditsY - 200f;

        float playY = Interpolation.exp10Out.apply(startPlayY, targetPlayY, progress);
        float creditsY = Interpolation.exp10Out.apply(startCreditsY, targetCreditsY, progress);

        float playX = (worldWidth - btnW) / 2;
        batch.draw(playButtonTexture, playX, playY, btnW, btnH);
        batch.draw(creditsButtonTexture, playX, creditsY, btnW, btnH);
    }

    private void updateLoading(float deltaTime) {
        loadingTimer += deltaTime;
        if (loadingTimer >= 2.0f) {
            state = GameState.PLAYING;

            player.reset(worldWidth / 2 - 32, worldHeight / 2 - 32);
            enemies.clear();
            bullets.clear();
            xpOrbs.clear();
            damageTexts.clear();
            survivalTime = 0f;
            spawnEnemy(100f, 20);
        }
    }

    private void drawLoading() {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Fade out menu
        float fadeOutAlpha = Math.max(0f, 1f - loadingTimer * 2f);
        if (fadeOutAlpha > 0) {
            batch.setColor(1f, 1f, 1f, fadeOutAlpha);

            float titleW = 400f;
            float titleH = titleW * (362f / 1077f);
            batch.draw(titleTexture, (worldWidth - titleW) / 2, worldHeight - titleH - 20, titleW, titleH);
            drawMainMenuItems(1f, fadeOutAlpha);

            batch.setColor(Color.WHITE); // Reset batch color
        }

        // Loading dots animation
        int dotCount = (int)(loadingTimer * 4) % 4;
        String dots = "";
        for(int i = 0; i < dotCount; i++) dots += ".";

        String loadingText = "Loading rn " + dots;

        font.getData().setScale(1.5f);

        font.draw(batch, loadingText, 20f, 40f);

        font.getData().setScale(1.0f);

        batch.end();
    }

    private void updateGame(float deltaTime) {
        // Handle pause input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            state = GameState.PAUSED;
            return;
        }

        if (Gdx.input.justTouched()) {
            Vector2 touch = hudViewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            float pauseSize = 28f;
            float margin = 18f;
            float pauseX = worldWidth - margin - pauseSize;
            float pauseY = worldHeight - margin - pauseSize;

            if (touch.x >= pauseX - 10f && touch.x <= pauseX + pauseSize + 10f && touch.y >= pauseY - 10f && touch.y <= pauseY + pauseSize + 10f) {
                state = GameState.PAUSED;
                return;
            }
        }

        survivalTime += deltaTime;
        player.update(deltaTime);

        // Update Camera Position to follow player
        camera.position.set(player.getCenter().x, player.getCenter().y, 0);
        camera.update();

        // Difficulty scaling
        int currentMinute = (int) (survivalTime / 60);

        float spawnDelay = Math.max(0.2f, 1.5f - (currentMinute * 0.1f));
        long spawnInterval = (long) (spawnDelay * 1_000_000_000L); 

        float enemySpeed = 70f + (currentMinute * 10f);

        int enemyHp = 20 + ((currentMinute / 2) * 10);

        if (TimeUtils.nanoTime() - lastEnemySpawnTime > spawnInterval) spawnEnemy(enemySpeed, enemyHp);

        // Fire rate scaling

        float fireRate = Math.max(0.15f, 1.0f - ((player.getLevel() - 1) * 0.05f));
        long fireIntervalNano = (long) (fireRate * 1_000_000_000L);
        if (TimeUtils.nanoTime() - lastShootTime > fireIntervalNano) shootTowardsCursor();

        Vector2 playerCenter = player.getCenter();
        // Shrink player hitbox for precision
        float playerRadius = (player.getWidth() / 2) * 0.4f;

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

            // Periksa batas bullet berdasarkan koordinat dunia saat ini (misal radius dari player)
            if (bullet.getCenter().dst(playerCenter) > 1000f) {
                bullets.removeIndex(i);
                continue;
            }

            boolean hit = false;
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (bullet.getCenter().dst(enemy.getCenter()) < (bullet.getRadius() + enemy.getRadius())) {
                    enemy.takeDamage(10);
                    hit = true;
                    // Tampilkan angka pop up damage
                    damageTexts.add(new DamageText(enemy.getCenter().x, enemy.getCenter().y + 10, 10));

                    if (enemy.isDead()) {
                        // Drop XP berdasarkan tier musuh (berdasar HP asli)
                        // HP 20 -> 1 XP, HP 30 -> 2 XP, HP 40 -> 3 XP, dst.
                        int xpDrop = Math.max(1, (enemy.getMaxHp() / 10) - 1);
                        Vector2 enemyCenter = enemy.getCenter();
                        xpOrbs.add(new XpOrb(enemyCenter.x, enemyCenter.y, xpDrop));
                        enemies.removeIndex(j);
                    }
                    break;
                }
            }
            if (hit) bullets.removeIndex(i);
        }

        // Update & koleksi XP orb dengan mekanik vakum
        for (int i = xpOrbs.size - 1; i >= 0; i--) {
            XpOrb orb = xpOrbs.get(i);

            if (orb.isCollectedBy(playerCenter, orbCollectRadius)) {
                // Orb menyentuh player: kumpulkan XP
                player.addXp(orb.getXpValue());
                xpOrbs.removeIndex(i);
            } else if (orb.isCollectedBy(playerCenter, orbVacuumRadius)) {
                // Orb dalam radius vakum: gerakkan menuju player
                orb.update(deltaTime, playerCenter);
            }
        }

        // Update animasi Damage Text
        for (int i = damageTexts.size - 1; i >= 0; i--) {
            DamageText dt = damageTexts.get(i);
            dt.update(deltaTime);
            if (dt.isFinished()) {
                damageTexts.removeIndex(i);
            }
        }

        // Game over: transisi ke animasi game over
        if (player.isDead()) {
            state = GameState.GAME_OVER;
            gameOverTimer = 0f; 
        }
    }

    private void drawGame() {
        // --- 1. DRAW WORLD ---
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        // Calculate U/V based on camera position for repeat wrapping
        float viewX = camera.position.x - viewport.getWorldWidth() / 2;
        float viewY = camera.position.y - viewport.getWorldHeight() / 2;
        float viewW = viewport.getWorldWidth();
        float viewH = viewport.getWorldHeight();
        
        float u = viewX / mapTexture.getWidth();
        float v = 1f - (viewY / mapTexture.getHeight());
        float u2 = (viewX + viewW) / mapTexture.getWidth();
        float v2 = 1f - ((viewY + viewH) / mapTexture.getHeight());
        
        batch.draw(mapTexture, viewX, viewY, viewW, viewH, u, v, u2, v2);

        for (Enemy enemy : enemies) enemy.draw(batch);
        player.draw(batch);
        for (DamageText dt : damageTexts) dt.draw(batch, font);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Bullet bullet : bullets) bullet.draw(shapeRenderer);
        for (XpOrb orb : xpOrbs) orb.draw(shapeRenderer);
        shapeRenderer.end();

        // --- 2. DRAW HUD ---
        batch.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawXpBar(shapeRenderer);
        shapeRenderer.end();

        batch.begin();
        player.drawHUD(batch, worldHeight);
        float pauseSize = 28f;
        float margin = 18f;
        batch.draw(pauseBtnTex, worldWidth - margin - pauseSize, worldHeight - margin - pauseSize, pauseSize, pauseSize);
        batch.end();

        drawLevelText();
    }

    private void drawXpBar(ShapeRenderer shapeRenderer) {
        float barHeight = 8f;
        float barY = worldHeight - barHeight; // tepat di paling atas layar

        shapeRenderer.setColor(new Color(0.15f, 0.15f, 0.15f, 1f));
        shapeRenderer.rect(0, barY, worldWidth, barHeight);

        shapeRenderer.setColor(new Color(0.3f, 0.7f, 1f, 1f));
        shapeRenderer.rect(0, barY, worldWidth * player.getXpProgress(), barHeight);

        shapeRenderer.setColor(new Color(1f, 1f, 1f, 0.2f));
        shapeRenderer.rect(0, barY, worldWidth, 1);
    }

    private void drawLevelText() {
        batch.begin();
        font.setColor(Color.WHITE);

        font.draw(batch, "LV " + player.getLevel(), 10, worldHeight - 10);

        int minutes = (int) (survivalTime / 60);
        int seconds = (int) (survivalTime % 60);
        String timeStr = String.format("%02d:%02d", minutes, seconds);

        font.draw(batch, timeStr, (worldWidth / 2) - 15f, worldHeight - 10);

        batch.end();
    }

    private void updateGameOver(float deltaTime) {
        if (!isGameOverFadingOut) {
            gameOverTimer += deltaTime;

            // Wait for input to return to menu
            if (gameOverTimer > 4.5f) {
                if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
                    isGameOverFadingOut = true;
                    gameOverFadeOutTimer = 0f;
                }
            }
        } else {
            // Fading out text
            gameOverFadeOutTimer += deltaTime;
            if (gameOverFadeOutTimer >= 0.5f) { 

                state = GameState.TRANSITION_TO_MENU;
                transitionTimer = 0.5f; 
                menuTimer = 0f;
                isGameOverFadingOut = false;
                gameOverTimer = 0f; 
            }
        }
    }

    private void drawGameOver() {
        // Draw frozen background
        if (gameOverTimer < 3.0f) {
            drawGame();
        } else {

            ScreenUtils.clear(0, 0, 0, 1);
        }

        // Alpha blending for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (gameOverTimer <= 0.1f) {
            // Phase 1: Flash
            shapeRenderer.setColor(new Color(1f, 0f, 0f, 0.4f)); 
            shapeRenderer.rect(0, 0, worldWidth, worldHeight);
        } else if (gameOverTimer > 0.1f && gameOverTimer <= 3.0f) {
            // Phase 2: Expanding ribbon
            float rectHeight = 100f; 
            float rectY = (worldHeight - rectHeight) / 2;
            float alpha = 0.7f;

            if (gameOverTimer > 2.0f) {

                float progress = gameOverTimer - 2.0f; 

                rectHeight = 100f + (worldHeight) * progress; 
                if (rectHeight > worldHeight) rectHeight = worldHeight;
                rectY = (worldHeight - rectHeight) / 2;

                alpha = 0.7f + (0.3f * progress);
            }

            shapeRenderer.setColor(new Color(0f, 0f, 0f, alpha));
            shapeRenderer.rect(0, rectY, worldWidth, rectHeight);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Render text
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        if (gameOverTimer > 0.1f && gameOverTimer <= 2.5f) {

            float textAlpha = 1.0f;
            if (gameOverTimer > 2.0f) {

                textAlpha = Math.max(0f, 1f - ((gameOverTimer - 2.0f) * 2f));
            }
            font.setColor(new Color(1f, 0f, 0f, textAlpha));
            font.getData().setScale(3.0f);

            GlyphLayout layout = new GlyphLayout(font, "YOU DIED");
            float textX = (worldWidth - layout.width) / 2;
            float textY = (worldHeight + layout.height) / 2;
            font.draw(batch, layout, textX, textY);

            font.getData().setScale(1.0f);
        } else if (gameOverTimer > 3.0f) {

            float textAlpha = Math.min(1.0f, gameOverTimer - 3.0f);

            if (isGameOverFadingOut) {
                textAlpha = Math.max(0f, 1.0f - (gameOverFadeOutTimer * 2f)); 
            }

            font.setColor(new Color(1f, 1f, 1f, textAlpha));
            font.getData().setScale(2.0f);

            GlyphLayout layoutGameOver = new GlyphLayout(font, "Game Over");
            float textX = (worldWidth - layoutGameOver.width) / 2;
            float textY = (worldHeight + layoutGameOver.height) / 2;
            font.draw(batch, layoutGameOver, textX, textY);

            font.getData().setScale(1.0f);

            // Show return instructions
            if (gameOverTimer > 4.5f) {
                if (!isGameOverFadingOut && (int)(gameOverTimer * 2) % 2 == 0) {
                    font.setColor(new Color(1f, 1f, 1f, textAlpha));
                    font.getData().setScale(0.8f);

                    GlyphLayout layoutPress = new GlyphLayout(font, "Press any key to return");
                    float pressX = (worldWidth - layoutPress.width) / 2;
                    float pressY = textY - layoutGameOver.height - 40f;
                    font.draw(batch, layoutPress, pressX, pressY);

                    font.getData().setScale(1.0f);
                } else if (isGameOverFadingOut) {

                    font.setColor(new Color(1f, 1f, 1f, textAlpha));
                    font.getData().setScale(0.8f);
                    GlyphLayout layoutPress = new GlyphLayout(font, "Press any key to return");
                    float pressX = (worldWidth - layoutPress.width) / 2;
                    float pressY = textY - layoutGameOver.height - 40f;
                    font.draw(batch, layoutPress, pressX, pressY);
                    font.getData().setScale(1.0f);
                }
            }
        }

        batch.end();
        font.setColor(Color.WHITE); // Reset color
    }

    private void updatePaused() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            state = GameState.PLAYING;
            return;
        }

        if (Gdx.input.justTouched()) {
            Vector2 touch = hudViewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            float btnSize = 120f; // Diperbesar lagi

            float panelW = 220f;
            float panelH = 220f; // Panel dibuat lebih compact (kotak)
            float panelX = (worldWidth - panelW) / 2;
            float panelY = (worldHeight - panelH) / 2;

            // Karena gambar aslinya punya banyak ruang kosong transparan di atas/bawah,
            // kita "tumpuk" batas bounding box-nya agar visualnya terlihat dekat
            float resumeX = panelX + (panelW - btnSize) / 2;
            float quitX = resumeX;

            float quitY = panelY + 10f; 
            float resumeY = panelY + 65f; // Bounding box tumpang tindih

            // Hitbox dipersempit secara vertikal (dikurangi 35px atas bawah) agar tidak bentrok
            if (touch.x >= resumeX && touch.x <= resumeX + btnSize && touch.y >= resumeY + 35f && touch.y <= resumeY + btnSize - 35f) {
                state = GameState.PLAYING;
            } else if (touch.x >= quitX && touch.x <= quitX + btnSize && touch.y >= quitY + 35f && touch.y <= quitY + btnSize - 35f) {
                state = GameState.TRANSITION_TO_MENU; 
                transitionTimer = 0.5f;
                menuTimer = 0f;
            }
        }
    }

    private void drawPaused() {
        drawGame(); // Gambar background game

        // Overlay redup
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0f, 0f, 0f, 0.6f));
        shapeRenderer.rect(0, 0, worldWidth, worldHeight);

        // Container Panel
        float panelW = 220f;
        float panelH = 220f; 
        float panelX = (worldWidth - panelW) / 2;
        float panelY = (worldHeight - panelH) / 2;

        shapeRenderer.setColor(new Color(0.15f, 0.15f, 0.15f, 1f));
        shapeRenderer.rect(panelX, panelY, panelW, panelH);

        // Outline tipis putih
        shapeRenderer.setColor(new Color(0.9f, 0.9f, 0.9f, 1f));
        shapeRenderer.rect(panelX, panelY, panelW, 2f); 
        shapeRenderer.rect(panelX, panelY + panelH - 2f, panelW, 2f); 
        shapeRenderer.rect(panelX, panelY, 2f, panelH); 
        shapeRenderer.rect(panelX + panelW - 2f, panelY, 2f, panelH); 

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Judul PAUSED
        font.getData().setScale(1.5f);
        GlyphLayout layout = new GlyphLayout(font, "PAUSED");
        font.draw(batch, layout, (worldWidth - layout.width) / 2, panelY + panelH - 20f);
        font.getData().setScale(1.0f);

        // Tombol (disusun atas bawah dan bounding box ditumpuk)
        float btnSize = 120f;

        float resumeX = panelX + (panelW - btnSize) / 2;
        float quitX = resumeX;

        float quitY = panelY + 10f;
        float resumeY = panelY + 65f;

        batch.draw(resumeBtnTex, resumeX, resumeY, btnSize, btnSize);
        batch.draw(quitBtnTex, quitX, quitY, btnSize, btnSize);

        batch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        player.dispose();
        titleTexture.dispose();
        pressStartTexture.dispose();
        catDecoTexture.dispose();
        playButtonTexture.dispose();
        creditsButtonTexture.dispose();
        pauseBtnTex.dispose();
        resumeBtnTex.dispose();
        quitBtnTex.dispose();
        batSheet.dispose();
        eyeSheet.dispose();
        mapTexture.dispose();

        if (customCursor != null) {
            customCursor.dispose();
        }
    }
}
