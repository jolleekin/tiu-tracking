package com.psu.capstone.tiutracking;

public class Vector2D {
  public double x, y;
  
  public Vector2D() {
    this(0, 0);
  }
  
  public Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  public Vector2D(Vector2D v) {
    this(v.x, v.y);
  }
  
  public void add(double x, double y) {
    this.x += x;
    this.y += y;
  }
  
  public void add(Vector2D v) {
    add(v.x, v.y);
  }
  
  public void sub(Vector2D v) {
    sub(v.x, v.y);
  }
  
  public void sub(double x, double y) {
    this.x -= x;
    this.y -= y;
  }
  
  public void mult(double x, double y) {
    this.x *= x;
    this.y *= y;
  }
  
  public void mult(Vector2D v) {
    mult(v.x, v.y);
  }
  
  public void mult(double c) {
    mult(c, c);
  }
  
  public void lerp(Vector2D other, double factor) {
	x += (other.x - x) * factor;
	y += (other.y - y) * factor;
  }
  
  public void lerp(Vector2D a, Vector2D b, double factor) {
	x = a.x + (b.x - a.x) * factor;
	y = b.y + (b.y - a.y) * factor;
  }
  
  public double mag() {
    if (x == 0 && y == 0) {
      return 0;
    }
	return Math.sqrt(x * x + y * y);
  }
  
  public double magSquared() {
    return x * x + y * y;
  }
  
  public void set(Vector2D v) {
    x = v.x;
    y = v.y;
  }
  
  public static Vector2D sub(Vector2D a, Vector2D b) {
    return new Vector2D(a.x - b.x, a.y - b.y);
  }
  
  public static Vector2D mult(Vector2D v, double c) {
    return new Vector2D(v.x * c, v.y * c);
  }
}