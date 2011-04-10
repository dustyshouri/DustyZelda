package myfirstzelda;

import java.text.DecimalFormat;
import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TiledMap;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.font.effects.ShadowEffect;

import myfirstzelda.Player;
import myfirstzelda.Camera;

public class HUD {
  GameContainer container;
  Player plyr;
  Camera cam;
  Image hudimg;
  public UnicodeFont unicodeFont;
  
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
  
  @SuppressWarnings("unchecked")
  public HUD() {
    try {
      unicodeFont = new UnicodeFont("res/lucon.ttf", 14, true, false);
      unicodeFont.getEffects().add(new ShadowEffect(java.awt.Color.black,1,1,1f));
      unicodeFont.getEffects().add(new ColorEffect(java.awt.Color.white));
    } catch (SlickException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
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
      debugtext.setLength(0);
      DecimalFormat df = new DecimalFormat("00.00");
      debugtext.append("Window size : " + container.getWidth() + "x" + container.getHeight() + " @ " + container.getFPS() + "fps & " +
              (int)(scale*100) + "% zoom\n" + 
              "Position    : " + df.format(plyr.x) + " : " + df.format(plyr.y) + " @ " + plyr.speed*1000 + "t/s\n" +
              "Camera      : " + df.format(cam.x) + " : " + df.format(cam.y) + "\n");
   
      unicodeFont.drawString(16,16,debugtext.toString());
      
      debugtext.setLength(0);
      debugtext.append("A/S to lift/slash\n");
      debugtext.append("Press -/+ to zoom in\n");
      debugtext.append("and out. 0 to reset\n");
      
      unicodeFont.drawString(container.getWidth()-unicodeFont.getWidth(debugtext.toString()) - 16,
                             container.getHeight()-unicodeFont.getHeight(debugtext.toString()) - 16,
                             debugtext.toString());
      
      try {
        debugtext.setLength(0);
        debugtext.append("Mouse: " + map.getTileId((int)mx,(int)my,0) + " @ " + mx + " : " + my);
        unicodeFont.drawString(16,container.getHeight() - unicodeFont.getHeight(debugtext.toString()) - 16,debugtext.toString());
      } catch (ArrayIndexOutOfBoundsException e) {} 
    }
  }
}
