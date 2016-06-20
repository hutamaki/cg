import java.util.Scanner;
import java.util.StringTokenizer;

class Solution {

    private static final int MAX_TEMP = 10000;
    private static final int NO_TEMP = 0;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(); // the number of temperatures to analyse
        in.nextLine();
        String temps = in.nextLine(); // the n temperatures expressed as integers ranging from -273 to 5526

        int res = MAX_TEMP;
        StringTokenizer st = new StringTokenizer(temps);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();

            int tmp = Integer.parseInt(tok);
            int abs_tmp = Math.abs(tmp);
            int abs_res = Math.abs(res);

            if (abs_tmp < abs_res) res = tmp;
            else if (abs_tmp == abs_res && res < tmp) res = tmp;
        }
        System.out.println(res == MAX_TEMP ? NO_TEMP : res);
    }
}