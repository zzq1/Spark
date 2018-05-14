package stringQuestion;

import java.util.Scanner;

public class ReverceTest {
    public static void reverce(char[] data,int start,int end){
        if (data.length <=0 || start < 0 || start > data.length || start > end || data==null){
            return;
        }
        while(start < end){
            char temp = data[start];
            data[start] = data[end];
            data[end] = temp;
            start ++;
            end --;
        }
    }
    public static char[] reverce1(String str,int start,int end){
        char[] data = str.toCharArray();
        if (data.length <=0 || start < 0 || start > data.length || start > end || data==null){
            return data;
        }
        while(start < end){
            char temp = data[start];
            data[start] = data[end];
            data[end] = temp;
            start ++;
            end --;
        }
        return data;
    }
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        System.out.println(s.length());
        char[] cc = reverce1(s,0,s.length()-1); //I love flower ==>rewolf evol I
        String ss = cc.toString();
        System.out.println(ss);

//        String[] sss = ss.split(" ");
//        System.out.println(sss.length);
//        String res = "";
//         for (int i=0;i<sss.length;i++){
//             res += reverce1(ss.split(" ")[i],0,ss.split(" ")[i].length()-1).toString();
//        }
//        System.out.println(res);
//        char[] c = new char[5];
//        for (int i=0;i<5;i++){
//            c[i]=scanner.next().charAt(0);
//        }
//        reverce(c,0,c.length-2);
//        for (int i=0;i<5;i++){
//            System.out.println(c[i]);
//        }

    }
}
