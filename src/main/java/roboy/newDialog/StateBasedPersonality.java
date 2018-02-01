package roboy.newDialog;

import roboy.dialog.action.Action;
import roboy.dialog.action.FaceAction;
import roboy.dialog.personality.Personality;
import roboy.linguistics.Linguistics;
import roboy.linguistics.sentenceanalysis.Interpretation;
import roboy.newDialog.states.State;
import roboy.talk.Verbalizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of Personality based on a DialogStateMachine.
 *
 * In contrast to previous Personality implementations, this one is more generic
 * as it loads the dialog from a file. Additionally, it is still possible to
 * define the dialog structure directly from code (as it was done in previous
 * implementations).
 *
 * Instead of using nested states that will pass an utterance to each other if a state
 * cannot give an appropriate reaction, we use a fallback concept.
 * If a state doesn't know how to react, it simply doesn't react at all. If a fallback
 * (with is another state) is attached to it, the personality will pass the utterance
 * to the fallback automatically.
 * This concept helps to decouple the states and reduce the dependencies between them.
 */
public class StateBasedPersonality extends DialogStateMachine implements Personality {


    private final Verbalizer verbalizer;


    public StateBasedPersonality(Verbalizer verb) {
        verbalizer = verb;
        reset();
    }


    private void reset() {
        // go back to initial state
        setActiveState(getInitialState());
    }

    /**
     * Always called once by the (new) DialogSystem at the beginning of every new conversation.
     * @return list of actions based on act() of the initial/active state
     */
    public List<Action> startConversation() {
        State activeState = getActiveState();
        if (activeState == null) {
            System.out.println("[!!] ERROR: This personality state machine has not been initialized!");
            return new ArrayList<>();
        }
        if ( ! activeState.equals(getInitialState())) {
            System.out.println("[!!] WARNING: The active state is different from the initial state " +
                    "at the beginning of conversation!");
        }

        List<Action> initialActions = new ArrayList<>();
        initialActions = stateAct(activeState, initialActions);

        return initialActions;
    }


    @Override
    public List<Action> answer(Interpretation input) {

        State activeState = getActiveState();
        if (activeState == null) {
            System.out.println("[!!] ERROR: This personality state machine has not been initialized!");
            return new ArrayList<>();
        }
        List<Action> answerActions = new ArrayList<>();


        // SPECIAL NON-STATE BASED BEHAVIOUR
        // TODO: special treatment for profanity, farewell phrases etc.
        if (input.getFeatures().containsKey(Linguistics.EMOTION)) {
            // change facial expression based on input
            answerActions.add(new FaceAction((String) input.getFeatures().get(Linguistics.EMOTION)));
        }


        // ACTIVE STATE REACTS TO INPUT
        answerActions = stateReact(activeState, input, answerActions);


        // MOVE TO THE NEXT STATE
        State next = activeState.getNextState();
        if (next == null) {
            reset(); // go back to initial state
            return answerActions;
        }
        setActiveState(next);


        // NEXT STATE ACTS
        answerActions = stateAct(next, answerActions);

        return answerActions;
    }


    /**
     * Call the act function of the state, verbalize the interpretation (if any) and add it to the list of actions.
     * @param state state to call ACT on
     * @param previousActions list of previous action to append the verbalized result
     * @return updated list of actions
     */
    private List<Action> stateAct(State state, List<Action> previousActions) {
        State.Output act;
        try {
            act = state.act();
        } catch (Exception e) {
            return exceptionHandler(state, e, previousActions, true);
        }

        if (act == null) {
            System.out.println("[!!] WARNING: state acted with null. This should not happen! " +
                    "Return Output.sayNothing() instead!");
            return previousActions;  // say nothing, just return the list of previous actions

        }

        if (act.hasInterpretation()) {
            Interpretation inter = act.getInterpretation();
            previousActions.add(verbalizer.verbalize(inter));
        } else if (act.requiresFallback()) {
            System.out.println("[!!] WARNING: act() required fallback! Fallbacks are currently only allowed in react().");
        }
        // else: act.isEmpty()

        return previousActions;
    }


    /**
     * Call the react function of the state. If the state can't react, recursively ask fallbacks.
     * Verbalize the resulting reaction and add it to the list of actions.
     *
     * @param state state to call REact on
     * @param input input from the person Roboy speaks to
     * @param previousActions list of previous action to append the verbalized result
     * @return updated list of actions
     */
    private List<Action> stateReact(State state, Interpretation input, List<Action> previousActions) {

        State.Output react;
        try {
            react = state.react(input);
        } catch (Exception e) {
            return exceptionHandler(state, e, previousActions, false);
        }


        if (react == null) {
            System.out.println("[!!] WARNING: state reacted with null. This should not happen! " +
                    "It is not clear whether it wants to say nothing or ask the fallback.");
            return previousActions;  // say nothing, just return the list of previous actions
        }


        // first, resolve fallbacks if needed
        State fallback = state.getFallback();
        int fallbackCount = 0, maxFallbackCount = 1000;  // limit to prevent infinite loops

        while (react.requiresFallback()) {
            // limit fallback depth
            fallbackCount++;
            if (fallbackCount >= maxFallbackCount) {
                System.out.println("[!!] WARNING: possibly infinite fallback loop, stopping after " +
                        maxFallbackCount + " iterations");
                return previousActions;  // say nothing, just return the list of previous actions
            }

            if (fallback == null) {
                System.out.println("[!!] WARNING: state with identifier " + state.getIdentifier()
                        + " required fallback but none was attached, saying nothing");
                return previousActions;  // say nothing, just return the list of previous actions
            }

            // fallback exists
            state = fallback;
            fallback = state.getFallback();
            try {
                react = state.react(input);
            } catch (Exception e) {
                return exceptionHandler(state, e, previousActions, false);
            }

            if (react == null) {
                System.out.println("[!!] WARNING: state with identifier " + state.getIdentifier() +
                        " reacted with null. This should not happen!" +
                        " It is not clear whether it wants to say nothing or ask the fallback.");
                return previousActions;  // say nothing, just return the list of previous actions
            }
        }

        // verbalize only if there is a reply
        if (react.hasInterpretation()) {
            Interpretation inter = react.getInterpretation();
            previousActions.add(verbalizer.verbalize(inter));
        }

        // else: react.isEmpty()

        return previousActions;
    }

    private List<Action> exceptionHandler(State state, Exception e, List<Action> previousActions, boolean comesFromAct) {
        String actOrReact = comesFromAct ? "act" : "react";
        System.out.println("[!!] ERROR: Exception in " + actOrReact + "() of state with identifier "
                + state.getIdentifier() + ":\n" + e.getMessage());
        previousActions.add(verbalizer.verbalize(
                new Interpretation("Well, looks like some states are not implemented correctly...")));
        return previousActions;
    }

}