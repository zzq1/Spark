package stringQuestion;

import java.util.Scanner;

public class LeftRotate {

    public void reverse(char[] data,int start,int end){
        if(data == null || data.length < 1 || start < 0 || end > data.length || start > end)
            return;
        /*
        while循环直接将整个句子的所有单词顺序翻转
        */
        while (start < end){
            char temp = data[start];
            data[start] = data[end];
            data[end] = temp;

            start++;
            end--;
        }
    }

    public char[] leftRotateFunction(char[] data,int n){
        if(data == null || n <= 0 )
            return null;

        int beforestart = 0;                //前半段起始位
        int beforeend = n-1;                //前半段终止位
        int afterstart = n;                 //后半段的起始位
        int afterend = data.length-1;       //后半段的终止位

        reverse(data,beforestart,beforeend);        //翻转字符串的前面n个字符
        reverse(data,afterstart,afterend);          //翻转字符串的后面部分字符
        reverse(data,0,data.length-1);              //翻转整个字符串

        return data;                                //返回最终翻转后的结果，是一个字符型数组
    }

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入一个英语字符串：");
        String str = scanner.nextLine();
        System.out.println("请输入要翻转字符串的前几位：");
        int n = scanner.nextInt();
        scanner.close();

        char[] data = new char[str.length()];
        data = str.toCharArray();

        LeftRotate lr = new LeftRotate();
        System.out.println("旋转后的字符串为：");
        System.out.println(lr.leftRotateFunction(data, n));
    }
}
