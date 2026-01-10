package com.example.flightlab;

import java.util.ArrayList;
import java.util.List;

public class QuestionDataBase {

    public static List<Question> getQuestions() {
        List<Question> list = new ArrayList<>();

        //AERODYNAMIKA I FIZYKA
        list.add(new Question("Co powoduje wzrost siły nośnej skrzydeł?", "Zwiększenie prędkości lotu", "Zmniejszenie kąta natarcia", "Zwiększenie masy samolotu", "Wyłączenie silnika", 1, null, null));
        list.add(new Question("Która siła przeciwdziała sile ciągu?", "Grawitacja", "Siła nośna", "Opór aerodynamiczny", "Bezwładność", 3, null, null));
        list.add(new Question("Czym jest przeciągnięcie (Stall)?", "Nagłym zgaśnięciem silnika", "Utratą siły nośnej przez zbyt duży kąt natarcia", "Lotem z prędkością naddźwiękową", "Awarią podwozia", 2, null, null));
        list.add(new Question("Co się dzieje z ciśnieniem powietrza wraz ze wzrostem wysokości?", "Rośnie", "Maleje", "Pozostaje bez zmian", "Waha się losowo", 2, null, null));
        list.add(new Question("Jak nazywa się obrót samolotu wokół osi podłużnej?", "Pochylenie (Pitch)", "Odchylenie (Yaw)", "Przechylenie (Roll)", "Wznoszenie", 3, null, null));

        //BUDOWA I PRZYRZĄDY
        list.add(new Question("Jaką część samolotu pokazano na ilustracji?", "Lotki", "Klapy", "Ster wysokości", "Podwozie", 2, "images/flaps.jpg", null));
        list.add(new Question("Do czego służy ster kierunku?", "Do zmiany wysokości", "Do przechylania samolotu na bok", "Do odchylania nosa w lewo/prawo", "Do hamowania", 3, null, null));
        list.add(new Question("Jaki przyrząd pokazuje wysokość lotu?", "Prędkościomierz", "Wysokościomierz", "Sztuczny horyzont", "Wariometr", 2, null, null));
        list.add(new Question("Który element skrzydła zwiększa siłę nośną przy lądowaniu?", "Spoiler", "Klapy (Flaps)", "Winglet", "Silnik", 2, null, null));

        //PRZEPISY I PROCEDURY
        list.add(new Question("Jaki kolor ma światło nawigacyjne na LEWYM skrzydle?", "Zielony", "Czerwony", "Biały", "Żółty", 2, null, null));
        list.add(new Question("Jaki kolor ma światło nawigacyjne na PRAWYM skrzydle?", "Zielony", "Czerwony", "Biały", "Niebieski", 1, null, null));
        list.add(new Question("Jaki kod transpondera oznacza awarię łączności?", "7500", "7600", "7700", "1200", 2, null, null));
        list.add(new Question("Jaki kod transpondera oznacza sytuację awaryjną (Emergency)?", "7500", "7600", "7700", "2000", 3, null, null));
        list.add(new Question("Co oznacza prędkość V1?", "Prędkość oderwania", "Prędkość decyzji (startu nie można przerwać)", "Prędkość przelotowa", "Prędkość lądowania", 2, null, null));
        list.add(new Question("Z której strony należy mijać inny statek powietrzny lecący na wprost?", "Z lewej", "Z prawej", "Z dołu", "Z góry", 2, null, null));

        //MULTIMEDIA I INNE
        list.add(new Question("Co oznacza usłyszany komunikat?", "Gwałtowne przeciągnięcie", "Dużą prędkość", "Silny boczny wiatr", "Awarię silnika", 2, null, "audio/overspeed.mp3"));
        list.add(new Question("Co oznacza skrót ATC?", "Automatic Turbo Control", "Air Traffic Control (Kontrola Ruchu)", "Aircraft Taxi Chart", "All Terrain Craft", 2, null, null));
        list.add(new Question("Co oznaczają liczby na początku pasa startowego (np. 27)?", "Długość pasa w metrach", "Azymut magnetyczny pasa (kierunek)", "Numer lotniska", "Maksymalną wagę samolotu", 2, null, null));
        list.add(new Question("W alfabecie fonetycznym litera 'A' to:", "Adam", "Alpha", "Apple", "Anton", 2, null, null));
        list.add(new Question("W alfabecie fonetycznym litera 'B' to:", "Beta", "Bravo", "Baker", "Blue", 2, null, null));

        return list;
    }
}
