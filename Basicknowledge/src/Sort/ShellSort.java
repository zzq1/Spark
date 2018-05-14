package Sort;

public class ShellSort {
    public static void shellSort(int[] num){
        int len = num.length;
        while(len != 0){
            len = len / 2;
            for (int x = 0; x < len; x++){
                for (int i = x + len; i < num.length; i += len){
                    int j = i - len;
                    int temp = num[i];
                    for (;j >= 0 && temp < num[j];j -= len){
                        num[j+len] = num[j];
                    }
                    num[j+len] = temp;
                }
            }
        }
    }
    public static void main(String[] args){
        int[] a = {32,43,23,13,5};
        int lenght = a.length;
        shellSort(a);
        for (int i = 0;i < lenght;i++){
            System.out.print(a[i] + " ");
        }
    }
}
