
De verkiezingsdata in de CSV_bestanden_TK2021.zip ZIP file is oorspronkelijk afkomstig van
https://data.overheid.nl/dataset/verkiezingsuitslag-tweede-kamer-2021.

Daar zijn 3 te downloaden bestanden met de uitslag van de Tweede Kamer verkiezingen 2021.

Deze (soms zeer grote) bestanden bevatten vooral tellingen van stemmen tot op het nivo van individuele stembureaus.

Het zijn echter zogenaamde EML bestanden, d.w.z. XML bestanden die voldoen aan de zogeheten EML standaard. Dit zijn
bestanden die zonder specifieke ondersteuning voor dat formaat vrij lastig zijn te gebruiken.

In dit project zijn die EML XML bestanden omgezet naar CSV formaat, zodat de data vervolgens gelezen kan worden in een
spreadsheet-programma. Niet alles uit de oorspronkelijke bestanden is in de CSV bestanden terug te vinden, maar de
tellingen per partij of kandidaat, van totalen voor het hele land tot aan individuele stembureaus zijn allemaal terug
te vinden.

Zoals gezegd, dit project (dutch-election) is vooral programmatuur, maar het resultaat, de CSV bestanden, is waar het
hier om gaat. Zie daarvoor bestand CSV_bestanden_TK2021.zip (die zelf 3 ZIP-bestanden bevat).

Hoe kunnen al de CSV bestanden in die ZIP file(s) geinterpreteerd worden?

Er zijn 4 soorten bestanden (net zoals in de originele EML XML bestanden): verkiezingsdefinitie, kandidatenlijsten,
tellingen, en een resultaat-bestand.

De verkiezingsdefinitie (Verkiezingsdefinitie_TK2021.csv) laat zien hoe de staat is opgedeeld in kieskringen, en hoe
elke kieskring is opgedeeld in gemeenten. Neem bijvoorbeeld gemeente 183, oftewel Tubbergen. Deze gemeente bevindt
zich in kieskring 4, zoals we zien in de verkiezingsdefinitie. Kieskring 4 is de kieskring Zwolle. Kieskring 4 bevindt
zich net als de 19 andere kieskringen in de staat (Staat der Nederlanden).

Voor de tellingen moeten we vooral de kandidatenlijsten voor de desbetreffende kieskring in gedachten houden als noodzakelijke
context. Neem de kandidatenlijst voor kieskring 4, in bestand Kandidatenlijsten_TK2021_Zwolle. Daarin zien we voor kieskring 4
de partijen ("affiliation") en kandidaten van de partijen. De Affiliation-ID en Candidate-ID hebben we nodig om in
tellingsbestanden de tellingen aan partijkandidaten (of partijen) te kunnen relateren.

Neem nu het tellingenbestand voor de gehele kieskring Zwolle (kieskring 4), namelijk Telling_TK2021_kieskring_Zwolle.csv.
We zien totaaltellingen per partij en per partijkandidaat, maar ook tellingen per gemeente (per partij en per partijkandidaat).
Bijvoorbeeld voor gemeente Tubbergen (reporting unit HSB4::0183). Zoals gezegd, voor partij (affiliation-ID) en partijkandidaat
(affiliation-ID plus candidate-ID), zie de kandidatenlijst voor kieskring Zwolle om deze ID's te kunnen koppelen aan namen
van partijen en kandidaten.

Laten we nu inzoomen op gemeente Tubbergen, in bestand Telling_TK2021_gemeente_Tubbergen. Kandidaten en partijen zijn weer
in de kandidatenlijst terug te vinden voor kieskring Zwolle (kieskring 4). Nu zien we totaaltellingen (voor de gemeente
Tubbergen) en tellingen per stembureau.

Het 4de en laatste (soort) bestand is het resultatenbestand Resultaat_TK2021.csv. Dit bestand bevat geen tellingen, maar
geeft per partij en per partijkandidaat aan wat het verkiezingsresultaat is.

