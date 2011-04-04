package myfirstzelda;

import java.util.Arrays;

import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.tiled.TiledMap;

public class Player {
  static Input input;
  static Image image;
  
  private int slidedir = -1;
  private int[] blocking;
  private int[] keys = {Input.KEY_UP,Input.KEY_LEFT,Input.KEY_DOWN,Input.KEY_RIGHT};
  private int[][] vectors = {{0,-1,0,1},{-1,0,1,0}};
  private boolean[] keyspressed = new boolean[10];

  float x,y,speed,slidespeed;
  TiledMap map;
  int lvlw,lvlh,dir;
  
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
    Player.image = img;
  }

  public Player(int nx,int ny) {
    this.dir = 2;
    this.x = nx;
    this.y = ny;
    this.speed = 10/1000f;
    this.slidespeed = 5/1000f;
  }
  
  public void move(float dt) {
    if (input.isKeyDown(Input.KEY_S)) {
      if (keyspressed[5] == false) {
        interactTiles("sword",dir);
        keyspressed[5] = true;
      }
    } else keyspressed[5] = false;
    
    if (input.isKeyDown(Input.KEY_A)) {
      if (keyspressed[6] == false) {
        interactTiles("lift",dir);
        keyspressed[6] = true;
      }
    } else keyspressed[6] = false;
    
    
    for (int i = 0;i<4;i++) {
      keyspressed[i] = false;
      if (input.isKeyDown(keys[i])) {
        keyspressed[i] = true;
        
        if (!onWall(x+vectors[0][i]*this.speed*dt,y+vectors[1][i]*this.speed*dt,2,2)) {
          slidedir = -1;
          this.x += vectors[0][i]*this.speed*dt;
          this.y += vectors[1][i]*this.speed*dt;
        } else hitWall(i,onWallDir(i,speed*dt),dt);
        this.dir = i;
      }
    }

    if (this.x < 0) this.x = 0;
    if (this.y < 0) this.y = 0;
    if (this.x > lvlw - 2) this.x = lvlw - 2;
    if (this.y > lvlh - 2) this.y = lvlh - 2;
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
    float slidespeed = 4/1000f;

    if (k == 1 || k == 3) x = (int)(x + .5);
    else y = (int)(y + .5);
    
    slidedir = -1;
    if (w[0] == true && w[1] == false) slidedir = k == 0 ? 3 : (k-1)%4;
    else if (w[0] == false && w[1] == true) slidedir = (k+1)%4;
    
    if (slidedir == -1 || this.keyspressed[slidedir] || (this.keyspressed[(k+1)%4] || this.keyspressed[k == 0 ? 3 : (k-1)%4])) return;

    if (k == 1 || k == 2) slidedir = (slidedir+2)%4;
    boolean[] checkslide = onWallDir(slidedir,slidespeed*dt);
    if (checkslide[0] == true || checkslide[1] == true) {
      if (slidedir == 0 || slidedir == 2) y = (int)(y + .5);
      else x = (int)(x + .5);
      return;
    }
    x += vectors[0][slidedir]*(slidespeed*dt);
    y += vectors[1][slidedir]*(slidespeed*dt);
    x = (x/(slidespeed*dt))*(slidespeed*dt);
    y = (y/(slidespeed*dt))*(slidespeed*dt);
  }
  
  void interactTiles(String action,int d) {
    int[][][] tiles = {
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
    
    for (int i=0;i<4;i++) {
      map.setTileId((int)(px+(i%2)),(int)(py+(int)(i/2)),0,tiles[object][1][i]);
    }

  }
  
  int checkTile(float cx, float cy) {
    return map.getTileId((int)cx,(int)cy,0);
  }

  public void render(float cx,float cy) {
    image.draw(Math.round((x - cx)*map.getTileWidth()),Math.round((y - cy)*map.getTileHeight())-8,
               Math.round((x - cx)*map.getTileWidth())+16,Math.round((y - cy)*map.getTileHeight())-8+24,
               this.dir*16,0,this.dir*16+16,24);
    //image.draw(Math.round((x - cx)*map.getTileWidth()),Math.round((y - cy)*map.getTileHeight())-8);
    //    image.draw(x*16 - cx*16,(y - cy)*16);
  }
  
}