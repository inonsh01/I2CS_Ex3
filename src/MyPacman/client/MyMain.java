package MyPacman.client;

import MyPacman.server.MyPacmanGame;
import MyPacman.server.MyGame;

public class MyMain {
    private static Character _cmd;

    public static void main(String[] args) {
        play();
    }
    public static void play() {
        MyPacmanGame game = new MyGame();

        game.init();
        MyAlgo man = new MyAlgo();

        while(game.getStatus() != MyGame.DONE) {
            _cmd = game.getKeyChar();
            if(_cmd !=null && _cmd == ' ') {
                game.play();
            }
            if (_cmd != null && _cmd == 'h') {
                System.out.println("Pacman help: keys: ' '-start, 'w,a,s,d'-directions, all other parameters should be configured via common.GameInfo.java, ");
            }
            int dir = man.move(game);
            if(dir == -1) continue;
            game.move(dir);
        }
    }
}
