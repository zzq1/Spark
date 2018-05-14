package stringQuestion;

import java.util.Scanner;

public class Reverse {
    public static String ReverseMethod2Fun(String str){
        StringBuffer stringbuffer = new StringBuffer("");
        if(str.length() <= 0 || str.trim().equals("")){
            return str;
        }
        String[] strSet = str.split(" ");
//        System.out.println(strSet[0]);
//        System.out.println(strSet[1]);
//        System.out.println(strSet[2]);
        int length = strSet.length;
        for(int i = length - 1; i > 0;i--){
            stringbuffer.append(strSet[i] + " ");
        }
//        System.out.println(stringbuffer.toString());
        stringbuffer.append(strSet[0]);
        return stringbuffer.toString();
    }

    public static void main(String[] args){
       Scanner s = new Scanner(System.in);
       String str = s.nextLine();
       //System.out.println(str.length());
       System.out.println(ReverseMethod2Fun(str));
    }

}
