package Sort;

import java.util.Scanner;
class Result{
    public void PrintResult(int a){
        System.out.println(a*a);
    }
}

class Data{
    public static String Input(){
        Scanner scanner = null;
        try {
            scanner = new Scanner(System.in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scanner.nextLine();
    }
    public  static int GetInt(){
        return Integer.parseInt(Input());
    }
}
public class loop {


    public static void main(String args[]){
        Result r = new Result();
        r.PrintResult(Data.GetInt());

        //       int j = 0;
//        for (int i = 0;i < 3;i ++) {
//            int temp = j;
//            j = j +1;
//            j = temp;
////            j = j++;
//            System.out.println(j);
//        }
//        Short myshort = 99;
//        float f = 1.0f;
//        char c = 'g';
//        int t = "abc".length();
//        System.out.println(t);
//        System.out.println(myshort);
//        System.out.println(f);
//        int num = 32;
//        System.out.println(num >> 32);


//        char cc[] = {'H','e','l','l','o'};
//        test(cc);
//        System.out.println(cc);
    }
}
