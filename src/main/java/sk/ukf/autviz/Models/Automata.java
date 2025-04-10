package sk.ukf.autviz.Models;

import java.util.*;

public class Automata {
    private Set<String> alphabet = new LinkedHashSet<String>();
    private Set<State> states = new LinkedHashSet<State>();
    private Set<Transition> transitions = new LinkedHashSet<Transition>();

    public Automata() {
        this.alphabet.clear();
        this.states.clear();
        this.transitions.clear();
    }

    public void addAlphabet(String character) {
        this.alphabet.add(character);
    }

    public void addState(State state) {
        this.states.add(state);
    }

    public void addTransition(Transition newTransition) {
        // nájsť prechod ak už existuje medzi source a destination
        for (Transition existing : transitions) {
            if (existing.getStateSource().getName().equals(newTransition.getStateSource().getName()) &&
                    existing.getStateDestination().getName().equals(newTransition.getStateDestination().getName())) {

                // transition už existuje pridame symbol do nej
                existing.addSymbol(newTransition.getCharacter());
                return;
            }
        }
        transitions.add(newTransition);
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
                return;
            }
        }
    }

    public void removeTransition(Transition transition) {
        for (Iterator<Transition> entry = this.getTransitions().iterator(); entry.hasNext();) {
            Transition transitions = entry.next();
            if (transitions.getStateSource().getName().equals(transition.getStateSource().getName()) &&
                    transitions.getCharacter().equals(transition.getCharacter()) &&
                    transitions.getStateDestination().getName().equals(transition.getStateDestination().getName())) {
                entry.remove();
                return;
            }
        }
    }
    
    /*
    public static Automata fromFile(String path) throws IOException {
        File file = new File(path);
        BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
        Automata automata = new Automata();
        while (fileBuffer.ready()) {
            String lineFile = fileBuffer.readLine().trim();
            if(!lineFile.isEmpty()) {
                if(lineFile.startsWith("A")) {
                    for (String simbolo : lineFile.substring(2).split(";")) {
                        automata.addAlphabet(simbolo.trim());
                    }
                } else if(lineFile.startsWith("E")) {
                    for (String states : lineFile.substring(2).split(";")) {
                        String[] stateArray = states.trim().split("-");
                        State state = new State(stateArray[0]);
                        for(int i = 1; i < stateArray.length; i++) {
                            if(stateArray[i].equals("I")) {
                                state.setStateBegin(Boolean.TRUE);
                            } else {
                                if(stateArray[i].equals("F")) {
                                    state.setStateEnd(Boolean.TRUE);
                                }
                            }
                        }
                        automata.addState(state);
                    }
                } else if(lineFile.startsWith("T")) {
                    for (String transition : lineFile.substring(2).split(";")) {
                        String transitionsLine[] = transition.split("-");
                        automata.addTransition(new Transition(new State(transitionsLine[0].trim()), transitionsLine[1].trim(), new State(transitionsLine[2].trim())));
                    }
                }
            }
        }
        fileBuffer.close();
        return automata;
    }
    */
    
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
        for (Transition t : transitions.stream().filter(transition -> transition.getCharacter().isEmpty()).toList()){
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

    private boolean checkDetermination(){
        if (transitions.stream().anyMatch(transition -> transition.getCharacter().isEmpty())){
            return false;
        }
        return transitions.stream().map(transition -> transition.getStateSource().toString() + "-" + transition.getCharacter()).distinct().toList().size() == transitions.size();
    }

    public void determinate(){
        if (checkDetermination()){
            return;
        }

        Map<String, Set<String>> epsilonReachabilityMap =  getEpsilonReachabilityMap();
        List<State> initialStates = states.stream().filter(state -> state.isStateBegin()).toList();
        if (initialStates.isEmpty()){ // No inital states
            return;
        }
        Set<State> newStates = new LinkedHashSet<State>();
        Set<Transition> newTransitions = new LinkedHashSet<Transition>();

        List<State> queue = new ArrayList<State>();

        if (initialStates.size() == 1) {
            State first = initialStates.iterator().next();
            if (!epsilonReachabilityMap.containsKey(first.getName())){
                newStates.add(first);
                queue.add(first);
            } else {
                List<State> tmpInitialStates = new ArrayList<>(initialStates); // initialStates is unmodifiable
                for (String name : epsilonReachabilityMap.get(first.getName())){
                    tmpInitialStates.add(states.stream().filter(state -> state.getName().equals(name)).findFirst().get());
                }
                initialStates = tmpInitialStates.stream().sorted(Comparator.comparing(State::getName)).toList();
                StringBuilder initStateName = new StringBuilder();
                boolean stateEnd = false;
                for (State st : initialStates) {
                    if (initStateName.isEmpty()){
                        initStateName.append(st.getName());
                    } else {
                        initStateName.append(",").append(st.getName());
                    }
                    stateEnd = stateEnd || st.isStateEnd();
                }
                State initState = new State(initStateName.toString());
                initState.setStateBegin(true);
                initState.setStateEnd(stateEnd);
                newStates.add(initState);
                queue.add(initState);
            }

        } else {
            return;
        }
        Set<State> seenStates = new LinkedHashSet<>();
        Set<String> endStatesNamesSet = new LinkedHashSet<>();
        for (State s : states){
            if (s.isStateEnd()){
                endStatesNamesSet.add(s.getName());
            }
        }

        while (!queue.isEmpty()){
            State currState = queue.remove(0);
            if (seenStates.contains(currState)) {
                continue;
            } else {
                seenStates.add(currState);
            }
            String s = currState.getName();
            String[] stateNames = s.split(",");
            for (String letter : alphabet){
                List<String> newStateNameList = new ArrayList<>();
                boolean newStateBegin = false;
                boolean newStateEnd = false;
                for (String stateName : stateNames) {
                    for (Transition t : transitions.stream().filter(transition -> transition.getStateSource().getName().equals(stateName) && transition.getCharacter().equals(letter)).toList()){
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
                StringBuilder newStateName = new StringBuilder();

                for (String sN : newStateNameList){
                    if (!newStateName.isEmpty()){
                        newStateName.append(",");
                    }
                    newStateName.append(sN);
                }

                if (!newStateName.toString().isEmpty()){
                    State newState = new State(newStateName.toString());
                    newState.setStateBegin(newStateBegin);
                    newState.setStateEnd(newStateEnd);

                    newStates.add(newState);
                    queue.add(newState);
                    newTransitions.add(new Transition(currState, letter, newState));
                }
            }
        }
        states = newStates;
        transitions = newTransitions;
    }
}