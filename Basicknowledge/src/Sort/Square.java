package Sort;



public class Square {
    private static int x = 100;
    long width;

//    public Square(long l) {
//        width = l;
//    }

    public static void main(String arg[]) {
//        Square a, b, c;
//        a = new Square(42L);
//        b = new Square(42L);
//        c = b;
//        long s = 42L;
//        System.out.println(b == c);
//        Integer i01 = 59;
//        int i02 = 59;
//        Integer i03 =Integer.valueOf(59);
//        Integer i04 = new Integer(59);
//        System.out.println(i02 == i04);


        Square hs1=new Square();
        hs1.x++;
        Square  hs2=new Square();
        hs2.x++;
        hs1=new Square();
        hs1.x++;
        Square.x--;
        System.out.println("x="+x);
    }
}