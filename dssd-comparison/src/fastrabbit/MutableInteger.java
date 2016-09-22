package fastrabbit;
import java.io.*;
import java.util.*;
import java.text.*;

class MutableInteger
{
    int value;
    public MutableInteger(int n) {
	value = n;
    }
    public void increment() {
	value = value + 1;
    }
    public void decrement() {
	value = value - 1;
    }
    public int get() {
	return value;
    }
    
    public void set(int n) {
	value = n;
    }
}

// Local Variables:
// coding: utf-8
// End:
