package algorithm.DP;

public class minPathSum {
    public static int minpathsum(int[][] a){
        int row = a.length;
        int col = a[0].length;
        int[][] dp = new int[row][col];
        dp[0][0] = a[0][0];
        for (int i=1;i<row;i++){
            dp[i][0] = dp[i-1][0] + a[i][0];
        }
        for (int j=1;j<col;j++){
            dp[0][j] = dp[0][j-1] + a[0][j];
        }
        for (int i=1;i<row;i++){
            for (int j=1;j<col;j++){
                dp[i][j] = Math.min(dp[i-1][j],dp[i][j-1])+a[i][j];
            }
        }
        return dp[row-1][col-1];
    }
    public static void main(String[] args){
        int[][] a = {
                {1,3,5,9},
                {8,1,3,4},
                {5,0,6,1},
                {8,8,4,0}
        };
        System.out.println(minpathsum(a));
    }
}
