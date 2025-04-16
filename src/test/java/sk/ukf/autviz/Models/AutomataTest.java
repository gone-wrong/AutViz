package sk.ukf.autviz.Models;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutomataTest {
    @Test
    void testAutomataMinimize() {
        Automata minimized = buildOriginal1();
        Automata expected = buildExpected1();

        minimized.BrzozowskiMinimalize();
        assertNotNull(minimized);
        assertStates(expected.getStates(), minimized.getStates());
        assertTransitions(expected.getTransitions(), minimized.getTransitions());
    }

    @Test
    void testAutomataDeterminize() {
        Automata determinize = buildOriginal2();
        Automata expected = buildExpected2();

        determinize.determinize();
        assertNotNull(determinize);
        assertStates(expected.getStates(), determinize.getStates());
        assertTransitions(expected.getTransitions(), determinize.getTransitions());
    }

    private void assertStates(Set<State> expected, Set<State> actual) {
        assertEquals(expected.size(), actual.size());
        for (State exp : expected) {
            State act = actual.stream()
                    .filter(s -> s.getName().equals(exp.getName()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(act, "Chýbajuci stav: " + exp.getName());
            assertEquals(exp.isStateBegin(), act.isStateBegin(),
                    "Chyba v isBegin " + exp.getName());
            assertEquals(exp.isStateEnd(), act.isStateEnd(),
                    "Chyba v isEnd " + exp.getName());
        }
    }

    private void assertTransitions(Set<Transition> expected, Set<Transition> actual) {
        Set<String> expKeys = expected.stream()
                .map(t -> keyOf(t))
                .collect(Collectors.toSet());
        Set<String> actKeys = actual.stream()
                .map(t -> keyOf(t))
                .collect(Collectors.toSet());

        assertEquals(expKeys, actKeys, () ->
                "Chyba v prechodoch\nOčakávali sme: " + expKeys + "\nDostali sme:   " + actKeys
        );
    }

    private String keyOf(Transition t) {
        return t.getStateSource().getName()
                + ":" + t.getSymbols()
                + "->" + t.getStateDestination().getName();
    }

    private Automata buildOriginal1() {
        Automata a = new Automata();
        State s0 = new State("s0"), s1 = new State("s1"), s2 = new State("s2"),
                s3 = new State("s3"), s4 = new State("s4");
        s0.setStateBegin(true);
        s0.setStateEnd(true);
        s2.setStateEnd(true);
        s4.setStateEnd(true);
        a.addState(s0);
        a.addState(s1);
        a.addState(s2);
        a.addState(s3);
        a.addState(s4);
        a.addTransition(new Transition(s0, "a", s0));
        a.addTransition(new Transition(s0, "b", s1));
        a.addTransition(new Transition(s1, "b", s3));
        a.addTransition(new Transition(s1, "a", s2));
        a.addTransition(new Transition(s2, "a", s2));
        a.addTransition(new Transition(s2, "b", s4));
        a.addTransition(new Transition(s3, "a", s2));
        a.addTransition(new Transition(s3, "b", s3));
        a.addTransition(new Transition(s4, "a,b", s4));
        return a;
    }

    private Automata buildOriginal2() {
        Automata a = new Automata();
        State q0 = new State("q0"), q1 = new State("q1"), q2 = new State("q2");
        q0.setStateBegin(true);
        q2.setStateEnd(true);

        a.addState(q0);
        a.addState(q1);
        a.addState(q2);

        a.addTransition(new Transition(q0, "a", q0));
        a.addTransition(new Transition(q0, "a", q1));
        a.addTransition(new Transition(q0, "b", q2));
        a.addTransition(new Transition(q1, "b", q2));

        return a;
    }

    private Automata buildExpected1() {
        Automata r = new Automata();
        State q0 = new State("q0"), q1 = new State("q1"), q2 = new State("q2");
        q0.setStateBegin(true);
        q0.setStateEnd(true);
        q2.setStateEnd(true);
        r.addState(q0);
        r.addState(q1);
        r.addState(q2);
        r.addTransition(new Transition(q0, "a", q0));
        r.addTransition(new Transition(q0, "b", q1));
        r.addTransition(new Transition(q1, "a", q2));
        r.addTransition(new Transition(q1, "b", q1));
        r.addTransition(new Transition(q2, "a", q2));
        r.addTransition(new Transition(q2, "b", q2));
        return r;
    }

    private Automata buildExpected2() {
        Automata automata = new Automata();

        State q0 = new State("q0"), q1 = new State("q1"),
                q2 = new State("q2"), peklo = new State("PEKLO");

        q0.setStateBegin(true);
        q2.setStateEnd(true);

        automata.addState(q0);
        automata.addState(q1);
        automata.addState(q2);
        automata.addState(peklo);

        automata.addTransition(new Transition(q0, "a", q1));
        automata.addTransition(new Transition(q0, "b", q2));
        automata.addTransition(new Transition(q1, "a", q1));
        automata.addTransition(new Transition(q1, "b", q2));
        automata.addTransition(new Transition(q2, "a", peklo));
        automata.addTransition(new Transition(q2, "b", peklo));

        automata.addTransition(new Transition(peklo, "a", peklo));
        automata.addTransition(new Transition(peklo, "b", peklo));

        return automata;
    }
}