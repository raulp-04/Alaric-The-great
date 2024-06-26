package entity;
import game.GamePanel;
import game.KeyHandler;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class Player extends Entity {
    KeyHandler keyHandler;
    public final int screenX;
    public final int screenY;
    public int hasGem = 0;
    public boolean hasSword = false;
    public boolean hasKey = false;
    BufferedImage[] walkUpS;
    BufferedImage[] walkDownS;
    BufferedImage[] walkLeftS;
    BufferedImage[] walkRightS;
    BufferedImage[] attackUp;
    BufferedImage[] attackDown;
    BufferedImage[] attackLeft;
    BufferedImage[] attackRight;
    int spriteNumAt = 0;


    public Player(GamePanel gp, KeyHandler keyH) {

        super(gp);

        this.keyHandler = keyH;

        screenX = gp.screenWidth / 2 - (gp.tileSize);
        screenY = gp.screenHeight / 2 - (gp.tileSize);

        solidArea = new Rectangle(12*3, 10*3, 9*3, 14*3);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        attackArea.width = 9*3;
        attackArea.height = 14*3;

        setDefaultValues();
        getPlayerImage();
        getPlayerAttack();
    }
    public void setDefaultValues() {

        invincible = false;
        hasSword = false;
        hasKey = false;
        hasGem = 0;
        worldX = gp.tileSize * 31;
        worldY = gp.tileSize * 28;
        speed = 4;
        direction = "right";

        // PLAYER STATUS
        // LIFE
        maxLife = 6;
        life = maxLife;
    }
    public void setDefaultPos(){
        life = maxLife;
        switch (gp.currentMap) {
            case 1: {
                worldX = gp.tileSize * 30;
                worldY = gp.tileSize * 28;
                break;
            }
            case 2:  {
                worldX = gp.tileSize * 31+23;
                worldY = gp.tileSize * 33;
                break;
            }
            case 3:  {
                worldX = gp.tileSize * 9+23;
                worldY = gp.tileSize * 14;
                break;
            }
            case 4:  {
                worldX = gp.tileSize * 31+23;
                worldY = gp.tileSize * 32;
                break;
            }
        }
    }
    public void getPlayerImage() {

        try {
            BufferedImage img = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("player/knight.png")));
            // WITHOUT SWORD
            walkRight = cutImage(img, 0, 160, new int[]{32, 32, 32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32, 32, 32});
            walkUp = cutImage(img, 0, 192, new int[]{32, 32, 32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32, 32, 32});
            walkDown = cutImage(img, 0, 224, new int[]{32, 32, 32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32, 32, 32});
            walkLeft = new BufferedImage[walkRight.length];
            for (int i = 0; i < walkRight.length; i++) {
                walkLeft[i] = mirrorImage(walkRight[i]);
            }

            // WITH SWORD
            walkUpS = cutImage(img, 0, 64, new int[]{32, 32, 32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32, 32, 32});
            walkDownS = cutImage(img, 0, 128, new int[]{32, 32, 32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32, 32, 32});
            walkLeftS = cutImage(img, 0, 96, new int[]{32, 32, 32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32, 32, 32});
            walkRightS = cutImage(img, 0, 32, new int[]{32, 32, 32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32, 32, 32});

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void getPlayerAttack() {
        //17
        try {
            BufferedImage img = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("player/knight.png")));

            attackUp = cutImage(img, 0, 17*32, new int[]{32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32});
            attackDown = cutImage(img, 0, 19*32, new int[]{32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32});
            attackLeft = cutImage(img, 0, 18*32, new int[]{32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32});
            attackRight = cutImage(img, 0, 16*32, new int[]{32, 32, 32, 32, 32, 32}, new int[]{32, 32, 32, 32, 32, 32});

        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void update() {
        if (attacking && hasSword) {
            attacking();
        } else if (keyHandler.upPressed || keyHandler.leftPressed || keyHandler.rightPressed || keyHandler.downPressed || keyHandler.enterPressed) {
            if (keyHandler.upPressed) {
                direction = "up";
            } else if (keyHandler.downPressed) {
                direction = "down";
            } else if (keyHandler.leftPressed) {
                direction = "left";
            } else if (keyHandler.rightPressed) {
                direction = "right";
            }

            // TILE COLL
            collisionOn = false;
            gp.cChecker.collisionCheckTile(this);

            // OBJ COLL
            int objIndex = gp.cChecker.collisionCheckObject(this, true);
            pickUpObj(objIndex);

            // NPC COLL
            int npcIndex = gp.cChecker.collisionCheckEntity(this, gp.npc);
            interactNPC(npcIndex);

            // MONSTER NPC
            int monsterIndex = gp.cChecker.collisionCheckEntity(this, gp.monsterArray);
            interactMonster(monsterIndex);

            // CHECK EVENT
            gp.eventHandler.checkEvent();

            // CHECK COLLISION, FALSE MEANS MOVING
            if (!collisionOn && !attacking && !gp.keyHandler.enterPressed) {
                switch (direction) {
                    case "up": worldY -= speed; break;
                    case "down": worldY += speed; break;
                    case "left": worldX -= speed; break;
                    case "right":  worldX += speed; break;
                }
            }

            spriteCounter++;
            if (spriteCounter > 4) {
                if (spriteNumber == 7) {
                    spriteNumber = 0;
                } else {
                    spriteNumber++;
                }
                spriteCounter = 0;
            }

        }
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > 60) {
                invincible = false;
                invincibleCounter = 0;
            }
        }

        if (life <= 0) {
            gp.gameState = gp.GAMEOVER_STATE;
            gp.ui.command = -1;
            gp.stopMusic();
            gp.playMusic(8);
        }

    }
    public void attacking() {

        spriteCounter++;
        if(spriteCounter <= 5) {
            spriteNumAt = 0;
        }
        if(spriteCounter > 5 && spriteCounter <= 10) {
            spriteNumAt = 1;
            if (spriteCounter == 6) gp.playSE(3);

            int currentWorldX = worldX;
            int currentWorldY = worldY;
            int solidAreaWidth = solidArea.width;
            int solidAreaHeight = solidArea.height;

            switch (direction) {
                case "up": worldY -= attackArea.width; break;
                case "down": worldY += attackArea.width; break;
                case "left": worldX -= attackArea.width; break;
                case "right": worldX += attackArea.width; break;
            }

            solidArea.width = attackArea.width;
            solidArea.height = attackArea.height;

            int monsterIndex = gp.cChecker.collisionCheckEntity(this, gp.monsterArray);
            damageMonster(monsterIndex);

            worldX = currentWorldX;
            worldY = currentWorldY;
            solidArea.width = solidAreaWidth;
            solidArea.height = solidAreaHeight;

        }
        if(spriteCounter > 10 && spriteCounter <= 15) {
            spriteNumAt = 2;
        }
        if(spriteCounter > 15 && spriteCounter <= 20) {
            spriteNumAt = 3;
        }
        if(spriteCounter > 20 && spriteCounter <= 25){
            spriteNumAt = 4;
        }
        if (spriteCounter > 25) {
            spriteNumAt = 0;
            spriteCounter = 0;
            attacking = false;
        }
    }
    public void interactNPC(int index) {
        if (index != gp.NO_COLLISION) {
            if (gp.keyHandler.enterPressed) {
                gp.gameState = gp.DIALOG_STATE;
                gp.npc[gp.currentMap][index].speak();
            } else gp.ui.showMessage("PRESS ENTER TO INTERACT");
        } else if (gp.keyHandler.enterPressed && hasSword) {
            attacking = true;
        }
    }
    public void interactMonster(int index) {
        if (index != gp.NO_COLLISION) {
            if (!invincible && !gp.monsterArray[gp.currentMap][index].dying) {
                gp.playSE(6);
                invincible = true;
                life--;
                gp.ui.showMessage("LIFE DECREASED");
            }
        }
    }
    public void damageMonster(int index) {
        if(index != gp.NO_COLLISION) {
            if (!gp.monsterArray[gp.currentMap][index].invincible) {
                gp.monsterArray[gp.currentMap][index].invincible = true;
                gp.monsterArray[gp.currentMap][index].life -= 2;

                if(gp.monsterArray[gp.currentMap][index].life <= 0) {
                    gp.monsterArray[gp.currentMap][index].dying = true;

                }
            }
        }
    }
    public void pickUpObj(int index) {
        if (index != gp.NO_COLLISION) {
            String name = gp.obj[gp.currentMap][index].name;
            switch (name) {
                case "Gem":
                    hasGem += 100;
                    gp.obj[gp.currentMap][index] = null;
                    gp.ui.showMessage("PICKED UP GEM");
                    gp.playSE(2);
                    break;
                case "Sword":
                    hasSword = true;
                    gp.obj[gp.currentMap][index] = null;
                    gp.ui.showMessage("PICKED UP SWORD AND SHIELD");
                    gp.playSE(2);
                    break;
                case "Key":
                    gp.obj[gp.currentMap][index] = null;
                    hasKey = true;
                    gp.ui.showMessage("PICKED UP A KEY");
                    gp.playSE(2);
                    break;
                case "Chest":
                    if(hasKey) {
                        gp.obj[gp.currentMap][index] = null;
                        gp.gameState = gp.WIN_STATE;
                        gp.stopMusic();
                        gp.playMusic(12);
                    } else gp.ui.showMessage("YOU NEED A KEY TO OPEN");
                    break;
                case "Cherry":
                    if (life + 3 > maxLife) {
                        life = maxLife;
                    } else life += 3;
                    gp.obj[gp.currentMap][index] = null;
                    gp.ui.showMessage("LIFE INCREASED");
                    gp.playSE(7);
            }
        }
    }
    @Override public void draw(Graphics2D g2d) {
        BufferedImage images = null;

        switch (direction) {
            case "up":
                if (!attacking) {
                    images = (!hasSword) ? walkUp[spriteNumber] : walkUpS[spriteNumber];
                } else if (hasSword) {
                    images = attackUp[spriteNumAt];
                }
                if (attacking && !hasSword) {
                    images = walkUp[spriteNumber];
                }
                break;
            case "down":
                if (!attacking) {
                    images = (!hasSword) ? walkDown[spriteNumber] : walkDownS[spriteNumber];
                } else if (hasSword) {
                    images = attackDown[spriteNumAt];
                }
                if (attacking && !hasSword) {
                    images = walkDown[spriteNumber];
                }
                break;
            case "left":
                if (!attacking) {
                    images = (!hasSword) ? walkLeft[spriteNumber] : walkLeftS[spriteNumber];
                } else if (hasSword) {
                    images = attackLeft[spriteNumAt];

                }
                if (attacking && !hasSword) {
                    images = walkLeft[spriteNumber];
                }
                break;
            case "right":
                if (!attacking) {
                    images = (!hasSword) ? walkRight[spriteNumber] : walkRightS[spriteNumber];
                } else if (hasSword) {
                    images = attackRight[spriteNumAt];

                }
                if (attacking && !hasSword) {
                    images = walkRight[spriteNumber];
                }
                break;
        }
        if (invincible) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        }

        g2d.drawImage(images, screenX, screenY, 96, 96, null);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // debug
//        g2d.setColor(Color.white);
//        g2d.drawRect(screenX+ solidArea.x,screenY+ solidArea.y, solidArea.width, solidArea.height);
//
//        g2d.drawRect(screenX+ solidArea.x+attackArea.width,screenY+ solidArea.y, solidArea.width, solidArea.height);
//        g2d.drawRect(screenX+ solidArea.x - attackArea.width,screenY+ solidArea.y, solidArea.width, solidArea.height);
//
//        g2d.drawRect(screenX+ solidArea.x,screenY+ solidArea.y+attackArea.width*2-12, solidArea.width, solidArea.width);
//        g2d.drawRect(screenX+ solidArea.x,screenY+ solidArea.y-attackArea.width, solidArea.width, solidArea.width);


//        g2d.setFont(new Font("Times New Roman", Font.PLAIN, 20));
//        g2d.setColor(Color.WHITE);
//        g2d.drawString("invincible = " + invincibleCounter, 10, 400);
    }
}
