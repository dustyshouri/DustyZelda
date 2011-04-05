package myfirstzelda;

import java.text.DecimalFormat;
import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TiledMap;

import myfirstzelda.Player;
import myfirstzelda.Camera;

public class HUD {
  GameContainer container;
  Player plyr;
  Camera cam;
  Image hudimg;
  
  int[][] drawhud = {
    {20 ,19,0 ,0 ,16,42}, // MAGIC CONTAINER
    {37 ,21,24,0 ,22,22}, // ITEM CONTAINER
    {72 ,16,0 ,66,8 ,8 }, // RUPEES
    {100,16,8 ,66,8 ,8 }, // BOMBS
    {120,16,16,66,16,8 }, // ARROWS
    {176,16,0 ,42,48,8 }, // -LIFE-
  };
  
  boolean debughud = false;
  StringBuffer debugtext = new StringBuffer();
  
  public HUD() {}
  
  public void listenInput(Input input) {
    if (input.isKeyPressed(Input.KEY_F1)) {
      debughud = !debughud;
    }
  }
  
  public void loadObj(GameContainer c,Player p,Camera ca,Image i) {
    container = c;
    plyr = p;
    cam = ca;
    hudimg = i;
  }
  
  public void render(Graphics g,float scale,TiledMap map,float mx,float my) {
    if (!debughud) {
      g.pushTransform();
      g.scale(2,2);
      
      for (int i=0;i<drawhud.length;i++) {
        hudimg.draw(drawhud[i][0],drawhud[i][1],drawhud[i][0]+drawhud[i][4],drawhud[i][1]+drawhud[i][5],
                    drawhud[i][2],drawhud[i][3],drawhud[i][2]+drawhud[i][4],drawhud[i][3]+drawhud[i][5]);
      }
      g.popTransform();
    } else {
      g.pushTransform();
      g.scale(.85f,.85f);
      debugtext.setLength(0);
      DecimalFormat df = new DecimalFormat("00.00");
      debugtext.append("Window size : " + container.getWidth() + "x" + container.getHeight() + " @ " + container.getFPS() + "fps & " +
              (int)(scale*100) + "% zoom\n" + 
              "Position    : " + df.format(plyr.x) + " : " + df.format(plyr.y) + " @ " + plyr.speed*1000 + "t/s\n" +
              "Camera      : " + df.format(cam.x) + " : " + df.format(cam.y) + "\n");
   
      g.drawString(debugtext.toString(),16,16);
      g.drawString("A/S to lift/slash",container.getWidth()-84,container.getHeight()+18);
      g.drawString("Press -/+ to zoom in",container.getWidth()-110,container.getHeight()+36);
      g.drawString(" and out. 0 to reset",container.getWidth()-110,container.getHeight()+54);

      g.popTransform();
      
      try {
        g.drawString("Mouse: " + map.getTileId((int)mx,(int)my,0) + " @ " + mx + " : " + my ,
                     16,container.getHeight() - 24);
      } catch (ArrayIndexOutOfBoundsException e) {} 
    }
  }
}
