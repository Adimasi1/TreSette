package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Gives random fantasy names to bots. Avoid repeats until pool is exhausted
 */
public final class FantasyNameProvider {
    private static final List<String> NAME_LIST_BASE = List.of(
        // 100 Italian (mostly traditional / historic) given names
        "Giovanni","Giuseppe","Antonio","Francesco","Luigi","Pietro","Paolo","Marco","Matteo","Carlo",
        "Domenico","Vincenzo","Salvatore","Raffaele","Rosario","Pasquale","Gennaro","Umberto","Aldo","Enrico",
        "Ernesto","Alberto","Bruno","Sergio","Franco","Mario","Vittorio","Ettore","Tullio","Italo",
        "Dino","Silvio","Giulio","Cesare","Lorenzo","Ottavio","Teodoro","Eugenio","Arturo","Renato",
        "Adolfo","Carmine","Michele","Angelo","Gaetano","Nicola","Ferdinando","Raimondo","Riccardo","Ruggero",
        "Corrado","Marino","Orlando","Armando","Albino","Celestino","Basilio","Benedetto","Clemente","Emanuele",
        "Fortunato","Gregorio","Leandro","Mariano","Nazario","Orazio","Placido","Quirino","Remo","Savino",
        "Tiberio","Valentino","Zaccaria","Bartolomeo","Eusebio","Fiorenzo","Geminiano","Ippolito","Lazzaro","Nerio",
        "Onofrio","Pantaleone","Rinaldo","Secondo","Tito","Ugolino","Virgilio","Zeno","Adelmo","Giacomo",
        "Jacopo","Taddeo","Bortolo","Elia","Isidoro","Sabino","Alvaro","Gualtiero","Costanzo", "Lucifero",
        "Leone", "Mefistofele", "Abelardo", "Adalgisa", "Augusto", "Abramo", "Eva", "Flaminia", "Terenzia", 
        "Ciro", "Gennaro", "Maria", "Giuda", "Maddalena", "Miriam", "Ponzio"
    );

    private static final List<String> pool = new ArrayList<>(NAME_LIST_BASE);
    private static final Random ran = new Random();
    private static final Set<String> RESERVED = new HashSet<>();

    // no static shuffle needed: selection is random on each call to next()

    private FantasyNameProvider(){}

    /**
     * Reserve a name so it will not be returned.
     * If the name is present in the pool it will be removed.
     */
    public static synchronized void reserve(String name){ 
        if (name != null) {
            RESERVED.add(name);
            pool.remove(name);
        }
    }

    /**
     * Return a name chosen at random from the available pool and mark it as used
     * Once a name is returned it is removed from the pool.
     */
    public static synchronized String next(){
        if (pool.isEmpty()) {
            return "Bot" + (100 + ran.nextInt(900));
        }
        int index = ran.nextInt(pool.size());
        String candidate = pool.remove(index);
        RESERVED.add(candidate);
        return candidate;
    }
}
