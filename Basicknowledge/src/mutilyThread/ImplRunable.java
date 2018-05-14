package mutilyThread;

public class ImplRunable {
    public static void main(String[] args){
        System.out.println("主线程ID"+Thread.currentThread().getId());
        MyRunable mr = new MyRunable();
        Thread t = new Thread(mr);
        t.start();

    }
    static class  MyRunable implements Runnable{
        @Override
        public void run(){
            System.out.println("子线程ID"+Thread.currentThread().getId());
        }
    }
}
