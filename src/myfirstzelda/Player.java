package myfirstzelda;

import java.util.Arrays;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;

public class Player {
  private enum Mode {
    IDLE(0,1,0),WALK(1,8,20/1000f),SWORD(9,10,24/1000f);
    
    private final int startframe,framecount;
    private final float anispeed;
    Mode(int startframe,int framecount,float anispeed) {
      this.startframe = startframe;
      this.framecount = framecount;
      this.anispeed = anispeed;
    }
    int getStartFrame() {
      return startframe;
    }
    int getFrameCount() {
      return framecount;
    }
    float getAniSpeed() {
      return anispeed;
    }
  }
  
  static Input input;
  static Image image;

  private int slidedir = -1;
  private int[] blocking;
  private int[] keys = {Input.KEY_UP,Input.KEY_LEFT,Input.KEY_DOWN,Input.KEY_RIGHT};
  private int[][] vectors = {{0,-1,0,1},{-1,0,1,0}};
  private boolean[] keyspressed = new boolean[4];

  float x,y,speed,slidespeed,aniframe,frozen;
  TiledMap map;
  int lvlw,lvlh,dir,sprite;
  Mode mode;
  
  public static void setInput(Input input) {
    Player.input = input;
  }
  
  public void getLevelData(TiledMap m,int[] b) {
    map = m;
    lvlw = m.getWidth();
    lvlh = m.getHeight();
    blocking = b;
  }
  
  public static void setImage(Image img) {
    //Player.image = img;
  }

  public Player(int nx,int ny) {
    try {
      image = new Image("res/zelda_linksprites.png",false,0x2);
    } catch (SlickException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    this.dir = 2;
    this.x = nx;
    this.y = ny;
    this.speed = 10/1000f;
    this.slidespeed = 4/1000f;
    mode = Mode.IDLE;
  }
  
  public void tick(float dt) {
    //System.out.println(modes.SWORD.getSprite());
    if (input.isKeyPressed(Input.KEY_S)) {
      interactTiles("sword",dir);
      Main.playSound("LTTP_Sword1.wav");
      mode = Mode.SWORD;
      aniframe = mode.getStartFrame();
      //frozen = 0.437f;
      frozen = ((mode.getFrameCount()-1)/(mode.getAniSpeed()*dt))*(dt/1000);
      //System.out.println(frozen);
    }
    
    if (input.isKeyPressed(Input.KEY_A)) {
      interactTiles("lift",dir);
    }
    
    if (frozen > 0 && mode != Mode.SWORD) move(dt);
    
    aniframe += mode.getAniSpeed()*dt;
    if (aniframe < mode.getStartFrame()) aniframe = mode.getStartFrame();
    if (aniframe > mode.getStartFrame() + mode.getFrameCount() - 1) {
      if (mode == Mode.SWORD) mode = Mode.IDLE;
      aniframe = mode.getStartFrame();
    }
    sprite = (int)aniframe + 1;
    
    //if (aniframe != 0) System.out.println(aniframe + " : " + mode.getStartFrame() + " : " + sprite);
    if (frozen > 0) frozen -= dt/1000;
    else frozen = 0;
    
    if (this.x < 0) this.x = 0;
    if (this.y < 0) this.y = 0;
    if (this.x > lvlw - 2) this.x = lvlw - 2;
    if (this.y > lvlh - 2) this.y = lvlh - 2;
  }
  
  void move (float dt) {
    if (input.isKeyDown(keys[0]) || input.isKeyDown(keys[1]) ||
        input.isKeyDown(keys[2]) || input.isKeyDown(keys[3])) mode = Mode.WALK;
    else mode = Mode.IDLE;
    
    for (int i = 0;i<4;i++) {
      keyspressed[i] = false;
      if (input.isKeyDown(keys[i])) {
        this.dir = i;
        keyspressed[i] = true;
        if (!onWall(x+(vectors[0][i]*0.0625f)+vectors[0][i]*this.speed*dt,y+(vectors[1][i]*0.0625f)+vectors[1][i]*this.speed*dt,2,2)) {
          slidedir = -1;
          this.x += vectors[0][i]*this.speed*dt;
          this.y += vectors[1][i]*this.speed*dt;
          //this.x += vectors[0][i]*this.speed*dt;
          //this.y += vectors[1][i]*this.speed*dt;
        } else hitWall(i,onWallDir(i,speed*dt),dt);
      }
    }
  }
  
  boolean onWall(float cx,float cy,float cw,float ch) {
    for (float i=0;i<cw;i+=0.0625) {
      for (float j=0;j<ch;j+=0.0625) {
        int blocktype = 0;
        try {
          blocktype = blocking[map.getTileId((int)(cx+i),(int)(cy+j),0)-1];
        } catch (ArrayIndexOutOfBoundsException e) {}
        if (blocktype == 1 || blocktype == 2) return true;
      }  
    }
    return false;
  }
  
  boolean[] onWallDir(int d,float s) {
    boolean[] block = {false,false};

    float cx[] = {x + vectors[0][d]*(d < 2 ? s : 2),x + 1 + vectors[0][d]*(d < 2 ? s + 1 : 2 - 1)};
    float cy[] = {y + vectors[1][d]*(d < 2 ? s : 2),y + 1 + vectors[1][d]*(d < 2 ? s + 1 : 2 - 1)};

    block[0] = onWall(cx[0],cy[0],((d == 1 || d == 3) ? s : 1)-1/16,((d == 0 || d == 2) ? s : 1)-1/16);
    block[1] = onWall(cx[1],cy[1],((d == 1 || d == 3) ? s : 1)-1/16,((d == 0 || d == 2) ? s : 1)-1/16);
    
    return block;
  }
  
  void hitWall(int k,boolean[] w,float dt) {
    if (k == 0) interactTiles("open",0);
    
    slidedir = -1;
    
    if (k == 1 || k == 3) x = (int)(x + .5);
    else y = (int)(y + .5);
    
    if (w[0] == true && w[1] == false) slidedir = k == 0 ? 3 : (k-1)%4;
    else if (w[0] == false && w[1] == true) slidedir = (k+1)%4;
    if (slidedir > -1) if (k == 1 || k == 2) slidedir = (slidedir+2)%4;
    
    if (slidedir == -1 || this.keyspressed[slidedir] || (this.keyspressed[(k+1)%4] || this.keyspressed[k == 0 ? 3 : (k-1)%4])) return;
    
    boolean[] checkslide = onWallDir(slidedir,slidespeed*dt);

    if (checkslide[0] == true || checkslide[1] == true) {
      if (slidedir == 0 || slidedir == 2) y = (int)(y + .5);
      else x = (int)(x + .5);
      return;
    }
    if (slidedir == 0 || slidedir == 2) {
      if (slidedir == 2 && y + .1 > (int)(y+1)) {
        y = (int)(y+1);
        slidedir = -1;
        return;
      } else if (slidedir == 0 && y - .1 < (int)y) {
        y = (int)y;
        slidedir = -1;
        return;
      } else y += vectors[1][slidedir]*(slidespeed*dt);
    }
    if (slidedir == 1 || slidedir == 3) {
      if (slidedir == 3 && x + .1 > (int)(x+1)) {
        x = (int)(x+1);
        slidedir = -1;
        return;
      } else if (slidedir == 1 && x - .1 < (int)x) {
        x = (int)x;
        slidedir = -1;
        return;
      } else x += vectors[0][slidedir]*(slidespeed*dt);
    }
  }
  
  void interactTiles(String action,int d) {
    int[][][] tiles = {
      {{518,519,582,583},{94,94,158,158}      ,{9,9}}, // DOOR
      {{3,4,67,68}      ,{662,663,726,727}    ,{0,0}}, // BUSH
      {{17,18,81,82}    ,{1083,1084,1147,1148},{2,0}}, // SIGN
      {{131,132,195,196},{1211,1212,1275,1276},{2,0}}  // STONE
    };
    
    float px = x + (d == 1 ? -1.5f :(d == 3 ? 3.5f : 1));
    float py = y + (d == 0 ? -1 :(d == 2 ? 3 : 1));

    int object = -1;
    for (int i=0;i<tiles.length;i++) {
      for (int j=0;j<4;j++) {
        if (checkTile(px,py) == tiles[i][0][j]) {
          px = px-(j%2);
          py = py-(int)(j/2);
          object = i;
          break;
        }
      }
    }
    
    if (object == -1) return;
    int[] checktiles = {checkTile(px,py),checkTile(px+1,py),checkTile(px,py+1),checkTile(px+1,py+1)};
    if (!Arrays.equals(checktiles,tiles[object][0])) return;
    if (action == "sword" && tiles[object][2][0] > 0) return;
    if (action == "lift" && tiles[object][2][1] > 0) return;
    if (action == "open" && object > 0) return;
    
    if (action == "lift") Main.playSound("LTTP_Link_Pickup.wav");
    if (action == "sword" && object == 1) Main.playSound("LTTP_Grass_Cut.wav");
    if (action == "open" && object == 0) Main.playSound("LTTP_Door.wav");
    
    for (int i=0;i<4;i++) {
      map.setTileId((int)(px+(i%2)),(int)(py+(int)(i/2)),0,tiles[object][1][i]);
    }

  }
  
  int checkTile(float cx, float cy) {
    return map.getTileId((int)cx,(int)cy,0);
  }
  
  public void render(float cx,float cy,boolean alpha) {
    int w = 24;
    int h = 24;
    if (alpha == false) {
      image.draw((x - cx)*map.getTileWidth()-4  ,(y - cy)*map.getTileHeight()-8,
                 (x - cx)*map.getTileWidth()-4+w,(y - cy)*map.getTileHeight()-8+h,
                 this.dir*w,0,this.dir*w+w,h);
      image.draw((x - cx)*map.getTileWidth()-4,(y - cy)*map.getTileHeight()-8,
          (x - cx)*map.getTileWidth()-4+w,(y - cy)*map.getTileHeight()-8+h,
          this.dir*w,sprite*h,this.dir*w+w,sprite*h+h);
      //image.draw(Math.round((x - cx)*map.getTileWidth())-4  ,Math.round((y - cy)*map.getTileHeight())-8,
      //    Math.round((x - cx)*map.getTileWidth())-4+w,Math.round((y - cy)*map.getTileHeight())-8+h,
      //    this.dir*w,h+(int)aniframe*h,this.dir*w+w,h+h+(int)aniframe*h);
    } else {
      /*
      if (map.getTileId((int)(x),(int)(y+.5),map.getLayerIndex("Drawover")) == 0 ||
          map.getTileId((int)(x+1),(int)(y+.5),map.getLayerIndex("Drawover")) == 0 ||
          map.getTileId((int)(x+1),(int)(y+1.5),map.getLayerIndex("Drawover")) == 0 ||
          map.getTileId((int)(x),(int)(y+1.5),map.getLayerIndex("Drawover")) == 0) return;
      image.draw(Math.round((x - cx)*map.getTileWidth()),Math.round((y - cy)*map.getTileHeight())-8,
          Math.round((x - cx)*map.getTileWidth())+16,Math.round((y - cy)*map.getTileHeight())-8+24,
          this.dir*16,0,this.dir*16+16,24,new Color(0,0,0,.5f));
          */
    }
    //image.draw(Math.round((x - cx)*map.getTileWidth()),Math.round((y - cy)*map.getTileHeight())-8);
    //    image.draw(x*16 - cx*16,(y - cy)*16);
  }
  
}