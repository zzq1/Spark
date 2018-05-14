package DesignPatterns.Singleton;

public class Singleton {

    private boolean empty;
    private boolean boiled;
    private static volatile Singleton uniqueIistence;


    private Singleton(){
        empty = true;
        boiled = false;
    }
    public static Singleton getUniqueIistence(){
        if (uniqueIistence == null){
            synchronized (Singleton.class){
                if (uniqueIistence == null){
                    uniqueIistence = new Singleton();
                }
            }
        }
        return uniqueIistence;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isBoiled() {
        return boiled;
    }

    public void fill(){
        if (isEmpty()){
            empty = false;
            boiled = false;
        }
    }

    public void drain(){
        if (!isEmpty() && isBoiled()){
            empty = true;
        }
    }

    public void boiled(){
        if(!isEmpty() && !isBoiled()){
            boiled = true;
        }
    }

    public static void main(String args[]){
        Singleton s1 = getUniqueIistence();
        Singleton s2 = getUniqueIistence();
        if(s1.equals(s2)){
            System.out.println("单例模式只有一个实例.");
        }else {
            System.out.println("单例模式例子失败.");
        }
    }


}
