package game.logics.records;

import game.logics.entities.player.Player;
import game.logics.entities.player.Player.PlayerDeath;

import game.logics.handler.Logics.GameInfo;
import game.utility.input.JSONReader;
import game.utility.input.JSONReaderImpl;
import game.utility.input.JSONWriter;
import game.utility.input.JSONWriterImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class handles statistics and records, furthermore it supersedes the
 * reading and writing of them.
 */
public final class Records {

    private static final int MAX_NUMBER_OF_SAVED_RECORD = 3;

    private final Supplier<GameInfo> getGame;

    private final Player player;
    private final JSONWriter writer;
    private final JSONReader reader;

    // Statistics and records list
    private final List<Integer> scoreRecords = new ArrayList<>(Records.MAX_NUMBER_OF_SAVED_RECORD); // absolute new score records
    private final List<Integer> moneyRecords = new ArrayList<>(Records.MAX_NUMBER_OF_SAVED_RECORD); // absolute new money records

    private int burnedTimes;
    private int zappedTimes;
    private int savedMoney;

    // Data read from game
    private static int playingRecordScore; // higher score obtained by playing consecutively
    private static boolean newPlayingRecordScore;

    private boolean newScoreRecord;
    private boolean newMoneyRecord;

    /*{
        this.scoreRecords.addAll(Collections.nCopies(Records.MAX_NUMBER_OF_SAVED_RECORD, Optional.empty()));
    }*/

    /**
     * Build a new {@link Records}.
     * @param getGame {@link Supplier} of {@link GameInfo} objects used to get
     *   current game informations.
     * @param player {@link Player} object used to get some player informations.
     */
    public Records(final Supplier<GameInfo> getGame, final Player player) {
        this.getGame = getGame;

        this.writer = new JSONWriterImpl(this);
        this.reader = new JSONReaderImpl(this);

        //this.game.getNumbersOfGamesPlayed();
        this.player = player;
    }

    /****************************************/
    /***    In and to file operations     ***/
    /****************************************/

/*
    private boolean checkAndSet(final GameUID newGameUID) {

        final BiPredicate<GameUID, GameUID> checkIfNew =
                (oldUID,newUID) -> oldUID.getGameDate() != newUID.getGameDate();
        boolean isNewUID = false;

        if (!newGameUID.isGamePlayed()) {
            isNewUID = true;
        } else if(checkIfNew.test(UIDGame, newGameUID)) {
            isNewUID = true;
            Records.UIDGame = newGameUID;
        }
        return isNewUID;
    }*/

    /**
     * Declares game ended: sets game end date and gets final score.
     */
    public void fetchGameEnded() {

        final GameInfo newGameInfo = this.getGame.get();

        // if different --> new game
        //if (oldGameInfo.getUID() != newGameInfo.getUID()) {

        // Only if new gameUID (new game)
        //if (this.checkAndSet(gameUID)) {
            // Only if the game is not set as finished
            if (!newGameInfo.isGameEnded()) { 

                final int score = player.getCurrentScore();
                final int money = player.getCurrentCoinsCollected();

                newGameInfo.setGameEnded(score, money);
                //System.out.println(newGameInfo.getFinalScore());

                this.fetch(newGameInfo);
            }
            //oldGameInfo = newGameInfo;
        //}true if the new score is a new
    }

    /**
     * Get data for updating in game, calling the data getters.
     *
     * @param newGameInfo {@link GameInfo} instance containing
     * final game data
     */
    public void fetch(final GameInfo newGameInfo) {

        final PlayerDeath causeOfDeath;

        //System.out.println(newGameInfo.isGamePlayed());
        //newGameInfo.getGameDate().ifPresent(System.out::println);

        if (player.hasDied()) {
            causeOfDeath = player.getCauseOfDeath();
            switch (causeOfDeath) {
                case BURNED:
                    this.burnedTimes++;
                    break;
                case ZAPPED:
                    this.zappedTimes++;
                    break;
                default:
                    break;
            }
           this.savedMoney = this.savedMoney + newGameInfo.getFinalMoney();
           //this.checkScore(this.getScore());
           this.checkScore(newGameInfo.getFinalScore());
           this.checkMoney(newGameInfo.getFinalMoney());
           //this.getScore();
        }
    }

    /**
     * Read from file.
     */
    public void refresh() {
        this.reader.read();
    }

    /**
     * Write to file.
     */
    public void update() {
        this.writer.write();
    }

    /****************************************/
    /***   Calculate and check records    ***/
    /****************************************/

    /**
     * This method checks if the new finalScore is a new record and only in
     * this case saves it.
     *
     * @param finalScore
     *   final score in the current game
     */
    public void checkScore(final int finalScore) {
       // final BiPredicate<GameUID, GameUID> checkIfNew =
       //         (oldUID,newUID) -> oldUID.getGameDate() != newUID.getGameDate();
       // checkIfNew.test(UIDGame, newGameUID)

        if (finalScore > Records.playingRecordScore) {
            Records.newPlayingRecordScore = true;
            Records.playingRecordScore = finalScore;
        } else if (finalScore < Records.playingRecordScore) {
            Records.newPlayingRecordScore = false;
        }

        this.newScoreRecord = this.scoreRecords.isEmpty() || finalScore > this.getHighestRecordScore();

        if (this.newScoreRecord || finalScore < this.getHighestRecordScore()
                && finalScore > this.getLowestRecordScore()
                || this.scoreRecords.size() < Records.getMaxSavedNumberOfRecords()) {
            this.addScoreRecord(finalScore);
        }
    }

    /**
     * This method checks if the new finalCoins is a new record and only in
     * this case saves it.
     *
     * @param finalCoinsCollected
     *   final number of coins collected in the current game
     */
    public void checkMoney(final int finalCoinsCollected) {

        if (this.moneyRecords.size() < Records.getMaxSavedNumberOfRecords()
                || finalCoinsCollected > this.getLowestMoneyRecord()) {
            this.newMoneyRecord = true;
            this.addMoneyRecord(finalCoinsCollected);
        } else {
            this.newMoneyRecord = false;
        }
    }

    /****************************************/
    /*** Getters & Setters from / to file ***/
    /****************************************/

    /**
     * This static method is used to get the constant value stored as
     * {@link Records#MAX_NUMBER_OF_SAVED_RECORD}.
     *
     * @return the number of records that will be written to file.
     */
    public static int getMaxSavedNumberOfRecords() {
        return Records.MAX_NUMBER_OF_SAVED_RECORD;
    }

    /**
     * This method is used to get burnedTimes value.
     *
     * @return how many times Barry died burned.
     */
    public int getBurnedTimes() {
        return this.burnedTimes;
    }

    /**
     * This method is used to get zappedTimes value.
     *
     * @return how many times Barry died electrocuted.
     */
    public int getZappedTimes() {
        return this.zappedTimes;
    }

    /**
     * This method is used to get how much savedMoney the player owns.
     *
     * @return how many money the player has saved.
     */
    public int getSavedMoney() {
        return this.savedMoney;
    }

    /**
     * This method is used to set burnedTimes value.
     *
     * @param readBurnedTimes how many times Barry died burned.
     */
    public void setBurnedTimes(final int readBurnedTimes) {
        this.burnedTimes = readBurnedTimes;
    }

    /**
     * This method is used to set zappedTimes value.
     *
     * @param readZappedTimes how many times Barry died electrocuted.
     */
    public void setZappedTimes(final int readZappedTimes) {
        this.zappedTimes = readZappedTimes;
    }

    /**
     * This method is used to set savedMoney value.
     *
     * @param savedMoney how much money the player has saved.
     */
    public void setSavedMoney(final int savedMoney) {
        this.savedMoney = savedMoney;
    }

    /**
     * This method adds a score record entry.
     *
     * @param newScoreRecord the new score record
     */
    public void addScoreRecord(final int newScoreRecord) {
        //this.recordScores.forEach(System.out::println);

        this.scoreRecords.add(newScoreRecord);
        this.scoreRecords.sort(Comparator.reverseOrder());

        if (this.scoreRecords.size() > Records.getMaxSavedNumberOfRecords()) {
            //System.out.println("ELIMINATO: "
            //    + this.scoreRecords.get(Records.getSavedNumberOfRecords()).toString());
            this.scoreRecords.remove(Records.getMaxSavedNumberOfRecords());
        }
    }

    /**
     * This method gets the score record list.
     * 
     * This method can be used to write the list to file.
     * 
     * @return the score record list
     */
    public List<Integer> getScoreRecords() {
        /*final List<Integer> recordScores = new ArrayList<>();

        for (final Optional<Integer> record : this.recordScores) {
            if (record.isPresent()) {
                recordScores.add(record.get());
            }
        }*/
        return List.copyOf(this.scoreRecords);
    }

    /**
     * This method sets the score record list.
     * 
     * This method is used to read that list from file.
     * 
     * @param recordScores the new score record list
     */
    public void setScoreRecords(final List<Integer> recordScores) {
        this.scoreRecords.clear();
        this.scoreRecords.addAll(recordScores);

        /*for (final Integer record : recordScores) {
                this.scoreRecords.add(Optional.of(record));
        }
        if (this.scoreRecords.size() < Records.getMaxSavedNumberOfRecords()) {
            this.scoreRecords.addAll(Collections.nCopies(
                    recordScores.size() - Records.NUMBER_OF_SAVED_RECORD,
                    Optional.empty()));
        }*/
    }

    /**
     * This method adds a money record entry.
     *
     * @param newMoneyRecord the new money record
     */
    public void addMoneyRecord(final int newMoneyRecord) {

        this.moneyRecords.add(newMoneyRecord);
        this.moneyRecords.sort(Comparator.reverseOrder());

        if (this.moneyRecords.size() > Records.getMaxSavedNumberOfRecords()) {
            //System.out.println("ELIMINATO: "
            //    + this.moneyRecords.get(Records.getMaxSavedNumberOfRecords()).toString());
            this.moneyRecords.remove(Records.getMaxSavedNumberOfRecords());
        }
    }

    /**
     * This method gets the money record list.
     * 
     * This method can be used to write the list to file.
     * 
     * @return the money record list
     */
    public List<Integer> getMoneyRecords() {
        return List.copyOf(this.moneyRecords);
    }

    /**
     * This method sets the money record list.
     * 
     * This method is used to read that list from file.
     * 
     * @param recordCoins the new money record list
     */
    public void setMoneyRecords(final List<Integer> recordCoins) {
        this.moneyRecords.clear();
        this.moneyRecords.addAll(recordCoins);
    }

    /****************************************/
    /*** Getters & Setters from / to game ***/
    /****************************************/

    /**
     * Get player score form {@link game.logics.handler.Logics.GameInfo GameInfo} instance.
     * @return the player score
     */
    public int getScore() {
        return this.getGame.get().getFinalScore();
    }

    /**
     * Get current highest score obtained by player.
     * @return the first element of the highest scores list
     */
    public int getHighestRecordScore() {
        if (this.scoreRecords.isEmpty()) {
            return 0;
        } else {
            return this.scoreRecords.get(0);
        }
    }

    /**
     * Get current least score obtained by player.
     * @return the last element of the highest scores list
     */
    private int getLowestRecordScore() {
        if (this.scoreRecords.isEmpty()) {
            return 0;
        } else {
            return this.scoreRecords.stream().sorted().findFirst().get();
            //return this.scoreRecords.get(this.recordScores.size() - 1);
        }
    }

    /**
     * Get if the new score is a new highest record.
     * @return true if the new score is a new highest record.
     */
    public boolean isNewScoreRecord() {
        return this.newScoreRecord;
    }

    /**
     * Get the playing consecutively record score.
     * @return the playing record score
     */
    public int getPlayingScoreRecord() {
        return Records.playingRecordScore;
    }

    /**
     * Get if the new score is a new playing consecutively record.
     * @return true if the new score is a new playing consecutively record.
     */
    public boolean isNewPlayingScoreRecord() {
        return Records.newPlayingRecordScore;
    }

    /**
     * Get coins collected by player given from {@link game.logics.handler.Logics.GameInfo GameInfo} instance.
     * @return player score
     */
    public int getCollectedMoney() {
        return this.getGame.get().getFinalMoney();
    }

    /**
     * Get current highest score obtained by player.
     * @return first element of the highest scores list
     */
    public Integer getHighestMoneyRecord() {
        return this.moneyRecords.get(0);
    }

    /**
     * Get current least score obtained by player.
     * @return last element of the highest scores list
     */
    private Integer getLowestMoneyRecord() {
        if (this.moneyRecords.isEmpty()) {
            return 0;
        } else {
            return this.moneyRecords.stream().sorted().findFirst().get();
        }
    }

    /**
     * Get if the new number of coins collected is a new highest record.
     * @return true if the new number of coins collected is a new highest record.
     */
    public boolean isNewMoneyRecord() {
        return this.newMoneyRecord;
    }

    /**
     * Orders the deletion of the record file.
     */
    public void clear() {
        this.writer.clear();
    }
}
