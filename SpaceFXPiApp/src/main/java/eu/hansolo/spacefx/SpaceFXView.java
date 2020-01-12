/*
 * Copyright (c) 2020 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.spacefx;


import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;


public class SpaceFXView extends BorderPane {
    private static final double                               SCALING_FACTOR             = 0.5;

    //----------- Switches to switch on/off different features ----------------
    private static final boolean                              SHOW_BACKGROUND            = true;
    private static final boolean                              SHOW_STARS                 = true;
    private static final boolean                              SHOW_ENEMIES               = true;
    private static final boolean                              SHOW_ASTEROIDS             = true;
    private static final int                                  NO_OF_STARS                = SHOW_STARS ? 100 : 0;
    private static final int                                  NO_OF_ASTEROIDS            = SHOW_ASTEROIDS ? 15 : 0;
    private static final int                                  NO_OF_ENEMIES              = SHOW_ENEMIES ? 5 : 0;
    //-------------------------------------------------------------------------
    private static final int                                  LIFES                      = 5;
    private static final int                                  SHIELDS                    = 10;
    private static final int                                  DEFLECTOR_SHIELD_TIME      = 5000;
    private static final int                                  MAX_NO_OF_ROCKETS          = 3;
    private static final double                               VELOCITY_FACTOR_X          = 0.7;
    private static final double                               VELOCITY_FACTOR_Y          = 0.6;
    private static final double                               VELOCITY_FACTOR_R          = 1.0;
    private static final double                               TORPEDO_SPEED              = 6 * VELOCITY_FACTOR_Y;
    private static final double                               ROCKET_SPEED               = 4 * VELOCITY_FACTOR_Y;
    private static final double                               ENEMY_TORPEDO_SPEED        = 5 * VELOCITY_FACTOR_Y;
    private static final double                               ENEMY_BOSS_TORPEDO_SPEED   = 6 * VELOCITY_FACTOR_Y;
    private static final int                                  ENEMY_FIRE_SENSITIVITY     = 10;
    private static final long                                 ENEMY_BOSS_ATTACK_INTERVAL = 20_000_000_000l;
    private static final long                                 CRYSTAL_SPAWN_INTERVAL     = 25_000_000_000l;
    private static final Random                               RND                        = new Random();
    public  static final double                               WIDTH                      = 700 * SCALING_FACTOR;
    public  static final double                               HEIGHT                     = 900 * SCALING_FACTOR;
    private static final double                               FIRST_QUARTER_WIDTH        = WIDTH * 0.25;
    private static final double                               LAST_QUARTER_WIDTH         = WIDTH * 0.75;
    private static final double                               SHIELD_INDICATOR_X         = WIDTH * 0.73;
    private static final double                               SHIELD_INDICATOR_Y         = HEIGHT * 0.06;
    private static final double                               SHIELD_INDICATOR_WIDTH     = WIDTH * 0.26;
    private static final double                               SHIELD_INDICATOR_HEIGHT    = HEIGHT * 0.01428571;
    private static final long                                 FPS_60                     = 0_016_666_666l;
    private static final long                                 FPS_30                     = 0_033_333_333l;
    private static final long                                 FPS_25                     = 0_040_000_000l;
    private static final long                                 FPS_20                     = 0_050_000_000l;
    private static final long                                 FPS_10                     = 0_100_000_000l;
    private static final long                                 FPS_2                      = 0_500_000_000l;
    private static final Color                                SCORE_COLOR                = Color.rgb(51, 210, 206);
    private static final String                               SPACE_BOY;
    private static       String                               spaceBoyName;
    private              boolean                              running;
    private              boolean                              gameOverScreen;
    private              boolean                              hallOfFameScreen;
    private              List<SpaceFXView.Player>             hallOfFame;
    private              boolean                              inputAllowed;
    private              Text                                 userName;
    private final        Image                                startImg                   = new Image(getClass().getResourceAsStream("startscreen.png"), 700 * SCALING_FACTOR, 900 * SCALING_FACTOR, true, false);
    private final        Image                                gameOverImg                = new Image(getClass().getResourceAsStream("gameover.png"), 700 * SCALING_FACTOR, 900 * SCALING_FACTOR, true, false);
    private final        Image                                backgroundImg              = new Image(getClass().getResourceAsStream("background.png"), 700 * SCALING_FACTOR, 3379 * SCALING_FACTOR, true, false);
    private final        Image[]                              asteroidImages             = { new Image(getClass().getResourceAsStream("asteroid1.png"), 140 * SCALING_FACTOR, 140 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("asteroid2.png"), 140 * SCALING_FACTOR, 140 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("asteroid3.png"), 140 * SCALING_FACTOR, 140 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("asteroid4.png"), 110 * SCALING_FACTOR, 110 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("asteroid5.png"), 100 * SCALING_FACTOR, 100 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("asteroid6.png"), 120 * SCALING_FACTOR, 120 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("asteroid7.png"), 110 * SCALING_FACTOR, 110 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("asteroid8.png"), 100 * SCALING_FACTOR, 100 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("asteroid9.png"), 130 * SCALING_FACTOR, 130 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("asteroid10.png"), 120 * SCALING_FACTOR, 120 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("asteroid11.png"), 140 * SCALING_FACTOR, 140 * SCALING_FACTOR, true, false) };
    private final        Image[]                              enemyImages                = { new Image(getClass().getResourceAsStream("enemy1.png"), 56 * SCALING_FACTOR, 56 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("enemy2.png"), 50 * SCALING_FACTOR, 50 * SCALING_FACTOR, true, false),
                                                                                             new Image(getClass().getResourceAsStream("enemy3.png"), 68 * SCALING_FACTOR, 68 * SCALING_FACTOR, true, false) };
    private final        Image                                enemyBossImg0              = new Image(getClass().getResourceAsStream("enemyBoss0.png"), 100 * SCALING_FACTOR, 100 * SCALING_FACTOR, true, false);
    private final        Image                                enemyBossImg1              = new Image(getClass().getResourceAsStream("enemyBoss1.png"), 100 * SCALING_FACTOR, 100 * SCALING_FACTOR, true, false);
    private final        Image                                enemyBossImg2              = new Image(getClass().getResourceAsStream("enemyBoss2.png"), 100 * SCALING_FACTOR, 100 * SCALING_FACTOR, true, false);
    private final        Image                                enemyBossImg3              = new Image(getClass().getResourceAsStream("enemyBoss3.png"), 100 * SCALING_FACTOR, 100 * SCALING_FACTOR, true, false);
    private final        Image                                enemyBossImg4              = new Image(getClass().getResourceAsStream("enemyBoss4.png"), 100 * SCALING_FACTOR, 100 * SCALING_FACTOR, true, false);
    private final        Image                                spaceshipImg               = new Image(getClass().getResourceAsStream("fighter.png"), 48 * SCALING_FACTOR, 48 * SCALING_FACTOR, true, false);
    private final        Image                                spaceshipThrustImg         = new Image(getClass().getResourceAsStream("fighterThrust.png"), 48 * SCALING_FACTOR, 48 * SCALING_FACTOR, true, false);
    private final        Image                                miniSpaceshipImg           = new Image(getClass().getResourceAsStream("fighter.png"), 16 * SCALING_FACTOR, 16 * SCALING_FACTOR, true, false);
    private final        Image                                deflectorShieldImg         = new Image(getClass().getResourceAsStream("deflectorshield.png"), 100 * SCALING_FACTOR, 100 * SCALING_FACTOR, true, false);
    private final        Image                                miniDeflectorShieldImg     = new Image(getClass().getResourceAsStream("deflectorshield.png"), 16 * SCALING_FACTOR, 16 * SCALING_FACTOR, true, false);
    private final        Image                                torpedoImg                 = new Image(getClass().getResourceAsStream("torpedo.png"), 17 * SCALING_FACTOR, 20 * SCALING_FACTOR, true, false);
    private final        Image                                enemyTorpedoImg            = new Image(getClass().getResourceAsStream("enemyTorpedo.png"), 21 * SCALING_FACTOR, 21 * SCALING_FACTOR, true, false);
    private final        Image                                enemyBossTorpedoImg        = new Image(getClass().getResourceAsStream("enemyBossTorpedo.png"), 26 * SCALING_FACTOR, 26 * SCALING_FACTOR, true, false);
    private final        Image                                explosionImg               = new Image(getClass().getResourceAsStream("explosion.png"), 960 * SCALING_FACTOR, 768 * SCALING_FACTOR, true, false);
    private final        Image                                asteroidExplosionImg       = new Image(getClass().getResourceAsStream("asteroidExplosion.png"), 2048 * SCALING_FACTOR, 1792 * SCALING_FACTOR, true, false);
    private final        Image                                spaceShipExplosionImg      = new Image(getClass().getResourceAsStream("spaceshipexplosion.png"), 800 * SCALING_FACTOR, 600 * SCALING_FACTOR, true, false);
    private final        Image                                hitImg                     = new Image(getClass().getResourceAsStream("torpedoHit2.png"), 400 * SCALING_FACTOR, 160 * SCALING_FACTOR, true, false);
    private final        Image                                enemyBossHitImg            = new Image(getClass().getResourceAsStream("torpedoHit.png"), 400 * SCALING_FACTOR, 160 * SCALING_FACTOR, true, false);
    private final        Image                                enemyBossExplosionImg      = new Image(getClass().getResourceAsStream("enemyBossExplosion.png"), 800 * SCALING_FACTOR, 1400 * SCALING_FACTOR, true, false);
    private final        Image                                crystalImg                 = new Image(getClass().getResourceAsStream("crystal.png"), 100 * SCALING_FACTOR, 100 * SCALING_FACTOR, true, false);
    private final        Image                                crystalExplosionImg        = new Image(getClass().getResourceAsStream("crystalExplosion.png"), 400 * SCALING_FACTOR, 700 * SCALING_FACTOR, true, false);
    private final        Image                                rocketImg                  = new Image(getClass().getResourceAsStream("rocket.png"), 17 * SCALING_FACTOR, 50 * SCALING_FACTOR, true, false);
    private final        Image                                rocketExplosionImg         = new Image(getClass().getResourceAsStream("rocketExplosion.png"), 512 * SCALING_FACTOR, 896 * SCALING_FACTOR, true, false);
    private final        double                               deflectorShieldRadius      = deflectorShieldImg.getRequestedWidth() * 0.5;
    private              Font                                 scoreFont;
    private              double                               backgroundViewportY;
    private              Canvas                               canvas;
    private              GraphicsContext                      ctx;
    private              SpaceFXView.Star[]                   stars;
    private              SpaceFXView.Asteroid[]               asteroids;
    private              SpaceFXView.Enemy[]                  enemies;
    private              SpaceFXView.SpaceShip                spaceShip;
    private              SpaceFXView.SpaceShipExplosion       spaceShipExplosion;
    private              List<SpaceFXView.EnemyBoss>          enemyBosses;
    private              List<SpaceFXView.EnemyBoss>          enemyBossesToRemove;
    private              List<SpaceFXView.Crystal>            crystals;
    private              List<SpaceFXView.Crystal>            crystalsToRemove;
    private              List<SpaceFXView.Torpedo>            torpedos;
    private              List<SpaceFXView.Torpedo>            torpedosToRemove;
    private              List<SpaceFXView.Rocket>             rockets;
    private              List<SpaceFXView.Rocket>             rocketsToRemove;
    private              List<SpaceFXView.RocketExplosion>    rocketExplosions;
    private              List<SpaceFXView.RocketExplosion>    rocketExplosionsToRemove;
    private              List<SpaceFXView.EnemyTorpedo>       enemyTorpedos;
    private              List<SpaceFXView.EnemyTorpedo>       enemyTorpedosToRemove;
    private              List<SpaceFXView.EnemyBossTorpedo>   enemyBossTorpedos;
    private              List<SpaceFXView.EnemyBossTorpedo>   enemyBossTorpedosToRemove;
    private              List<SpaceFXView.EnemyBossExplosion> enemyBossExplosions;
    private              List<SpaceFXView.EnemyBossExplosion> enemyBossExplosionsToRemove;
    private              List<SpaceFXView.Explosion>          explosions;
    private              List<SpaceFXView.Explosion>          explosionsToRemove;
    private              List<SpaceFXView.AsteroidExplosion>  asteroidExplosions;
    private              List<SpaceFXView.AsteroidExplosion>  asteroidExplosionsToRemove;
    private              List<SpaceFXView.CrystalExplosion>   crystalExplosions;
    private              List<SpaceFXView.CrystalExplosion>   crystalExplosionsToRemove;
    private              List<SpaceFXView.Hit>                hits;
    private              List<SpaceFXView.Hit>                hitsToRemove;
    private              List<SpaceFXView.EnemyBossHit>       enemyBossHits;
    private              List<SpaceFXView.EnemyBossHit>       enemyBossHitsToRemove;
    private              long                                 score;
    private              double                               scorePosX;
    private              double                               scorePosY;
    private              boolean                              hasBeenHit;
    private              int                                  noOfLifes;
    private              int                                  noOfShields;
    private              long                                 lastShieldActivated;
    private              long                                 lastEnemyBossAttack;
    private              long                                 lastCrystal;
    private              long                                 lastTimerCall;
    private              AnimationTimer                       timer;
    private              BooleanBinding                       showing;

    static {
        try {
            spaceBoyName = Font.loadFont(SpaceFXView.class.getResourceAsStream("spaceboy.ttf"), 10).getName();
        } catch (Exception exception) { }
        SPACE_BOY = spaceBoyName;
    }


    public SpaceFXView() {
        init();
        //setupBinding();

        setCenter(canvas);
        setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
    }


    // ******************** Methods *******************************************
    private void init() {
        scoreFont        = spaceBoy(60 * SCALING_FACTOR);
        running          = false;
        gameOverScreen   = false;
        hallOfFameScreen = false;

        // PreFill hall of fame
        SpaceFXView.Player p1 = new SpaceFXView.Player("--", 0l);
        SpaceFXView.Player p2 = new SpaceFXView.Player("--", 0l);
        SpaceFXView.Player p3 = new SpaceFXView.Player("--", 0l);
        hallOfFame = new ArrayList<>(3);
        hallOfFame.add(p1);
        hallOfFame.add(p2);
        hallOfFame.add(p3);
        inputAllowed = false;
        userName = new Text("--");
        userName.setFont(scoreFont);
        userName.setTextOrigin(VPos.CENTER);
        userName.setTextAlignment(TextAlignment.CENTER);
        userName.setManaged(false);
        userName.setVisible(false);

        // Variable initialization
        backgroundViewportY         = 2079 * SCALING_FACTOR; //backgroundImg.getHeight() - HEIGHT;
        canvas                      = new Canvas(WIDTH, HEIGHT);
        ctx                         = canvas.getGraphicsContext2D();
        stars                       = new SpaceFXView.Star[NO_OF_STARS];
        asteroids                   = new SpaceFXView.Asteroid[NO_OF_ASTEROIDS];
        enemies                     = new SpaceFXView.Enemy[NO_OF_ENEMIES];
        spaceShip                   = new SpaceFXView.SpaceShip(spaceshipImg, spaceshipThrustImg);
        spaceShipExplosion          = new SpaceFXView.SpaceShipExplosion(0, 0);
        enemyBosses                 = new ArrayList<>();
        enemyBossesToRemove         = new ArrayList<>();
        crystals                    = new ArrayList<>();
        crystalsToRemove            = new ArrayList<>();
        rockets                     = new ArrayList<>();
        rocketsToRemove             = new ArrayList<>();
        torpedos                    = new ArrayList<>();
        torpedosToRemove            = new ArrayList<>();
        rocketExplosions            = new ArrayList<>();
        rocketExplosionsToRemove    = new ArrayList<>();
        explosions                  = new ArrayList<>();
        explosionsToRemove          = new ArrayList<>();
        asteroidExplosions          = new ArrayList<>();
        asteroidExplosionsToRemove  = new ArrayList<>();
        crystalExplosions           = new ArrayList<>();
        crystalExplosionsToRemove   = new ArrayList<>();
        enemyTorpedos               = new ArrayList<>();
        enemyTorpedosToRemove       = new ArrayList<>();
        enemyBossTorpedos           = new ArrayList<>();
        enemyBossTorpedosToRemove   = new ArrayList<>();
        enemyBossExplosions         = new ArrayList<>();
        enemyBossExplosionsToRemove = new ArrayList<>();
        hits                        = new ArrayList<>();
        hitsToRemove                = new ArrayList<>();
        enemyBossHits               = new ArrayList<>();
        enemyBossHitsToRemove       = new ArrayList<>();
        score                       = 0;
        hasBeenHit                  = false;
        noOfLifes                   = LIFES;
        noOfShields                 = SHIELDS;
        lastShieldActivated         = 0;
        lastEnemyBossAttack         = System.nanoTime();
        lastCrystal                 = System.nanoTime();
        long deltaTime              = FPS_30;
        timer                       = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall) {
                    lastTimerCall = now + deltaTime;
                    updateAndDraw();
                }
                if (now > lastEnemyBossAttack + ENEMY_BOSS_ATTACK_INTERVAL) {
                    spawnEnemyBoss();
                    lastEnemyBossAttack = now;
                }
                if (now > lastCrystal + CRYSTAL_SPAWN_INTERVAL) {
                    spawnCrystal();
                    lastCrystal = now;
                }
            }
        };

        initStars();
        initAsteroids();
        initEnemies();

        scorePosX = WIDTH * 0.5;
        scorePosY = 40 * SCALING_FACTOR;

        // Preparing GraphicsContext
        ctx.setFont(scoreFont);
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.setTextBaseline(VPos.CENTER);
        ctx.drawImage(startImg, 0, 0);
    }

    public void registerListeners() {
        Scene scene = getScene();

        // Setup key listener
        scene.setOnKeyPressed(e -> {
            if (running) {
                switch(e.getCode()) {
                    case UP:
                        spaceShip.vY = -5;
                        break;
                    case RIGHT:
                        spaceShip.vX = 5;
                        break;
                    case DOWN:
                        spaceShip.vY = 5;
                        break;
                    case LEFT:
                        spaceShip.vX = -5;
                        break;
                    case S:
                        if (noOfShields > 0 && !spaceShip.shield) {
                            lastShieldActivated = System.currentTimeMillis();
                            spaceShip.shield    = true;
                        }
                        break;
                    case R:
                        // Max 5 rockets at the same time
                        if (rockets.size() < MAX_NO_OF_ROCKETS) {
                            spawnRocket(spaceShip.x, spaceShip.y);
                        }
                        break;
                    case SPACE:
                        spawnTorpedo(spaceShip.x, spaceShip.y);
                        break;
                }
            } else if (e.getCode() == KeyCode.P && !gameOverScreen) {
                ctx.clearRect(0, 0, WIDTH, HEIGHT);
                if (SHOW_BACKGROUND) {
                    ctx.drawImage(backgroundImg, 0, 0);
                }
                running = true;
                timer.start();
            }
        });
        scene.setOnKeyReleased( e -> {
            if (running) {
                switch (e.getCode()) {
                    case UP   : spaceShip.vY = 0; break;
                    case RIGHT: spaceShip.vX = 0; break;
                    case DOWN : spaceShip.vY = 0; break;
                    case LEFT : spaceShip.vX = 0; break;
                }
            }
        });
        scene.setOnKeyTyped(e -> {
            if (inputAllowed) {
                if (userName.getText().startsWith("_")) {
                    userName.setText(e.getCharacter().toUpperCase() + "_");
                } else if (userName.getText().endsWith("_")) {
                    userName.setText(userName.getText().substring(0, 1) + e.getCharacter().toUpperCase());
                    inputAllowed = false;
                }
            }
        });
    }

    private void setupBinding() {
        showing = Bindings.createBooleanBinding(() -> {
            if (getScene() != null && getScene().getWindow() != null) {
                return getScene().getWindow().isShowing();
            } else {
                return false;
            }
        }, sceneProperty(), getScene().windowProperty(), getScene().getWindow().showingProperty());

        showing.addListener(o -> {
            if (showing.get()) {
                registerListeners();
            }
        });
    }

    private void initStars() {
        for (int i = 0 ; i < NO_OF_STARS ; i++) {
            SpaceFXView.Star star =  new SpaceFXView.Star();
            star.y = RND.nextDouble() * HEIGHT;
            stars[i] = star;
        }
    }

    private void initAsteroids() {
        for (int i = 0 ; i < NO_OF_ASTEROIDS ; i++) {
            asteroids[i] = new SpaceFXView.Asteroid(asteroidImages[RND.nextInt(asteroidImages.length)]);
        }
    }

    private void initEnemies() {
        for (int i = 0 ; i < NO_OF_ENEMIES ; i ++) {
            enemies[i] = new SpaceFXView.Enemy(enemyImages[RND.nextInt(enemyImages.length)]);
        }
    }


    // Update and draw
    private void updateAndDraw() {
        torpedosToRemove.clear();
        rocketsToRemove.clear();
        enemyTorpedosToRemove.clear();
        explosionsToRemove.clear();
        hitsToRemove.clear();
        enemyBossHitsToRemove.clear();
        asteroidExplosionsToRemove.clear();
        enemyBossExplosionsToRemove.clear();
        rocketExplosionsToRemove.clear();
        crystalsToRemove.clear();
        enemyBossesToRemove.clear();
        enemyBossTorpedosToRemove.clear();

        ctx.clearRect(0, 0, WIDTH, HEIGHT);

        // Draw background
        if (SHOW_BACKGROUND) {
            backgroundViewportY -= 0.5;
            if (backgroundViewportY <= 0) {
                backgroundViewportY = 2079 * SCALING_FACTOR; //backgroundImg.getHeight() - HEIGHT;
            }
            ctx.drawImage(backgroundImg, 0, backgroundViewportY, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT);
        }

        // Draw Stars
        if (SHOW_STARS) {
            ctx.setFill(Color.rgb(255, 255, 255, 0.9));
            for (int i = 0; i < NO_OF_STARS; i++) {
                SpaceFXView.Star star = stars[i];
                star.update();
                ctx.fillOval(star.x, star.y, star.size, star.size);
            }
        }

        // Draw Asteroids
        for (int i = 0 ; i < NO_OF_ASTEROIDS ; i++) {
            SpaceFXView.Asteroid asteroid = asteroids[i];
            asteroid.update();
            ctx.save();
            ctx.translate(asteroid.cX, asteroid.cY);
            ctx.rotate(asteroid.rot);
            ctx.scale(asteroid.scale, asteroid.scale);
            ctx.translate(-asteroid.imgCenterX, -asteroid.imgCenterY);
            ctx.drawImage(asteroid.image, 0, 0);
            ctx.restore();

            // Check for torpedo hits
            for (SpaceFXView.Torpedo torpedo : torpedos) {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, asteroid.cX, asteroid.cY, asteroid.radius)) {
                    asteroid.hits--;
                    if (asteroid.hits == 0) {
                        asteroidExplosions.add(new SpaceFXView.AsteroidExplosion(asteroid.cX - SpaceFXView.AsteroidExplosion.FRAME_CENTER * asteroid.scale, asteroid.cY - SpaceFXView.AsteroidExplosion.FRAME_CENTER * asteroid.scale, asteroid.vX, asteroid.vY, asteroid.scale));
                        score += asteroid.value;
                        asteroid.respawn();
                        torpedosToRemove.add(torpedo);
                    } else {
                        hits.add(new SpaceFXView.Hit(torpedo.x - SpaceFXView.Hit.FRAME_CENTER, torpedo.y - SpaceFXView.Hit.FRAME_HEIGHT, asteroid.vX, asteroid.vY));
                        torpedosToRemove.add(torpedo);
                    }
                }
            }

            // Check for rocket hits
            for (SpaceFXView.Rocket rocket : rockets) {
                if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, asteroid.cX, asteroid.cY, asteroid.radius)) {
                    rocketExplosions.add(new SpaceFXView.RocketExplosion(asteroid.cX - SpaceFXView.RocketExplosion.FRAME_CENTER * asteroid.scale, asteroid.cY - SpaceFXView.RocketExplosion.FRAME_CENTER * asteroid.scale, asteroid.vX, asteroid.vY, asteroid.scale));
                    score += asteroid.value;
                    asteroid.respawn();
                    rocketsToRemove.add(rocket);
                }
            }

            // Check for space ship hit
            if (!hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, deflectorShieldRadius, asteroid.cX, asteroid.cY, asteroid.radius);
                } else {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, spaceShip.radius, asteroid.cX, asteroid.cY, asteroid.radius);
                }
                if (hit) {
                    spaceShipExplosion.countX = 0;
                    spaceShipExplosion.countY = 0;
                    spaceShipExplosion.x      = spaceShip.x - SpaceFXView.SpaceShipExplosion.FRAME_WIDTH;
                    spaceShipExplosion.y      = spaceShip.y - SpaceFXView.SpaceShipExplosion.FRAME_HEIGHT;
                    if (spaceShip.shield) {
                        asteroidExplosions.add(new SpaceFXView.AsteroidExplosion(asteroid.cX - SpaceFXView.AsteroidExplosion.FRAME_CENTER * asteroid.scale, asteroid.cY - SpaceFXView.AsteroidExplosion.FRAME_CENTER * asteroid.scale, asteroid.vX, asteroid.vY, asteroid.scale));
                    } else {
                        hasBeenHit = true;
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
                    }
                    asteroid.respawn();
                }
            }
        }

        // Draw Enemies
        for (int i = 0 ; i < NO_OF_ENEMIES ; i++) {
            SpaceFXView.Enemy enemy = enemies[i];
            enemy.update();
            ctx.save();
            ctx.translate(enemy.x - enemy.radius, enemy.y - enemy.radius);
            ctx.save();
            ctx.translate(enemy.radius, enemy.radius);
            ctx.rotate(enemy.rot);
            ctx.translate(-enemy.radius, -enemy.radius);
            ctx.drawImage(enemy.image, 0, 0);
            ctx.restore();
            ctx.restore();
            // Fire if spaceship is below enemy
            if (enemy.y < spaceShip.y) {
                if (enemy.x > spaceShip.x - ENEMY_FIRE_SENSITIVITY && enemy.x < spaceShip.x + ENEMY_FIRE_SENSITIVITY) {
                    if (enemy.y - enemy.lastShotY > 15) {
                        spawnEnemyTorpedo(enemy.x, enemy.y, enemy.vX, enemy.vY);
                        enemy.lastShotY = enemy.y;
                    }
                }
            }

            // Check for torpedo hits
            for (SpaceFXView.Torpedo torpedo : torpedos) {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, enemy.x, enemy.y, enemy.radius)) {
                    explosions.add(new SpaceFXView.Explosion(enemy.x - SpaceFXView.Explosion.FRAME_WIDTH * 0.25, enemy.y - SpaceFXView.Explosion.FRAME_HEIGHT * 0.25, enemy.vX, enemy.vY, 0.5));
                    score += enemy.value;
                    enemy.respawn();
                    torpedosToRemove.add(torpedo);
                }
            }

            // Check for rocket hits
            for (SpaceFXView.Rocket rocket : rockets) {
                if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, enemy.x, enemy.y, enemy.radius)) {
                    rocketExplosions.add(new SpaceFXView.RocketExplosion(enemy.x - SpaceFXView.RocketExplosion.FRAME_WIDTH * 0.25, enemy.y - SpaceFXView.RocketExplosion.FRAME_HEIGHT * 0.25, enemy.vX, enemy.vY, 0.5));
                    score += enemy.value;
                    enemy.respawn();
                    rocketsToRemove.add(rocket);
                }
            }

            // Check for space ship hit
            if (!hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, deflectorShieldRadius, enemy.x, enemy.y, enemy.radius);
                } else {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, spaceShip.radius, enemy.x, enemy.y, enemy.radius);
                }
                if (hit) {
                    if (spaceShip.shield) {
                        explosions.add(new SpaceFXView.Explosion(enemy.x - SpaceFXView.Explosion.FRAME_WIDTH * 0.125, enemy.y - SpaceFXView.Explosion.FRAME_HEIGHT * 0.125, enemy.vX, enemy.vY, 0.5));
                    } else {
                        spaceShipExplosion.countX = 0;
                        spaceShipExplosion.countY = 0;
                        spaceShipExplosion.x      = spaceShip.x - SpaceFXView.SpaceShipExplosion.FRAME_WIDTH;
                        spaceShipExplosion.y      = spaceShip.y - SpaceFXView.SpaceShipExplosion.FRAME_HEIGHT;
                        hasBeenHit = true;
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
                    }
                    enemy.respawn();
                }
            }
        }

        // Draw EnemyBoss
        for (SpaceFXView.EnemyBoss enemyBoss : enemyBosses) {
            enemyBoss.update();
            ctx.save();
            ctx.translate(enemyBoss.x - enemyBoss.radius, enemyBoss.y - enemyBoss.radius);
            ctx.save();
            ctx.translate(enemyBoss.radius, enemyBoss.radius);
            ctx.rotate(enemyBoss.rot);
            ctx.translate(-enemyBoss.radius, -enemyBoss.radius);
            ctx.drawImage(enemyBoss.image, 0, 0);
            ctx.restore();
            ctx.restore();
            // Fire if spaceship is below enemy
            if (enemyBoss.x > spaceShip.x - ENEMY_FIRE_SENSITIVITY && enemyBoss.x < spaceShip.x + ENEMY_FIRE_SENSITIVITY) {
                if (enemyBoss.y - enemyBoss.lastShotY > 15) {
                    spawnEnemyBossTorpedo(enemyBoss.x, enemyBoss.y, enemyBoss.vX, enemyBoss.vY);
                    enemyBoss.lastShotY = enemyBoss.y;
                }
            }

            // Check for torpedo hits with enemy boss
            for (SpaceFXView.Torpedo torpedo : torpedos) {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, enemyBoss.x, enemyBoss.y, enemyBoss.radius)) {
                    enemyBoss.hits--;
                    if (enemyBoss.hits == 0) {
                        enemyBossExplosions.add(new SpaceFXView.EnemyBossExplosion(enemyBoss.x - SpaceFXView.EnemyBossExplosion.FRAME_WIDTH * 0.25, enemyBoss.y - SpaceFXView.EnemyBossExplosion.FRAME_HEIGHT * 0.25, enemyBoss.vX, enemyBoss.vY, 0.5));
                        score += enemyBoss.value;
                        enemyBossesToRemove.add(enemyBoss);
                        torpedosToRemove.add(torpedo);
                    } else {
                        enemyBossHits.add(new SpaceFXView.EnemyBossHit(torpedo.x - SpaceFXView.Hit.FRAME_CENTER, torpedo.y - SpaceFXView.Hit.FRAME_HEIGHT, enemyBoss.vX, enemyBoss.vY));
                        torpedosToRemove.add(torpedo);
                    }
                }
            }

            // Check for rocket hits with enemy boss
            for (SpaceFXView.Rocket rocket : rockets) {
                if (isHitCircleCircle(rocket.x, rocket.y, rocket.radius, enemyBoss.x, enemyBoss.y, enemyBoss.radius)) {
                    enemyBossExplosions.add(new SpaceFXView.EnemyBossExplosion(enemyBoss.x - SpaceFXView.EnemyBossExplosion.FRAME_WIDTH * 0.25, enemyBoss.y - SpaceFXView.EnemyBossExplosion.FRAME_HEIGHT * 0.25, enemyBoss.vX, enemyBoss.vY, 0.5));
                    score += enemyBoss.value;
                    enemyBossesToRemove.add(enemyBoss);
                    rocketsToRemove.add(rocket);
                }
            }


            // Check for space ship hit with enemy boss
            if (!hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, deflectorShieldRadius, enemyBoss.x, enemyBoss.y, enemyBoss.radius);
                } else {
                    hit = isHitCircleCircle(spaceShip.x, spaceShip.y, spaceShip.radius, enemyBoss.x, enemyBoss.y, enemyBoss.radius);
                }
                if (hit) {
                    if (spaceShip.shield) {
                        explosions.add(new SpaceFXView.Explosion(enemyBoss.x - SpaceFXView.Explosion.FRAME_WIDTH * 0.125, enemyBoss.y - SpaceFXView.Explosion.FRAME_HEIGHT * 0.125, enemyBoss.vX, enemyBoss.vY, 0.5));
                    } else {
                        spaceShipExplosion.countX = 0;
                        spaceShipExplosion.countY = 0;
                        spaceShipExplosion.x = spaceShip.x - SpaceFXView.SpaceShipExplosion.FRAME_WIDTH;
                        spaceShipExplosion.y = spaceShip.y - SpaceFXView.SpaceShipExplosion.FRAME_HEIGHT;
                        hasBeenHit = true;
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
                    }
                    enemyBossesToRemove.add(enemyBoss);
                }
            }
        }
        enemyBosses.removeAll(enemyBossesToRemove);

        // Draw Crystal
        for (SpaceFXView.Crystal crystal : crystals) {
            crystal.update();
            ctx.save();
            ctx.translate(crystal.cX, crystal.cY);
            ctx.rotate(crystal.rot);
            ctx.translate(-crystal.imgCenterX, -crystal.imgCenterY);
            ctx.drawImage(crystal.image, 0, 0);
            ctx.restore();

            // Check for space ship contact
            boolean hit;
            if (spaceShip.shield) {
                hit = isHitCircleCircle(spaceShip.x, spaceShip.y, deflectorShieldRadius, crystal.cX, crystal.cY, crystal.radius);
            } else {
                hit = isHitCircleCircle(spaceShip.x, spaceShip.y, spaceShip.radius, crystal.cX, crystal.cY, crystal.radius);
            }
            if (hit) {
                if (noOfShields <= SHIELDS - 1) { noOfShields++; }
                crystalExplosions.add(new SpaceFXView.CrystalExplosion(crystal.cX - SpaceFXView.CrystalExplosion.FRAME_CENTER, crystal.cY - SpaceFXView.CrystalExplosion.FRAME_CENTER, crystal.vX, crystal.vY, 1.0));
                crystalsToRemove.add(crystal);
            }
        }
        crystals.removeAll(crystalsToRemove);

        // Draw Torpedos
        for (SpaceFXView.Torpedo torpedo : torpedos) {
            torpedo.update();
            ctx.drawImage(torpedo.image, torpedo.x - torpedo.radius, torpedo.y - torpedo.radius);
        }
        torpedos.removeAll(torpedosToRemove);

        // Draw Rockets
        for (SpaceFXView.Rocket rocket : rockets) {
            rocket.update();
            ctx.drawImage(rocket.image, rocket.x - rocket.halfWidth, rocket.y - rocket.halfHeight);
        }
        rockets.removeAll(rocketsToRemove);

        // Draw EnemyTorpedos
        for (SpaceFXView.EnemyTorpedo enemyTorpedo : enemyTorpedos) {
            enemyTorpedo.update();
            ctx.drawImage(enemyTorpedo.image, enemyTorpedo.x, enemyTorpedo.y);
        }
        enemyTorpedos.removeAll(enemyTorpedosToRemove);

        // Draw EnemyBossTorpedos
        for (SpaceFXView.EnemyBossTorpedo enemyBossTorpedo : enemyBossTorpedos) {
            enemyBossTorpedo.update();
            ctx.drawImage(enemyBossTorpedo.image, enemyBossTorpedo.x, enemyBossTorpedo.y);
        }
        enemyBossTorpedos.removeAll(enemyBossTorpedosToRemove);

        // Draw Explosions
        for (SpaceFXView.Explosion explosion : explosions) {
            explosion.update();
            ctx.drawImage(explosionImg, explosion.countX * SpaceFXView.Explosion.FRAME_WIDTH, explosion.countY * SpaceFXView.Explosion.FRAME_HEIGHT, SpaceFXView.Explosion.FRAME_WIDTH, SpaceFXView.Explosion.FRAME_HEIGHT, explosion.x, explosion.y, SpaceFXView.Explosion.FRAME_WIDTH * explosion.scale, SpaceFXView.Explosion.FRAME_HEIGHT * explosion.scale);
        }
        explosions.removeAll(explosionsToRemove);

        // Draw AsteroidExplosions
        for (SpaceFXView.AsteroidExplosion asteroidExplosion : asteroidExplosions) {
            asteroidExplosion.update();
            ctx.drawImage(asteroidExplosionImg, asteroidExplosion.countX * SpaceFXView.AsteroidExplosion.FRAME_WIDTH, asteroidExplosion.countY * SpaceFXView.AsteroidExplosion.FRAME_HEIGHT, SpaceFXView.AsteroidExplosion.FRAME_WIDTH, SpaceFXView.AsteroidExplosion.FRAME_HEIGHT, asteroidExplosion.x, asteroidExplosion.y, SpaceFXView.AsteroidExplosion.FRAME_WIDTH * asteroidExplosion.scale, SpaceFXView.AsteroidExplosion.FRAME_HEIGHT * asteroidExplosion.scale);
        }
        asteroidExplosions.removeAll(asteroidExplosionsToRemove);

        // Draw RocketExplosions
        for (SpaceFXView.RocketExplosion rocketExplosion : rocketExplosions) {
            rocketExplosion.update();
            ctx.drawImage(rocketExplosionImg, rocketExplosion.countX * SpaceFXView.RocketExplosion.FRAME_WIDTH, rocketExplosion.countY * SpaceFXView.RocketExplosion.FRAME_HEIGHT, SpaceFXView.RocketExplosion.FRAME_WIDTH, SpaceFXView.RocketExplosion.FRAME_HEIGHT, rocketExplosion.x, rocketExplosion.y, SpaceFXView.RocketExplosion.FRAME_WIDTH * rocketExplosion.scale, SpaceFXView.RocketExplosion.FRAME_HEIGHT * rocketExplosion.scale);
        }
        rocketExplosions.removeAll(rocketExplosionsToRemove);

        // Draw EnemyBpssExplosions
        for (SpaceFXView.EnemyBossExplosion enemyBossExplosion : enemyBossExplosions) {
            enemyBossExplosion.update();
            ctx.drawImage(enemyBossExplosionImg, enemyBossExplosion.countX * SpaceFXView.EnemyBossExplosion.FRAME_WIDTH, enemyBossExplosion.countY * SpaceFXView.EnemyBossExplosion.FRAME_HEIGHT, SpaceFXView.EnemyBossExplosion.FRAME_WIDTH, SpaceFXView.EnemyBossExplosion.FRAME_HEIGHT, enemyBossExplosion.x, enemyBossExplosion.y, SpaceFXView.EnemyBossExplosion.FRAME_WIDTH * enemyBossExplosion.scale, SpaceFXView.EnemyBossExplosion.FRAME_HEIGHT * enemyBossExplosion.scale);
        }
        enemyBossExplosions.removeAll(enemyBossExplosionsToRemove);

        // Draw CrystalExplosions
        for (SpaceFXView.CrystalExplosion crystalExplosion : crystalExplosions) {
            crystalExplosion.update();
            ctx.drawImage(crystalExplosionImg, crystalExplosion.countX * SpaceFXView.CrystalExplosion.FRAME_WIDTH, crystalExplosion.countY * SpaceFXView.CrystalExplosion.FRAME_HEIGHT, SpaceFXView.CrystalExplosion.FRAME_WIDTH, SpaceFXView.CrystalExplosion.FRAME_HEIGHT, crystalExplosion.x, crystalExplosion.y, SpaceFXView.CrystalExplosion.FRAME_WIDTH * crystalExplosion.scale, SpaceFXView.CrystalExplosion.FRAME_HEIGHT * crystalExplosion.scale);
        }
        crystalExplosions.removeAll(crystalExplosionsToRemove);

        // Draw Hits
        for (SpaceFXView.Hit hit : hits) {
            hit.update();
            ctx.drawImage(hitImg, hit.countX * SpaceFXView.Hit.FRAME_WIDTH, hit.countY * SpaceFXView.Hit.FRAME_HEIGHT, SpaceFXView.Hit.FRAME_WIDTH, SpaceFXView.Hit.FRAME_HEIGHT, hit.x, hit.y, SpaceFXView.Hit.FRAME_WIDTH, SpaceFXView.Hit.FRAME_HEIGHT);
        }
        hits.removeAll(hitsToRemove);

        // Draw EnemyBoss Hits
        for (SpaceFXView.EnemyBossHit hit : enemyBossHits) {
            hit.update();
            ctx.drawImage(enemyBossHitImg, hit.countX * SpaceFXView.Hit.FRAME_WIDTH, hit.countY * SpaceFXView.Hit.FRAME_HEIGHT, SpaceFXView.Hit.FRAME_WIDTH, SpaceFXView.Hit.FRAME_HEIGHT, hit.x, hit.y, SpaceFXView.Hit.FRAME_WIDTH, SpaceFXView.Hit.FRAME_HEIGHT);
        }
        enemyBossHits.removeAll(enemyBossHitsToRemove);

        // Draw Spaceship, score, lifes and shields
        if (noOfLifes > 0) {
            // Draw Spaceship or it's explosion
            if (hasBeenHit) {
                spaceShipExplosion.update();
                ctx.drawImage(spaceShipExplosionImg, spaceShipExplosion.countX * spaceShipExplosion.FRAME_WIDTH, spaceShipExplosion.countY * spaceShipExplosion.FRAME_HEIGHT,
                              spaceShipExplosion.FRAME_WIDTH, spaceShipExplosion.FRAME_HEIGHT, spaceShip.x - spaceShipExplosion.FRAME_CENTER, spaceShip.y - spaceShipExplosion.FRAME_CENTER,
                              spaceShipExplosion.FRAME_WIDTH, spaceShipExplosion.FRAME_HEIGHT);
            } else {
                // Draw space ship
                spaceShip.update();

                ctx.drawImage((0 == spaceShip.vX && 0 == spaceShip.vY) ? spaceShip.image : spaceShip.imageThrust, spaceShip.x - spaceShip.radius, spaceShip.y - spaceShip.radius);

                if (spaceShip.shield) {
                    long delta = System.currentTimeMillis() - lastShieldActivated;
                    if (delta > DEFLECTOR_SHIELD_TIME) {
                        spaceShip.shield = false;
                        noOfShields--;
                    } else {
                        ctx.setStroke(SCORE_COLOR);
                        ctx.setFill(SCORE_COLOR);
                        ctx.strokeRect(SHIELD_INDICATOR_X, SHIELD_INDICATOR_Y, SHIELD_INDICATOR_WIDTH, SHIELD_INDICATOR_HEIGHT);
                        ctx.fillRect(SHIELD_INDICATOR_X, SHIELD_INDICATOR_Y, SHIELD_INDICATOR_WIDTH - SHIELD_INDICATOR_WIDTH * delta / DEFLECTOR_SHIELD_TIME, SHIELD_INDICATOR_HEIGHT);
                        ctx.setGlobalAlpha(RND.nextDouble() * 0.5 + 0.1);
                        ctx.drawImage(deflectorShieldImg, spaceShip.x - deflectorShieldRadius, spaceShip.y - deflectorShieldRadius);
                        ctx.setGlobalAlpha(1);
                    }
                }
            }

            // Draw score
            ctx.setFill(SCORE_COLOR);
            ctx.setFont(scoreFont);
            ctx.fillText(Long.toString(score), scorePosX, scorePosY);

            // Draw lifes
            for (int i = 0 ; i < noOfLifes ; i++) {
                ctx.drawImage(miniSpaceshipImg, i * miniSpaceshipImg.getWidth() + 10, 20);
            }

            // Draw shields
            for (int i = 0 ; i < noOfShields ; i++) {
                ctx.drawImage(miniDeflectorShieldImg, WIDTH - i * (miniDeflectorShieldImg.getWidth() + 5), 20);
            }
        }
    }


    // Spawn different objects
    private void spawnTorpedo(final double x, final double y) {
        torpedos.add(new SpaceFXView.Torpedo(torpedoImg, x, y));
    }

    private void spawnRocket(final double x, final double y) {
        rockets.add(new SpaceFXView.Rocket(rocketImg, x, y));
    }

    private void spawnEnemyTorpedo(final double x, final double y, final double vX, final double vY) {
        double vFactor = ENEMY_TORPEDO_SPEED / vY; // make sure the speed is always the defined one
        enemyTorpedos.add(new SpaceFXView.EnemyTorpedo(enemyTorpedoImg, x, y, vFactor * vX, vFactor * vY));
    }

    private void spawnEnemyBoss() {
        enemyBosses.add(new SpaceFXView.EnemyBoss(enemyBossImg4));
    }

    private void spawnCrystal() {
        crystals.add(new SpaceFXView.Crystal(crystalImg));
    }

    private void spawnEnemyBossTorpedo(final double x, final double y, final double vX, final double vY) {
        double vFactor = ENEMY_BOSS_TORPEDO_SPEED / vY; // make sure the speed is always the defined one
        enemyBossTorpedos.add(new SpaceFXView.EnemyBossTorpedo(enemyBossTorpedoImg, x, y, vFactor * vX, vFactor * vY));
    }


    // Hit test
    private boolean isHitCircleCircle(final double c1X, final double c1Y, final double c1R, final double c2X, final double c2Y, final double c2R) {
        double distX    = c1X - c2X;
        double distY    = c1Y - c2Y;
        double distance = Math.sqrt((distX * distX) + (distY * distY));
        return (distance <= c1R+c2R);
    }


    // Game Over
    private void gameOver() {
        timer.stop();
        running        = false;
        gameOverScreen = true;

        PauseTransition pauseBeforeGameOverScreen = new PauseTransition(Duration.millis(1000));
        pauseBeforeGameOverScreen.setOnFinished(e -> {
            checkForHighScore(new SpaceFXView.Player("", score));
            ctx.clearRect(0, 0, WIDTH, HEIGHT);
            ctx.drawImage(gameOverImg, 0, 0, WIDTH, HEIGHT);
            ctx.setFill(SCORE_COLOR);
            ctx.setFont(scoreFont);
            ctx.fillText(Long.toString(score), scorePosX, HEIGHT * 0.25);
        });
        pauseBeforeGameOverScreen.play();

        PauseTransition pauseInGameOverScreen = new PauseTransition(Duration.millis(5000));
        pauseInGameOverScreen.setOnFinished(e -> {
            ctx.clearRect(0, 0, WIDTH, HEIGHT);
            ctx.drawImage(startImg, 0, 0);

            gameOverScreen = false;
            explosions.clear();
            torpedos.clear();
            enemyTorpedos.clear();
            for (SpaceFXView.Asteroid asteroid : asteroids) { asteroid.respawn(); }
            for (SpaceFXView.Enemy enemy : enemies) { enemy.respawn(); }
            initEnemies();
            spaceShip.x  = WIDTH * 0.5;
            spaceShip.y  = HEIGHT - 2 * spaceShip.image.getHeight();
            spaceShip.vX = 0;
            spaceShip.vY = 0;
            hasBeenHit   = false;
            noOfLifes    = LIFES;
            noOfShields  = SHIELDS;
            score        = 0;
        });
        pauseInGameOverScreen.play();
    }


    // Check for highscore
    private void checkForHighScore(final SpaceFXView.Player player) {
        if (player.score < hallOfFame.get(2).score) {
            return;
        }

        // Ask for player name
        inputAllowed = true;
        userName.setVisible(true);
        userName.setManaged(true);
        userName.setX(WIDTH * 0.5);
        userName.setY(HEIGHT * 0.75);

        hallOfFame.add(player);
        Collections.sort(hallOfFame);
        hallOfFame = hallOfFame.stream().limit(3).collect(Collectors.toList());

        // Show hall of fame

    }


    // Font definition
    private static Font spaceBoy(final double size) { return new Font(SPACE_BOY, size); }


    // ******************** Space Object Classes ******************************
    private class Star {
        private final Random  rnd          = new Random();
        private final double  xVariation   = 0;
        private final double  minSpeedY    = 4;
        private       double  x;
        private       double  y;
        private       double  size;
        private       double  vX;
        private       double  vY;
        private       double  vYVariation;


        public Star() {
            // Random size
            size = rnd.nextInt(2) + 1;

            // Position
            x = (int)(rnd.nextDouble() * WIDTH);
            y = -size;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            // Velocity
            vX = (int) (Math.round((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X);
            vY = (int) (Math.round(((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y);
        }


        private void respawn() {
            x = (int) (RND.nextDouble() * WIDTH);
            y = -size;
        }

        private void update() {
            x += vX;
            y += vY;

            // Respawn star
            if(y > HEIGHT + size) {
                respawn();
            }
        }
    }

    private class Asteroid {
        private static final int     MAX_VALUE      = 10;
        private final        Random  rnd            = new Random();
        private final        double  xVariation     = 2;
        private final        double  minSpeedY      = 2;
        private final        double  minRotationR   = 0.1;
        private              Image   image;
        private              double  x;
        private              double  y;
        private              double  width;
        private              double  height;
        private              double  size;
        private              double  imgCenterX;
        private              double  imgCenterY;
        private              double  radius;
        private              double  cX;
        private              double  cY;
        private              double  rot;
        private              double  vX;
        private              double  vY;
        private              double  vR;
        private              boolean rotateRight;
        private              double  scale;
        private              double  vYVariation;
        private              int     value;
        private              int     hits;


        public Asteroid(final Image image) {
            // Image
            this.image = image;
            init();
        }


        private void init() {
            // Position
            x   = rnd.nextDouble() * WIDTH;
            y   = -image.getHeight();
            rot = 0;

            // Random Size
            scale = (rnd.nextDouble() * 0.6) + 0.2;

            // No of hits (0.2 - 0.8)
            hits = (int) (scale * 5.0);

            // Value
            value = (int) (1 / scale * MAX_VALUE);

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            width      = image.getWidth() * scale;
            height     = image.getHeight() * scale;
            size       = width > height ? width : height;
            radius     = size * 0.5;
            imgCenterX = image.getWidth() * 0.5;
            imgCenterY = image.getHeight() * 0.5;

            // Velocity
            vX          = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            vY          = (((rnd.nextDouble() * 1.5) + minSpeedY * 1/scale) * vYVariation) * VELOCITY_FACTOR_Y;
            vR          = ((rnd.nextDouble() * 0.5) + minRotationR) * VELOCITY_FACTOR_R;
            rotateRight = rnd.nextBoolean();
        }

        private void respawn() {
            this.image = asteroidImages[RND.nextInt(asteroidImages.length)];
            init();
        }

        private void update() {
            x += vX;
            y += vY;

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            if (rotateRight) {
                rot += vR;
                if (rot > 360) { rot = 0; }
            } else {
                rot -= vR;
                if (rot < 0) { rot = 360; }
            }

            // Respawn asteroid
            if(x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                respawn();
            }
        }
    }

    private class SpaceShip {
        private final Image   image;
        private final Image   imageThrust;
        private       double  x;
        private       double  y;
        private       double  size;
        private       double  radius;
        private       double  width;
        private       double  height;
        private       double  vX;
        private       double  vY;
        private       boolean shield;


        public SpaceShip(final Image image, final Image imageThrust) {
            this.image       = image;
            this.imageThrust = imageThrust;
            this.x           = WIDTH * 0.5;
            this.y           = HEIGHT - 2 * image.getHeight();
            this.width       = image.getWidth();
            this.height      = image.getHeight();
            this.size        = width > height ? width : height;
            this.radius      = size * 0.5;
            this.vX          = 0;
            this.vY          = 0;
            this.shield      = false;
        }


        private void update() {
            x += vX;
            y += vY;
            if (x + width * 0.5 > WIDTH) {
                x = WIDTH - width * 0.5;
            }
            if (x - width * 0.5 < 0) {
                x = width * 0.5;
            }
            if (y + height * 0.5 > HEIGHT) {
                y = HEIGHT - height * 0.5;
            }
            if (y - height * 0.5< 0) {
                y = height * 0.5;
            }
        }
    }

    private class Torpedo {
        private final Image  image;
        private       double x;
        private       double y;
        private       double width;
        private       double height;
        private       double size;
        private       double radius;
        private       double vX;
        private       double vY;


        public Torpedo(final Image image, final double x, final double y) {
            this.image  = image;
            this.x      = x;
            this.y      = y - image.getHeight();
            this.width  = image.getWidth();
            this.height = image.getHeight();
            this.size   = width > height ? width : height;
            this.radius = size * 0.5;
            this.vX     = 0;
            this.vY     = TORPEDO_SPEED;
        }


        private void update() {
            y -= vY;
            if (y < -size) {
                torpedosToRemove.add(SpaceFXView.Torpedo.this);
            }
        }
    }

    private class Rocket {
        private final Image  image;
        private       double x;
        private       double y;
        private       double width;
        private       double height;
        private       double halfWidth;
        private       double halfHeight;
        private       double size;
        private       double radius;
        private       double vX;
        private       double vY;


        public Rocket(final Image image, final double x, final double y) {
            this.image      = image;
            this.x          = x;
            this.y          = y - image.getHeight();
            this.width      = image.getWidth();
            this.height     = image.getHeight();
            this.halfWidth  = width * 0.5;
            this.halfHeight = height * 0.5;
            this.size       = width > height ? width : height;
            this.radius     = size * 0.5;
            this.vX         = 0;
            this.vY         = ROCKET_SPEED;
        }


        private void update() {
            y -= vY;
            if (y < -size) {
                rocketsToRemove.add(SpaceFXView.Rocket.this);
            }
        }
    }

    private class RocketExplosion {
        private static final double FRAME_WIDTH  = 128 * SCALING_FACTOR;
        private static final double FRAME_HEIGHT = 128 * SCALING_FACTOR;
        private static final double FRAME_CENTER = FRAME_WIDTH * 0.5;
        private static final int    MAX_FRAME_X  = 4;
        private static final int    MAX_FRAME_Y  = 7;
        private              double x;
        private              double y;
        private              double vX;
        private              double vY;
        private              double scale;
        private              int    countX;
        private              int    countY;


        public RocketExplosion(final double x, final double y, final double vX, final double vY, final double scale) {
            this.x      = x;
            this.y      = y;
            this.vX     = vX;
            this.vY     = vY;
            this.scale  = scale;
            this.countX = 0;
            this.countY = 0;
        }


        private void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == MAX_FRAME_X) {
                countY++;
                if (countX == MAX_FRAME_X && countY == MAX_FRAME_Y) {
                    rocketExplosionsToRemove.add(SpaceFXView.RocketExplosion.this);
                }
                countX = 0;
                if (countY == MAX_FRAME_Y) {
                    countY = 0;
                }
            }
        }
    }

    private class AsteroidExplosion {
        private static final double FRAME_WIDTH  = 256 * SCALING_FACTOR;
        private static final double FRAME_HEIGHT = 256 * SCALING_FACTOR;
        private static final double FRAME_CENTER = FRAME_WIDTH * 0.5;
        private static final int    MAX_FRAME_X  = 8;
        private static final int    MAX_FRAME_Y  = 7;
        private              double x;
        private              double y;
        private              double vX;
        private              double vY;
        private              double scale;
        private              int    countX;
        private              int    countY;


        public AsteroidExplosion(final double x, final double y, final double vX, final double vY, final double scale) {
            this.x      = x;
            this.y      = y;
            this.vX     = vX;
            this.vY     = vY;
            this.scale  = scale;
            this.countX = 0;
            this.countY = 0;
        }


        private void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == MAX_FRAME_X) {
                countY++;
                if (countX == MAX_FRAME_X && countY == MAX_FRAME_Y) {
                    asteroidExplosionsToRemove.add(SpaceFXView.AsteroidExplosion.this);
                }
                countX = 0;
                if (countY == MAX_FRAME_Y) {
                    countY = 0;
                }
            }
        }
    }

    private class Explosion {
        private static final double FRAME_WIDTH  = 192 * SCALING_FACTOR;
        private static final double FRAME_HEIGHT = 192 * SCALING_FACTOR;
        private static final double FRAME_CENTER = FRAME_WIDTH * 0.5;
        private static final int    MAX_FRAME_X  = 5;
        private static final int    MAX_FRAME_Y  = 4;
        private              double x;
        private              double y;
        private              double vX;
        private              double vY;
        private              double scale;
        private              int    countX;
        private              int    countY;


        public Explosion(final double x, final double y, final double vX, final double vY, final double scale) {
            this.x      = x;
            this.y      = y;
            this.vX     = vX;
            this.vY     = vY;
            this.scale  = scale;
            this.countX = 0;
            this.countY = 0;
        }


        private void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == MAX_FRAME_X) {
                countY++;
                if (countX == MAX_FRAME_X && countY == MAX_FRAME_Y) {
                    explosionsToRemove.add(SpaceFXView.Explosion.this);
                }
                countX = 0;
                if (countY == MAX_FRAME_Y) {
                    countY = 0;
                }
            }
        }
    }

    private class CrystalExplosion {
        private static final double FRAME_WIDTH  = 100 * SCALING_FACTOR;
        private static final double FRAME_HEIGHT = 100 * SCALING_FACTOR;
        private static final double FRAME_CENTER = FRAME_WIDTH * 0.5;
        private static final int    MAX_FRAME_X  = 4;
        private static final int    MAX_FRAME_Y  = 7;
        private              double x;
        private              double y;
        private              double vX;
        private              double vY;
        private              double scale;
        private              int    countX;
        private              int    countY;


        public CrystalExplosion(final double x, final double y, final double vX, final double vY, final double scale) {
            this.x      = x;
            this.y      = y;
            this.vX     = vX;
            this.vY     = vY;
            this.scale  = scale;
            this.countX = 0;
            this.countY = 0;
        }


        private void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == MAX_FRAME_X) {
                countY++;
                if (countX == MAX_FRAME_X && countY == MAX_FRAME_Y) {
                    crystalExplosionsToRemove.add(SpaceFXView.CrystalExplosion.this);
                }
                countX = 0;
                if (countY == MAX_FRAME_Y) {
                    countY = 0;
                }
            }
        }
    }

    private class SpaceShipExplosion {
        private static final double FRAME_WIDTH  = 100 * SCALING_FACTOR;
        private static final double FRAME_HEIGHT = 100 * SCALING_FACTOR;
        private static final double FRAME_CENTER = FRAME_WIDTH * 0.5;
        private static final int    MAX_FRAME_X  = 8;
        private static final int    MAX_FRAME_Y  = 6;
        private              double x;
        private              double y;
        private              int    countX;
        private              int    countY;


        public SpaceShipExplosion(final double x, final double y) {
            this.x      = x;
            this.y      = y;
            this.countX = 0;
            this.countY = 0;
        }


        private void update() {
            countX++;
            if (countX == MAX_FRAME_X) {
                countX = 0;
                countY++;
                if (countY == MAX_FRAME_Y) {
                    countY = 0;
                }
                if (countX == 0 && countY == 0) {
                    hasBeenHit = false;
                    spaceShip.x = WIDTH * 0.5;
                    spaceShip.y = HEIGHT - 2 * spaceShip.height;
                }
            }
        }
    }

    private class Hit {
        private static final double FRAME_WIDTH  = 80 * SCALING_FACTOR;
        private static final double FRAME_HEIGHT = 80 * SCALING_FACTOR;
        private static final double FRAME_CENTER = FRAME_WIDTH * 0.5;
        private static final int    MAX_FRAME_X  = 5;
        private static final int    MAX_FRAME_Y  = 2;
        private              double x;
        private              double y;
        private              double vX;
        private              double vY;
        private              int    countX;
        private              int    countY;


        public Hit(final double x, final double y, final double vX, final double vY) {
            this.x      = x;
            this.y      = y;
            this.vX     = vX;
            this.vY     = vY;
            this.countX = 0;
            this.countY = 0;
        }


        private void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == MAX_FRAME_X) {
                countY++;
                if (countX == MAX_FRAME_X && countY == MAX_FRAME_Y) {
                    hitsToRemove.add(SpaceFXView.Hit.this);
                }
                countX = 0;
                if (countY == MAX_FRAME_Y) {
                    countY = 0;
                }
            }
        }
    }

    private class EnemyBossHit {
        private static final double FRAME_WIDTH  = 80 * SCALING_FACTOR;
        private static final double FRAME_HEIGHT = 80 * SCALING_FACTOR;
        private static final double FRAME_CENTER = FRAME_WIDTH * 0.5;
        private static final int    MAX_FRAME_X  = 5;
        private static final int    MAX_FRAME_Y  = 2;
        private              double x;
        private              double y;
        private              double vX;
        private              double vY;
        private              int    countX;
        private              int    countY;


        public EnemyBossHit(final double x, final double y, final double vX, final double vY) {
            this.x      = x;
            this.y      = y;
            this.vX     = vX;
            this.vY     = vY;
            this.countX = 0;
            this.countY = 0;
        }


        private void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == MAX_FRAME_X) {
                countY++;
                if (countX == MAX_FRAME_X && countY == MAX_FRAME_Y) {
                    enemyBossHitsToRemove.add(SpaceFXView.EnemyBossHit.this);
                }
                countX = 0;
                if (countY == MAX_FRAME_Y) {
                    countY = 0;
                }
            }
        }
    }

    private class Enemy {
        private static final int     MAX_VALUE  = 49;
        private final        Random  rnd        = new Random();
        private final        double  xVariation = 1;
        private final        double  minSpeedY  = 3;
        private              Image   image;
        private              double  x;
        private              double  y;
        private              double  rot;
        private              double  width;
        private              double  height;
        private              double  size;
        private              double  radius;
        private              double  vX;
        private              double  vY;
        private              double  vYVariation;
        private              int     value;
        private              double  lastShotY;


        public Enemy(final Image image) {
            // Image
            this.image = image;
            init();
        }


        private void init() {
            // Position
            x = rnd.nextDouble() * WIDTH;
            y = -image.getHeight();

            // Value
            value = rnd.nextInt(MAX_VALUE) + 1;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            width  = image.getWidth();
            height = image.getHeight();
            size   = width > height ? width : height;
            radius = size * 0.5;

            // Velocity
            if (x < FIRST_QUARTER_WIDTH) {
                vX = (rnd.nextDouble() * 0.5) * VELOCITY_FACTOR_X;
            } else if (x > LAST_QUARTER_WIDTH) {
                vX = -(rnd.nextDouble() * 0.5) * VELOCITY_FACTOR_X;
            } else {
                vX = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            }
            vY = (((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y;

            // Rotation
            rot = Math.toDegrees(Math.atan2(vY, vX)) - 90;

            // Related to laser fire
            lastShotY = 0;
        }

        private void respawn() {
            image = enemyImages[RND.nextInt(enemyImages.length)];
            init();
        }

        private void update() {
            x += vX;
            y += vY;

            // Respawn Enemy
            if (x < -size || x > WIDTH + size || y > HEIGHT + size) {
                respawn();
            }
        }
    }

    private class EnemyBoss {
        private static final int     MAX_VALUE  = 99;
        private final        Random  rnd        = new Random();
        private final        double  xVariation = 1;
        private final        double  minSpeedY  = 3;
        private              Image   image;
        private              double  x;
        private              double  y;
        private              double  rot;
        private              double  width;
        private              double  height;
        private              double  size;
        private              double  radius;
        private              double  vX;
        private              double  vY;
        private              double  vYVariation;
        private              int     value;
        private              double  lastShotY;
        private              int     hits;


        public EnemyBoss(final Image image) {
            // Image
            this.image = image;
            init();
        }


        private void init() {
            // Position
            x = rnd.nextDouble() * WIDTH;
            y = -image.getHeight();

            // Value
            value = rnd.nextInt(MAX_VALUE) + 1;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            width  = image.getWidth();
            height = image.getHeight();
            size   = width > height ? width : height;
            radius = size * 0.5;

            // Velocity
            if (x < FIRST_QUARTER_WIDTH) {
                vX = (rnd.nextDouble() * 0.5) * VELOCITY_FACTOR_X;
            } else if (x > LAST_QUARTER_WIDTH) {
                vX = -(rnd.nextDouble() * 0.5) * VELOCITY_FACTOR_X;
            } else {
                vX = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            }
            vY = (((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y;

            // Rotation
            rot = Math.toDegrees(Math.atan2(vY, vX)) - 90;

            // Related to laser fire
            lastShotY = 0;

            // No of hits
            hits = 5;
        }

        private void update() {
            x += vX;
            y += vY;

            switch(hits) {
                case 5: image = enemyBossImg4; break;
                case 4: image = enemyBossImg3; break;
                case 3: image = enemyBossImg2; break;
                case 2: image = enemyBossImg1; break;
                case 1: image = enemyBossImg0; break;
            }

            // Respawn Enemy
            if (x < -size || x > WIDTH + size || y > HEIGHT + size) {
                enemyBossesToRemove.add(SpaceFXView.EnemyBoss.this);
            }
        }
    }

    private class EnemyBossExplosion {
        private static final double FRAME_WIDTH  = 200 * SCALING_FACTOR;
        private static final double FRAME_HEIGHT = 200 * SCALING_FACTOR;
        private static final double FRAME_CENTER = FRAME_WIDTH * 0.5;
        private static final int    MAX_FRAME_X  = 4;
        private static final int    MAX_FRAME_Y  = 7;
        private              double x;
        private              double y;
        private              double vX;
        private              double vY;
        private              double scale;
        private              int    countX;
        private              int    countY;


        public EnemyBossExplosion(final double x, final double y, final double vX, final double vY, final double scale) {
            this.x      = x;
            this.y      = y;
            this.vX     = vX;
            this.vY     = vY;
            this.scale  = scale;
            this.countX = 0;
            this.countY = 0;
        }


        private void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == MAX_FRAME_X) {
                countY++;
                if (countX == MAX_FRAME_X && countY == MAX_FRAME_Y) {
                    enemyBossExplosionsToRemove.add(SpaceFXView.EnemyBossExplosion.this);
                }
                countX = 0;
                if (countY == MAX_FRAME_Y) {
                    countY = 0;
                }
            }
        }
    }

    private class Crystal {
        private final Random  rnd            = new Random();
        private final double  xVariation     = 2;
        private final double  minSpeedY      = 2;
        private final double  minRotationR   = 0.1;
        private       Image   image;
        private       double  x;
        private       double  y;
        private       double  width;
        private       double  height;
        private       double  size;
        private       double  imgCenterX;
        private       double  imgCenterY;
        private       double  radius;
        private       double  cX;
        private       double  cY;
        private       double  rot;
        private       double  vX;
        private       double  vY;
        private       double  vR;
        private       boolean rotateRight;
        private       double  vYVariation;


        public Crystal(final Image image) {
            // Image
            this.image = image;
            init();
        }


        private void init() {
            // Position
            x   = rnd.nextDouble() * WIDTH;
            y   = -image.getHeight();
            rot = 0;

            // Random Speed
            vYVariation = (rnd.nextDouble() * 0.5) + 0.2;

            width      = image.getWidth();
            height     = image.getHeight();
            size       = width > height ? width : height;
            radius     = size * 0.5;
            imgCenterX = image.getWidth() * 0.5;
            imgCenterY = image.getHeight() * 0.5;

            // Velocity
            if (x < FIRST_QUARTER_WIDTH) {
                vX = rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else if (x > LAST_QUARTER_WIDTH) {
                vX = -rnd.nextDouble() * VELOCITY_FACTOR_X;
            } else {
                vX = ((rnd.nextDouble() * xVariation) - xVariation * 0.5) * VELOCITY_FACTOR_X;
            }
            vY          = (((rnd.nextDouble() * 1.5) + minSpeedY) * vYVariation) * VELOCITY_FACTOR_Y;
            vR          = (((rnd.nextDouble()) * 0.5) + minRotationR) * VELOCITY_FACTOR_R;
            rotateRight = rnd.nextBoolean();
        }

        private void update() {
            x += vX;
            y += vY;

            cX = x + imgCenterX;
            cY = y + imgCenterY;

            if (rotateRight) {
                rot += vR;
                if (rot > 360) { rot = 0; }
            } else {
                rot -= vR;
                if (rot < 0) { rot = 360; }
            }

            // Respawn asteroid
            if(x < -size || x - radius > WIDTH || y - height > HEIGHT) {
                crystalsToRemove.add(SpaceFXView.Crystal.this);
            }
        }
    }

    private class EnemyTorpedo {
        private final Image  image;
        private       double x;
        private       double y;
        private       double width;
        private       double height;
        private       double size;
        private       double radius;
        private       double vX;
        private       double vY;


        public EnemyTorpedo(final Image image, final double x, final double y, final double vX, final double vY) {
            this.image  = image;
            this.x      = x - image.getWidth() / 2.0;
            this.y      = y;
            this.width  = image.getWidth();
            this.height = image.getHeight();
            this.size   = width > height ? width : height;
            this.radius = size * 0.5;
            this.vX     = vX;
            this.vY     = vY;
        }


        private void update() {
            x += vX;
            y += vY;

            if (!hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, deflectorShieldRadius);
                } else {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, spaceShip.radius);
                }
                if (hit) {
                    enemyTorpedosToRemove.add(SpaceFXView.EnemyTorpedo.this);
                    if (spaceShip.shield) {
                    } else {
                        hasBeenHit = true;
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
                    }
                }
            } else if (y > HEIGHT) {
                enemyTorpedosToRemove.add(SpaceFXView.EnemyTorpedo.this);
            }
        }
    }

    private class EnemyBossTorpedo {
        private final Image  image;
        private       double x;
        private       double y;
        private       double width;
        private       double height;
        private       double size;
        private       double radius;
        private       double vX;
        private       double vY;


        public EnemyBossTorpedo(final Image image, final double x, final double y, final double vX, final double vY) {
            this.image  = image;
            this.x      = x - image.getWidth() / 2.0;
            this.y      = y;
            this.width  = image.getWidth();
            this.height = image.getHeight();
            this.size   = width > height ? width : height;
            this.radius = size * 0.5;
            this.vX     = vX;
            this.vY     = vY;
        }


        private void update() {
            x += vX;
            y += vY;

            if (!hasBeenHit) {
                boolean hit;
                if (spaceShip.shield) {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, deflectorShieldRadius);
                } else {
                    hit = isHitCircleCircle(x, y, radius, spaceShip.x, spaceShip.y, spaceShip.radius);
                }
                if (hit) {
                    enemyBossTorpedosToRemove.add(SpaceFXView.EnemyBossTorpedo.this);
                    if (spaceShip.shield) {
                    } else {
                        hasBeenHit = true;
                        noOfLifes--;
                        if (0 == noOfLifes) {
                            gameOver();
                        }
                    }
                }
            } else if (y > HEIGHT) {
                enemyBossTorpedosToRemove.add(SpaceFXView.EnemyBossTorpedo.this);
            }
        }
    }

    private class Player implements Comparable<SpaceFXView.Player> {
        private final String id;
        private       String name;
        private       Long   score;


        public Player(final String name, final Long score) {
            this.id    = UUID.randomUUID().toString();
            this.name  = name;
            this.score = score;
        }


        @Override public int compareTo(final SpaceFXView.Player player) {
            return Long.compare(player.score, this.score);
        }
    }
}
