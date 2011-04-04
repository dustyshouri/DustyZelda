package myfirstzelda;

public class Camera {
  float x,y;
  int lvlw,lvlh;
  
	public Camera(float nx,float ny) {
		this.x = nx;
		this.y = ny;
	}
	
	public void getLevelData(int w,int h) {
		lvlw = w;
		lvlh = h;
	}
	
	public void follow(float px,float py,float cx,float cy) {
		this.x = px - cx/2 + 1;
		this.y = py - cy/2 + 1;
		
		if (this.x < 0) this.x = 0;
		if (this.y < 0) this.y = 0;
  	if (this.x > lvlw - cx) this.x = lvlw - cx;
  	if (this.y > lvlh - cy) this.y = lvlh - cy;
	}
}
