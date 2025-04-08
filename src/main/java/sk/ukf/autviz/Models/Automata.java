package sk.ukf.autviz.Models;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

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

    public void addTransition(Transition transition) {
        this.transitions.add(transition);
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
        automataSaveFile.append("E: ");
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

}