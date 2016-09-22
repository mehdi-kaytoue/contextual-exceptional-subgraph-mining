package fastrabbit;
import java.io.*;
import java.util.*;

public class Tuple {
    BitSet t;
    int v1;
    int v2;
    Tuple(BitSet t, int a, int b) {
	this.t = t;
	this.v1 = a;
	this.v2 = b;
    }

    Tuple() {

    }
}
