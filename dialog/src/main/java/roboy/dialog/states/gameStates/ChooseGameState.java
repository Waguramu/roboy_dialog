package roboy.dialog.states.gameStates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import roboy.dialog.states.definitions.State;
import roboy.dialog.states.definitions.StateParameters;
import roboy.linguistics.Linguistics;
import roboy.linguistics.sentenceanalysis.Interpretation;
import roboy.talk.PhraseCollection;
import roboy.talk.Verbalizer;
import roboy.util.RandomList;
import java.util.Arrays;
import java.util.List;

public class ChooseGameState extends State {

    private final static String TRANSITION_CHOSE_SNAPCHAT = "choseSnapchat";
    private final static String TRANSITION_CHOSE_20_Q = "chose20questions";
    private final static String TRANSITION_EXIT = "exitGame";

    private final static RandomList<String> EXISTING_GAMES = new RandomList<>(Arrays.asList("Snapchat", "Akinator"));

    private final Logger LOGGER = LogManager.getLogger();

    private String game = null;
    private String suggestedGame = null;

    public ChooseGameState(String stateIdentifier, StateParameters params) {
        super(stateIdentifier, params);
    }

    @Override
    public Output act() {
        suggestedGame = EXISTING_GAMES.getRandomElement();
        return Output.say(String.format(PhraseCollection.GAME_ASKING_PHRASES.getRandomElement(), suggestedGame));
    }

    @Override
    public Output react(Interpretation input) {

        Linguistics.UtteranceSentiment inputSentiment = getInference().inferSentiment(input);
        String inputGame = inferGame(input);

        if (inputSentiment == Linguistics.UtteranceSentiment.POSITIVE){
            game = suggestedGame;
            return Output.say(Verbalizer.startSomething.getRandomElement());
        } else if (inputGame != null){
            game = inputGame;
            return Output.say(Verbalizer.startSomething.getRandomElement());
        } else if (inputSentiment == Linguistics.UtteranceSentiment.NEGATIVE){
            game = "exit";
        }
        return Output.sayNothing();
    }

    @Override
    public State getNextState() {

        switch (game){
            case "Akinator":
                return getTransition(TRANSITION_CHOSE_20_Q);
            case "Snapchat":
                return getTransition(TRANSITION_CHOSE_SNAPCHAT);
            case "exit":
                return getTransition(TRANSITION_EXIT);
            default:
                return this;
        }

    }
    
    private String inferGame(Interpretation input){

        List<String> tokens = input.getTokens();
        game = null;
        if(tokens != null && !tokens.isEmpty()){
            if(tokens.contains("akinator") || tokens.contains("guessing") || tokens.contains("questions")){
                game = "Akinator";
            } else if (tokens.contains("snapchat") || tokens.contains("filters") || tokens.contains("filter") || tokens.contains("mask")){
                game = "Snapchat";
            }
        }
        return game;
    }
}
