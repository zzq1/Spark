package Sort;

public class QuickSort {
    public static void quickSort(int[] num, int start, int end){
        if (start < end){
            int base = num[start];
            int temp;
            int i = start,j = end;
            do {
                while(num[i] < base && i < end)
                    i++;
                while(num[j] > base && j > start)
                    j--;
                if (i <= j){
                    temp = num[i];
                    num[i] = num[j];
                    num[j] = temp;
                    i++;
                    j--;
                }
            }while (i <= j);
            if (start <j)
                quickSort(num,start,j);
            if (end > i)
                quickSort(num,i,end);
        }

    }
    public static void main(String[] args){
        int a[] = {32,43,23,13,5};
        int length = a.length-1;
        quickSort(a,0,length);
        int lengtha = a.length;
        for (int i = 0;i < lengtha; i++){
            System.out.println(a[i]);
        }
    }
}
