package Sort;

public class InsertSort {
    public static void insrtSort(int num[]){
        int length = num.length;
        int insertNum;
        for (int i = 1;i < length;i++){
            insertNum = num[i];
            //已经排好序的元素个数
            int j = i - 1;
            while(j >= 0 && num[j] > insertNum){
                num[j+1] = num[j];
                j--;
            }
            num[j+1] = insertNum;
        }
        for (int i = 0;i < length; i++){
            System.out.println(num[i]);
        }

    }
    public static void main(String args[]){
        int a[] = {10,12,2,56,1,3,45};
        insrtSort(a);
    }
}
