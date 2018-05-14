package Recursive;

public class ComputeSum {
    public static void main(String arr[]){
        System.out.println(Sum(3));
    }
    public static int Sum(int num){
        if (num > 0){
            return num + Sum(num - 1);
        }
        return 0;
    }
}
