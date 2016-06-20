import java.util.Scanner;

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int road = in.nextInt(); // the length of the road before the gap.
        int gap = in.nextInt(); // the length of the gap.
        int platform = in.nextInt(); // the length of the landing platform.

        int before_gap = road;
        // game loop
        while (true) {
            int speed = in.nextInt(); // the motorbike's speed.
            int coordX = in.nextInt(); // the position on the road of the motorbike.

            before_gap -= speed;

            if ((coordX >= (road + gap) && speed > 0) || (speed > (gap + 1))) System.out.println("SLOW");
            else if (speed >= before_gap) System.out.println("JUMP");
            else if (speed < (gap + 1)) System.out.println("SPEED");
            else System.out.println("WAIT");
        }
    }
}