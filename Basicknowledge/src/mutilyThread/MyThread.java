package mutilyThread;

class ThreadTest extends Thread{
    public String name;
    public ThreadTest(String name){
        this.name = name;
    }
    @Override
    public void run(){
        System.out.println("name"+name+"当前线程ID"+Thread.currentThread().getId());
    }

}
public class MyThread{
    public static void main(String[] args){
        ThreadTest t1 = new ThreadTest("t1");
        t1.start();
        ThreadTest t2 = new ThreadTest("t2");
        t2.run();
    }
}
