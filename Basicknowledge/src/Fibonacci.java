public class Fibonacci {
    public static int Fibonacci(int num){
        if (num == 0) {return 0;}
        if (num == 1) {return 1;}
        return Fibonacci(num-1) + Fibonacci(num-2);
    }
    public static void main(String args[]){
        System.out.println(Fibonacci(4) - Fibonacci(3));
    }
}
