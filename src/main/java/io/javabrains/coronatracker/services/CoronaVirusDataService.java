package io.javabrains.coronatracker.services;

import jakarta.annotation.PostConstruct;
import models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


@Service
public class CoronaVirusDataService {
    private final static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {                  //alle stats aus fetchVirusData() werden hier gespeichert.
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "0 0 */3 * * *")
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();
        HttpClient client =  HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString()); //sendAsync mit send eventl. ersetzen
        System.out.println(res.body());

        StringReader csvBodyReader = new StringReader(res.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationStat = new LocationStats();

            locationStat.setCountry(record.get("Country/Region"));
            locationStat.setState(record.get("Province/State"));
            locationStat.setLatestTotalCases(Integer.parseInt(record.get(record.size() - 1)));
            newStats.add(locationStat);
        }


        this.allStats = newStats;
    }

}
