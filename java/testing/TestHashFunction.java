package testing;

import hashing.Hasher;

public class TestHashFunction {
    public static void main(String[] args) {
        Hasher h = new Hasher();
        String s1 = h.hash("127.0.0.1:1231");
        String s2 = h.hash("127.0.0.1:1232");

       // boolean x = h.compare(s1, s2);

        //System.out.println(x);
        System.out.println(s1);
        System.out.println(s2);

    }
}
