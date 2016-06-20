import java.util.Scanner;

class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int L = in.nextInt();
        int H = in.nextInt();
        in.nextLine();
        String T = in.nextLine();

        String[] strTabs = new String[H];
        for (int i = 0; i < H; i++) {
            strTabs[i] = in.nextLine();
        }

        int strLen = T.length();
        for (int y = 0; y < H; y++) {
            String res = "";
            String ref = strTabs[y];
            for (int j = 0; j < strLen; j++) {
                int letter = Character.toLowerCase(T.charAt(j)) - 'a';
                if (letter < 0 || letter > 26) letter = 26;
                int idx = letter * L;
                res += ref.substring(idx, idx + L);
            }
            System.out.println(res);
        }
    }
}