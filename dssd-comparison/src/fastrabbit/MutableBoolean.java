package fastrabbit;
import java.io.*;
import java.util.*;
import java.text.*;

class MutableBoolean
{
    boolean value;
    public MutableBoolean(boolean n) {
	value = n;
    }
    public boolean get() {
	return value;
    }
    
    public void set(boolean n) {
	value = n;
    }
}

// Local Variables:
// coding: utf-8
// End:
