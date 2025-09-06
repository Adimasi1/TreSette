# TreSette (JTresette)

A desktop implementation of the Italian trick‑taking card game Tresette with a Swing GUI. Play the classic 4‑player (2v2) variant against AI opponents with adjustable difficulty. The app features partner signals (Busso, Volo, Liscio), persistent user profiles with stats, and a clean, modular MVC architecture.

## Highlights

This project implements 4‑player (2v2) Tresette with the traditional 40‑card Italian deck, including partner signals (Busso, Volo, Liscio). AI opponents use heuristics modulated by difficulty, while user profiles persist wins, losses, and win rate locally. The Swing UI provides a splash screen, pause overlay, and score popups. The codebase embraces clear Model–View–Controller separation with snapshot‑based updates and is built with Maven to run on Java 17 or newer.


## Rules at a glance (2v2)

A 40‑card deck in four suits (Denari/Coins, Coppe/Cups, Bastoni/Clubs, Spade/Swords) is used. Within each suit the rank from strongest to weakest is 3, 2, Asso (Ace), Re (King), Cavallo (Knight), Fante (Jack), 7, 6, 5, 4. Scoring per hand assigns 1 point to each Ace and 1/3 to 2, 3, and face cards, with other cards scoring 0; points are rounded to whole numbers at the end of the hand, a bonus is awarded for the last trick, and a shutout (“cappotto”) triggers special scoring. Players must follow suit (“palo”), if unable, they may discard but cannot win the trick. When leading in pairs, you may signal: Busso asks your partner to play their highest in the led suit and implies continuing that suit, Volo declares you are out of the suit, and Liscio indicates you still hold more cards in that suit.

## Architecture overview

The design follows MVC with explicit boundaries. The Model contains the core engine for players, hands, deals and tricks, rules, and scoring, mananged by 'GameManager' and 'Deal'. The Controller, ''GameController', translates model events into view events and exposes user commands. The View is built with Swing components for the frame, menus, board, hand panels, and overlays. Event flow relies on Java’s Observable/Observer (it was a non-optional specification request) from the  to decouple layers: the model publishes immutable 'ModelEvents' that carry a full 'DealSnapshot' of the hand state, which the controller adapts into 'ViewEvent's consumed by the UI. Profiles adopt a Repository + Service split ('ProfileRepository', 'ProfileService') with an adapter ('ProfilesAdapter') as the UI boundary.

## Build and run

Requirements: Java 17+ and Maven 3.9+. From the project root you can package the app, run the built JAR, or run directly via the exec plugin. 
The main class is 'main.JTresette'.

## What’s intentionally out of scope (for now)

The current version does not include the optional “Accusa” rule, the 2‑player (“a spizzico”) and 3‑player variants, or deeper bot understanding of received signals (an entry point is already in place).

## Acknowledgments

Parts of the grammar review and some Javadoc generation were assisted by an AI tool. All design and code decisions were authored and verified by the project owner.
