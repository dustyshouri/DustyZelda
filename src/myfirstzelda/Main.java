package myfirstzelda;

import java.text.DecimalFormat;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.*;
import org.newdawn.slick.tiled.TiledMap;

public class Main extends BasicGame {
  public Input input;
  public Image img,debug;
  public int bx,by;
  public float mx,my;
  
  // BLOCK STUFF
  public int[] blocking = new int[2048];
  public Color clr;
  // END BLOCK STUFF
  
	public Player plyr = new Player(23,35);
	public Camera cam  = new Camera(plyr.x,plyr.y);
	
	float scale = 2;
	int tileW,tileH;

	StringBuffer debugtext = new StringBuffer();
	
  public Main() {
    super("Link to the Past"); 
  }
  
	public TiledMap map;
	
  @Override
  public void init(GameContainer container) throws SlickException {
    img = new Image("res/zelda_debug-link.png",false,0x2);
    input = container.getInput();
    Player.setImage(img);
    Player.setInput(container.getInput());
    map = new TiledMap("res/zelda_debug.tmx","res");
    tileW = map.getTileWidth();
    tileH = map.getTileHeight();
    cam.getLevelData(map.getWidth(),map.getHeight());

  	debug = new Image("res/zelda_blockmap.png");
  	System.out.print((0 - 5)%4);
    //System.out.print(debug.getColor(9,55).getRed());
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

  @Override
  public void update(GameContainer container, int delta) throws SlickException {
  	if (input.isKeyDown(Input.KEY_ESCAPE)) container.exit();
  	if (input.isKeyDown(Input.KEY_F2)) container.setFullscreen(!container.isFullscreen());
  	if (input.isKeyDown(Input.KEY_MINUS)) scale -= .1;
  	else if (input.isKeyDown(Input.KEY_EQUALS)) scale += .1;
  	else if (input.isKeyDown(Input.KEY_0)) scale = 2;
  	if (scale < 1) scale = 1;
  	if (scale > 8) scale = 8;
    plyr.move(delta);
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
    
    plyr.render(cam.x,cam.y);
    map.render(renderx,rendery,(int)cam.x-1,(int)cam.y-1,screenwidth,screenheight,map.getLayerIndex("Drawover"),false);
    
    g.drawRect(Math.round((mx - cam.x)*tileW),Math.round((my - cam.y)*tileH),tileW,tileH);
    
  	g.popTransform();
  	
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

  public static void main(String[] args) {
    try {
      AppGameContainer app = new AppGameContainer(new Main());
      app.setShowFPS(false);
      app.setAlwaysRender(true);

      app.setDisplayMode(256*2,224*2,false);
      app.setTargetFrameRate(60);
      //app.setVSync(true);
 
      app.start(); 
      
    } catch (SlickException e) {
      e.printStackTrace(); 
    }
  }
}