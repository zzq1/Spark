import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

class Pair implements Comparable<Pair>{
    int x;
    int y;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Pair o) {
        return x==o.x ? o.y - y : x - o.x ; //X升序Y降序
    }
}

public class Main
{
    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        Pair[] arr = new Pair[n];
        for ( int i = 0; i < n; i++ ) {
            int x = in.nextInt();
            int y = in.nextInt();
            arr[i] = new Pair(x, y);
        }
        Arrays.sort(arr);
        List<Pair> res = new ArrayList<>();
        for ( int i = 0; i < n; i++ ) {
            boolean f = true;
            for ( int j = 0; i!=j && j < n; j++ ) {
                if(arr[i].x < arr[j].x && arr[i].y < arr[j].y) {
                    f = false;
                    break;
                }
            }
            if(f) res.add(arr[i]);
        }
        res.forEach((Pair o)->System.out.println(o.x + " " + o.y));
        in.close();
    }
}
