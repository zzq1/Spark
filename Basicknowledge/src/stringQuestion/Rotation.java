package stringQuestion;

import java.util.*;

public class Rotation {
    /*
     *如果对于一个字符串A，将A的前面任意一部分挪到后边去形成的字符串称为A的旋转词。
     *比如A=”12345”,A的旋转词有”12345”,”23451”,”34512”,”45123”和”51234”。
     */
    public static boolean chkRotation(String A, int lena, String B, int lenb) {
        // write code here
        if (lena != lenb){
            return false;
        } else {
            String S = A + A;
            return (S.contains(B));
        }
    }
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        int n = 2;
        System.out.println("Please input two strings");
        String str1 = scanner.next();
        String str2 = scanner.next();
        //System.out.println(str1+" "+str2);
        System.out.println(chkRotation(str1,str1.length(),str2,str2.length()));
    }

}
