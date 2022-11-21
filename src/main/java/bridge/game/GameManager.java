package bridge.game;

import bridge.BridgeMaker;
import bridge.BridgeRandomNumberGenerator;
import bridge.user.User;
import bridge.view.InputView;
import bridge.view.OutputView;

import java.util.List;

/**
 * 게임을 진행하는 역할 (사용자와 다리 건너기 게임을 연결하는 클래스)
 * 1. 사용자의 상태를 변경하기 위하여 User 메소드 호출
 * 2. 다리 건너기 게임을 위하여 BridgeMaker 메소드 호출
 * 3. 사용자에게서 입력을 받기 위하여 InputViewer 메소드 호출
 * 4. 사용자의 콘솔 화면에 내용을 출력하기 위하여 OutputViewer 메소드 호출
 */
public class GameManager {

    private static User player;
    private static BridgeGame bridgeGame;

    /**
     * 다리 생성 게임, 사용자 생성
     */
    public GameManager() {
        bridgeGame = new BridgeGame();
        player = new User(true, User.GameStatus.NONE.getStatusNumber(), 1);
    }

    /**
     * 게임 초기화
     */
    public void startBridgeGame() {
        OutputView.printGameStartMessage();
        int bridgeSize = -1;
        while (bridgeSize == -1) bridgeSize = setBridgeSize();
        makeBridge(bridgeSize);
    }

    private int setBridgeSize() {
        OutputView.askBridgeSize();
        int bridgeSize = -1;
        try {
            bridgeSize = InputView.readBridgeSize();
        } catch (IllegalArgumentException exception) {
            OutputView.printErrorMessage_bridgeSize();
        }
        return bridgeSize;
    }

    private void makeBridge(int bridgeSize) {
        BridgeMaker bridgeMaker = new BridgeMaker(new BridgeRandomNumberGenerator());
        List<String> bridgeAnswer = bridgeMaker.makeBridge(bridgeSize);
        bridgeGame.setBridgeAnswer(bridgeAnswer);
    }

    /**
     * 게임 진행
     */
    public void playBridgeGame() {
        while (player.isPlayingGame()) {
            moveUser();
            printBridge_userPredict();
            if (isEndOfTheGame()) break;
            if (player.getUserGameStatus() != User.GameStatus.NONE.getStatusNumber() && player.isPlayingGame())
                retryGame();
        }
        printGameResult();
    }

    // 칸 이동
    private void moveUser() {
        String userMoveDirection = "";
        while (userMoveDirection.compareTo("") == 0) userMoveDirection = askUserMoveDirection();
        bridgeGame.move(userMoveDirection);
        player.increaseNumberOfMoves();
    }

    private String askUserMoveDirection() {
        OutputView.askUserMoveDirection();
        String userMoveDirection = "";
        try {
            userMoveDirection = InputView.readMoving();
        } catch (IllegalArgumentException exception) {
            OutputView.printErrorMessage_userMoveDirection();
        }
        return userMoveDirection;
    }

    // 현재까지 건넌 다리 출력
    private void printBridge_userPredict() {
        OutputView.printMap(bridgeGame.getBridge_answer(), bridgeGame.getBridge_userMove());
    }

    // 게임 종료 여부 확인
    private boolean isEndOfTheGame() {
        if (isGameSucceed()) return true;
        return isGameFailed() && isQuitGame();
    }

    // 성공 여부 확인
    private boolean isGameSucceed() {
        int userNumberOfMoves = player.getNumberOfMoves();
        boolean isGameSucceed = bridgeGame.checkIfGameIsSucceed(userNumberOfMoves);
        if (isGameSucceed) {
            player.setUserGameStatus_succeed();
            player.setNotPlayingGame();
        }
        return isGameSucceed;
    }

    // 실패 여부 확인
    private boolean isGameFailed() {
        int userNumberOfMoves = player.getNumberOfMoves();
        return bridgeGame.checkIfGameIsFailed(userNumberOfMoves);
    }

    // 게임 종료 입력 여부 확인
    private boolean isQuitGame() {
        String userGameCommand = "";
        while (userGameCommand.compareTo("") == 0) userGameCommand = askGameCommand();
        if (userGameCommand.compareTo(User.GameCommand.QUIT.getCommand()) == 0) {
            player.setUserGameStatus_failed();
            player.setNotPlayingGame();
            return true;
        }
        return false;
    }

    private String askGameCommand() {
        OutputView.askGameCommand();
        String gameCommand = "";
        try {
            gameCommand = InputView.readGameCommand();
        } catch (IllegalStateException exception) {
            OutputView.printErrorMessage_gameCommand();
        }
        return gameCommand;
    }

    private void printGameResult() {
        OutputView.printResult(player, bridgeGame.getBridge_answer(), bridgeGame.getBridge_userMove());
    }

    // 게임 재시작
    private void retryGame() {
        bridgeGame.retry();
        player.increaseNumberOfGameTrials();
        player.resetNumberOfMoves();
    }

}
