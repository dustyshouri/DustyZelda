package myfirstzelda;

import java.io.IOException;
import java.text.DecimalFormat;
import javax.sound.midi.*;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TiledMap;

public class Main extends BasicGame {
  private UnicodeFont unicodeFont;
  
  public Input input;
 
  public Image img,debug,hudimg;
  public int bx,by;
  public float mx,my;
  
  // BLOCK STUFF
  public int[] blocking = new int[2048];
  public Color clr;
  // END BLOCK STUFF
  
  public TiledMap map;
  public Player plyr;
  public Camera cam;
  public HUD hud;
  
  float scale,scaledelta = 2;
  int tileW,tileH;
  
  public Main() {
    super("Link to the Past"); 
  }
  
  @Override
  public void init(GameContainer container) throws SlickException {
    plyr = new Player(17,1);
    cam  = new Camera(plyr.x,plyr.y);
    hud  = new HUD();
    
    //img = new Image("res/zelda_linksprites.png",false,0x2);
    debug = new Image("res/zelda_blockmap.png");
    input = container.getInput();
    Player.setImage(img);
    Player.setInput(container.getInput());
    map = new TiledMap("res/zelda_kakoriko.tmx","res");
    tileW = map.getTileWidth();
    tileH = map.getTileHeight();
    cam.getLevelData(map.getWidth(),map.getHeight());
    hud.loadObj(container,plyr,cam,new Image("res/zelda_hud.png",false,0x2));
    loadBlockMap();
    
    playSong("kakiriko.mid");
  } 

  @Override
  public void update(GameContainer container, int delta) throws SlickException {
    hud.unicodeFont.loadGlyphs(1);
    hud.listenInput(input);
    if (input.isKeyDown(Input.KEY_ESCAPE)) container.exit();
    
    if (input.isKeyDown(Input.KEY_MINUS)) scaledelta -= (4/1000f)*delta;
    else if (input.isKeyDown(Input.KEY_EQUALS)) scaledelta += (4/1000f)*delta;
    else if (input.isKeyDown(Input.KEY_0)) scaledelta = 2;
    
    if (scaledelta < 1) scaledelta = 1;
    if (scaledelta > 8) scaledelta = 8;
    
    DecimalFormat df = new DecimalFormat("0.0");
    
    scale =  Float.valueOf(df.format(scaledelta)).floatValue();
    
    plyr.tick(delta);
    cam.follow(plyr.x,plyr.y,container.getWidth()/scale/tileW,container.getHeight()/scale/tileH);
    
    mx = (int)((Mouse.getX()/tileW/scale) + cam.x);
    my = (int)(((container.getHeight()-Mouse.getY())/tileH/scale) + cam.y);
  } 

  @Override
  public void render(GameContainer container, Graphics g) throws SlickException {
    g.setBackground(new Color(74,156,74));
  
    g.pushTransform();
    g.scale(scale,scale);
    int screenwidth = (int)(container.getWidth()/scale/tileW) + 3;
    int screenheight = (int)(container.getHeight()/scale/tileH) + 3;
    //int renderx = Math.round(((tileW*-1)-(cam.x*tileW)%tileW));
    //int rendery = Math.round((tileH*-1)-(cam.y*tileH)%tileH);
    int renderx = Math.round((tileW*-1)-(cam.x*tileW)%tileW);
    int rendery = Math.round((tileH*-1)-(cam.y*tileH)%tileH);

    map.render(renderx,rendery,(int)cam.x-1,(int)cam.y-1,screenwidth,screenheight);
    //map.render(Math.round((tileW*-1)-renderx%tileW),Math.round((tileH*-1)-rendery%tileH),(int)cam.x-1,(int)cam.y-1,screenwidth,screenheight,map.getLayerIndex("Objects"),false);
    plyr.render(cam.x,cam.y,false);
    map.render(renderx,rendery,(int)cam.x-1,(int)cam.y-1,screenwidth,screenheight,map.getLayerIndex("Drawover"),false);
    plyr.render(cam.x,cam.y,true);
    
    g.setFont(unicodeFont);
    if (hud.debughud) g.drawRect(Math.round((mx - cam.x)*tileW),Math.round((my - cam.y)*tileH),tileW,tileH);
    g.popTransform();
 
    hud.render(g,scale,map,mx,my);
    
    //unicodeFont.drawString(10, 33, "Testing Testing.");
  }

  public static void main(String[] args) {
    try {
      AppGameContainer app = new AppGameContainer(new Main());
      app.setShowFPS(false);
      app.setAlwaysRender(true);

      //app.setSmoothDeltas(true);
      app.setDisplayMode(256*2,224*2,false);
      //app.setTargetFrameRate(20);
      //app.setVSync(true);
 
      app.start(); 
      
    } catch (SlickException e) {
      e.printStackTrace(); 
    }
  }
  
  public void playSong(String s) {
    Sequence sequence;
    try {
      //InputStream is = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream("res/music/" + s));
      sequence = MidiSystem.getSequence(getClass().getClassLoader().getResourceAsStream("res/music/" + s));
      //sequence = MidiSystem.getSequence(new File("/res/music/" + s));
      Sequencer sequencer = MidiSystem.getSequencer();
      sequencer.open();
      sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
      sequencer.setSequence(sequence);
      sequencer.start();
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (MidiUnavailableException e) {
      e.printStackTrace();
    }
  }
  
  public static void playSound(String s) {
    try {
      Sound sfx = new Sound("res/sounds/" + s);
      sfx.play();
    } catch (SlickException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public void loadBlockMap() {
    for (int i=0;i<64*32;i++) {
      clr = debug.getColor(i%64,(int)i/64);
      // 0 = NONBLOCK
      // 1 = BLOCK
      // 2 = THROWOVER
      // 3 = SHALLOW WATER
      // 4 = WATER
      // 5 = HIGH GRASS
      int test = 0;
      if (clr.getRed() == 0) test = 1;
      else if (clr.getRed() == 140) test = 2;
      else if (clr.getRed() == 90) test = 3;
      else if (clr.getRed() == 57) test = 4;
      else if (clr.getRed() == 41) test = 5;
      else test = 0;

      //System.out.print(clr.getRed() + "," + ((i%128 == 127) ? "\n" : ""));
      blocking[i] = test;
      //System.out.print(test + "," + ((i%64 == 63) ? "\n" : ""));
    }
    plyr.getLevelData(map,blocking);
  }
}