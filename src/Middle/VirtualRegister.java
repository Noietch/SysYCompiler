package Middle;

public class VirtualRegister {
    public static int num = 0;

    public static String getRegister() {
        int res = num;
        num++;
        return Integer.toString(res);
    }

    public static void setZero() {
        num = 0;
    }
}
