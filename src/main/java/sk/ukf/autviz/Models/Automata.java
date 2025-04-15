package sk.ukf.autviz.Models;

import java.util.*;

import javafx.util.Pair;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class Automata {
    private Set<String> alphabet = new LinkedHashSet<String>();
    private Set<State> states = new LinkedHashSet<State>();
    private Set<Transition> transitions = new LinkedHashSet<Transition>();

    private final StringProperty alphabetProperty = new SimpleStringProperty();

    public Automata() {
        this.alphabet.clear();
        this.states.clear();
        this.transitions.clear();
    }

    private void updateAlphabetProperty() {
        String joined = String.join(", ", alphabet);
        alphabetProperty.set(joined);
    }

    public void updateAlphabet() {
        alphabet.clear();

        for (Transition t : transitions) {
            alphabet.addAll(t.getSymbols());
        }
        updateAlphabetProperty();
    }

    public void addState(State state) {
        this.states.add(state);
    }

    public void addTransition(Transition newTransition) {
        for (Transition existing : transitions) {
            if (existing.getStateSource().getName().equals(newTransition.getStateSource().getName()) &&
                    existing.getStateDestination().getName().equals(newTransition.getStateDestination().getName())) {

                for (String symbol : newTransition.getSymbols()) {
                    existing.addSymbol(symbol);
                }

                if (transitions.stream().anyMatch(t ->
                        t != existing &&
                                t.getStateSource().getName().equals(newTransition.getStateDestination().getName()) &&
                                t.getStateDestination().getName().equals(newTransition.getStateSource().getName())
                )) {
                    existing.setHasOpposite(true);
                    newTransition.setHasOpposite(true);
                }
                updateAlphabet();
                return;
            }

            if (existing.getStateSource().getName().equals(newTransition.getStateDestination().getName()) &&
                    existing.getStateDestination().getName().equals(newTransition.getStateSource().getName())) {
                existing.setHasOpposite(true);
                newTransition.setHasOpposite(true);
            }
        }

        transitions.add(newTransition);
        updateAlphabet();
    }

    public StringProperty alphabetProperty() {
        return alphabetProperty;
    }

    public Set<String> getAlphabet() {
        return alphabet;
    }

    public Set<State> getStates() {
        return states;
    }

    public Set<Transition> getTransitions() {
        return transitions;
    }

    public void removeState(State state) {
        for (Iterator<State> entry = this.getStates().iterator(); entry.hasNext();) {
            State states = entry.next();
            if (states.getName().equals(state.getName())) {
                entry.remove();
                updateAlphabet();
                return;
            }
        }
    }

    public void removeTransition(Transition transition) {
        boolean removed = false;
        Iterator<Transition> iterator = this.getTransitions().iterator();
        while (iterator.hasNext()) {
            Transition t = iterator.next();
            if (t.getStateSource().getName().equals(transition.getStateSource().getName()) &&
                    t.getCharacter().equals(transition.getCharacter()) &&
                    t.getStateDestination().getName().equals(transition.getStateDestination().getName())) {
                iterator.remove();
                removed = true;
                break;
            }
        }

        if (removed) {
            Set<Transition> remainingTransitions = this.getTransitions();
            for (Transition t : remainingTransitions) {
                t.updateOppositeStatus(remainingTransitions);
            }
            updateAlphabet();
        }
    }
    
    @Override
    public String toString() {
        StringBuilder automataSaveFile = new StringBuilder();
        automataSaveFile.append("A: ");
        for(String alphabet: this.getAlphabet()) {
            automataSaveFile.append(alphabet + "; ");
        }
        automataSaveFile.append(System.getProperty("line.separator"));
        automataSaveFile.append("S: ");
        for(State state: this.getStates()) {
            automataSaveFile.append(state.getName());
            if(state.isStateBegin()) automataSaveFile.append("-I");
            if(state.isStateEnd()) automataSaveFile.append("-F");
            automataSaveFile.append("; ");
        }
        automataSaveFile.append(System.getProperty("line.separator"));
        automataSaveFile.append("T: ");
        for(Transition transition: this.getTransitions()) {
            automataSaveFile.append(transition.getStateSource().getName() + "-" + transition.getCharacter() + "-" + transition.getStateDestination().getName() + "; ");
        }
        return automataSaveFile.toString();
    }

    public Map<String, Set<String>> getEpsilonReachabilityMap(){
        Map<String, Set<String>> reachabilityMap = new HashMap<>();
        for (Transition t : transitions.stream().filter(transition -> transition.getSymbols().contains("ε")).toList()){
            if (!reachabilityMap.containsKey(t.getStateSource().getName())) {
                reachabilityMap.put(t.getStateSource().getName(), new HashSet<>(Arrays.asList(t.getStateDestination().getName())));
            } else {
                reachabilityMap.get(t.getStateSource().getName()).add(t.getStateDestination().getName());
            }
        }
        boolean changed = true;
        while (changed){
            changed = false;

            //Copy Map
            Map<String, Set<String>> tmpMap = new HashMap<>();
            for (String k : reachabilityMap.keySet()){
                tmpMap.put(k, new HashSet<>(reachabilityMap.get(k)));
            }

            for (String k : tmpMap.keySet()){
                int s = reachabilityMap.get(k).size();
                for (String v : tmpMap.get(k)){
                    if (reachabilityMap.containsKey(v)){
                        reachabilityMap.get(k).addAll(reachabilityMap.get(v));
                    }
                }
                changed = changed || reachabilityMap.get(k).size() > s;
            }
        }
        return reachabilityMap;
    }

    public boolean checkDetermination(){
        if (transitions.stream().anyMatch(transition -> transition.getSymbols().contains("ε"))){
            return false;
        }
        List<String> t =  transitions.stream().map(transition -> transition.getSymbols().stream().map(ch -> transition.getStateSource().getName() + "-" + ch).toList()).flatMap(l -> l.stream()).toList();
        if (t.size() != t.stream().distinct().toList().size()){
            return false;
        }
        if (t.size() != alphabet.size()* states.size()){
            return false;
        }
        return true;
    }

    private Transition getTransitionFromSetByStartDestination(Set<Transition> transitionSet, State start, State destination){
        for (Transition transition : transitionSet){
            if (transition.getStateSource().getName().equals(start.getName()) && transition.getStateDestination().getName().equals(destination.getName())){
                return transition;
            }
        }
        return null;
    }

    private State getStateByNameFromStates(Set<State> stateSet, String name){
        for (State s : stateSet){
            if (s.getName().equals(name)){
                return s;
            }
        }
        return null;
    }

    public void determinize(){
        if (checkDetermination()){
            return;
        }

        Map<String, Set<String>> epsilonReachabilityMap =  getEpsilonReachabilityMap();
        List<State> initialStates = states.stream().filter(state -> state.isStateBegin()).toList();
        if (initialStates.isEmpty()){
            // No inital states
            return;
        }

        Set<State> newStates = new LinkedHashSet<State>();
        Set<Transition> newTransitions = new LinkedHashSet<Transition>();

        List<State> queue = new ArrayList<State>();

        StringBuilder initialStateName = new StringBuilder();
        List<State> initialReachableStates = new ArrayList<>(initialStates);
        for (State iState : initialStates){
            if (epsilonReachabilityMap.containsKey(iState.getName())){
                for (String name : epsilonReachabilityMap.get(iState.getName())){
                    initialReachableStates.add(states.stream().filter(state -> state.getName().equals(name)).findFirst().get());
                }
            }
        }

        if (initialReachableStates.size() == 1){
            State initState = new State(initialReachableStates.get(0).getName() + "|");
            initState.setStateBegin(initialReachableStates.get(0).isStateBegin());
            initState.setStateEnd(initialReachableStates.get(0).isStateEnd());
            newStates.add(initState);

            queue.add(initialReachableStates.get(0));
        } else {
            initialReachableStates = initialReachableStates.stream().sorted(Comparator.comparing(State::getName)).toList();

            boolean stateEnd = initialReachableStates.stream().anyMatch(State::isStateEnd);
            State initState = new State(String.join("", initialReachableStates.stream().map(State::getName).toList()) + "|");
            initState.setStateBegin(true);
            initState.setStateEnd(stateEnd);
            newStates.add(initState);

            initState = new State(String.join("#", initialReachableStates.stream().map(State::getName).toList()));
            initState.setStateBegin(true);
            initState.setStateEnd(stateEnd);
            queue.add(initState);
        }

        Set<String> seenStatesNames = new LinkedHashSet<>();
        Set<String> endStatesNamesSet = new LinkedHashSet<>();
        for (State s : states){
            if (s.isStateEnd()){
                endStatesNamesSet.add(s.getName());
            }
        }

        State hell = new State("PEKLO");
        hell.setStateBegin(false);
        hell.setStateEnd(false);

        while (!queue.isEmpty()){
            State currState = queue.remove(0);
            if (seenStatesNames.contains(currState.getName())) {
                continue;
            } else {
                seenStatesNames.add(currState.getName());
            }
            String s = currState.getName();
            String[] stateNames = s.split("#");
            for (String letter : alphabet){
                List<String> newStateNameList = new ArrayList<>();
                boolean newStateBegin = false;
                boolean newStateEnd = false;
                for (String stateName : stateNames) {
                    for (Transition t : transitions.stream().filter(transition -> transition.getStateSource().getName().equals(stateName) && transition.getSymbols().contains(letter)).toList()){
                        newStateNameList.add(t.getStateDestination().getName());
                        if (epsilonReachabilityMap.containsKey(t.getStateDestination().getName())){
                            newStateNameList.addAll(epsilonReachabilityMap.get(t.getStateDestination().getName()));

                            for (String str : epsilonReachabilityMap.get(t.getStateDestination().getName())){
                                if (endStatesNamesSet.contains(str)){
                                    newStateEnd = true;
                                }
                            }
                        }
                        newStateEnd = newStateEnd || t.getStateDestination().isStateEnd();
                    }
                }
                newStateNameList = newStateNameList.stream().distinct().sorted(Comparator.comparing(String::toString)).toList();
                String newStateName = String.join("#", newStateNameList);
                String newStateName2 = String.join("", newStateNameList) + "|";

                if (!newStateName2.equals("|")){
                    State newState = new State(newStateName);
                    newState.setStateBegin(newStateBegin);
                    newState.setStateEnd(newStateEnd);
                    queue.add(newState);

                    newState = new State(newStateName2);
                    newState.setStateBegin(newStateBegin);
                    newState.setStateEnd(newStateEnd);
                    List<State> tmpList = newStates.stream().filter(tmpState -> tmpState.getName().equals(newStateName2)).toList();
                    if (!tmpList.isEmpty()){
                        newState = tmpList.get(0);
                    }
                    newStates.add(newState);

                    State tmpCurr = getStateByNameFromStates(newStates, currState.getName().replace("#", "") + "|");

                    Transition tmpTransition = getTransitionFromSetByStartDestination(newTransitions, tmpCurr, newState);
                    if (tmpTransition == null){
                        newTransitions.add(new Transition(tmpCurr, letter, newState));
                    } else {
                        tmpTransition.addSymbol(letter);
                    }
                } else {
                    //Go to HELL
                    if (!newStates.contains(hell)){
                        newStates.add(hell);
                        //Loop to add all transitions for hell (loops)
                        Transition hellTransition = null;
                        for (String l : alphabet){
                            if (hellTransition == null){
                                hellTransition = new Transition(hell, l, hell);
                            } else {
                                hellTransition.addSymbol(l);
                            }
                        }
                        newTransitions.add(hellTransition);
                    }
                    State tmpCurr = getStateByNameFromStates(newStates, currState.getName().replace("#", "") + "|");
                    Transition tmpTransition = getTransitionFromSetByStartDestination(newTransitions, tmpCurr, hell);
                    if (tmpTransition == null){
                        newTransitions.add(new Transition(tmpCurr, letter, hell));
                    } else {
                        tmpTransition.addSymbol(letter);
                    }
                }
            }
        }
        states = newStates;
        // Premenovanie stavov na skratenie mien
        int c = 0;
        for (State s : states){
            if (s.getName().equals("PEKLO")){
                continue;
            }
            s.setStateName("q" + c);
            c++;
        }
        transitions = newTransitions;
        for (Transition t : transitions){
            t.updateOppositeStatus(transitions);
        }
    }

    public void reverse(){
        Set<Transition> newTransitions = new LinkedHashSet<Transition>();
        for (Transition t : transitions){
            Transition tmpTransition = null;
            for (String l : t.getSymbols()){
                if (tmpTransition == null){
                    tmpTransition = new Transition(t.getStateDestination(), l, t.getStateSource());
                } else {
                    tmpTransition.addSymbol(l);
                }
            }
            newTransitions.add(tmpTransition);
        }
        transitions = newTransitions;

        for (State s : states){
            if (s.isStateEnd() != s.isStateBegin()){
                if (s.isStateBegin()){
                    s.setStateBegin(false);
                    s.setStateEnd(true);
                } else {
                    s.setStateBegin(true);
                    s.setStateEnd(false);
                }
            }
        }
    }

    public void BrzozowskiMinimalize(){
        this.reverse();
        this.determinize();
        this.reverse();
        this.determinize();
    }

    public boolean isWordLegal(String word){
        List<State> initial = states.stream().filter(State::isStateBegin).toList();
        List<Set<State>> compute = new ArrayList<>();
        compute.add(new HashSet<>(initial));

        Map<String, Set<String>> epsilonReachabilityMap = getEpsilonReachabilityMap();

        for (State s : initial){
            if (epsilonReachabilityMap.containsKey(s.getName())){
                for (String str : epsilonReachabilityMap.get(s.getName())){
                    compute.get(compute.size() - 1).add(states.stream().filter(st -> st.getName().equals(str)).toList().get(0));
                }
            }
        }

        for (char letter : word.toCharArray()){
            Set<State> nextStates = new LinkedHashSet<>();
            for (State s : compute.get(compute.size() - 1)){
                nextStates.addAll(transitions.stream().filter(t -> t.getStateSource().getName().equals(s.getName()) && t.getSymbols().contains(String.valueOf(letter))).map(Transition::getStateDestination).toList());
            }

            Set<State> nextStatesCopy = new LinkedHashSet<>(nextStates);
            for (State s : nextStatesCopy){
                if (epsilonReachabilityMap.containsKey(s.getName())){
                    for (String str : epsilonReachabilityMap.get(s.getName())){
                        nextStates.add(states.stream().filter(st -> st.getName().equals(str)).toList().get(0));
                    }
                }
            }

            compute.add(nextStates);
        }
        return compute.get(compute.size() - 1).stream().anyMatch(State::isStateEnd);
    }

    private List<State> getWordPathRec(String word, State state, Set<String> epsilonTransitions){
        List<State> result = new ArrayList<>();
        result.add(state);
        if (word.isEmpty()){
            if (state.isStateEnd()){
                return result;
            }
        } else {
            String letter = String.valueOf(word.charAt(0));
            List<Transition> availableTransitions = transitions.stream().filter(t -> t.getStateSource().getName().equals(state.getName()) && t.getSymbols().contains(letter)).toList();
            for (Transition t : availableTransitions){
                List<State> tmpResult = getWordPathRec(word.substring(1), t.getStateDestination(), new LinkedHashSet<>());
                if (tmpResult != null){
                    result.addAll(tmpResult);
                    return result;
                }
            }
        }

        epsilonTransitions.add(state.getName());
        List<Transition> availableEpsilonTransitions = transitions.stream().filter(t -> t.getStateSource().getName().equals(state.getName()) && t.getSymbols().contains("ε")).toList();
        for (Transition t : availableEpsilonTransitions){
            if (epsilonTransitions.contains(t.getStateDestination().getName())){
                continue;
            }
            List<State> tmpResult = getWordPathRec(word, t.getStateDestination(), epsilonTransitions);
            if (tmpResult != null){
                result.addAll(tmpResult);
                return result;
            }
        }

        return null;
    }

    private Pair<List<State>, List<String>> getWholePathRec(String word, State state, Set<String> epsilonTransitions, List<State> currStateList, List<String> currWordList){
        Pair<List<State>, List<String>> result = new Pair<>(new ArrayList<>(), new ArrayList<>());
        currStateList.add(state);
        currWordList.add(word);
        boolean endOfJourney = true;

        if (!word.isEmpty()){
            String letter = String.valueOf(word.charAt(0));
            List<Transition> availableTransitions = transitions.stream().filter(t -> t.getStateSource().getName().equals(state.getName()) && t.getSymbols().contains(letter)).toList();
            for (Transition t : availableTransitions){
                endOfJourney = false;
                Pair<List<State>, List<String>> tmpResult = getWholePathRec(word.substring(1), t.getStateDestination(), new LinkedHashSet<>(), new ArrayList<>(currStateList), new ArrayList<>(currWordList));
                result.getKey().addAll(tmpResult.getKey());
                result.getValue().addAll(tmpResult.getValue());
            }
        }

        epsilonTransitions.add(state.getName());
        List<Transition> availableEpsilonTransitions = transitions.stream().filter(t -> t.getStateSource().getName().equals(state.getName()) && t.getSymbols().contains("ε")).toList();
        for (Transition t : availableEpsilonTransitions){
            if (epsilonTransitions.contains(t.getStateDestination().getName())){
                continue;
            }
            endOfJourney = false;
            Pair<List<State>, List<String>> tmpResult = getWholePathRec(word, t.getStateDestination(), epsilonTransitions, new ArrayList<>(currStateList), new ArrayList<>(currWordList));
            result.getKey().addAll(tmpResult.getKey());
            result.getValue().addAll(tmpResult.getValue());
        }
        if (endOfJourney){
            result = new Pair<>(new ArrayList<>(currStateList), new ArrayList<>(currWordList));
        }
        return result;
    }

    public Pair<List<State>, List<String>> getWordPath(String word){
        State initial = states.stream().filter(State::isStateBegin).toList().get(0);
        if (isWordLegal(word)){
            return new Pair<>(getWordPathRec(word, initial, new LinkedHashSet<>()), new ArrayList<>());
        }
        return getWholePathRec(word, initial, new LinkedHashSet<>(), new ArrayList<>(), new ArrayList<>());
    }
}